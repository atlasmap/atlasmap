package io.atlasmap.java.module;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.java.inspect.ClassHelper;
import io.atlasmap.java.inspect.JdkPackages;
import io.atlasmap.java.inspect.StringUtil;
import io.atlasmap.v2.Field;

public class JavaWriterUtil {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaWriterUtil.class);
    protected AtlasConversionService conversionService = null;

    public JavaWriterUtil(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Instantiate the given class.
     *
     * @param JavaField
     *            The JavaField to instantiate.
     * @param segmentContext
     *            Provided for error clarity, no use to actual functionality here.
     * @return
     * @throws AtlasException
     *             If class is abstract or an unsupported collectionType is
     *             specified.
     */
    public Object instantiateObject(Class<?> clz, SegmentContext segmentContext, boolean createWrapperArray)
            throws AtlasException {
        try {
            if (Modifier.isAbstract(clz.getModifiers()) && !clz.isPrimitive() && !clz.isArray()) {
                throw new AtlasException("Cannot instantiate object, class is abstract: " + clz.getName()
                        + ", segment: " + segmentContext);
            }
            if (createWrapperArray && AtlasPath.isArraySegment(segmentContext.getSegment())) {
                int size = AtlasPath.indexOfSegment(segmentContext.getSegment()) + 1;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Instantiating array of size " + size + " for class '" + clz.getName() + "', segment: "
                            + segmentContext);
                }
                return Array.newInstance(clz, size);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Instantiating object for class '" + clz.getName() + "', segment: " + segmentContext);
            }
            return clz.newInstance();
        } catch (Exception e) {
            throw new AtlasException(
                    "Could not instantiate class: " + clz.getName() + ", segment: " + segmentContext.getSegmentPath(),
                    e);
        }
    }

    /**
     * Retrieve a child object (which may be a complex class or collection class)
     * from the given parentObject.
     *
     * @param field
     *            - provided for convenience, probably not needed here
     * @param ParentObject
     *            - the object to find the child on
     * @param segmentContext
     *            - the segment of the field's path that references the child object
     */
    public Object getObjectFromParent(Field field, Object parentObject, SegmentContext segmentContext)
            throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Retrieving child '" + segmentContext.getSegmentPath() + "'.\n\tparentObject: " + parentObject);
        }

        if (parentObject == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot find child '" + segmentContext.getSegmentPath() + "', parent is null.");
            }
            return null;
        }

        // clean up our segment from something like "@addressLine1" to "addressLine1".
        // collection segments like "orders[4]" will be cleaned to "orders"
        String cleanedSegment = AtlasPath.cleanPathSegment(segmentContext.getSegment());

        // FIXME: this doesn't work if there isn't a getter but there is a private
        // member variable

        List<String> getters = ClassHelper.getterMethodNames(cleanedSegment);
        Method getterMethod = null;
        for (String getter : getters) {
            try {
                getterMethod = ClassHelper.detectGetterMethod(parentObject.getClass(), getter);
                break;
            } catch (NoSuchMethodException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Looking for getter for '" + segmentContext.getSegmentPath() + " on this class: "
                            + parentObject.getClass().getName(), e);
                }
            }
        }

        if (getterMethod == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unable to detect getter method for: " + segmentContext.getSegment() + " from "
                        + segmentContext.getSegmentPath() + " on parent: " + parentObject);
            }
            return null;
        }

        getterMethod.setAccessible(true);
        Object childObject;
        try {
            childObject = getterMethod.invoke(parentObject);
        } catch (Exception e) {
            throw new AtlasException(e);
        }

        if (LOG.isDebugEnabled()) {
            if (childObject == null) {
                LOG.debug("Could not find child object for path: " + segmentContext.getSegmentPath());
            } else {
                LOG.debug("Found child object for path '" + segmentContext.getSegmentPath() + "': " + childObject);
            }
        }

        return childObject;
    }

    /**
     * Set the given object within the parentObject.
     *
     * @param field
     *            - provided if we need it, I don't think we will since we already
     *            have the value in hand?
     * @param segmentContext
     *            - current segment for the field's path, this will be the last
     *            segment in the path.
     * @param parentObject
     *            - the object we're setting the value in
     * @param childObject
     *            - the childObject to set
     */

    public void setObjectOnParent(Field javaField, SegmentContext segmentContext, Object parentObject,
            Object childObject) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting object for path:'" + javaField.getPath() + "'.\n\tchildObject: " + childObject
                    + "\n\tparentObject: " + parentObject);
        }

        AtlasPath pathUtil = new AtlasPath(javaField.getPath());

        try {
            Class<?> childClass = childObject == null ? null : childObject.getClass();
            Method targetMethod = resolveSetMethod(parentObject, segmentContext, childClass);
            Object targetObject = parentObject;

            // We already know we have a 1 paramter setter here
            if (childObject == null && conversionService.isPrimitive(targetMethod.getParameterTypes()[0])) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Not setting null value for primitive method paramter for path:'" + javaField.getPath()
                            + "'.\n\tchildObject: " + childObject + "\n\tparentObject: " + parentObject);
                }
                return;
            }

            if (targetMethod != null) {
                targetMethod.invoke(targetObject, childObject);
            } else {
                try {
                    java.lang.reflect.Field field = targetObject.getClass().getField(pathUtil.getLastSegment());
                    field.setAccessible(true);
                    field.set(targetObject, childObject);
                    javaField.setValue(field.get(targetObject));
                } catch (NoSuchFieldException nsfe) {
                    throw new AtlasException("Unable to find matting setter method or field for path: "
                            + javaField.getPath() + " on parentObject: " + parentObject.getClass().getName());
                }
            }
        } catch (Exception e) {
            String parentClassName = parentObject == null ? null : parentObject.getClass().getName();
            String childClassName = childObject == null ? null : childObject.getClass().getName();
            throw new AtlasException("Unable to set value for path: " + javaField.getPath() + " parentObject: "
                    + parentClassName + " childObject: " + childClassName, e);
        }
    }

    protected Method resolveSetMethod(Object sourceObject, SegmentContext segmentContext, Class<?> targetType)
            throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String setterMethodName = "set" + capitalizeFirstLetter(AtlasPath.cleanPathSegment(segmentContext.getSegment()));

        List<Class<?>> classTree = resolveMappableClasses(sourceObject.getClass());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found " + classTree.size() + " mappable classes for class '"
                    + sourceObject.getClass().getName() + "': " + classTree);
        }

        Method m = null;
        for (Class<?> clazz : classTree) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Looking for setter '" + setterMethodName + "' on this class: " + clazz.getName());
            }
            try {
                m = ClassHelper.detectSetterMethod(clazz, setterMethodName, targetType);
                if (m != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found setter '" + setterMethodName + "' on this class: " + clazz.getName());
                    }
                    return m;
                }
            } catch (NoSuchMethodException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Did not find setter '" + setterMethodName + "' on this class: " + clazz.getName(), e);
                }
            }

            // Try the boxUnboxed version
            if (conversionService.isPrimitive(targetType) || conversionService.isBoxedPrimitive(targetType)) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Looking for boxed setter '" + setterMethodName + "' on this class: "
                                + clazz.getName());
                    }
                    m = ClassHelper.detectSetterMethod(clazz, setterMethodName,
                            conversionService.boxOrUnboxPrimitive(targetType));
                    if (m != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found setter '" + setterMethodName + "' on this class: " + clazz.getName());
                        }
                        return m;
                    }
                } catch (NoSuchMethodException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Did not find setter '" + setterMethodName + "' on this class: " + clazz.getName(),
                                e);
                    }
                }
            }
        }

        throw new NoSuchMethodException("Unable to resolve expected setter '" + setterMethodName + "' for path: "
                + segmentContext.getSegmentPath() + ", on object: " + sourceObject);
    }

    public static String capitalizeFirstLetter(String string) {
        if (StringUtil.isEmpty(string)) {
            return string;
        }
        if (string.length() == 1) {
            return String.valueOf(string.charAt(0)).toUpperCase();
        }
        return String.valueOf(string.charAt(0)).toUpperCase() + string.substring(1);
    }

    protected List<Class<?>> resolveMappableClasses(Class<?> className) {
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

        // DON'T reverse.. prefer child -> parent -> grandparent
        // List<Class<?>> reverseTree = classTree.subList(0, classTree.size());
        // Collections.reverse(reverseTree);
        // return reverseTree;

        return classTree;
    }
}
