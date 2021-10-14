/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.ActionProcessor;
import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldAction;
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
import io.atlasmap.v2.Expression;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Multiplicity;
import io.atlasmap.v2.SimpleField;

public class DefaultAtlasFieldActionService implements AtlasFieldActionService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasFieldActionService.class);
    private static DefaultAtlasFieldActionService instance;
    private static Set<String> listClasses = new HashSet<>(Arrays.asList("java.util.List", "java.util.ArrayList", "java.util.LinkedList", "java.util.Vector",
            "java.util.Stack", "java.util.AbstractList", "java.util.AbstractSequentialList"));
    private static Set<String> mapClasses =  new HashSet<>(Arrays.asList("java.util.Map", "java.util.HashMap",
        "java.util.TreeMap", "java.util.Hashtable", "java.util.IdentityHashMap", "java.util.LinkedHashMap",
        "java.util.LinkedHashMap", "java.util.SortedMap", "java.util.WeakHashMap", "java.util.Properties",
        "java.util.concurrent.ConcurrentHashMap", "java.util.concurrent.ConcurrentMap"));

    private List<ActionProcessor> actionProcessors = new ArrayList<>();
    private ReadWriteLock actionProcessorsLock = new ReentrantReadWriteLock();
    private AtlasConversionService conversionService = null;
    private ActionResolver actionResolver = null;

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
            this.actionResolver = ActionResolver.getInstance().init(classLoader);
            actionProcessors.addAll(loadFieldActions(classLoader));
        } finally {
            writeLock.unlock();
        }
    }

    public List<ActionProcessor> loadFieldActions() {
        return loadFieldActions(this.getClass().getClassLoader());
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
                        Object object = clazz.getDeclaredConstructor().newInstance();
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
            o = Modifier.isStatic(method.getModifiers()) ? clazz.getDeclaredConstructor().newInstance() : null;
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
        // Java does not return methods in any consistent order, so sort
        // methods by name to ensure parameter types and values get
        // assigned to the correct method. This means that field actions with
        // multiple parameters must define their setter methods in alphabetical
        // order to be processed correctly. Not an ideal situation, but the only
        // other option would be to force the specification of an order in the
        // AtlasActionProperty annotation via a new parameter, which is also
        // clunky.
        Method[] methods = actionClazz.getMethods();
        Arrays.sort(methods, new Comparator<Method>() {

            @Override
            public int compare(Method method1, Method method2) {
                return method1.getName().compareToIgnoreCase(method2.getName());
            }
        });
        for (Method method : methods) {
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

    @Override
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

    public Field buildAndProcessAction(ActionProcessor actionProcessor, Map<String, Object> actionParameters, Field field) {
        FieldType valueType = determineFieldType(field);

        try {
            Action action = actionProcessor.getActionClass().getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> property : actionParameters.entrySet()) {
                String setter = "set" + property.getKey().substring(0, 1).toUpperCase() + property.getKey().substring(1);
                action.getClass().getMethod(setter, property.getValue().getClass()).invoke(action, property.getValue());
            }

            return processAction(action, actionProcessor, valueType, field);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("The action '%s' cannot be processed", actionProcessor.getActionDetail().getName()), e);
        }
    }

    @Override
    public Field processActions(AtlasInternalSession session, Field field) throws AtlasException {
        ArrayList<Action> actions = field.getActions();

        if (actions == null || actions.isEmpty()) {
            return field;
        }

        Field tmpSourceField = field;
        FieldType currentType = determineFieldType(field);
        for (Action action : actions) {
            ActionProcessor processor = findActionProcessor(action, currentType);
            if (processor == null) {
                AtlasUtil.addAudit(session, field, String.format(
                    "Couldn't find metadata for a FieldAction '%s', please make sure it's in the classpath, and also have a service declaration under META-INF/services. Ignoring...", action.getDisplayName()),
                    AuditStatus.WARN, null);
                continue;
            }
            ActionDetail detail = processor.getActionDetail();
            if (detail == null) {
                AtlasUtil.addAudit(session, field, String.format(
                    "Couldn't find metadata for a FieldAction '%s', ignoring...", action.getDisplayName()),
                    AuditStatus.WARN, null);
                continue;
            }

            tmpSourceField = processAction(action, processor, currentType, tmpSourceField);
            currentType = determineFieldType(tmpSourceField);
        }

        return tmpSourceField;
    }

    private FieldType determineFieldType(Field field) {
        if (field == null) {
            return null;
        }
        if (field instanceof FieldGroup) {
            for (Field f : FieldGroup.class.cast(field).getField()) {
                FieldType type = determineFieldType(f);
                if (type != null) {
                    return type;
                }
            }
            return FieldType.NONE;
        }
        if (field.getFieldType() != null) {
            return field.getFieldType();
        } else if (field.getValue() != null) {
            return getConversionService().fieldTypeFromClass(field.getValue().getClass());
        }
        return FieldType.NONE;
    }

    private Field processAction(Action action, ActionProcessor processor, FieldType sourceType, Field field) throws AtlasException {
        ActionDetail detail = processor.getActionDetail();
        Multiplicity multiplicity = detail.getMultiplicity()!= null ? detail.getMultiplicity() : Multiplicity.ONE_TO_ONE;

        if (multiplicity == Multiplicity.MANY_TO_ONE) {
            return processManyToOne(action, processor, sourceType, field);
        }

        if (multiplicity == Multiplicity.ONE_TO_MANY) {
            if (field instanceof FieldGroup) {
                while (field instanceof FieldGroup) {
                    field = ((FieldGroup)field).getField().get(0);
                }
            }
            return processOneToMany(action, processor, sourceType, field);
        }
        
        if (field instanceof FieldGroup) {
            return processActionForEachCollectionItem(action, processor, sourceType, (FieldGroup)field);
        }
        
        Object value = field.getValue();
        if (value != null && !isAssignableFieldType(detail.getSourceType(), sourceType)) {
            value = getConversionService().convertType(value, sourceType, detail.getSourceType());
        }
        value = processor.process(action, value);
        field.setFieldType(processor.getActionDetail().getTargetType());
        field.setValue(value);
        return field;
    }

    private Field processManyToOne(Action action, ActionProcessor processor, FieldType sourceType, Field field)
    throws AtlasException {
        ActionDetail detail = processor.getActionDetail();
        List<Object> values = new LinkedList<>();
        if (action instanceof Expression) {
            this.extractNestedListValuesForExpressionAction(field, values);//preserve top level list of parameters and arguments
            convertCollectionValues(values, detail.getSourceType());
            Object out = processor.process(action, values);
            return packExpressionActionOutcomeIntoField(out, field);
        }

        if (field instanceof FieldGroup) {
            extractFlatListValuesFromFieldGroup((FieldGroup)field, values);
        } else {
            Object value = field.getValue();
            if (value != null && getConversionService().isAssignableFieldType(detail.getSourceType(), sourceType)) {
                value = getConversionService().convertType(value, sourceType, detail.getSourceType());
            }
            values.add(value);
        }
        convertCollectionValues(values, detail.getSourceType());
        Object out = processor.process(action, values);
        field = AtlasModelFactory.cloneFieldToSimpleField(field);
        if (out != null) {
            field.setFieldType(getConversionService().fieldTypeFromClass(out.getClass()));
            field.setValue(out);
        }
        return field;
    }

    private void extractNestedListValuesForExpressionAction(Field field, List<Object> fieldValues) {
        if (!(field instanceof FieldGroup)) {
            fieldValues.add(field.getValue());
            return;
        }
        FieldGroup fieldGroup = (FieldGroup)field;
        if (fieldGroup == null || fieldGroup.getField() == null || fieldGroup.getField().isEmpty()) {
            return;
        }
        List<Field> fields = null;
        // Peeling off anonymous wrapper FieldGroup.
        // The size of extracted List "fieldValues" should reflect the number of Fields
        // passed in to Expression.
        while (fieldGroup.getPath() == null || fieldGroup.getPath().isEmpty()) {
            if (fieldGroup.getField().size() == 1 && (fieldGroup.getField().get(0) instanceof FieldGroup)) {
                fieldGroup = (FieldGroup)fieldGroup.getField().get(0);
            } else {
                fields = fieldGroup.getField();
                break;
            }
        }
        if (fields == null) {
            fields = new LinkedList<>();
            fields.add(fieldGroup);
        }
        doExtractValuesForExpressionAction(fields, fieldValues);
    }

    private void doExtractValuesForExpressionAction(List<Field> fields, List<Object> values) {
        for (Field subField : fields) {
            Object subValue = null;
            if (subField instanceof FieldGroup) {
                List<Object> subValues = new ArrayList<>();
                doExtractValuesForExpressionAction(((FieldGroup)subField).getField(), subValues);
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

    private Field packExpressionActionOutcomeIntoField(Object values, Field field) {
        if (values instanceof List) {
            // n -> n and 1 -> n - create new FieldGroup
            FieldGroup fieldGroup = new FieldGroup();

            // Make sure fieldGroup is of a collection type
            AtlasPath groupPath = new AtlasPath(AtlasModelFactory.GENERATED_PATH);
            fieldGroup.setCollectionType(CollectionType.LIST);
            groupPath = new AtlasPath(groupPath.toString() + AtlasPath.PATH_LIST_SUFFIX);
            fieldGroup.setPath(groupPath.toString());

            List<?> tmpSourceList = (List<?>)values;
            while (tmpSourceList.size() == 1 && (tmpSourceList.get(0) instanceof List)) {
                tmpSourceList = (List<Object>)tmpSourceList.get(0);
            }
            FieldType type = null;
            for (int i=0; i<tmpSourceList.size(); i++) {
                Object subValue = tmpSourceList.get(i);
                if (type == null && subValue != null) {
                    type = getConversionService().fieldTypeFromClass(subValue.getClass());
                }
                Field subField = new SimpleField();
                AtlasPath subPath = groupPath.clone();
                subPath.setVacantCollectionIndex(i);
                AtlasModelFactory.copyField(fieldGroup, subField, false);
                subField.setPath(subPath.toString());
                subField.setIndex(null);
                subField.setValue(subValue);
                subField.setFieldType(type);
                subField.setCollectionType(CollectionType.NONE);
                fieldGroup.getField().add(subField);
            }
            return fieldGroup;
        }
        if (values != null) {
            field = new SimpleField();
            field.setPath(AtlasModelFactory.GENERATED_PATH);
            field.setValue(values);
            field.setFieldType(getConversionService().fieldTypeFromClass(values.getClass()));
        }
        return field;
    }

    private void extractFlatListValuesFromFieldGroup(FieldGroup fieldGroup, List<Object> values) {
        if (fieldGroup == null || fieldGroup.getField() == null || fieldGroup.getField().isEmpty()) {
            return;
        }
        while ((fieldGroup.getPath() == null || fieldGroup.getPath().isEmpty())
            && fieldGroup.getField().size() == 1 && (fieldGroup.getField().get(0) instanceof FieldGroup)) {
            fieldGroup = (FieldGroup)fieldGroup.getField().get(0);
        }
        List<Object> tmpValues = new LinkedList<>();
        for (int i=0; i<fieldGroup.getField().size(); i++) {
            Field subField = fieldGroup.getField().get(i);
            Object value = null;
            if (subField instanceof FieldGroup) {
                List<Object> subValues = new LinkedList<>();
                extractFlatListValuesFromFieldGroup((FieldGroup)subField, subValues);
                value = subValues;
            } else {
                value = subField.getValue();
            }
            Integer index = subField.getIndex();
            if (index != null) {
                while (index >= tmpValues.size()) {
                    tmpValues.add(null);
                }
                tmpValues.set(index, value);
            } else {
                tmpValues.add(value);
            }
        }
        values.addAll(flatten(tmpValues));
    }

    private List<Object> flatten(List<Object> values) {
        List<Object> answer = new LinkedList<>();
        for (Object o : values) {
            if (o instanceof List) {
                answer.addAll(flatten((List<Object>)o));
            } else {
                answer.add(o);
            }
        }
        return answer;
    }

    private Field processOneToMany(Action action, ActionProcessor processor, FieldType sourceType, Field field)
            throws AtlasException {
        Object value = field.getValue();
        if (value != null && isAssignableFieldType(processor.getActionDetail().getSourceType(), sourceType)) {
            value = getConversionService().convertType(value, sourceType, processor.getActionDetail().getSourceType());
        }
        value = processor.process(action, value);
        FieldGroup answer = AtlasModelFactory.createFieldGroupFrom(field, false);
        AtlasPath path = new AtlasPath(answer.getPath() + AtlasPath.PATH_LIST_SUFFIX);
        answer.setPath(path.toString());
        answer.setCollectionType(CollectionType.LIST);
        answer.setFieldType(processor.getActionDetail().getTargetType());
        List<Object> values;
        if (value != null && value.getClass().isArray()) {
            values = Arrays.asList((Object[]) value);
        } else if ((value instanceof Collection) && !(value instanceof List)) {
            values = Arrays.asList(((Collection<?>) value).toArray());
        } else {
            values = new LinkedList<>();
            if (value != null) {
                values.add(value);
            }
        }
        for (int i=0; i<values.size(); i++) {
            Field subField = AtlasModelFactory.cloneFieldToSimpleField(answer);
            AtlasPath subPath = new AtlasPath(answer.getPath());
            subPath.setVacantCollectionIndex(i);
            subField.setPath(subPath.toString());
            subField.setCollectionType(CollectionType.NONE);
            subField.setIndex(null);
            subField.setValue(values.get(i));
            answer.getField().add(subField);
        }
        return answer;
    }

    private FieldGroup processActionForEachCollectionItem(Action action, ActionProcessor processor, FieldType sourceType, FieldGroup fieldGroup)
     throws AtlasException {
        for (Field subField : fieldGroup.getField()) {
            if (subField instanceof FieldGroup) {
                processActionForEachCollectionItem(action, processor, sourceType, (FieldGroup)subField);
                continue;
            }
            Object value = subField.getValue();
            if (value != null && isAssignableFieldType(processor.getActionDetail().getSourceType(), sourceType)) {
                value = getConversionService().convertType(value, sourceType, processor.getActionDetail().getSourceType());
            }
            value = processor.process(action, value);
            subField.setValue(value);
        }
        return fieldGroup;
    }

    private void convertCollectionValues(List<Object> sourceList, FieldType type) throws AtlasConversionException {
        for (int i = 0; i < sourceList.size(); i++) {
            Object subValue = sourceList.get(i);
            if (subValue instanceof List) {
                convertCollectionValues((List<Object>) subValue, type);
                continue;
            }
            FieldType subType = (subValue != null ? getConversionService().fieldTypeFromClass(subValue.getClass()) : FieldType.NONE);
            if (subValue != null && !isAssignableFieldType(type, subType)) {
                subValue = getConversionService().convertType(subValue, subType, type);
                sourceList.set(i, subValue);
            }
        }
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
