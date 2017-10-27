package io.atlasmap.java.module;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.PathUtil;
import io.atlasmap.java.inspect.ClassHelper;
import io.atlasmap.java.module.DocumentJavaFieldWriter.JavaFieldWriterValueConverter;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;

public class OutputValueConverter implements JavaFieldWriterValueConverter {
    private static final Logger LOG = LoggerFactory.getLogger(OutputValueConverter.class);

    private Field inputField = null;
    private AtlasSession session = null;
    private AtlasConversionService conversionService = null;
    private Mapping mapping = null;

    public OutputValueConverter(Field inputField, AtlasSession session, Mapping mapping,
            AtlasConversionService conversionService) {
        super();
        this.inputField = inputField;
        this.session = session;
        this.mapping = mapping;
        this.conversionService = conversionService;
    }

    @Override
    public Object convertValue(Object parentObject, Field outputField) throws AtlasException {
        // FIXME: this javafield cast is going to break for enum values.
        return populateOutputValue(parentObject, inputField, outputField);
    }

    protected Object populateOutputValue(Object parentObject, Field inputField, Field outputField)
            throws AtlasException {
        FieldType inputType = inputField.getFieldType();
        Object inputValue = inputField.getValue();

        Object outputValue = null;
        FieldType outputType = outputField.getFieldType();

        if (LOG.isDebugEnabled()) {
            LOG.debug("processOutputMapping iPath=" + inputField.getPath() + " iV=" + inputValue + " iT=" + inputType
                    + " oPath=" + outputField.getPath() + " docId: " + outputField.getDocId());
        }

        if (inputValue == null) {
            // TODO: Finish targetValue = null processing
            LOG.warn("Null sourceValue for field: " + outputField.getPath() + " docId: " + outputField.getDocId());
            return null;
        }

        String outputClassName = (outputField instanceof JavaField) ? ((JavaField) outputField).getClassName() : null;
        outputClassName = (outputField instanceof JavaEnumField) ? ((JavaEnumField) outputField).getClassName()
                : outputClassName;
        if (outputType == null || outputClassName == null) {
            try {
                Method setter = resolveOutputSetMethod(parentObject, outputField, null);
                if (setter != null && setter.getParameterCount() == 1) {
                    if (outputField instanceof JavaField) {
                        ((JavaField) outputField).setClassName(setter.getParameterTypes()[0].getName());
                    } else if (outputField instanceof JavaEnumField) {
                        ((JavaEnumField) outputField).setClassName(setter.getParameterTypes()[0].getName());
                    }

                    outputType = conversionService.fieldTypeFromClass(setter.getParameterTypes()[0]);
                    outputField.setFieldType(outputType);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Auto-detected targetType as {} for class={} path={}", outputType,
                                parentObject.toString(), outputField.getPath());
                    }
                }
            } catch (Exception e) {
                LOG.debug("Unable to auto-detect targetType for class={} path={}", parentObject.toString(),
                        outputField.getPath());
            }
        }

        if (inputField instanceof JavaEnumField || outputField instanceof JavaEnumField) {
            if (!(inputField instanceof JavaEnumField) || !(outputField instanceof JavaEnumField)) {
                throw new AtlasException(
                        "Value conversion between enum fields and non-enum fields is not yet supported.");
            }
            return populateEnumValue((JavaEnumField) inputField, (JavaEnumField) outputField);
        }

        AtlasFieldActionService fieldActionService = session.getAtlasContext().getContextFactory()
                .getFieldActionService();
        try {
            outputValue = fieldActionService.processActions(outputField.getActions(), inputValue, outputType);
            if (outputValue != null) {
                FieldType conversionInputType = conversionService.fieldTypeFromClass(outputValue.getClass());
                outputValue = conversionService.convertType(outputValue, conversionInputType, outputType);
            }
        } catch (AtlasConversionException e) {
            LOG.error(String.format("Unable to auto-convert for sT=%s tT=%s tF=%s msg=%s", inputType, outputType,
                    outputField.getPath(), e.getMessage()), e);
            return null;
        }

        return outputValue;
    }

    @SuppressWarnings("unchecked")
    private Object populateEnumValue(JavaEnumField inputField, JavaEnumField outputField) throws AtlasException {
        if (inputField == null || inputField.getValue() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Input enum field or value is null, field: " + inputField);
            }
            return null;
        }

        String lookupTableName = mapping.getLookupTableName();
        LookupTable table = null;
        for (LookupTable t : session.getMapping().getLookupTables().getLookupTable()) {
            if (t.getName().equals(lookupTableName)) {
                table = t;
                break;
            }
        }
        if (table == null) {
            throw new AtlasException(
                    "Could not find lookup table with name '" + lookupTableName + "' for mapping: " + mapping);
        }

        String inputValue = ((Enum<?>) inputField.getValue()).name();
        String outputValue = null;
        for (LookupEntry e : table.getLookupEntry()) {
            if (e.getSourceValue().equals(inputValue)) {
                outputValue = e.getTargetValue();
                break;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Mapped input enum value '" + inputValue + "' to output enum value '" + outputValue + "'.");
        }

        if (outputValue == null) {
            return null;
        }

        @SuppressWarnings("rawtypes")
        Class enumClass = null;
        try {
            enumClass = Class.forName(outputField.getClassName());
        } catch (Exception e) {
            throw new AtlasException(
                    "Could not find class for output field class '" + outputField.getClassName() + "'.", e);
        }

        return Enum.valueOf(enumClass, outputValue);

    }

    protected Method resolveOutputSetMethod(Object sourceObject, Field field, Class<?> targetType)
            throws AtlasException {

        PathUtil pathUtil = new PathUtil(field.getPath());
        Object parentObject = sourceObject;

        List<Class<?>> classTree = JavaModule.resolveMappableClasses(parentObject.getClass());

        if (field instanceof JavaField) {
            JavaField javaField = (JavaField) field;
            for (Class<?> clazz : classTree) {
                try {
                    String setterMethodName = javaField.getSetMethod();
                    if (setterMethodName == null) {
                        setterMethodName = "set" + JavaModule.capitalizeFirstLetter(pathUtil.getLastSegment());
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
                            setterMethodName = "set" + JavaModule.capitalizeFirstLetter(pathUtil.getLastSegment());
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
                    String setterMethodName = "set" + JavaModule.capitalizeFirstLetter(pathUtil.getLastSegment());
                    return ClassHelper.detectSetterMethod(clazz, setterMethodName, targetType);
                } catch (NoSuchMethodException e) {
                    // method does not exist
                }
            }
        }

        throw new AtlasException(String.format("Unable to resolve setter for path=%s", field.getPath()));
    }

}
