package io.atlasmap.java.core;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
            Field field = session.head().getSourceField();
            if (sourceDocument == null) {
                AtlasUtil.addAudit(session, field.getDocId(), String.format(
                    "Unable to read sourceField (path=%s),  document (docId=%s) is null",
                    field.getPath(), field.getDocId()),
                    field.getPath(), AuditStatus.ERROR, null);
            }

            AtlasPath path = new AtlasPath(field.getPath());

            List<Field> fields = getFieldsForPath(session, sourceDocument, field, path, 0);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processed input field sPath=" + field.getPath() + " sV=" + field.getValue()
                    + " sT=" + field.getFieldType() + " docId: " + field.getDocId());
            }

            if (path.hasCollection() && !path.isIndexedCollection()) {
                FieldGroup fieldGroup = AtlasModelFactory.createFieldGroupFrom(field);
                fieldGroup.getField().addAll(fields);
                session.head().setSourceField(fieldGroup);
                return fieldGroup;
            } else if (fields.size() == 1) {
                field.setValue(fields.get(0).getValue());
                return field;
            } else {
                return field;
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private List<Field> getFieldsForPath(AtlasInternalSession session, Object source, Field field, AtlasPath path, int depth) throws AtlasException {
        List<Field> fields = new ArrayList<>();
        List<SegmentContext> segments = path.getSegments(true);

        if (segments.size() == depth) {
            Field newField = field instanceof JavaEnumField ? new JavaEnumField() : new JavaField();
            AtlasModelFactory.copyField(field, newField, false);
            if (source != null && (conversionService.isPrimitive(source.getClass())
                || conversionService.isBoxedPrimitive(source.getClass()))) {
                source = conversionService.copyPrimitive(source);
            }
            newField.setValue(source);
            newField.setIndex(null); //reset index for subfields
            fields.add(newField);
        } else if (segments.size() > depth) {
            SegmentContext segmentContext = segments.get(depth);
            Object child = source;
            if (depth == 0 && segments.size() > 1 && segmentContext.getCollectionType() == CollectionType.NONE) {
                depth++;
                segmentContext = segments.get(depth);
            }

            if (depth > 0) {
                child = getChildForSegment(session, source, field, path, segmentContext);

                if (child == null) {
                    if (path.getLastSegment() != segmentContext) {
                        //add audit only if not the last segment on the path
                        AtlasUtil.addAudit(session, field.getDocId(), String.format(
                            "Assigning null value for path path=%s docId=%s due to null parent", field.getPath(), field.getDocId()),
                            field.getPath(), AuditStatus.WARN, null);

                        Field newField = field instanceof JavaEnumField ? new JavaEnumField() : new JavaField();
                        AtlasModelFactory.copyField(field, newField, false);
                        newField.setValue(null);
                        fields.add(newField);
                    }
                    return fields;
                }
            }

            if (segmentContext.getCollectionType() == CollectionType.NONE) {
                List<Field> childFields = getFieldsForPath(session, child, field, path, depth + 1);
                fields.addAll(childFields);
            } else {
                if (segmentContext.getCollectionIndex() != null) {
                    Object indexItem = extractFromCollection(session, child, segmentContext);
                    List<Field> childFields = getFieldsForPath(session, indexItem, field, path, depth + 1);
                    fields.addAll(childFields);
                } else {
                    Object array = null;
                    if (child instanceof Collection) {
                        array = ((Collection) child).toArray();
                    } else if (child.getClass().isArray()) {
                        array = child;
                    }
                    if (array != null) {
                        for (int i = 0; i < Array.getLength(array); i++) {
                            //include the array index within the path
                            AtlasPath subPath = new AtlasPath(field.getPath());
                            subPath.setCollectionIndex(depth, i);
                            field.setPath(subPath.toString());

                            List<Field> arrayFields = getFieldsForPath(session, Array.get(array, i), field, path, depth + 1);
                            fields.addAll(arrayFields);
                        }
                    } else {
                        List<Field> arrayFields = getFieldsForPath(session, child, field, path, depth + 1);
                        fields.addAll(arrayFields);
                    }
                }
            }
        }

        return fields;
    }

    private Object getChildForSegment(AtlasInternalSession session, Object source, Field field, AtlasPath path, SegmentContext segmentContext) throws AtlasException {
        Object child = null;
        Method getterMethod = ClassHelper.lookupGetterMethod(source, segmentContext.getName());
        java.lang.reflect.Field classField = null;

        if (getterMethod != null) {
            try {
                child = getterMethod.invoke(source);
            } catch (Exception e) {
                throw new AtlasException(e);
            }
        } else {
            classField = ClassHelper.lookupJavaField(source, segmentContext.getName());
            if (classField != null) {
                try {
                    child = classField.get(source);
                } catch (IllegalAccessException e) {
                    throw new AtlasException(e);
                }
            } else {
                AtlasUtil.addAudit(session, field.getDocId(), String.format(
                    "Field '%s' not found on object '%s'", segmentContext.getName(), source),
                    field.getPath(), AuditStatus.ERROR, null);
            }
        }

        if (path.getLastSegment() == segmentContext) {
            if (field.getFieldType() == null
                && (field instanceof JavaField || field instanceof JavaEnumField)) {
                Class<?> returnType = null;
                if (getterMethod != null) {
                    returnType = getterMethod.getReturnType();
                } else if (classField != null) {
                    returnType = classField.getType();
                }

                if (returnType != null) {
                    field.setFieldType(conversionService.fieldTypeFromClass(returnType));
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Auto-detected sourceField type p=" + field.getPath() + " t="
                            + field.getFieldType());
                    }
                } else {
                    AtlasUtil.addAudit(session, field.getDocId(), String.format(
                        "Unable to auto-detect sourceField type path=%s docId=%s",
                        field.getPath(), field.getDocId()),
                        field.getPath(), AuditStatus.WARN, null);
                }
            }
        }
        return child;
    }



    private Object extractFromCollection(AtlasInternalSession session, Object source, SegmentContext segmentContext) {
        if (source == null) {
            return null;
        }
        CollectionType collectionType = segmentContext.getCollectionType();
        Integer index = segmentContext.getCollectionIndex();
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
