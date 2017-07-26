package io.atlasmap.java.module;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.PathUtil;
import io.atlasmap.java.inspect.ClassHelper;
import io.atlasmap.java.module.DocumentJavaFieldWriter.JavaFieldWriterValueConverter;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

public class OutputValueConverter implements JavaFieldWriterValueConverter {
    private static final Logger logger = LoggerFactory.getLogger(OutputValueConverter.class);

    private Field inputField = null;
    private AtlasConversionService conversionService = null;

    public OutputValueConverter(Field inputField, AtlasConversionService conversionService) {
        super();
        this.inputField = inputField;
        this.conversionService = conversionService;
    }

    @Override
    public Object convertValue(Object parentObject, Field outputField) throws AtlasException {
        //FIXME: this javafield cast is going to break for enum values.
        return populateOutputValue(parentObject, inputField, outputField);        
    }

    protected Object populateOutputValue(Object parentObject, Field inputField, Field outputField) throws AtlasException {
        FieldType inputType = inputField.getFieldType();
        Object inputValue = inputField.getValue();

        Object outputValue = null;
        FieldType outputType = outputField.getFieldType();

        if (logger.isDebugEnabled()) {
            logger.debug("processOutputMapping iPath=" + inputField.getPath() + " iV=" + inputValue + " iT=" + inputType
                    + " oPath=" + outputField.getPath() + " docId: " + outputField.getDocId());
        }

        if (inputValue == null) {
            // TODO: Finish targetValue = null processing
            logger.warn("Null sourceValue for field: " + outputField.getPath() + " docId: " + outputField.getDocId());
            return null;
        }

        if (outputType == null) {
            try {
                Method setter = resolveOutputSetMethod(parentObject, outputField, null);
                if (setter != null && setter.getParameterCount() == 1) {
                    outputType = conversionService.fieldTypeFromClass(setter.getParameterTypes()[0]);
                    outputField.setFieldType(outputType);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Auto-detected targetType as {} for class={} path={}", outputType,
                                parentObject.toString(), outputField.getPath());
                    }
                }
            } catch (Exception e) {
                logger.debug("Unable to auto-detect targetType for class={} path={}", parentObject.toString(),
                        outputField.getPath());
            }
        }

        if (inputType != null && inputType.equals(outputType)) {
            outputValue = inputValue;
        } else {
            try {
                outputValue = conversionService.convertType(inputValue, inputType, outputType);
            } catch (AtlasConversionException e) {
                logger.error(String.format("Unable to auto-convert for sT=%s tT=%s tF=%s msg=%s", inputType, outputType,
                        outputField.getPath(), e.getMessage()), e);
                return null;
            }
        }

        return outputValue;
    }

    protected Method resolveOutputSetMethod(Object sourceObject, Field field, Class<?> targetType) throws AtlasException {

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
                        }
                        setterMethodName = "set" + JavaModule.capitalizeFirstLetter(pathUtil.getLastSegment());
                        return ClassHelper.detectSetterMethod(clazz, setterMethodName,
                                conversionService.boxOrUnboxPrimitive(targetType));
                    } catch (NoSuchMethodException e) {
                        // method does not exist
                    }
                }
            }
        }

        throw new AtlasException(String.format("Unable to resolve setter for path=%s", field.getPath()));
    }

}
