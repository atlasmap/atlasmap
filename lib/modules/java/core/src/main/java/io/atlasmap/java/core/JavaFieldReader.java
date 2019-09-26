package io.atlasmap.java.core;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

public class JavaFieldReader implements AtlasFieldReader {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaFieldReader.class);

    private AtlasConversionService conversionService;
    private Object sourceDocument;

    @Override
    public Field read(AtlasInternalSession session) throws AtlasException {
        try {
            Field sourceField = session.head().getSourceField();
            if (sourceDocument == null) {
                AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                    "Unable to read sourceField (path=%s),  document (docId=%s) is null",
                    sourceField.getPath(), sourceField.getDocId()),
                    sourceField.getPath(), AuditStatus.ERROR, null);
            }
            Method getter = null;
            if (sourceField.getFieldType() == null
                    && (sourceField instanceof JavaField || sourceField instanceof JavaEnumField)) {
                getter = resolveGetMethod(session, sourceDocument, sourceField);
                if (getter == null) {
                    AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                            "Unable to auto-detect sourceField type path=%s docId=%s",
                            sourceField.getPath(), sourceField.getDocId()),
                            sourceField.getPath(), AuditStatus.WARN, null);
                    return sourceField;
                }
                Class<?> returnType = getter.getReturnType();
                sourceField.setFieldType(conversionService.fieldTypeFromClass(returnType));
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Auto-detected sourceField type p=" + sourceField.getPath() + " t="
                            + sourceField.getFieldType());
                }
            }

            populateSourceFieldValue(session, sourceField, sourceDocument, getter);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Processed input field sPath=" + sourceField.getPath() + " sV=" + sourceField.getValue()
                + " sT=" + sourceField.getFieldType() + " docId: " + sourceField.getDocId());
            }
            return session.head().getSourceField();
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private void populateSourceFieldValue(AtlasInternalSession session, Field field, Object source, Method m) throws Exception {
        Method getter = m;
        AtlasPath atlasPath = new AtlasPath(field.getPath());
        FieldGroup fieldGroup = null;
        if (atlasPath.hasCollection() && !atlasPath.isIndexedCollection()) {
            fieldGroup = AtlasModelFactory.createFieldGroupFrom(field);
            session.head().setSourceField(fieldGroup);
        }

        List<Object> parents = Arrays.asList(source);
        if (atlasPath.isRoot()) {
            if (atlasPath.getLastSegment().getCollectionType() == CollectionType.NONE) {
                processField(fieldGroup, field, source);
            } else {
                if (atlasPath.getLastSegment().getCollectionIndex() != null) {
                    processField(fieldGroup, field, extractFromCollection(session, source, atlasPath));
                } else {
                    if (source instanceof Collection) {
                        for (Object item : ((Collection<?>)source).toArray()) {
                            processField(fieldGroup, field, item);
                        }
                    } else if (source.getClass().isArray()) {
                        for (int i=0; i<Array.getLength(source); i++) {
                            processField(fieldGroup, field, Array.get(source, i));
                        }
                    } else {
                        processField(fieldGroup, field, source);
                    }
                }
            }
            return;
        }

        if (!atlasPath.isRoot()) {
            parents = ClassHelper.getParentObjectsForPath(session, source, atlasPath);
        }
        String cleanedLastSegment = atlasPath.getLastSegment().getName();
        getter = (getter == null) ? resolveGetMethod(session, source, field) : getter;
        for (Object parent : parents) {
            if (parent == null) {
                processField(fieldGroup, field, null);
                Field updated = fieldGroup != null ? fieldGroup.getField().get(fieldGroup.getField().size()-1) : field;
                AtlasUtil.addAudit(session, updated.getDocId(), String.format(
                        "Assigning null value for path path=%s docId=%s due to null parent", updated.getPath(), updated.getDocId()),
                        updated.getPath(), AuditStatus.WARN, null);
                continue;
            }
            Object child = null;
            if (getter != null) {
                child = getter.invoke(parent);
            } else {
                child = getValueFromMemberField(session, parent, cleanedLastSegment);
            }
            if (child == null) {
                continue;
            }
            if (atlasPath.getLastSegment().getCollectionType() == CollectionType.NONE) {
                processField(fieldGroup, field, child);
            } else {
                if (atlasPath.getLastSegment().getCollectionIndex() != null) {
                    processField(fieldGroup, field, extractFromCollection(session, child, atlasPath));
                } else {
                    if (child instanceof Collection) {
                        for (Object item : ((Collection<?>)child).toArray()) {
                            processField(fieldGroup, field, item);
                        }
                    } else if (child.getClass().isArray()) {
                        for (int i=0; i<Array.getLength(child); i++) {
                            processField(fieldGroup, field, Array.get(child, i));
                        }
                    } else {
                        processField(fieldGroup, field, child);
                    }
                }
            }
        }
    }

    private void processField(FieldGroup fieldGroup, Field origField, Object item) {
        Object value = item;
        Field field = origField;
        if (fieldGroup != null) {
            field = field instanceof JavaEnumField ? new JavaEnumField() : new JavaField();
            AtlasModelFactory.copyField(origField, field, false);
            AtlasPath atlasPath = new AtlasPath(field.getPath());
            atlasPath.setVacantCollectionIndex(fieldGroup.getField().size());
            field.setIndex(fieldGroup.getField().size());
            field.setPath(atlasPath.toString());
            fieldGroup.getField().add(field);
        }
        if (value != null && (conversionService.isPrimitive(value.getClass())
                || conversionService.isBoxedPrimitive(value.getClass()))) {
            value = conversionService.copyPrimitive(value);
        }
        field.setValue(value);
    }

    private Method resolveGetMethod(AtlasInternalSession session, Object sourceObject, Field field)
            throws AtlasException {
        Object parentObject = sourceObject;
        AtlasPath atlasPath = new AtlasPath(field.getPath());
        Method getter = null;

        if (!atlasPath.isRoot()) {
            List<Object> parents = ClassHelper.getParentObjectsForPath(session, sourceObject, atlasPath);
            parentObject = parents != null && parents.size() > 0 ? parents.get(0) : null;
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
                String getterMethod = m + capitalizeFirstLetter(atlasPath.getLastSegment().getName());
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

    private Object getValueFromMemberField(AtlasInternalSession session, Object source, String fieldName) throws IllegalArgumentException, IllegalAccessException {
        java.lang.reflect.Field reflectField = lookupJavaField(source, fieldName);
        if (reflectField != null) {
            reflectField.setAccessible(true);
            return reflectField.get(source);
        }
        Field sourceField = session.head().getSourceField();
        AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                "Field '%s' not found on object '%s'", fieldName, source),
                sourceField.getPath(), AuditStatus.ERROR, null);
        return null;
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

    private java.lang.reflect.Field lookupJavaField(Object source, String fieldName) {
        Class<?> targetClazz = source != null ? source.getClass() : null;
        while (targetClazz != null && targetClazz != Object.class) {
            try {
                return targetClazz.getDeclaredField(fieldName);
            } catch (Exception e) {
                e.getMessage(); // ignore
                targetClazz = targetClazz.getSuperclass();
            }
        }
        return null;
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

    private Object extractFromCollection(AtlasInternalSession session, Object source, AtlasPath atlasPath) {
        if (source == null) {
            return null;
        }

        SegmentContext lastSegment = atlasPath.getLastSegment();
        CollectionType collectionType = lastSegment.getCollectionType();
        Integer index = lastSegment.getCollectionIndex();
        if (collectionType == CollectionType.NONE || index == null) {
            return source;
        }

        if (collectionType == CollectionType.ARRAY) {
            return Array.get(source, index);
        } else if (collectionType == CollectionType.LIST) {
            return Collection.class.cast(source).toArray()[index];
        } else if (collectionType == CollectionType.MAP) {
            // TODO support map key
            String key = index.toString();
            return Map.class.cast(source).get(key);
        } else {
            Field sourceField = session.head().getSourceField();
            AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                    "Ignoring unknown collection type in path '%s'", sourceField.getPath()),
                    sourceField.getPath(), AuditStatus.WARN, null);
            return source;
        }
    }

    public void setDocument(Object sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public void setConversionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
