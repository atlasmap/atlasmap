package io.atlasmap.java.core;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassHelper;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupTable;

public class DocumentJavaFieldWriter implements AtlasFieldWriter {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DocumentJavaFieldWriter.class);

    private Object rootObject = null;
    private Map<String, Class<?>> classesForFields = new HashMap<>();
    private JavaWriterUtil writerUtil = new JavaWriterUtil(DefaultAtlasConversionService.getInstance());
    private List<String> processedPaths = new LinkedList<>();
    private TargetValueConverter converter;

    public void write(AtlasInternalSession session) throws AtlasException {
        LookupTable lookupTable = session.head().getLookupTable();
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();

        try {
            if (targetField == null) {
                throw new AtlasException(new IllegalArgumentException("Argument 'field' cannot be null"));
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Now processing field: " + targetField);
                LOG.debug("Field type: " + targetField.getFieldType());
                LOG.debug("Field path: " + targetField.getPath());
                LOG.debug("Field value: " + targetField.getValue());
                String fieldClassName = (targetField instanceof JavaField) ? ((JavaField) targetField).getClassName()
                        : ((JavaEnumField) targetField).getClassName();
                LOG.debug("Field className: " + fieldClassName);
            }

            processedPaths.add(targetField.getPath());

            AtlasPath path = new AtlasPath(targetField.getPath());
            Object parentObject = rootObject;
            boolean segmentIsComplexSegment = true;
            for (SegmentContext segmentContext : path.getSegmentContexts(true)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Now processing segment: " + segmentContext);
                    LOG.debug("Parent object is currently: " + writeDocumentToString(false, parentObject));
                }

                if ("/".equals(segmentContext.getSegmentPath())) {
                    if (rootObject == null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Creating root node: " + segmentContext);
                        }
                        rootObject = createParentObject(targetField, parentObject, segmentContext);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Root node already exists, skipping segment: " + segmentContext);
                        }
                    }
                    parentObject = rootObject;
                    continue;
                }

                // if we're on the last segment, the
                boolean segmentIsLastSegment = (segmentContext.getNext() == null);
                if (segmentIsLastSegment) {
                    if (FieldType.COMPLEX.equals(targetField.getFieldType())) {
                        segmentIsComplexSegment = true;
                    } else {
                        segmentIsComplexSegment = false;
                    }
                    if (targetField instanceof JavaEnumField) {
                        segmentIsComplexSegment = false;
                    }
                }
                if (LOG.isDebugEnabled()) {
                    if (segmentIsComplexSegment) {
                        LOG.debug("Now processing complex segment: " + segmentContext);
                    } else if (targetField instanceof JavaEnumField) {
                        LOG.debug("Now processing field enum value segment: " + segmentContext);
                    } else {
                        LOG.debug("Now processing field value segment: " + segmentContext);
                    }
                }

                if (segmentIsComplexSegment) { // processing parent object
                    Object childObject = findChildObject(targetField, segmentContext, parentObject);

                    if (childObject == null) {
                        childObject = createParentObject(targetField, parentObject, segmentContext);
                    }
                    parentObject = childObject;
                } else { // processing field value
                    if (AtlasPath.isCollectionSegment(segmentContext.getSegment())) {
                        parentObject = findOrCreateOrExpandParentCollectionObject(targetField, parentObject, segmentContext);
                    }
                    Object value = converter.convert(session, lookupTable, sourceField, parentObject, targetField);
                    addChildObject(targetField, segmentContext, parentObject, value);
                }
            }
        } catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occured while writing field: " + targetField.getPath(), t);
            }
            if (t instanceof AtlasException) {
                throw (AtlasException) t;
            }
            throw new AtlasException(t);
        }
    }

    private Object findChildObject(Field field, SegmentContext segmentContext, Object parentObject)
            throws AtlasException {
        if (parentObject == null) {
            if (this.rootObject != null && segmentContext.getSegmentPath().equals("/")) {
                return this.rootObject;
            }
            return null;
        }

        String segment = segmentContext.getSegment();
        String parentSegment = segmentContext.getPrev() == null ? null : segmentContext.getPrev().getSegment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for child object '" + segment + "' in parent '" + parentSegment + "': "
                    + writeDocumentToString(false, parentObject));
        }

        // find the child object on the given parent
        Object childObject = writerUtil.getObjectFromParent(field, parentObject, segmentContext);
        if (childObject != null && AtlasPath.isCollectionSegment(segment)) {
            if (!collectionHasRoomForIndex(childObject, segmentContext)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found child collection '" + segment + "' (" + childObject.getClass().getName()
                            + ") in parent '" + parentSegment
                            + "', but it doesn't have room for the segment's index. Parent Object: "
                            + writeDocumentToString(false, parentObject));
                }
                return null;
            }
            childObject = getCollectionItem(childObject, segmentContext);
        }

        if (LOG.isDebugEnabled()) {
            if (childObject == null) {
                LOG.debug("Could not find child object '" + segment + "' in parent '" + parentSegment + "'.");
            } else {
                LOG.debug("Found child object '" + segment + "' in parent '" + parentSegment + "', class: "
                        + childObject.getClass().getName() + ", child object: "
                        + writeDocumentToString(false, childObject));
            }
        }

        return childObject;
    }

    private Object createParentObject(Field field, Object parentObject, SegmentContext segmentContext)
            throws AtlasException {
        String segment = segmentContext.getSegment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating parent object: " + segmentContext);
        }
        Object childObject = null;
        if (AtlasPath.isCollectionSegment(segment)) {
            // first, let's see if we have the collection object at all
            if (LOG.isDebugEnabled()) {
                LOG.debug("Looking for collection wrapper child for " + segmentContext + " on parent: " + parentObject);
            }
            Object collectionObject = findOrCreateOrExpandParentCollectionObject(field, parentObject, segmentContext);
            childObject = getCollectionItem(collectionObject, segmentContext);

            if (childObject == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Could not find child object in collection, creating it.");
                }
                childObject = createObject(field, segmentContext, parentObject, false);
                addChildObject(field, segmentContext, collectionObject, childObject);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Child object inside collection wrapper for segment '" + segment + "': "
                        + writeDocumentToString(false, childObject));
            }
        } else {
            childObject = createObject(field, segmentContext, parentObject, false);
            addChildObject(field, segmentContext, parentObject, childObject);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Created child object for segment '" + segment + "': " + writeDocumentToString(true, childObject));
        }
        return childObject;
    }

    private Object findOrCreateOrExpandParentCollectionObject(Field field, Object parentObject,
            SegmentContext segmentContext) throws AtlasException {
        String segment = segmentContext.getSegment();
        // first, let's see if we have the collection object at all
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for collection wrapper child for " + segmentContext + " on parent: " + parentObject);
        }
        Object collectionObject = writerUtil.getObjectFromParent(field, parentObject, segmentContext);
        if (collectionObject == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot find pre-existing child collection for segment '" + segment
                        + "', creating the collection.");
            }
            collectionObject = createCollectionWrapperObject(field, segmentContext, parentObject);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Collection wrapper child object for segment '" + segment + "': "
                    + writeDocumentToString(false, collectionObject));
        }

        collectionObject = expandCollectionToFitItem(field, collectionObject, segmentContext, parentObject);
        addChildObject(field, segmentContext, parentObject, collectionObject);

        return collectionObject;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object expandCollectionToFitItem(Field field, Object obj, SegmentContext segmentContext,
            Object parentObject) throws AtlasException {
        Object collectionObject = obj;
        String segment = segmentContext.getSegment();
        if (!collectionHasRoomForIndex(collectionObject, segmentContext)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Collection is not large enough for segment '" + segment + "', expanding the collection.");
            }
            int index = AtlasPath.indexOfSegment(segment);
            if (collectionObject instanceof List) {
                List list = (List) collectionObject;
                while (list.size() < (index + 1)) {
                    list.add(null);
                }
            } else if (collectionObject instanceof Map) {
                throw new AtlasException("FIXME: Cannot yet handle adding children to maps");
            } else if (collectionObject.getClass().isArray()) {
                if (Array.getLength(collectionObject) < (index + 1)) {
                    // resize the array to fit the item
                    Object newArray = (Object) createObject(field, segmentContext, parentObject, true);
                    // copy pre-existing items over to new array
                    for (int i = 0; i < Array.getLength(collectionObject); i++) {
                        Array.set(newArray, i, Array.get(collectionObject, i));
                    }
                    collectionObject = newArray;
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished expanding collection: " + collectionObject);
            }
        }
        return collectionObject;
    }

    private Object createCollectionWrapperObject(Field field, SegmentContext segmentContext, Object parentObject)
            throws AtlasException {
        // create the "List" part of List<Contact>
        String segment = segmentContext.getSegment();
        if (AtlasPath.isArraySegment(segment)) {
            return createObject(field, segmentContext, parentObject, true);
        } else if (AtlasPath.isListSegment(segment)) {
            // TODO: look up field level or document level default list impl
            return writerUtil.instantiateObject(LinkedList.class, segmentContext, false);
        } else if (AtlasPath.isMapSegment(segment)) {
            // TODO: look up field level or document level default map impl
            return writerUtil.instantiateObject(HashMap.class, segmentContext, false);
        }
        throw new AtlasException("Can't create collection object for segment: " + segment);
    }

    private Class<?> getClassForField(Field field, SegmentContext segmentContext, Object parentObject,
            boolean unwrapCollectionType) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking up class to use for segment: " + segmentContext + "\n\tparentObject: " + parentObject);
        }
        Class<?> clz = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for configured class for field: " + field + ".");
        }
        String className = null;
        if (field instanceof JavaField) {
            className = ((JavaField) field).getClassName();
        } else if (field instanceof JavaEnumField) {
            className = ((JavaEnumField) field).getClassName();
        }
        if (className != null) {
            try {
                clz = className == null ? null : Class.forName(className);
            } catch (Exception e) {
                throw new AtlasException("Could not find class for '" + className + "', for segment: " + segmentContext
                        + ", on field: " + field, e);
            }
        }

        if (clz == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Couldn't find class on field. Looking for configured class for segment: " + segmentContext
                        + ".");
            }
            String normalizedSegment = AtlasPath.removeCollectionIndexes(segmentContext.getSegmentPath());
            clz = this.classesForFields.get(normalizedSegment);
        }
        Type clzType = null;
        if (clz == null) { // attempt to determine it from the parent object.
            if (LOG.isDebugEnabled()) {
                LOG.debug("Couldn't find configured class for segment: " + segmentContext
                        + ", looking up getter method.");
            }
            Method m = null;
            try {
                String methodName = "get"
                        + JavaWriterUtil.capitalizeFirstLetter(AtlasPath.cleanPathSegment(segmentContext.getSegment()));
                m = ClassHelper.detectGetterMethod(parentObject.getClass(), methodName);
            } catch (NoSuchMethodException e) {
                // it's ok, we didnt find a getter.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Couldn't find getter method for segment: " + segmentContext, e);
                }
            }
            clz = m == null ? null : m.getReturnType();
            clzType = m.getGenericReturnType();
        }
        if (clz == null) {
            throw new AtlasException(
                    "Could not create object, can't find class to instantiate for segment: " + segmentContext);
        }

        if (unwrapCollectionType) {
            clz = unwrapCollectionType(field, segmentContext, parentObject, clz, clzType);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found class '" + clz.getName() + "' to use for segment: " + segmentContext);
        }
        return clz;
    }

    private Class<?> unwrapCollectionType(Field field, SegmentContext segmentContext,
            Object parentObject, Class<?> clz, Type clzType) throws AtlasException {
        Class<?> answer = clz;
        if (answer.isArray()) {
            Class<?> oldClass = answer;
            answer = answer.getComponentType();
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "Unwrapped type '" + answer.getName() + "' from wrapper array type '" + oldClass.getName() + "'.");
            }
        } else if (Collection.class.isAssignableFrom(answer)) {
            Class<?> oldClass = answer;
            answer = null;
            String cleanedSegment = AtlasPath.cleanPathSegment(segmentContext.getSegment());

            // From return type of getter method
            if (clzType instanceof Class) {
                // No type parameter, use Object
                answer = Object.class;
            } else if (clzType instanceof ParameterizedType){
                ParameterizedType pt = (ParameterizedType) clzType;
                String typeName = pt.getActualTypeArguments()[0].getTypeName();
                try {
                    answer = typeName == null ? null : Class.forName(typeName);
                } catch (Exception e) {
                    throw new AtlasException("Could not find class for '" + typeName + "', for segment: "
                            + segmentContext + ", on field: " + field, e);
                }

            // No getter found - check fields of parent object
            } else if (answer == null) {
                Class<?> parentClass = parentObject.getClass();
                while (parentClass != Object.class && answer == null) {
                    answer = findClassOfNamedField(parentClass, cleanedSegment);
                    parentClass = parentClass.getSuperclass();
                }
            }

            if (answer == null) {
                throw new AtlasException(
                        "Could not unwrap list collection's generic type for segment: " + segmentContext);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "Unwrapped type '" + answer.getName() + "' from wrapper list type '" + oldClass.getName() + "'.");
            }
        }
        return answer;
    }

    private Class<?> findClassOfNamedField(Class<?> clazz, String name) {
        for (java.lang.reflect.Field declaredField : clazz.getDeclaredFields()) {
            if (name.equals(declaredField.getName())) {
                if (declaredField.getGenericType() == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skipping field '{}' on class '{}', the field isn't generic", declaredField.getName(),
                                clazz.getName());
                    }
                    continue;
                }
                ParameterizedType paramType = (ParameterizedType) declaredField.getGenericType();
                String typeName = paramType.getActualTypeArguments()[0].getTypeName();
                try {
                    if (typeName != null) {
                        return Class.forName(typeName);
                    }
                } catch (Exception e) {
                    LOG.warn("Could not load class '{}' for field '{}' on class '{}': {}", typeName, name,
                            clazz.getName(), e.getMessage());
                    LOG.debug(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private Object createObject(Field javaField, SegmentContext segmentContext, Object parentObject,
            boolean createWrapperArray) throws AtlasException {
        Class<?> clazz = getClassForField(javaField, segmentContext, parentObject, true);
        // TODO https://github.com/atlasmap/atlasmap-runtime/issues/229
        // - Allow default implementation for abstract target field
        return writerUtil.instantiateObject(clazz, segmentContext, createWrapperArray);
    }

    private Object getCollectionItem(Object collection, SegmentContext segmentContext) throws AtlasException {
        String segment = segmentContext.getSegment();
        int index = AtlasPath.indexOfSegment(segment);
        if (AtlasPath.isArraySegment(segment)) {
            return Array.get(collection, index);
        } else if (AtlasPath.isListSegment(segment)) {
            return ((List<?>) collection).get(index);
        } else if (AtlasPath.isMapSegment(segment)) {
            throw new AtlasException("Maps are currently unhandled for segment: " + segment);
        }
        throw new AtlasException("Cannot determine collection type from segment: " + segment);
    }

    private boolean collectionHasRoomForIndex(Object collection, SegmentContext segmentContext) throws AtlasException {
        String segment = segmentContext.getSegment();
        int index = AtlasPath.indexOfSegment(segment);
        int size = getCollectionSize(collection);
        boolean result = size > index;
        if (LOG.isDebugEnabled()) {
            LOG.debug("collectionHasRoomForIndex: " + result + ", size: " + size + ", index: " + index);
        }
        return result;
    }

    private int getCollectionSize(Object collection) throws AtlasException {
        if (collection instanceof List) {
            return ((List<?>) collection).size();
        } else if (collection instanceof Map) {
            return ((Map<?, ?>) collection).size();
        } else if (collection.getClass().isArray()) {
            return Array.getLength(collection);
        }
        throw new AtlasException("Cannot determine collection size for: " + collection);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addChildObject(Field field, SegmentContext segmentContext, Object parentObject, Object childObject)
            throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding child object for segment: " + segmentContext + "\n\tparentObject: " + parentObject
                    + "\n\tchild: " + childObject);
        }
        if (this.rootObject == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting root object: " + childObject);
            }
            this.rootObject = childObject;
            return;
        }
        boolean parentIsCollection = (parentObject instanceof Collection) || (parentObject.getClass().isArray());
        if (parentIsCollection) {
            String segment = segmentContext.getSegment();
            int index = AtlasPath.indexOfSegment(segment);
            if (parentObject instanceof List) {
                List list = (List) parentObject;
                if (index >= list.size()) {
                    throw new AtlasException("Cannot fit item in list, list size: " + list.size() + ", item index: "
                            + index + ", segment: " + segmentContext);
                }
                list.set(index, childObject);
            } else if (parentObject instanceof Map) {
                throw new AtlasException("FIXME: Cannot yet handle adding children to maps");
            } else if (parentObject.getClass().isArray()) {
                if (index >= Array.getLength(parentObject)) {
                    throw new AtlasException("Cannot fit item in array, array size: " + Array.getLength(parentObject)
                            + ", item index: " + index + ", segment: " + segmentContext);
                }
                try {
                    Array.set(parentObject, index, childObject);
                } catch (Exception e) {
                    String parentClass = parentObject == null ? null : parentObject.getClass().getName();
                    String childClass = childObject == null ? null : childObject.getClass().getName();
                    throw new AtlasException("Could not set child class '" + childClass + "' on parent '" + parentClass
                            + "' for: " + segmentContext, e);
                }
            } else {
                throw new AtlasException("Cannot determine collection type for: " + parentObject);
            }
        } else {
            writerUtil.setObjectOnParent(field, segmentContext, parentObject, childObject);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished adding child object for segment: " + segmentContext + "\n\tparentObject: "
                    + parentObject + "\n\t: " + childObject);
        }
    }

    private String writeDocumentToString(boolean stripSpaces, Object object) throws AtlasException {
        try {
            if (object == null) {
                return "";
            }

            String result = object.toString();

            if (stripSpaces) {
                result = result.replaceAll("\n|\r", "");
                result = result.replaceAll("> *?<", "><");
            }
            return result;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    public Object getRootObject() {
        return rootObject;
    }

    public void setRootObject(Object rootObject) {
        this.rootObject = rootObject;
    }

    public void setTargetValueConverter(TargetValueConverter converter) {
        this.converter = converter;
    }

}
