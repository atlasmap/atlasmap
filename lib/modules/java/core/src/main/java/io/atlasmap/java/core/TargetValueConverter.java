package io.atlasmap.java.core;

import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;

/**
 * TODO consolidate with JavaFieldWriterUtil - https://github.com/atlasmap/atlasmap/issues/730
 */
public class TargetValueConverter {
    private static final Logger LOG = LoggerFactory.getLogger(TargetValueConverter.class);

    private AtlasConversionService conversionService = null;
    private JavaFieldWriterUtil writerUtil = null;
    private ClassLoader classLoader;

    public TargetValueConverter(ClassLoader loader, AtlasConversionService conversionService, JavaFieldWriterUtil writerUtil) {
        this.classLoader = loader;
        this.conversionService = conversionService;
        this.writerUtil = writerUtil;
    }

    public void populateTargetField(AtlasInternalSession session, LookupTable lookupTable, Field sourceField,
            Object parentObject, Field targetField) throws AtlasException {
        Object sourceValue = sourceField.getValue();

        if (LOG.isDebugEnabled()) {
            LOG.debug("processTargetMapping srcPath={} srcVal={} srcType={}  tgtPath={} tgtdocId={}",
                sourceField.getPath(), sourceField.getValue(), sourceField.getFieldType(),
                targetField.getPath(), targetField.getDocId());
        }

        String targetClassName = (targetField instanceof JavaField) ? ((JavaField) targetField).getClassName() : null;
        targetClassName = (targetField instanceof JavaEnumField) ? ((JavaEnumField) targetField).getClassName()
                : targetClassName;
        if (targetClassName == null && parentObject != null) {
            SegmentContext segment = new AtlasPath(targetField.getPath()).getLastSegment();
            Class<?> clazz = segment.getCollectionType() == CollectionType.NONE
                    ? writerUtil.resolveChildClass(parentObject, segment)
                    : writerUtil.resolveCollectionItemClass(parentObject, segment);
            if (targetField.getFieldType() == null) {
                targetField.setFieldType(conversionService.fieldTypeFromClass(clazz));
            }
            if (targetField.getFieldType() == FieldType.COMPLEX) {
                targetClassName = clazz != null && !Modifier.isAbstract(clazz.getModifiers()) ? clazz.getName() : null;
                if (targetField instanceof JavaField) {
                    ((JavaField)targetField).setClassName(targetClassName);
                } else {
                    ((JavaEnumField)targetField).setClassName(targetClassName);
                }
            }
        }

        if (sourceField instanceof JavaEnumField || targetField instanceof JavaEnumField) {
            if (!(sourceField instanceof JavaEnumField) || !(targetField instanceof JavaEnumField)) {
                AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                        "Value conversion between enum fields and non-enum fields is not yet supported: sourceType=%s targetType=%s targetPath=%s msg=%s",
                        sourceField.getFieldType(), targetField.getFieldType(), targetField.getPath()),
                        targetField.getPath(), AuditStatus.ERROR, sourceValue != null ? sourceValue.toString() : null);
            }
            populateEnumValue(session, lookupTable, (JavaEnumField) sourceField, (JavaEnumField) targetField);
            return;
        }

        JavaField javaTargetField = (JavaField) targetField;
        if (sourceValue == null) {
            if (targetField.getFieldType() != FieldType.COMPLEX) {
                AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                        "Null sourceValue for targetDocId=%s, targetPath=%s", targetField.getDocId(), targetField.getPath()),
                        targetField.getPath(), AuditStatus.WARN, sourceValue != null ? sourceValue.toString() : null);
                targetField.setValue(null);
                return;
            }
            if (javaTargetField.getClassName() != null) {
                Object created = writerUtil.instantiateObject(writerUtil.loadClass(javaTargetField.getClassName()));
                javaTargetField.setValue(created);
                return;
            }
        }

        Object targetValue = doConvertTargetValue(session, sourceValue, targetClassName, targetField);
        targetField.setValue(targetValue);
    }

    public void convertTargetValue(AtlasInternalSession session,
            Object parentObject, Field targetField) throws AtlasException {
        String targetClassName = (targetField instanceof JavaField) ? ((JavaField) targetField).getClassName() : null;
        targetClassName = (targetField instanceof JavaEnumField) ? ((JavaEnumField) targetField).getClassName()
                : targetClassName;
        Object targetValue = doConvertTargetValue(session, targetField.getValue(), targetClassName, targetField);
        targetField.setValue(targetValue);
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    private Object doConvertTargetValue(AtlasInternalSession session, Object sourceValue,
            String targetClassName, Field targetField) throws AtlasException {
        if (sourceValue == null) {
            return null;
        }

        FieldType sourceType = conversionService.fieldTypeFromClass(sourceValue.getClass());
        FieldType targetType = targetField.getFieldType();
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
                        targetField.getPath(), AuditStatus.ERROR, null);
                return null;
            }
        }

        if (targetClazz != null) {
            if (targetField.getFieldType() == null) {
                targetField.setFieldType(conversionService.fieldTypeFromClass(targetClazz));
            }
            if (conversionService.isConvertionAvailableFor(sourceValue, targetClazz)) {
                return conversionService.convertType(sourceValue, null, targetClazz, null);
            } else {
                return null;
            }
        } else {
            return sourceValue;
        }
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
            enumClass = this.classLoader.loadClass(targetField.getClassName());
        } catch (Exception e) {
            AtlasUtil.addAudit(session, targetField.getDocId(),
                    String.format("Could not find class for output field class '%s': %s", targetField.getClassName(), e.getMessage()),
                    targetField.getPath(), AuditStatus.ERROR, targetValue);
            return null;
        }

        try {
            Enum<?> enumValue = Enum.valueOf(enumClass, targetValue);
            targetField.setValue(enumValue);
            return enumValue;
        } catch (IllegalArgumentException e) {
            AtlasUtil.addAudit(session, targetField.getDocId(),
                    String.format("No enum entry found for value '%s': %s", targetValue, e.getMessage()),
                    targetField.getPath(), AuditStatus.ERROR, targetValue);
            return null;
        }
    }

}
