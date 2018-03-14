package io.atlasmap.java.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.java.inspect.ClassHelper;
import io.atlasmap.java.inspect.JdkPackages;
import io.atlasmap.java.inspect.StringUtil;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;

public class TargetValueConverter {
    private static final Logger LOG = LoggerFactory.getLogger(TargetValueConverter.class);

    private AtlasConversionService conversionService = null;
    private ClassLoader classLoader;

    public TargetValueConverter(ClassLoader loader, AtlasConversionService conversionService) {
        this.classLoader = loader;
        this.conversionService = conversionService;
    }

    public Object convert(AtlasInternalSession session, LookupTable lookupTable, Field sourceField,
            Object parentObject, Field targetField) throws AtlasException {
        FieldType sourceType = sourceField.getFieldType();
        Object sourceValue = sourceField.getValue();

        Object targetValue = null;
        FieldType targetType = targetField.getFieldType();

        if (LOG.isDebugEnabled()) {
            LOG.debug("processTargetMapping iPath=" + sourceField.getPath() + " iV=" + sourceValue + " iT=" + sourceType
                    + " oPath=" + targetField.getPath() + " docId: " + targetField.getDocId());
        }

        if (sourceValue == null) {
            // TODO: Finish targetValue = null processing
            AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                    "Null sourceValue for targetDocId=%s, targetPath=%s", targetField.getDocId(), targetField.getPath()),
                    targetField.getPath(), AuditStatus.WARN, sourceValue != null ? sourceValue.toString() : null);
            return null;
        }

        String targetClassName = (targetField instanceof JavaField) ? ((JavaField) targetField).getClassName() : null;
        targetClassName = (targetField instanceof JavaEnumField) ? ((JavaEnumField) targetField).getClassName()
                : targetClassName;
        if (targetType == null || targetClassName == null) {
            try {
                Method setter = resolveTargetSetMethod(parentObject, targetField, null);
                if (setter != null && setter.getParameterCount() == 1) {
                    if (targetField instanceof JavaField) {
                        ((JavaField) targetField).setClassName(setter.getParameterTypes()[0].getName());
                    } else if (targetField instanceof JavaEnumField) {
                        ((JavaEnumField) targetField).setClassName(setter.getParameterTypes()[0].getName());
                    }

                    targetType = conversionService.fieldTypeFromClass(setter.getParameterTypes()[0]);
                    targetField.setFieldType(targetType);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Auto-detected targetType as {} for class={} path={}", targetType,
                                parentObject.toString(), targetField.getPath());
                    }
                }
            } catch (Exception e) {
                LOG.debug("Unable to auto-detect targetType for class={} path={}", parentObject.toString(),
                        targetField.getPath());
            }
        }

        if (sourceField instanceof JavaEnumField || targetField instanceof JavaEnumField) {
            if (!(sourceField instanceof JavaEnumField) || !(targetField instanceof JavaEnumField)) {
                AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                        "Value conversion between enum fields and non-enum fields is not yet supported: sourceType=%s targetType=%s targetPath=%s msg=%s",
                        sourceType, targetType, targetField.getPath()),
                        targetField.getPath(), AuditStatus.ERROR, sourceValue != null ? sourceValue.toString() : null);
            }
            return populateEnumValue(session, lookupTable, (JavaEnumField) sourceField, (JavaEnumField) targetField);
        }

        Class<?> targetClazz = null;
        if (targetClassName == null) {
            if (targetType != null) {
                targetClazz = conversionService.classFromFieldType(targetType);
            } else {
                AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                                "Target field doesn't have fieldType nor className: automatic conversion won't work: targetPath=%s",
                                targetField.getPath()),
                        targetField.getPath(), AuditStatus.WARN, sourceValue != null ? sourceValue.toString() : null);
            }
        } else if (conversionService.isPrimitive(targetClassName)) {
            targetClazz = conversionService.boxOrUnboxPrimitive(targetClassName);
        } else {
            try {
                targetClazz = classLoader.loadClass(targetClassName);
            } catch (ClassNotFoundException e) {
                AtlasUtil.addAudit(session, targetField.getDocId(),
                        String.format("Target field class '%s' was not found: sourceType=%s targetType=%s targetPath=%s msg=%s",
                                ((JavaField)targetField).getClassName(), sourceType, targetType, targetField.getPath(), e.getMessage()),
                        targetField.getPath(), AuditStatus.ERROR, targetValue != null ? targetValue.toString() : null);
                return null;
            }
        }

        if (targetClazz != null) {
            targetValue = conversionService.convertType(sourceValue, null, targetClazz, null);
        } else {
            targetValue = sourceValue;
        }

        AtlasFieldActionService fieldActionService = session.getAtlasContext().getContextFactory().getFieldActionService();
        try {
            targetValue = fieldActionService.processActions(targetField.getActions(), targetValue, targetType);
            if (targetValue != null) {
                if (targetClazz != null) {
                    targetValue = conversionService.convertType(targetValue, null, targetClazz, null);
                } else {
                    FieldType conversionInputType = conversionService.fieldTypeFromClass(targetValue.getClass());
                    targetValue = conversionService.convertType(targetValue, conversionInputType, targetType);
                }
            }
        } catch (AtlasConversionException e) {
            AtlasUtil.addAudit(session, targetField.getDocId(),
                    String.format("Unable to auto-convert for sourceType=%s targetType=%s targetPath=%s msg=%s", sourceType, targetType,
                            targetField.getPath(), e.getMessage()),
                    targetField.getPath(), AuditStatus.ERROR, targetValue != null ? targetValue.toString() : null);
            return null;
        }

        return targetValue;
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    @SuppressWarnings("unchecked")
    private Object populateEnumValue(AtlasInternalSession session, LookupTable lookupTable, JavaEnumField sourceField, JavaEnumField targetField) {
        if (sourceField == null || sourceField.getValue() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Input enum field or value is null, field: " + sourceField);
            }
            return null;
        }

        String sourceValue = ((Enum<?>) sourceField.getValue()).name();
        String targetValue = sourceValue;
        if (lookupTable != null) {
            for (LookupEntry e : lookupTable.getLookupEntry()) {
                if (e.getSourceValue().equals(sourceValue)) {
                    targetValue = e.getTargetValue();
                    break;
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Mapped input enum value '" + sourceValue + "' to output enum value '" + targetValue + "'.");
        }

        if (targetValue == null) {
            return null;
        }

        @SuppressWarnings("rawtypes")
        Class enumClass = null;
        try {
            enumClass = Class.forName(targetField.getClassName());
        } catch (Exception e) {
            AtlasUtil.addAudit(session, targetField.getDocId(),
                    String.format("Could not find class for output field class '%s': %s", targetField.getClassName(), e.getMessage()),
                    targetField.getPath(), AuditStatus.ERROR, targetValue);
            return null;
        }

        try {
            return Enum.valueOf(enumClass, targetValue);
        } catch (IllegalArgumentException e) {
            AtlasUtil.addAudit(session, targetField.getDocId(),
                    String.format("No enum entry found for value '%s': %s", targetValue, e.getMessage()),
                    targetField.getPath(), AuditStatus.ERROR, targetValue);
            return null;
        }
    }

    private Method resolveTargetSetMethod(Object sourceObject, Field field, Class<?> targetType)
            throws AtlasException {

        AtlasPath atlasPath = new AtlasPath(field.getPath());
        Object parentObject = sourceObject;

        List<Class<?>> classTree = resolveMappableClasses(parentObject.getClass());

        if (field instanceof JavaField) {
            JavaField javaField = (JavaField) field;
            for (Class<?> clazz : classTree) {
                try {
                    String setterMethodName = javaField.getSetMethod();
                    if (setterMethodName == null) {
                        setterMethodName = "set" + capitalizeFirstLetter(atlasPath.getLastSegment());
                    }
                    return ClassHelper.detectSetterMethod(clazz, setterMethodName, targetType);
                } catch (NoSuchMethodException e) {
                    // method does not exist
                }

                // Try the boxUnboxed version
                if (conversionService.isPrimitive(targetType) || conversionService.isBoxedPrimitive(targetType)) {
                    try {
                        String setterMethodName = javaField.getSetMethod();
                        if (setterMethodName == null) {
                            setterMethodName = "set" + capitalizeFirstLetter(atlasPath.getLastSegment());
                        }
                        return ClassHelper.detectSetterMethod(clazz, setterMethodName,
                                conversionService.boxOrUnboxPrimitive(targetType));
                    } catch (NoSuchMethodException e) {
                        // method does not exist
                    }
                }
            }
        } else if (field instanceof JavaEnumField) {
            for (Class<?> clazz : classTree) {
                try {
                    String setterMethodName = "set" + capitalizeFirstLetter(atlasPath.getLastSegment());
                    return ClassHelper.detectSetterMethod(clazz, setterMethodName, targetType);
                } catch (NoSuchMethodException e) {
                    // method does not exist
                }
            }
        }

        throw new AtlasException(String.format("Unable to resolve setter for path=%s", field.getPath()));
    }

    private List<Class<?>> resolveMappableClasses(Class<?> className) {
        List<Class<?>> classTree = new ArrayList<>();
        classTree.add(className);

        Class<?> superClazz = className.getSuperclass();
        while (superClazz != null) {
            if (JdkPackages.contains(superClazz.getPackage().getName())) {
                superClazz = null;
            } else {
                classTree.add(superClazz);
                superClazz = superClazz.getSuperclass();
            }
        }

        return classTree;
    }

    private String capitalizeFirstLetter(String sentence) {
        if (StringUtil.isEmpty(sentence)) {
            return sentence;
        }
        if (sentence.length() == 1) {
            return String.valueOf(sentence.charAt(0)).toUpperCase();
        }
        return String.valueOf(sentence.charAt(0)).toUpperCase() + sentence.substring(1);
    }

}
