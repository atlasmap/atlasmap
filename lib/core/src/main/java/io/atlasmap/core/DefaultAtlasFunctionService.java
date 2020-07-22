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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFunction;
import io.atlasmap.spi.AtlasFunctionInfo;
import io.atlasmap.spi.AtlasFunctionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.BaseFunction;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.CustomFunction;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Function;
import io.atlasmap.v2.FunctionDetail;
import io.atlasmap.v2.FunctionParameter;
import io.atlasmap.v2.FunctionParameters;
import io.atlasmap.v2.FunctionResolver;
import io.atlasmap.v2.Mapping;

public class DefaultAtlasFunctionService implements AtlasFunctionService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasFunctionService.class);
    private static DefaultAtlasFunctionService instance;
    private static Set<String> listClasses = new HashSet<>(
            Arrays.asList("java.util.List", "java.util.ArrayList", "java.util.LinkedList", "java.util.Vector",
                    "java.util.Stack", "java.util.AbstractList", "java.util.AbstractSequentialList"));
    private static Set<String> mapClasses = new HashSet<>(Arrays.asList("java.util.Map", "java.util.HashMap",
            "java.util.TreeMap", "java.util.Hashtable", "java.util.IdentityHashMap", "java.util.LinkedHashMap",
            "java.util.LinkedHashMap", "java.util.SortedMap", "java.util.WeakHashMap", "java.util.Properties",
            "java.util.concurrent.ConcurrentHashMap", "java.util.concurrent.ConcurrentMap"));

    private List<FunctionProcessor> functionProcessors = new ArrayList<>();
    private ReadWriteLock functionProcessorsLock = new ReentrantReadWriteLock();
    private AtlasConversionService conversionService = null;
    private FunctionResolver functionResolver = null;

    private DefaultAtlasFunctionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public static DefaultAtlasFunctionService getInstance() {
        if (instance == null) {
            synchronized (DefaultAtlasFunctionService.class) {
                if (instance == null) {
                    instance = new DefaultAtlasFunctionService(DefaultAtlasConversionService.getInstance());
                    instance.init();
                }
            }
        }
        return instance;
    }

    public void init() {
        // TODO load custom functions in application bundles
        // on hierarchical class loader environment
        init(this.getClass().getClassLoader());
    }

    public void init(ClassLoader classLoader) {
        Lock writeLock = functionProcessorsLock.writeLock();
        try {
            writeLock.lock();
            functionProcessors.clear();
            functionResolver = FunctionResolver.getInstance(classLoader);
            functionProcessors.addAll(loadFunctions(classLoader));
        } finally {
            writeLock.unlock();
        }
    }

    public List<FunctionProcessor> loadFunctions() {
        return loadFunctions(this.getClass().getClassLoader());
    }

    interface FunctionProcessor {
        FunctionDetail getFunctionDetail();

        Class<? extends Function> getFunctionClass();

        Object process(Function function) throws AtlasException;
    }

    public List<FunctionProcessor> loadFunctions(ClassLoader classLoader) {
        final ServiceLoader<AtlasFunction> fieldFunctionServiceLoader = ServiceLoader.load(AtlasFunction.class,
                classLoader);
        final ServiceLoader<io.atlasmap.api.AtlasFunction> compat = ServiceLoader
                .load(io.atlasmap.api.AtlasFunction.class, classLoader);
        List<FunctionProcessor> answer = new ArrayList<>();
        fieldFunctionServiceLoader.forEach(atlasFunction -> createFunctionProcessor(atlasFunction, answer));
        compat.forEach(atlasFunction -> createFunctionProcessor(atlasFunction, answer));

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Loaded %s functions", answer.size()));
        }
        return answer;
    }

    private void createFunctionProcessor(AtlasFunction atlasFunction, List<FunctionProcessor> answer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading function class: " + atlasFunction.getClass().getCanonicalName());
        }

        Class<?> clazz = atlasFunction.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {

            // Continue supporting creating details from @AtlasFunctionInfo
            FunctionProcessor det = createDetailFromFunctionInfo(clazz, method);
            if (det != null) {
                answer.add(det);
            }

            // Also support using new simpler @AtlasFunctionProcessor
            det = createDetailFromProcessor(clazz, method);
            if (det != null) {
                answer.add(det);
            }
        }
    }

    private FunctionProcessor createDetailFromFunctionInfo(final Class<?> clazz, final Method method) {
        AtlasFunctionInfo annotation = method.getAnnotation(AtlasFunctionInfo.class);
        if (annotation == null) {
            return null;
        }

        final FunctionDetail det = new FunctionDetail();
        det.setClassName(clazz.getName());
        det.setMethod(method.getName());
        det.setName(annotation.name());
        det.setReturnType(annotation.returnType());
        CollectionType resultCollectionType = annotation.returnCollectionType();

        Class<? extends BaseFunction> functionClazz;
        try {
            functionClazz = (Class<? extends BaseFunction>) Class.forName("io.atlasmap.v2." + annotation.name());
        } catch (Exception e) {
            functionClazz = null;
            det.setCustom(true);
        }

        try {
            det.setFunctionSchema(functionClazz);
        } catch (Exception e) {
            LOG.error(String.format("Could not get json schema for function=%s msg=%s", annotation.name(),
                    e.getMessage()), e);
        }

        try {
            // TODO https://github.com/atlasmap/atlasmap/issues/538
            if (det.isCustom() == null || !det.isCustom()) {
                det.setParameters(detectFunctionParameters(functionClazz));
            }
        } catch (ClassNotFoundException e) {
            LOG.error(String.format("Error detecting parameters for function=%s msg=%s", annotation.name(),
                    e.getMessage()), e);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Loaded Function: " + det.getName());
        }

        Class<? extends Function> finalFunctionClazz = functionClazz;
        return new FunctionProcessor() {

            @Override
            public FunctionDetail getFunctionDetail() {
                return det;
            }

            @Override
            public Class<? extends Function> getFunctionClass() {
                return finalFunctionClazz;
            }

            @Override
            public Object process(Function function) throws AtlasException {
                Object result = null;
                try {
                    if (Modifier.isStatic(method.getModifiers())) {
                        // TODO eliminate Function parameter even for OOTB
                        // we can use annotation also for the parameters instead
                        // cf. https://github.com/atlasmap/atlasmap/issues/536
                        if (det.isCustom() != null && det.isCustom()) {
                            result = method.invoke(null);
                        } else {
                            result = method.invoke(null, function);
                        }
                    } else {
                        Object object = clazz.newInstance();
                        if (det.isCustom() != null && det.isCustom()) {
                            result = method.invoke(object);
                        } else {
                            result = method.invoke(object, function);
                        }
                    }
                } catch (Throwable e) {
                    throw new AtlasException(String.format("Error processing function %s", det.getName()), e);
                }
                return result;
            }
        };
    }

    private FunctionProcessor createDetailFromProcessor(Class<?> clazz, Method method) {
        if (method.getParameterCount() < 1) {
            LOG.debug("Invalid function method. Expected at least 1 parameter: " + method);
        }

        Class<? extends BaseFunction> functionClazz = null;
        if (Function.class.isAssignableFrom(method.getParameterTypes()[0])) {
            functionClazz = (Class<? extends BaseFunction>) method.getParameterTypes()[0];
        } else {
            LOG.debug("Invalid function method: First parameter does not subclass " + Function.class.getName() + ": "
                    + method);
        }

        final Class<?> returnTypeClass = method.getReturnType();
        String name = functionResolver.toId(functionClazz);

        FunctionDetail det = new FunctionDetail();
        det.setClassName(clazz.getName());
        det.setMethod(method.getName());
        det.setName(name);
        det.setReturnType(toFieldType(returnTypeClass, method.getGenericReturnType()));
        if (!clazz.getPackage().getName().equals("io.atlasmap.function")) {
            det.setCustom(true);
        }

        try {
            det.setFunctionSchema(functionClazz);
        } catch (Exception e) {
            LOG.error(
                    String.format("Could not get json schema for function=%s msg=%s", clazz.getName(), e.getMessage()),
                    e);
        }

        try {
            det.setParameters(detectFunctionParameters(functionClazz));
        } catch (ClassNotFoundException e) {
            LOG.error(String.format("Error detecting parameters for field function=%s msg=%s", det.getName(),
                    e.getMessage()), e);
        }

        Object o = null;
        try {
            o = Modifier.isStatic(method.getModifiers()) ? clazz.newInstance() : null;
        } catch (Throwable e) {
            LOG.error(String.format("Error creating object instance for function=%s msg=%s", det.getName(),
                    e.getMessage()), e);
        }
        final Object object = o;

        Class<? extends Function> finalFunctionClazz = functionClazz;

        return new FunctionProcessor() {
            @Override
            public FunctionDetail getFunctionDetail() {
                return det;
            }

            @Override
            public Class<? extends Function> getFunctionClass() {
                return finalFunctionClazz;
            }

            @Override
            public Object process(Function function) throws AtlasException {
                try {
                    return method.invoke(object, function);
                } catch (Throwable e) {
                    throw new AtlasException(String.format("Error processing function %s", det.getName()), e);
                }
            }
        };
    }

    private CollectionType toFieldCollectionType(Class<?> clazz) {
        if (clazz.isArray()) {
            return CollectionType.ARRAY;
        }
        if (clazz == Collection.class) {
            return CollectionType.ALL;
        }
        if (listClasses.contains(clazz.getName())) {
            return CollectionType.LIST;
        }
        if (mapClasses.contains(clazz.getName())) {
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
                return DefaultAtlasConversionService.getInstance().fieldTypeFromClass((Class) t);
            case LIST:
                t = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
                return DefaultAtlasConversionService.getInstance().fieldTypeFromClass((Class) t);
            case MAP:
                t = ((ParameterizedType) parameterType).getActualTypeArguments()[1];
                return DefaultAtlasConversionService.getInstance().fieldTypeFromClass((Class) t);
            default:
                return DefaultAtlasConversionService.getInstance().fieldTypeFromClass(clazz);
        }
    }

    private FunctionParameters detectFunctionParameters(Class<?> functionClazz) throws ClassNotFoundException {
        FunctionParameters params = null;
        for (Method method : functionClazz.getMethods()) {
            // Find setters to avoid the get / is confusion
            if (method.getParameterCount() == 1 && method.getName().startsWith("set")) {
                // We have a parameter
                if (params == null) {
                    params = new FunctionParameters();
                }

                FunctionParameter functionParam = null;
                for (Parameter methodParam : method.getParameters()) {
                    functionParam = new FunctionParameter();
                    functionParam.setName(camelize(method.getName().substring("set".length())));
                    // TODO set displayName/description -
                    // https://github.com/atlasmap/atlasmap/issues/96
                    functionParam.setFieldType(getConversionService().fieldTypeFromClass(methodParam.getType()));
                    // TODO fix this dirty hack for https://github.com/atlasmap/atlasmap/issues/386
                    if (methodParam.getType().isEnum()) {
                        functionParam.setFieldType(FieldType.STRING);
                        try {
                            for (Object e : methodParam.getType().getEnumConstants()) {
                                Method m = e.getClass().getDeclaredMethod("value", new Class[0]);
                                functionParam.getValues().add(m.invoke(e, new Object[0]).toString());
                            }
                        } catch (Exception e) {
                            LOG.debug("Failed to populate possible enum parameter values, ignoring...", e);
                        }
                    }
                    params.getParameters().add(functionParam);
                }
            }
        }

        return params;
    }

    @Override
    public List<FunctionDetail> listFunctionDetails() {
        Lock readLock = this.functionProcessorsLock.readLock();
        try {
            readLock.lock();
            return functionProcessors.stream().map(x -> x.getFunctionDetail()).collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }

    /*
     * TODO: getFunctionDetailByFunctionName() when all references are updated to
     * use
     *
     * FunctionDetail = findFunctionDetail(String functionName)
     *
     * ref: https://github.com/atlasmap/atlasmap-runtime/issues/216
     */
    @Deprecated
    protected FunctionDetail getFunctionDetailByFunctionName(String functionName) {
        for (FunctionDetail functionDetail : listFunctionDetails()) {
            if (functionDetail.getName().equals(functionName)) {
                return functionDetail;
            }
        }

        return null;
    }

    /**
     * 1. Find function by name 2. If multiple matches are found, return the first
     * Function 4. If no matches found, return null
     *
     * @param function The name of the function
     * @return FunctionDetail
     */
    @Override
    public FunctionDetail findFunctionDetail(Function function) throws AtlasException {
        FunctionProcessor processor = findFunctionProcessor(function);
        if (processor == null) {
            return null;
        }
        return processor.getFunctionDetail();
    }

    public FunctionProcessor findFunctionProcessor(Function function) throws AtlasException {
        CustomFunction customFunction = null;
        if (function instanceof CustomFunction) {
            customFunction = (CustomFunction) function;
            if (customFunction.getClassName() == null || customFunction.getMethodName() == null) {
                throw new AtlasException("The class name and method name must be specified for custom function: "
                        + customFunction.getName());
            }
        }
        List<FunctionProcessor> matches = new ArrayList<>();
        Lock readLock = functionProcessorsLock.readLock();
        try {
            readLock.lock();
            for (FunctionProcessor processor : functionProcessors) {
                if (customFunction != null) {
                    FunctionDetail detail = processor.getFunctionDetail();
                    if (customFunction.getClassName().equals(detail.getClassName())
                            && customFunction.getMethodName().equals(detail.getMethod())) {
                        matches.add(processor);
                        break;
                    }
                } else if (processor.getFunctionClass() == function.getClass()) {
                    matches.add(processor);
                }
            }
        } finally {
            readLock.unlock();
        }

        return findBestFunctionProcessor(matches);
    }

    public FunctionProcessor findFunctionProcessor(String name) {
        String uppercaseName = name.toUpperCase();

        List<FunctionProcessor> processors = new ArrayList<>();
        Lock readLock = functionProcessorsLock.readLock();
        try {
            readLock.lock();
            for (FunctionProcessor processor : functionProcessors) {
                if (processor.getFunctionDetail().getName().toUpperCase().equals(uppercaseName)) {
                    processors.add(processor);
                }
            }
        } finally {
            readLock.unlock();
        }

        return findBestFunctionProcessor(processors);
    }

    private FunctionProcessor findBestFunctionProcessor(List<FunctionProcessor> processors) {
        if (processors.isEmpty()) {
            return null;
        }
        return processors.get(0);
    }

    public Object buildAndProcessFunction(FunctionProcessor functionProcessor, Map<String, Object> functionParameters,
            List<Field> fields) {
        try {
            Function function = functionProcessor.getFunctionClass().newInstance();
            for (Map.Entry<String, Object> property : functionParameters.entrySet()) {
                String setter = "set" + property.getKey().substring(0, 1).toUpperCase()
                        + property.getKey().substring(1);
                function.getClass().getMethod(setter, property.getValue().getClass()).invoke(function,
                        property.getValue());
            }

            return functionProcessor.process(function);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("The function '%s' cannot be processed",
                    functionProcessor.getFunctionDetail().getName()), e);
        }
    }

    @Override
    public Mapping processFunctions(AtlasInternalSession session, Mapping mapping) throws AtlasException {
        // ArrayList<Function> actions = mapping.getFunctions();
        // FieldType targetType = mapping.getFieldType();

        // if (actions == null || actions == null || actions.isEmpty()) {
        // return mapping;
        // }

        // if (FieldType.COMPLEX.equals(targetType)) {
        // return mapping;
        // }

        // Object sourceObject = mapping.getValue();
        // FieldType sourceType = (sourceObject != null
        // ? getConversionService().fieldTypeFromClass(sourceObject.getClass())
        // : FieldType.NONE);

        // FieldGroup fieldGroup = null;
        // if (mapping instanceof FieldGroup) {
        // fieldGroup = (FieldGroup) mapping;

        // List<Object> values = new ArrayList<>();
        // if (hasExpressionFunction(actions)) {
        // extractNestedListValuesFromFieldGroup(fieldGroup, values); // preserve top
        // level list of parameters and
        // // arguments
        // mapping = findLastIndexField(fieldGroup); // arguments are last in the list
        // } else {
        // extractFlatListValuesFromFieldGroup(session, fieldGroup, values);
        // }

        // sourceObject = values;
        // sourceType = determineFieldType(values);
        // }

        // Object tmpSourceObject = sourceObject;
        // FieldType currentType = sourceType;
        // for (Function action : actions) {
        // FunctionProcessor processor = findFunctionProcessor(action, currentType);
        // FunctionDetail detail = processor.getFunctionDetail();
        // if (detail == null) {
        // AtlasUtil.addAudit(session, mapping.getDocId(),
        // String.format("Couldn't find metadata for function '%s', ignoring...",
        // action.getDisplayName()),
        // mapping.getPath(), AuditStatus.WARN, null);
        // continue;
        // }

        // tmpSourceObject = processFunction(action, processor, currentType,
        // tmpSourceObject);
        // currentType = null;
        // if (tmpSourceObject instanceof List) {
        // for (Object item : ((List<?>) tmpSourceObject)) {
        // if (item != null) {
        // currentType = conversionService.fieldTypeFromClass(item.getClass());
        // break;
        // }
        // }
        // } else if (tmpSourceObject != null) {
        // currentType =
        // conversionService.fieldTypeFromClass(tmpSourceObject.getClass());
        // }
        // }

        // if (fieldGroup != null) {
        // if (tmpSourceObject instanceof List) {
        // // n -> n - reuse passed-in FieldGroup
        // List<?> sourceList = (List<?>) tmpSourceObject;
        // Field lastSubField = null;
        // FieldGroup existingFieldGroup = fieldGroup;
        // if (mapping instanceof FieldGroup) {
        // existingFieldGroup = (FieldGroup) mapping;
        // }

        // for (int i = 0; i < sourceList.size(); i++) {
        // if (existingFieldGroup.getField().size() > i) {
        // lastSubField = existingFieldGroup.getField().get(i);
        // } else {
        // Field subField = new SimpleField();
        // AtlasModelFactory.copyField(lastSubField, subField, true);
        // existingFieldGroup.getField().add(subField);
        // lastSubField = subField;
        // }
        // lastSubField.setValue(sourceList.get(i));
        // lastSubField.setFieldType(currentType);
        // }
        // mapping = existingFieldGroup;
        // } else {
        // // n -> 1 - create new Field
        // Field newField = new SimpleField();
        // AtlasModelFactory.copyField(mapping, newField, false);
        // newField.setValue(tmpSourceObject);
        // newField.setFieldType(currentType);
        // mapping = newField;
        // }
        // } else if (tmpSourceObject instanceof List) {
        // // 1 -> n - create new FieldGroup
        // fieldGroup = AtlasModelFactory.createFieldGroupFrom(mapping, true);
        // for (Object subValue : (List<?>) tmpSourceObject) {
        // Field subField = new SimpleField();
        // AtlasModelFactory.copyField(mapping, subField, false);
        // subField.setValue(subValue);
        // subField.setFieldType(currentType);
        // fieldGroup.getField().add(subField);
        // }
        // mapping = fieldGroup;
        // } else {
        // // 1 -> 1 = reuse passed-in Field
        // mapping.setValue(tmpSourceObject);
        // mapping.setFieldType(currentType);
        // }
        return mapping;
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

    // private void extractFlatListValuesFromFieldGroup(AtlasSession session,
    // FieldGroup fieldGroup, List<Object> values) {
    // if (fieldGroup == null || fieldGroup.getField() == null ||
    // fieldGroup.getField().isEmpty()) {
    // return;
    // }
    // for (Field subField : fieldGroup.getField()) {
    // Integer index = subField.getIndex();
    // Object value = null;
    // if (subField instanceof FieldGroup) {
    // // Collection field
    // List<Field> fields = ((FieldGroup) subField).getField();
    // if (fields != null && !fields.isEmpty()) {
    // if (fieldGroup.getField().size() == 1 && (index == 0 || index == null)) {
    // // For backwards compatibility treat as a collection in a SimpleField
    // for (Field field : fields) {
    // values.add(field.getValue());
    // }
    // break;
    // } else {
    // AtlasUtil.addAudit(session, subField.getDocId(),
    // "Using only the first element of "
    // + "the collection since a single value is expected in a multi-field
    // selection.",
    // subField.getPath(), AuditStatus.WARN, null);
    // value = fields.get(0).getValue(); // get only the first value
    // }
    // }
    // } else {
    // value = subField.getValue();
    // }

    // if (index != null) {
    // while (index >= values.size()) {
    // values.add(null);
    // }
    // values.set(index, value);
    // } else {
    // values.add(value);
    // }
    // }
    // }

    private void extractNestedListValuesFromFieldGroup(FieldGroup fieldGroup, List<Object> values) {
        if (fieldGroup == null || fieldGroup.getField() == null || fieldGroup.getField().isEmpty()) {
            return;
        }
        for (Field subField : fieldGroup.getField()) {
            Object subValue = null;
            if (subField instanceof FieldGroup) {
                List<Object> subValues = new ArrayList<>();
                extractNestedListValuesFromFieldGroup((FieldGroup) subField, subValues);
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

    // private Field findLastIndexField(FieldGroup fieldGroup) {
    // Field lastSubField = fieldGroup.getField().get(0);
    // for (Field subField : fieldGroup.getField()) {
    // int subFieldIndex = (subField.getIndex() == null) ? Integer.MAX_VALUE :
    // subField.getIndex();
    // int lastSubFieldIndex = (lastSubField.getIndex() == null) ? Integer.MAX_VALUE
    // : lastSubField.getIndex();
    // if (lastSubFieldIndex <= subFieldIndex) {
    // lastSubField = subField;
    // }
    // }
    // return lastSubField;
    // }

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
