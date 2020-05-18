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
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.Expression;
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
import io.atlasmap.v2.Multiplicity;
import io.atlasmap.v2.SimpleField;

public class DefaultAtlasFieldActionService implements AtlasFieldActionService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasFieldActionService.class);
    private List<ActionProcessor> actionProcessors = new ArrayList<>();
    private ReadWriteLock actionProcessorsLock = new ReentrantReadWriteLock();
    private AtlasConversionService conversionService = null;
    private ActionResolver actionResolver = null;
    private static DefaultAtlasFieldActionService instance;

    private DefaultAtlasFieldActionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public static DefaultAtlasFieldActionService getInstance() {
        if (instance == null) {
            synchronized (DefaultAtlasFieldActionService.class) {
                if (instance == null) {
                    instance = new DefaultAtlasFieldActionService(DefaultAtlasConversionService.getInstance());
                    instance.init();
                }
            }
        }
        return instance;
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
        Lock writeLock = actionProcessorsLock.writeLock();
        try {
            writeLock.lock();
            actionProcessors.clear();
            this.actionResolver = ActionResolver.getInstance(classLoader);
            actionProcessors.addAll(loadFieldActions(classLoader));
        } finally {
            writeLock.unlock();
        }
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
        CollectionType sourceCollection = annotation.sourceCollectionType();
        CollectionType targetCollection = annotation.sourceCollectionType();
        if (sourceCollection != null && sourceCollection != CollectionType.NONE) {
            det.setMultiplicity(Multiplicity.MANY_TO_ONE);
        } else if (targetCollection != null && targetCollection != CollectionType.NONE) {
            det.setMultiplicity(Multiplicity.ONE_TO_MANY);
        } else {
            det.setMultiplicity(Multiplicity.ONE_TO_ONE);
        }

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
                            targetObject = det.getMultiplicity() == Multiplicity.ZERO_TO_ONE
                                ? method.invoke(null) : method.invoke(null, convertedSourceObject);
                        } else {
                            targetObject = det.getMultiplicity() == Multiplicity.ZERO_TO_ONE
                                ? method.invoke(null, action) : method.invoke(null, action, convertedSourceObject);
                        }
                    } else {
                        Object object = clazz.newInstance();
                        if (det.isCustom() != null && det.isCustom()) {
                            targetObject = det.getMultiplicity() == Multiplicity.ZERO_TO_ONE
                                ? method.invoke(object) : method.invoke(object, convertedSourceObject);
                        } else {
                            targetObject = det.getMultiplicity() == Multiplicity.ZERO_TO_ONE
                                ? method.invoke(object, action) : method.invoke(object, action, convertedSourceObject);
                        }
                    }
                } catch (Throwable e) {
                    throw new AtlasException(String.format("Error processing action %s", det.getName()), e);
                }
                return targetObject;
            }

            private Object convertSourceObject(Object sourceObject) throws AtlasConversionException {
                Class<?> paramType;
                int paramCount = method.getParameterCount();
                if (paramCount < 2) {
                    return null;
                }
                paramType = method.getParameterTypes()[1];
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

        if (method.getParameterCount() < 1) {
            LOG.debug("Invalid @AtlasActionProcessor method.  Expected at least 1 parameter: " + method);
        }

        Class<? extends Action> actionClazz = null;
        if (Action.class.isAssignableFrom(method.getParameterTypes()[0])) {
            actionClazz = (Class<? extends Action>) method.getParameterTypes()[0];
        } else {
            LOG.debug("Invalid @AtlasActionProcessor method.  1st parameter does not subclass " + Action.class.getName() + ": " + method);
        }


        final Class<?> targetClass = method.getReturnType();
        String name = actionResolver.toId(actionClazz);

        ActionDetail det = new ActionDetail();
        det.setClassName(clazz.getName());
        det.setMethod(method.getName());
        det.setName(name);
        det.setTargetType(toFieldType(targetClass, method.getGenericReturnType()));
        if (!clazz.getPackage().getName().equals("io.atlasmap.actions")) {
            det.setCustom(true);
        }

        Type[] genericParameterTypes = method.getGenericParameterTypes();
        if (genericParameterTypes.length >= 2) {
            Class<?> sourceClass = method.getParameterTypes()[1];
            if (annotation.sourceType() != FieldType.NONE) {
                det.setSourceType(annotation.sourceType());
            } else {
                det.setSourceType(toFieldType(sourceClass, method.getGenericParameterTypes()[1]));
            }
            CollectionType sourceCollection = toFieldCollectionType(sourceClass);
            CollectionType targetCollection = toFieldCollectionType(targetClass);
            if (sourceCollection != CollectionType.NONE) {
                if (targetCollection != CollectionType.NONE) {
                    det.setMultiplicity(Multiplicity.MANY_TO_MANY);
                } else {
                    det.setMultiplicity(Multiplicity.MANY_TO_ONE);
                }
            } else if (targetCollection != CollectionType.NONE) {
                det.setMultiplicity(Multiplicity.ONE_TO_MANY);
            } else {
                det.setMultiplicity(Multiplicity.ONE_TO_ONE);
            }
        } else if (genericParameterTypes.length == 1) {
            det.setMultiplicity(Multiplicity.ZERO_TO_ONE);
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
                    if( det.getMultiplicity() == Multiplicity.ZERO_TO_ONE ) {
                        return method.invoke(object, action);
                    } else {
                        sourceObject = convertSourceObject(sourceObject);
                        return method.invoke(object, action, sourceObject);
                    }
                } catch (Throwable e) {
                    throw new AtlasException(String.format("Error processing action %s", det.getName()), e);
                }
            }

            private Object convertSourceObject(Object sourceObject) throws AtlasConversionException {
                if (sourceObject == null) {
                    return null;
                }

                Class<?> paramType;
                paramType = method.getParameterTypes()[1];
                CollectionType paramCollectionType = toFieldCollectionType(paramType);
                CollectionType sourceCollectionType = toFieldCollectionType(sourceObject.getClass()) ;
                if (paramCollectionType != CollectionType.NONE) {
                    List<Object> sourceList;
                    Type itemType = method.getGenericParameterTypes()[1];
                    Class<?> itemClass = paramType.isArray() ? paramType.getComponentType()
                        : (Class<?>)((ParameterizedType) itemType).getActualTypeArguments()[0];

                    if (sourceCollectionType != CollectionType.NONE) {
                        if (sourceCollectionType == CollectionType.ARRAY) {
                            sourceList = Arrays.asList(sourceObject);
                        } else if (sourceCollectionType == CollectionType.LIST) {
                            sourceList = (List<Object>)sourceObject;
                        } else if (sourceCollectionType == CollectionType.MAP) {
                            sourceList = new ArrayList(((Map)sourceObject).values());
                        } else {
                            sourceList = new ArrayList((Collection)sourceObject);
                        }
                    } else {
                        sourceList = Arrays.asList(sourceObject);
                    }

                    convertItems(sourceList, itemClass);

                    if (paramType.isArray()) {
                        return sourceList.toArray();
                    } else {
                        return sourceList;
                    }
                } else if (paramType.isInstance(sourceObject)) {
                    return sourceObject;
                }
                return conversionService.convertType(sourceObject, null, paramType, null);
            }
        };
    }

    private void convertItems(List<Object> sourceList, Class<?> itemClass) throws AtlasConversionException {
        for (int i=0; i<sourceList.size(); i++) {
            Object item = sourceList.get(i);
            if (item != null) {
                item = conversionService.convertType(item, null, itemClass, null);
            }
            sourceList.set(i, item);
        }
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
        Lock readLock = this.actionProcessorsLock.readLock();
        try {
            readLock.lock();
            return actionProcessors.stream().map(x->x.getActionDetail()).collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
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
        Lock readLock = actionProcessorsLock.readLock();
        try {
            readLock.lock();
            for (ActionProcessor processor : actionProcessors) {
                if (customAction != null) {
                    ActionDetail detail = processor.getActionDetail();
                    if (customAction.getClassName().equals(detail.getClassName())
                        && customAction.getMethodName().equals(detail.getMethod())) {
                        matches.add(processor);
                        break;
                    }
                } else if (processor.getActionClass() == action.getClass()) {
                    matches.add(processor);
                }
            }
        } finally {
            readLock.unlock();
        }

        return findBestActionProcessor(matches, sourceType);
    }

    public ActionProcessor findActionProcessor(String name, Object value) {
        FieldType valueType = (value != null ? getConversionService().fieldTypeFromClass(value.getClass()) : FieldType.NONE);
        String uppercaseName = name.toUpperCase();

        List<ActionProcessor> processors = new ArrayList<>();
        Lock readLock = actionProcessorsLock.readLock();
        try {
            readLock.lock();
            for (ActionProcessor processor : actionProcessors) {
                if (processor.getActionDetail().getName().toUpperCase().equals(uppercaseName)) {
                    processors.add(processor);
                }
            }
        } finally {
            readLock.unlock();
        }

        return findBestActionProcessor(processors, valueType);
    }

    private ActionProcessor findBestActionProcessor(List<ActionProcessor> processors, FieldType valueType) {
        if (processors.isEmpty()) {
            return null;
        } else if (processors.size() == 1) {
            return processors.get(0);
        } else if (valueType != null && !Arrays.asList(FieldType.ANY, FieldType.NONE).contains(valueType)) {
            for (ActionProcessor processor: processors) {
                if (valueType.equals(processor.getActionDetail().getSourceType())) {
                    return processor;
                }
            }
        }
        return processors.get(0);
    }

    private Object flattenList(Object value) {
        if (value instanceof Iterable) {
            List<Object> extractedValues = new ArrayList<>();
            for (Object argument : (Iterable) value) {
                if (argument instanceof Iterable) {
                    for (Object item : (Iterable) argument) {
                        extractedValues.add(item);
                    }
                } else {
                    extractedValues.add(argument);
                }
            }
            return extractedValues;
        } else {
            return value;
        }
    }

    public Object buildAndProcessAction(ActionProcessor actionProcessor, Map<String, Object> actionParameters, List<Object> valueOrField) {
        List<Object> flattenedValue = new ArrayList<>();
        for (Object item : valueOrField) {
            if (item instanceof FieldGroup) {
                FieldGroup fieldGroup = (FieldGroup)item;
                List<Object> values = new ArrayList<>();
                extractNestedListValuesFromFieldGroup(fieldGroup, values); //preserve top level list of parameters and arguments
                flattenedValue.addAll(values);
            } else if (item instanceof Field) {
                flattenedValue.add(((Field)item).getValue());
            } else {
                Object o = flattenList(item);
                if (o instanceof Collection) {
                    flattenedValue.addAll((Collection<?>)o);
                } else {
                    flattenedValue.add(item);
                }
            }
        }
        FieldType valueType = determineFieldType(flattenedValue);
        try {
            Action action = actionProcessor.getActionClass().newInstance();
            for (Map.Entry<String, Object> property : actionParameters.entrySet()) {
                String setter = "set" + property.getKey().substring(0, 1).toUpperCase() + property.getKey().substring(1);
                action.getClass().getMethod(setter, property.getValue().getClass()).invoke(action, property.getValue());
            }

            return processAction(action, actionProcessor, valueType, flattenedValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("The action '%s' cannot be processed", actionProcessor.getActionDetail().getName()), e);
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
            if (hasExpressionAction(actions)) {
                extractNestedListValuesFromFieldGroup(fieldGroup, values); //preserve top level list of parameters and arguments
                field = findLastIndexField(fieldGroup); //arguments are last in the list
            } else {
                extractFlatListValuesFromFieldGroup(session, fieldGroup, values);
            }

            sourceObject = values;
            sourceType = determineFieldType(values);
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

            tmpSourceObject = processAction(action, processor, currentType, tmpSourceObject);
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
                Field lastSubField = null;
                FieldGroup existingFieldGroup = fieldGroup;
                if (field instanceof FieldGroup) {
                    existingFieldGroup = (FieldGroup) field;
                }

                for (int i = 0; i < sourceList.size(); i++) {
                    if (existingFieldGroup.getField().size() > i) {
                        lastSubField = existingFieldGroup.getField().get(i);
                    } else {
                        Field subField = new SimpleField();
                        AtlasModelFactory.copyField(lastSubField, subField, true);
                        existingFieldGroup.getField().add(subField);
                        lastSubField = subField;
                    }
                    lastSubField.setValue(sourceList.get(i));
                    lastSubField.setFieldType(currentType);
                }
                field = existingFieldGroup;
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
            fieldGroup = AtlasModelFactory.createFieldGroupFrom(field, true);
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

    private FieldType determineFieldType(List<Object> values) {
        if (values.size() > 0) {
            // Use last argument to determine type
            Optional<Object> o = values.stream().filter(v -> v != null).reduce((a, b) -> b);
            if (o.isPresent()) {
                if (o.get() instanceof List) {
                    // Check deeper level for expressions
                    o = ((List) o.get()).stream().filter(v -> v != null).reduce((a, b) -> b);
                }
                if (o.isPresent()) {
                    return getConversionService().fieldTypeFromClass(o.get().getClass());
                }
            }
        }
        return FieldType.NONE;
    }

    private void extractFlatListValuesFromFieldGroup(AtlasSession session, FieldGroup fieldGroup, List<Object> values) {
        if (fieldGroup == null || fieldGroup.getField() == null || fieldGroup.getField().isEmpty()) {
            return;
        }
        for (Field subField : fieldGroup.getField()) {
            Integer index = subField.getIndex();
            Object value = null;
            if (subField instanceof FieldGroup) {
                // Collection field
                List<Field> fields = ((FieldGroup) subField).getField();
                if (fields != null && !fields.isEmpty()) {
                    if (fieldGroup.getField().size() == 1 && (index == 0 || index == null)) {
                        //For backwards compatibility treat as a collection in a SimpleField
                        for (Field field: fields) {
                            values.add(field.getValue());
                        }
                        break;
                    } else {
                        AtlasUtil.addAudit(session, subField.getDocId(), "Using only the first element of " +
                                "the collection since a single value is expected in a multi-field selection.",
                            subField.getPath(), AuditStatus.WARN, null);
                        value = fields.get(0).getValue(); //get only the first value
                    }
                }
            } else {
                value = subField.getValue();
            }

            if (index != null) {
                while (index >= values.size()) {
                    values.add(null);
                }
                values.set(index, value);
            } else {
                values.add(value);
            }
        }
    }

    private void extractNestedListValuesFromFieldGroup(FieldGroup fieldGroup, List<Object> values) {
        if (fieldGroup == null || fieldGroup.getField() == null || fieldGroup.getField().isEmpty()) {
            return;
        }
        for (Field subField : fieldGroup.getField()) {
            Object subValue = null;
            if (subField instanceof FieldGroup) {
                List<Object> subValues = new ArrayList<>();
                extractNestedListValuesFromFieldGroup((FieldGroup)subField, subValues);
                subValue = subValues;
            } else {
                subValue = subField.getValue();
            }
            Integer index = subField.getIndex();
            if (index != null) {
                while (index >= values.size()) {
                    values.add(null);
                }
                values.set(index, subValue);
            } else {
                values.add(subValue);
            }
        }
    }

    private boolean hasExpressionAction(List<Action> actions) {
        for (Action action: actions) {
            if (action instanceof Expression) {
                return true;
            }
        }
        return false;
    }

    private Field findLastIndexField(FieldGroup fieldGroup) {
        Field lastSubField = fieldGroup.getField().get(0);
        for (Field subField: fieldGroup.getField()) {
            int subFieldIndex = (subField.getIndex() == null) ? Integer.MAX_VALUE : subField.getIndex();
            int lastSubFieldIndex = (lastSubField.getIndex() == null) ? Integer.MAX_VALUE: lastSubField.getIndex();
            if (lastSubFieldIndex <= subFieldIndex) {
                lastSubField = subField;
            }
        }
        return lastSubField;
    }

    private Object processAction(Action action, ActionProcessor processor, FieldType sourceType, Object sourceObject) throws AtlasException {
        ActionDetail detail = processor.getActionDetail();
        Multiplicity multiplicity = detail.getMultiplicity()!= null ? detail.getMultiplicity() : Multiplicity.ONE_TO_ONE;

        if (sourceObject instanceof List) {
            List<Object> tmpSourceList = (List<Object>) sourceObject;
            for (int i = 0; i < tmpSourceList.size(); i++) {
                Object subValue = tmpSourceList.get(i);
                FieldType subType = (subValue != null ? getConversionService().fieldTypeFromClass(subValue.getClass()) : FieldType.NONE);
                if (subValue != null && !isAssignableFieldType(detail.getSourceType(), subType)) {
                    subValue = getConversionService().convertType(subValue, subType, detail.getSourceType());
                    tmpSourceList.set(i, subValue);
                }
                if (multiplicity != Multiplicity.MANY_TO_ONE) {
                    subValue = processor.process(action, subValue);
                    tmpSourceList.set(i, subValue);
                }
            }
        } else if (!isAssignableFieldType(detail.getSourceType(), sourceType)) {
            sourceObject = getConversionService().convertType(sourceObject, sourceType, detail.getSourceType());
        }
        if (!(sourceObject instanceof List) || multiplicity == Multiplicity.MANY_TO_ONE) {
            sourceObject = processor.process(action, sourceObject);
        }

        if (sourceObject != null && sourceObject.getClass().isArray()) {
            sourceObject = Arrays.asList((Object[]) sourceObject);
        } else if ((sourceObject instanceof Collection) && !(sourceObject instanceof List)) {
            sourceObject = Arrays.asList(((Collection<?>) sourceObject).toArray());
        }
        return sourceObject;
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
