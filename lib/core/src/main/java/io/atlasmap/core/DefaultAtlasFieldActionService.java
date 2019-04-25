package io.atlasmap.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.ActionParameter;
import io.atlasmap.v2.ActionParameters;
import io.atlasmap.v2.ActionResolver;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.CustomAction;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.SimpleField;

public class DefaultAtlasFieldActionService implements AtlasFieldActionService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasFieldActionService.class);
    private List<ActionProcessor> actionProcessors = new ArrayList<>();
    private AtlasConversionService conversionService = null;

    public DefaultAtlasFieldActionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void init() {
        // TODO load custom field actions in application bundles
        // on hierarchical class loader environment
        init(this.getClass().getClassLoader());
    }

    private JavaType javaType(Type type) {
        return TypeFactory.defaultInstance().constructType(type);
    }

    public void init(ClassLoader classLoader) {
        actionProcessors.clear();
        actionProcessors.addAll(loadFieldActions(classLoader));
    }

    public List<ActionProcessor> loadFieldActions() {
        return loadFieldActions(this.getClass().getClassLoader());
    }

    interface ActionProcessor {
        ActionDetail getActionDetail();
        Class<? extends Action> getActionClass();
        Object process(Action action, Object sourceObject) throws AtlasException;
    }

    public List<ActionProcessor> loadFieldActions(ClassLoader classLoader) {
        final ServiceLoader<AtlasFieldAction> fieldActionServiceLoader = ServiceLoader.load(AtlasFieldAction.class,
            classLoader);
        final ServiceLoader<io.atlasmap.api.AtlasFieldAction> compat = ServiceLoader.load(io.atlasmap.api.AtlasFieldAction.class,
            classLoader);
        List<ActionProcessor> answer = new ArrayList<>();
        fieldActionServiceLoader.forEach(atlasFieldAction -> createActionProcessor(atlasFieldAction, answer));
        compat.forEach(atlasFieldAction -> createActionProcessor(atlasFieldAction, answer));

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Loaded %s Field Actions", answer.size()));
        }
        return answer;
    }

    private void createActionProcessor(AtlasFieldAction atlasFieldAction, List<ActionProcessor> answer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading FieldAction class: " + atlasFieldAction.getClass().getCanonicalName());
        }

        Class<?> clazz = atlasFieldAction.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {

            // Continue supporting creating details from @AtlasFieldActionInfo
            ActionProcessor det = createDetailFromFieldActionInfo(clazz, method);
            if (det != null) {
                answer.add(det);
            }

            // Also support using new simpler @AtlasActionProcessor
            det = createDetailFromProcessor(clazz, method);
            if (det != null) {
                answer.add(det);
            }
        }
    }

    private ActionProcessor createDetailFromFieldActionInfo(final Class<?> clazz, final Method method) {
        AtlasFieldActionInfo annotation = method.getAnnotation(AtlasFieldActionInfo.class);
        if (annotation == null) {
            return null;
        }

        final ActionDetail det = new ActionDetail();
        det.setClassName(clazz.getName());
        det.setMethod(method.getName());
        det.setName(annotation.name());
        det.setSourceType(annotation.sourceType());
        det.setTargetType(annotation.targetType());
        det.setSourceCollectionType(annotation.sourceCollectionType());
        det.setTargetCollectionType(annotation.targetCollectionType());

        Class<? extends Action> actionClazz;
        try {
            actionClazz = (Class<? extends Action>) Class.forName("io.atlasmap.v2." + annotation.name());
        } catch (Exception e) {
            actionClazz = null;
            det.setCustom(true);
        }

        try {
            det.setActionSchema(actionClazz);
        } catch (Exception e) {
            LOG.error(String.format("Could not get json schema for action=%s msg=%s", annotation.name(), e.getMessage()), e);
        }

        try {
            // TODO https://github.com/atlasmap/atlasmap/issues/538
            if (det.isCustom() == null || !det.isCustom()) {
                det.setParameters(detectFieldActionParameters(actionClazz));
            }
        } catch (ClassNotFoundException e) {
            LOG.error(String.format("Error detecting parameters for field action=%s msg=%s", annotation.name(), e.getMessage()), e);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Loaded FieldAction: " + det.getName());
        }

        Class<? extends Action> finalActionClazz = actionClazz;
        return new ActionProcessor() {

            @Override
            public ActionDetail getActionDetail() {
                return det;
            }

            @Override
            public Class<? extends Action> getActionClass() {
                return finalActionClazz;
            }

            @Override
            public Object process(Action action, Object sourceObject) throws AtlasException {
                if (det == null) {
                    return sourceObject;
                }

                Object targetObject = null;
                try {
                    Object convertedSourceObject = convertSourceObject(sourceObject);

                    if (Modifier.isStatic(method.getModifiers())) {
                        // TODO eliminate Action parameter even for OOTB
                        // we can use annotation also for the parameters instead
                        // cf. https://github.com/atlasmap/atlasmap/issues/536
                        if (det.isCustom() != null && det.isCustom()) {
                            targetObject = method.invoke(null, convertedSourceObject);
                        } else {
                            targetObject = method.invoke(null, action, convertedSourceObject);
                        }
                    } else {
                        Object object = clazz.newInstance();
                        if (det.isCustom() != null && det.isCustom()) {
                            targetObject = method.invoke(object, convertedSourceObject);
                        } else {
                            targetObject = method.invoke(object, action, convertedSourceObject);
                        }
                    }
                } catch (Throwable e) {
                    throw new AtlasException(String.format("Error processing action %s", det.getName()), e);
                }
                return targetObject;
            }

            private Object convertSourceObject(Object sourceObject) throws AtlasConversionException {
                Class<?> paramType;
                // TODO eliminate Action parameter even for OOTB
                // we can use annotation also for the parameters instead
                // cf. https://github.com/atlasmap/atlasmap/issues/536
                if (det.isCustom() != null && det.isCustom()) {
                    paramType = method.getParameterTypes()[0];
                } else {
                    paramType = method.getParameterTypes()[1];
                }
                if (paramType.isInstance(sourceObject)) {
                    return sourceObject;
                }
                return conversionService.convertType(sourceObject, null, paramType, null);
            }
        };
    }

    private ActionProcessor createDetailFromProcessor(Class<?> clazz, Method method) {
        AtlasActionProcessor annotation = method.getAnnotation(AtlasActionProcessor.class);
        if (annotation == null) {
            return null;
        }

        if (method.getParameterCount() >= 1) {
            LOG.debug("Invalid @AtlasActionProcessor method.  Expected at least 1 parameter: " + method);
        }

        Class<? extends Action> actionClazz = null;
        if (Action.class.isAssignableFrom(method.getParameterTypes()[0])) {
            actionClazz = (Class<? extends Action>) method.getParameterTypes()[0];
        } else {
            LOG.debug("Invalid @AtlasActionProcessor method.  1st parameter does not subclass " + Action.class.getName() + ": " + method);
        }


        final Class<?> targetClass = method.getReturnType();



        String name = ActionResolver.toId(actionClazz);

        ActionDetail det = new ActionDetail();
        det.setClassName(clazz.getName());
        det.setMethod(method.getName());
        det.setName(name);
        det.setTargetType(toFieldType(targetClass, method.getGenericReturnType()));
        det.setTargetCollectionType(toFieldCollectionType(targetClass));



        Type[] genericParameterTypes = method.getGenericParameterTypes();
        if( genericParameterTypes.length >= 2) {
            Class<?> sourceClass = method.getParameterTypes()[1];
            det.setSourceType(toFieldType(sourceClass, method.getGenericParameterTypes()[1]));
            det.setSourceCollectionType(toFieldCollectionType(sourceClass));
        }

        try {
            det.setActionSchema(actionClazz);
        } catch (Exception e) {
            LOG.error(String.format("Could not get json schema for action=%s msg=%s", clazz.getName(), e.getMessage()), e);
        }

        try {
            det.setParameters(detectFieldActionParameters(actionClazz));
        } catch (ClassNotFoundException e) {
            LOG.error(String.format("Error detecting parameters for field action=%s msg=%s", det.getName(), e.getMessage()), e);
        }

        Object o = null;
        try {
            o = Modifier.isStatic(method.getModifiers()) ? clazz.newInstance() : null;
        } catch (Throwable e) {
            LOG.error(String.format("Error creating object instance for action=%s msg=%s", det.getName(), e.getMessage()), e);
        }
        final Object object = o;

        Class<? extends Action> finalActionClazz = actionClazz;
        return new ActionProcessor() {
            @Override
            public ActionDetail getActionDetail() {
                return det;
            }

            @Override
            public Class<? extends Action> getActionClass() {
                return finalActionClazz;
            }

            @Override
            public Object process(Action action, Object sourceObject) throws AtlasException {
                try {
                    if( det.getSourceType() == null ) {
                        return method.invoke(object, action);
                    } else {
                        sourceObject = conversionService.convertType(sourceObject, det.getSourceType(), det.getTargetType());
                        return method.invoke(object, action, sourceObject);
                    }
                } catch (Throwable e) {
                    throw new AtlasException(String.format("Error processing action %s", det.getName()), e);
                }
            }
        };
    }

    private static Set<String> listClasses = new HashSet<>(Arrays.asList("java.util.List", "java.util.ArrayList", "java.util.LinkedList", "java.util.Vector",
            "java.util.Stack", "java.util.AbstractList", "java.util.AbstractSequentialList"));
    private static Set<String> mapClasses =  new HashSet<>(Arrays.asList("java.util.Map", "java.util.HashMap",
        "java.util.TreeMap", "java.util.Hashtable", "java.util.IdentityHashMap", "java.util.LinkedHashMap",
        "java.util.LinkedHashMap", "java.util.SortedMap", "java.util.WeakHashMap", "java.util.Properties",
        "java.util.concurrent.ConcurrentHashMap", "java.util.concurrent.ConcurrentMap"));

    private CollectionType toFieldCollectionType(Class<?> clazz) {
        if (clazz.isArray()) {
            return CollectionType.ARRAY;
        }
        if (clazz == Collection.class) {
            return CollectionType.ALL;
        }
        if( listClasses.contains(clazz.getName())) {
            return CollectionType.LIST;
        }
        if( mapClasses.contains(clazz.getName())) {
            return CollectionType.MAP;
        }
        return CollectionType.NONE;
    }

    private FieldType toFieldType(Class<?> clazz, Type parameterType) {
        switch (toFieldCollectionType(clazz)) {
            case ARRAY:
                return toFieldType(clazz.getComponentType(), parameterType);
            case ALL:
                Type t = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
                return DefaultAtlasConversionService.getInstance().fieldTypeFromClass((Class)t);
            case LIST:
                t = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
                return DefaultAtlasConversionService.getInstance().fieldTypeFromClass((Class)t);
            case MAP:
                t = ((ParameterizedType) parameterType).getActualTypeArguments()[1];
                return DefaultAtlasConversionService.getInstance().fieldTypeFromClass((Class)t);
            default:
                return DefaultAtlasConversionService.getInstance().fieldTypeFromClass(clazz);
        }
    }


    private ActionParameters detectFieldActionParameters(Class<?> actionClazz) throws ClassNotFoundException {
        ActionParameters params = null;
        for (Method method : actionClazz.getMethods()) {
            // Find setters to avoid the get / is confusion
            if (method.getParameterCount() == 1 && method.getName().startsWith("set")) {
                // We have a parameter
                if (params == null) {
                    params = new ActionParameters();
                }

                ActionParameter actionParam = null;
                for (Parameter methodParam : method.getParameters()) {
                    actionParam = new ActionParameter();
                    actionParam.setName(camelize(method.getName().substring("set".length())));
                    // TODO set displayName/description - https://github.com/atlasmap/atlasmap/issues/96
                    actionParam.setFieldType(getConversionService().fieldTypeFromClass(methodParam.getType()));
                    // TODO fix this dirty hack for https://github.com/atlasmap/atlasmap/issues/386
                    if (methodParam.getType().isEnum()) {
                        actionParam.setFieldType(FieldType.STRING);
                        try {
                            for (Object e : methodParam.getType().getEnumConstants()) {
                                Method m = e.getClass().getDeclaredMethod("value", new Class[0]);
                                actionParam.getValues().add(m.invoke(e, new Object[0]).toString());
                            }
                        } catch (Exception e) {
                            LOG.debug("Failed to populate possible enum parameter values, ignoring...", e);
                        }
                    }
                    params.getParameter().add(actionParam);
                }
            }
        }

        return params;
    }

    @Override
    public List<ActionDetail> listActionDetails() {
        return actionProcessors.stream().map(x->x.getActionDetail()).collect(Collectors.toList());
    }

    /*
     * TODO: getActionDetailByActionName() when all references are updated to use
     *
     * ActionDetail = findActionDetail(String actionName, FieldType sourceType)
     *
     * ref: https://github.com/atlasmap/atlasmap-runtime/issues/216
     */
    @Deprecated
    protected ActionDetail getActionDetailByActionName(String actionName) {
        for (ActionDetail actionDetail : listActionDetails()) {
            if (actionDetail.getName().equals(actionName)) {
                return actionDetail;
            }
        }

        return null;
    }

    /**
     * 1. Find FieldAction by name
     * 2. If multiple matches are found, return the best one based on FieldType sourceType
     * 3. If there is not an exact match to sourceType, return the first FieldAction
     * 4. If no matches found, return null
     *
     * @param action     The name of the FieldAction
     * @param sourceType A hint used to determine which FieldAction to use when
     *                   multiple FieldActions exist with the same name
     * @return ActionDetail
     */
    @Override
    public ActionDetail findActionDetail(Action action, FieldType sourceType) throws AtlasException {
        ActionProcessor processor = findActionProcessor(action, sourceType);
        if( processor == null ) {
            return null;
        }
        return processor.getActionDetail();
    }

    public ActionProcessor findActionProcessor(Action action, FieldType sourceType) throws AtlasException {
        CustomAction customAction = null;
        if (action instanceof CustomAction) {
            customAction = (CustomAction) action;
            if (customAction.getClassName() == null || customAction.getMethodName() == null) {
                throw new AtlasException("The class name and method name must be specified for custom FieldAction: " + customAction.getName());
            }
        }
        List<ActionProcessor> matches = new ArrayList<>();
        for (ActionProcessor processor : actionProcessors) {
            ActionDetail detail = processor.getActionDetail();
            if (customAction != null) {
                if (customAction.getClassName().equals(detail.getClassName())
                    && customAction.getMethodName().equals(detail.getMethod())) {
                    matches.add(processor);
                    break;
                }
            } else if (processor.getActionClass() == action.getClass()) {
                matches.add(processor);
            }
        }

        switch (matches.size()) {
            case 0:
                return null;
            case 1:
                return matches.get(0);
            default:
                if (sourceType != null && !Arrays.asList(FieldType.ANY, FieldType.NONE).contains(sourceType)) {
                    for (ActionProcessor processor : matches) {
                        if (sourceType.equals(processor.getActionDetail().getSourceType())) {
                            return processor;
                        }
                    }
                }
                return matches.get(0);
        }
    }

    @Override
    public Field processActions(AtlasInternalSession session, Field field) throws AtlasException {
        ArrayList<Action> actions = field.getActions();
        FieldType targetType = field.getFieldType();

        if (actions == null || actions == null || actions.isEmpty()) {
            return field;
        }

        if (FieldType.COMPLEX.equals(targetType)) {
            return field;
        }

        Object sourceObject = field.getValue();
        FieldType sourceType = (sourceObject != null ? getConversionService().fieldTypeFromClass(sourceObject.getClass()) : FieldType.NONE);
        FieldGroup fieldGroup = null;
        if (field instanceof FieldGroup) {
            fieldGroup = (FieldGroup) field;
            List<Object> values = new ArrayList<>();
            for (Field subField : fieldGroup.getField()) {
                values.add(subField.getValue());
            }
            sourceObject = values;
            if (values.size() > 0) {
                sourceType = getConversionService().fieldTypeFromClass(values.get(0).getClass());
            } else {
                sourceType = FieldType.NONE;
            }
        }

        Object tmpSourceObject = sourceObject;

        FieldType currentType = sourceType;
        for (Action action : actions) {
            ActionProcessor processor = findActionProcessor(action, currentType);
            ActionDetail detail = processor.getActionDetail();
            if (detail == null) {
                AtlasUtil.addAudit(session, field.getDocId(), String.format(
                    "Couldn't find metadata for a FieldAction '%s', ignoring...", action.getDisplayName()),
                    field.getPath(), AuditStatus.WARN, null);
                continue;
            }

            CollectionType sourceCollectionType = detail.getSourceCollectionType() != null ? detail.getSourceCollectionType() : CollectionType.NONE;
            if (tmpSourceObject instanceof List) {
                List<Object> tmpSourceList = (List<Object>) tmpSourceObject;
                for (int i = 0; i < tmpSourceList.size(); i++) {
                    Object subValue = tmpSourceList.get(i);
                    FieldType subType = (subValue != null ? getConversionService().fieldTypeFromClass(subValue.getClass()) : FieldType.NONE);
                    if (subValue != null && !isAssignableFieldType(detail.getSourceType(), subType)) {
                        subValue = getConversionService().convertType(subValue, subType, detail.getSourceType());
                        tmpSourceList.set(i, subValue);
                    }
                    if (sourceCollectionType == CollectionType.NONE) {
                        subValue = processor.process(action, subValue);
                        tmpSourceList.set(i, subValue);
                    }
                }
            } else if (!isAssignableFieldType(detail.getSourceType(), currentType)) {
                tmpSourceObject = getConversionService().convertType(sourceObject, currentType, detail.getSourceType());
            }
            if (!(tmpSourceObject instanceof List) || sourceCollectionType != CollectionType.NONE) {
                tmpSourceObject = processor.process(action, tmpSourceObject);
            }

            if (tmpSourceObject != null && tmpSourceObject.getClass().isArray()) {
                tmpSourceObject = Arrays.asList((Object[]) tmpSourceObject);
            } else if ((tmpSourceObject instanceof java.util.Collection) && !(tmpSourceObject instanceof List)) {
                tmpSourceObject = Arrays.asList(((java.util.Collection<?>) tmpSourceObject).toArray());
            }
            currentType = null;
            if (tmpSourceObject instanceof List) {
                for (Object item : ((List<?>) tmpSourceObject)) {
                    if (item != null) {
                        currentType = conversionService.fieldTypeFromClass(item.getClass());
                        break;
                    }
                }
            } else if (tmpSourceObject != null) {
                currentType = conversionService.fieldTypeFromClass(tmpSourceObject.getClass());
            }
        }

        if (fieldGroup != null) {
            if (tmpSourceObject instanceof List) {
                // n -> n - reuse passed-in FieldGroup
                List<?> sourceList = (List<?>) tmpSourceObject;
                for (int i = 0; i < sourceList.size(); i++) {
                    if (fieldGroup.getField().size() > i) {
                        Field subField = fieldGroup.getField().get(i);
                        subField.setValue(sourceList.get(i));
                        subField.setFieldType(currentType);
                    } else {
                        AtlasUtil.addAudit(session, fieldGroup.getDocId(),
                            "FieldAction created more values than expected, ignoring", fieldGroup.getPath(),
                            AuditStatus.WARN, sourceList.get(i).toString());
                    }
                }
                field = fieldGroup;
            } else {
                // n -> 1 - create new Field
                Field newField = new SimpleField();
                AtlasModelFactory.copyField(field, newField, false);
                newField.setValue(tmpSourceObject);
                newField.setFieldType(currentType);
                field = newField;
            }
        } else if (tmpSourceObject instanceof List) {
            // 1 -> n - create new FieldGroup
            fieldGroup = AtlasModelFactory.createFieldGroupFrom(field);
            for (Object subValue : (List<?>) tmpSourceObject) {
                Field subField = new SimpleField();
                AtlasModelFactory.copyField(field, subField, false);
                subField.setValue(subValue);
                subField.setFieldType(currentType);
                fieldGroup.getField().add(subField);
            }
            field = fieldGroup;
        } else {
            // 1 -> 1 = reuse passed-in Field
            field.setValue(tmpSourceObject);
            field.setFieldType(currentType);
        }
        return field;
    }

    private boolean isAssignableFieldType(FieldType expected, FieldType actual) {
        if (FieldType.ANY.equals(expected)) {
            return true;
        }
        if (FieldType.ANY_DATE.equals(expected)) {
            return FieldType.DATE.equals(actual) || FieldType.TIME.equals(actual)
                || FieldType.DATE_TIME.equals(actual) || FieldType.DATE_TIME_TZ.equals(actual)
                || FieldType.DATE_TZ.equals(actual) || FieldType.TIME_TZ.equals(actual);
        }
        return expected.equals(actual);
    }

    public AtlasConversionService getConversionService() {
        return this.conversionService;
    }

    private String camelize(String parameter) {
        if (parameter == null || parameter.length() == 0) {
            return parameter;
        }
        char c[] = parameter.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
