package io.atlasmap.java.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.java.inspect.ClassHelper;
import io.atlasmap.java.inspect.JdkPackages;
import io.atlasmap.java.inspect.StringUtil;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.Field;

public class DocumentJavaFieldReader implements AtlasFieldReader {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DocumentJavaFieldReader.class);

    private AtlasConversionService conversionService;
    private Object sourceDocument;

    @Override
    public void read(AtlasInternalSession session) throws AtlasException {
        try {
            Field sourceField = session.head().getSourceField();
            Method getter = null;
            if (sourceField.getFieldType() == null
                    && (sourceField instanceof JavaField || sourceField instanceof JavaEnumField)) {
                getter = resolveGetMethod(sourceDocument, sourceField, false);
                if (getter == null) {
                    LOG.warn("Unable to auto-detect sourceField type p=" + sourceField.getPath() + " d="
                            + sourceField.getDocId());
                    return;
                }
                Class<?> returnType = getter.getReturnType();
                sourceField.setFieldType(conversionService.fieldTypeFromClass(returnType));
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Auto-detected sourceField type p=" + sourceField.getPath() + " t="
                            + sourceField.getFieldType());
                }
            }

            populateSourceFieldValue(sourceField, sourceDocument, getter);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Processed input field sPath=" + sourceField.getPath() + " sV=" + sourceField.getValue()
                + " sT=" + sourceField.getFieldType() + " docId: " + sourceField.getDocId());
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private void populateSourceFieldValue(Field field, Object source, Method m) throws Exception {
        Method getter = m;
        Object parentObject = source;
        AtlasPath atlasPath = new AtlasPath(field.getPath());
        if (atlasPath.hasParent()) {
            parentObject = ClassHelper.parentObjectForPath(source, atlasPath, true);
        }
        getter = (getter == null) ? resolveGetMethod(parentObject, field, (!parentObject.equals(source))) : getter;

        Object sourceValue = null;
        if (getter != null) {
            sourceValue = getter.invoke(parentObject);
        }

        // TODO: support doing parent stuff at field level vs getter
        if (sourceValue == null) {
            sourceValue = getValueFromMemberField(source, atlasPath.getLastSegment());
        }

        if (sourceValue != null && (conversionService.isPrimitive(sourceValue.getClass())
                || conversionService.isBoxedPrimitive(sourceValue.getClass()))) {
            sourceValue = conversionService.copyPrimitive(sourceValue);
        }

        field.setValue(sourceValue);
    }

    private Method resolveGetMethod(Object sourceObject, Field field, boolean objectIsParent)
            throws AtlasException {
        Object parentObject = sourceObject;
        AtlasPath atlasPath = new AtlasPath(field.getPath());
        Method getter = null;

        if (atlasPath.hasParent() && !objectIsParent) {
            parentObject = ClassHelper.parentObjectForPath(sourceObject, atlasPath, true);
        }
        if (parentObject == null) {
            return null;
        }

        List<Class<?>> classTree = resolveMappableClasses(parentObject.getClass());

        for (Class<?> clazz : classTree) {
            try {
                if (field instanceof JavaField && ((JavaField) field).getGetMethod() != null) {
                    getter = clazz.getMethod(((JavaField) field).getGetMethod());
                    getter.setAccessible(true);
                    return getter;
                }
            } catch (NoSuchMethodException e) {
                // no getter method specified in mapping file
            }

            for (String m : Arrays.asList("get", "is")) {
                String getterMethod = m + capitalizeFirstLetter(atlasPath.getLastSegment());
                try {
                    getter = clazz.getMethod(getterMethod);
                    getter.setAccessible(true);
                    return getter;
                } catch (NoSuchMethodException e) {
                    // method does not exist
                }
            }
        }
        return null;
    }

    private Object getValueFromMemberField(Object source, String fieldName) throws Exception {
        try {
            java.lang.reflect.Field reflectField = source.getClass().getField(fieldName);
            reflectField.setAccessible(true);
            return reflectField.get(source);
        } catch (NoSuchFieldException nsfe) {
            // TODO: Add audit entry
            LOG.error(nsfe.getMessage(), nsfe);
        }
        return null;
    }

    private List<Class<?>> resolveMappableClasses(Class<?> className) {
        List<Class<?>> classTree = new ArrayList<Class<?>>();
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

    public void setDocument(Object sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public void setConversionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
