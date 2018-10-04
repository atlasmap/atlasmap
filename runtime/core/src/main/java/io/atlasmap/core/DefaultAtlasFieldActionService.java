package io.atlasmap.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.converters.DateTimeHelper;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.ActionDetails;
import io.atlasmap.v2.ActionParameter;
import io.atlasmap.v2.ActionParameters;
import io.atlasmap.v2.Actions;
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
    private ActionDetails actionDetails = new ActionDetails();
    private AtlasConversionService conversionService = null;

    public DefaultAtlasFieldActionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void init() {
        listActionDetails().addAll(loadFieldActions());
        // TODO load custom field actions in application bundles
        // on hierarchical class loader environment
    }

    public List<ActionDetail> loadFieldActions() {
        return loadFieldActions(this.getClass().getClassLoader());
    }

    public List<ActionDetail> loadFieldActions(ClassLoader classLoader) {
        final ServiceLoader<AtlasFieldAction> fieldActionServiceLoader = ServiceLoader.load(AtlasFieldAction.class, classLoader);
        List<ActionDetail> answer = new ArrayList<>();
        for (final AtlasFieldAction atlasFieldAction : fieldActionServiceLoader) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loading FieldAction class: " + atlasFieldAction.getClass().getCanonicalName());
            }

            Class<?> clazz = atlasFieldAction.getClass();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                AtlasFieldActionInfo annotation = method.getAnnotation(AtlasFieldActionInfo.class);
                if (annotation == null) {
                    continue;
                }

                ActionDetail det = new ActionDetail();
                det.setClassName(clazz.getName());
                det.setMethod(method.getName());
                det.setName(annotation.name());
                det.setSourceType(annotation.sourceType());
                det.setTargetType(annotation.targetType());
                det.setSourceCollectionType(annotation.sourceCollectionType());
                det.setTargetCollectionType(annotation.targetCollectionType());

                Class<?> actionClazz;
                try {
                    actionClazz = Class.forName("io.atlasmap.v2." + annotation.name());
                } catch (Exception e) {
                    actionClazz = clazz;
                    det.setCustom(true);
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
                answer.add(det);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Loaded %s Field Actions", answer.size()));
        }
        return answer;
    }

    private ActionParameters detectFieldActionParameters(Class<?> actionClazz) throws ClassNotFoundException {
        ActionParameters params = null;
        for(Method method : actionClazz.getMethods()) {
            // Find setters to avoid the get / is confusion
            if(method.getParameterCount() == 1 && method.getName().startsWith("set")) {
                // We have a parameter
                if(params == null) {
                    params = new ActionParameters();
                }

                ActionParameter actionParam = null;
                for(Parameter methodParam : method.getParameters()) {
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
        return actionDetails.getActionDetail();
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
        for(ActionDetail actionDetail : listActionDetails()) {
            if(actionDetail.getName().equals(actionName)) {
                return actionDetail;
            }
        }

        return null;
    }

    /*
     * 1. Find FieldAction by name
     * 2. If multiple matches are found, return the best one based on FieldType sourceType
     * 3. If there is not an exact match to sourceType, return the first FieldAction
     * 4. If no matches found, return null
     *
     *
     * @param actionName The name of the FieldAction
     * @param sourceType A hint used to determine which FieldAction to use
     *                   when multiple FieldActions exist with the same name
     *
     * @return ActionDetail
     */
    protected ActionDetail findActionDetail(Action action, FieldType sourceType) throws AtlasException {
        String actionName = action.getDisplayName();
        CustomAction customAction = null;
        if (action instanceof CustomAction) {
            customAction = (CustomAction)action;
            if (customAction.getClassName() == null || customAction.getMethodName() == null) {
                throw new AtlasException("The class name and method name must be specified for custom FieldAction: " + customAction.getName());
            }
        }
        List<ActionDetail> matches = new ArrayList<>();
        for(ActionDetail actionDetail : listActionDetails()) {
            if (customAction != null) {
                if (customAction.getClassName().equals(actionDetail.getClassName())
                        && customAction.getMethodName().equals(actionDetail.getMethod())) {
                    matches.add(actionDetail);
                    break;
                }
                actionDetail.getClassName();
            } else if(actionDetail.getName().equals(actionName)) {
                matches.add(actionDetail);
            }
        }

        switch(matches.size()) {
        case 0: return null;
        case 1: return matches.get(0);
        default:
            if(sourceType != null && !Arrays.asList(FieldType.ANY, FieldType.NONE).contains(sourceType)) {
                for(ActionDetail actionDetail : matches) {
                    if(sourceType.equals(actionDetail.getSourceType())) {
                        return actionDetail;
                    }
                }
            }
            return matches.get(0);
        }
    }

    @Override
    public Field processActions(AtlasInternalSession session, Field field) throws AtlasException {
        Actions actions = field.getActions();
        FieldType targetType = field.getFieldType();

        if(actions == null || actions.getActions() == null || actions.getActions().isEmpty()) {
            return field;
        }

        if(FieldType.COMPLEX.equals(targetType)) {
            return field;
        }

        Object sourceObject = field.getValue();
        FieldType sourceType = (sourceObject != null ? getConversionService().fieldTypeFromClass(sourceObject.getClass()) : FieldType.NONE);
        FieldGroup fieldGroup = null;
        if (field instanceof FieldGroup) {
            fieldGroup = (FieldGroup)field;
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
        for(Action action : actions.getActions()) {
            ActionDetail detail = findActionDetail(action, currentType);
            if (detail == null) {
                AtlasUtil.addAudit(session, field.getDocId(), String.format(
                        "Couldn't find metadata for a FieldAction '%s', ignoring...", action.getDisplayName()),
                        field.getPath(), AuditStatus.WARN, null);
                continue;
            }

            CollectionType sourceCollectionType = detail.getSourceCollectionType() != null ? detail.getSourceCollectionType() : CollectionType.NONE;
            if (tmpSourceObject instanceof List) {
                List<Object> tmpSourceList = (List<Object>)tmpSourceObject;
                for (int i=0; i<tmpSourceList.size(); i++) {
                    Object subValue = tmpSourceList.get(i);
                    FieldType subType = (subValue != null ? getConversionService().fieldTypeFromClass(subValue.getClass()) : FieldType.NONE);
                    if(subValue != null && !isAssignableFieldType(detail.getSourceType(), subType)) {
                        subValue = getConversionService().convertType(subValue, subType, detail.getSourceType());
                        tmpSourceList.set(i, subValue);
                    }
                    if (sourceCollectionType == CollectionType.NONE) {
                        subValue = processAction(action, detail, subValue);
                        tmpSourceList.set(i, subValue);
                    }
                }
            } else if(!isAssignableFieldType(detail.getSourceType(), currentType)) {
                tmpSourceObject = getConversionService().convertType(sourceObject, currentType, detail.getSourceType());
            }
            if (!(tmpSourceObject instanceof List) || sourceCollectionType != CollectionType.NONE) {
                tmpSourceObject = processAction(action, detail, tmpSourceObject);
            }

            currentType = detail.getTargetType();
            if (tmpSourceObject != null && tmpSourceObject.getClass().isArray()) {
                tmpSourceObject = Arrays.asList((Object[])tmpSourceObject);
            } else if ((tmpSourceObject instanceof java.util.Collection) && !(tmpSourceObject instanceof List)) {
                tmpSourceObject = Arrays.asList(((java.util.Collection<?>)tmpSourceObject).toArray());
            }
        }

        if (fieldGroup != null) {
            if (tmpSourceObject instanceof List) {
                // n -> n - reuse passed-in FieldGroup
                List<?> sourceList = (List<?>)tmpSourceObject;
                for (int i=0; i<sourceList.size(); i++) {
                    if (fieldGroup.getField().size() > i) {
                        Field subField = fieldGroup.getField().get(i);
                        subField.setValue(sourceList.get(i));
                        subField.setFieldType(currentType);
                    } else {
                        AtlasUtil.addAudit(session, fieldGroup.getDocId(),
                                "FieldAction created more values than expected, ignoring",
                                fieldGroup.getPath(), AuditStatus.WARN, sourceList.get(i).toString());
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
            for (Object subValue : (List<?>)tmpSourceObject) {
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

    protected Object processAction(Action action, ActionDetail actionDetail, Object sourceObject) throws AtlasException {
        Object targetObject = null;
        if(actionDetail != null) {
            Object actionObject = null;
            try {
                Class<?> actionClazz = Class.forName(actionDetail.getClassName());
                actionObject = actionClazz.newInstance();

                Method method =  null;
                if (actionDetail.getSourceType() != null) {
                    List<Class<?>> paramTypes = new LinkedList<>();
                    if (actionDetail.isCustom() == null || !actionDetail.isCustom()) {
                        // TODO eliminate this Action parameter even for OOTB
                        // we can use annotation also for the parameters instead
                        // cf. https://github.com/atlasmap/atlasmap/issues/536
                        paramTypes.add(Action.class);
                    }
                    CollectionType sourceCollectionType = actionDetail.getSourceCollectionType();
                    if (sourceCollectionType != null && sourceCollectionType != CollectionType.NONE) {
                        // TODO allow array/collection as a parameter type
                        paramTypes.add(Object.class);
                        method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                    } else {
                        switch(actionDetail.getSourceType()) {
                        case ANY:
                            paramTypes.add(Object.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case BIG_INTEGER:
                            paramTypes.add(BigInteger.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case BOOLEAN:
                            paramTypes.add(Boolean.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case BYTE:
                            paramTypes.add(Byte.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case BYTE_ARRAY:
                            paramTypes.add(Byte[].class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case CHAR:
                            paramTypes.add(Character.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case DATE:
                        case DATE_TIME:
                        case DATE_TZ:
                        case TIME_TZ:
                        case DATE_TIME_TZ:
                        case ANY_DATE:
                            if (sourceObject instanceof Calendar) {
                                sourceObject = DateTimeHelper.toZonedDateTime((Calendar)sourceObject);
                            } else if (sourceObject instanceof Date) {
                                sourceObject = DateTimeHelper.toZonedDateTime((Date)sourceObject, null);
                            } else if (sourceObject instanceof LocalDate) {
                                sourceObject = DateTimeHelper.toZonedDateTime((LocalDate)sourceObject, null);
                            } else if (sourceObject instanceof LocalTime) {
                                sourceObject = DateTimeHelper.toZonedDateTime((LocalTime)sourceObject, null);
                            } else if (sourceObject instanceof LocalDateTime) {
                                sourceObject = DateTimeHelper.toZonedDateTime((LocalDateTime)sourceObject, null);
                            } else if (!(sourceObject instanceof ZonedDateTime)) {
                                LOG.warn(String.format("Unsupported sourceObject type=%s in actionClass=%s", sourceObject.getClass(), actionDetail.getClassName()));
                                break;
                            }
                            paramTypes.add(ZonedDateTime.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case DECIMAL:
                            paramTypes.add(BigDecimal.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case DOUBLE:
                            paramTypes.add(Double.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case FLOAT:
                            paramTypes.add(Float.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case INTEGER:
                            paramTypes.add(Integer.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case LONG:
                            paramTypes.add(Long.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case NUMBER:
                            paramTypes.add(Number.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case SHORT:
                            paramTypes.add(Short.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        case STRING:
                            paramTypes.add(String.class);
                            method = actionClazz.getMethod(actionDetail.getMethod(), paramTypes.toArray(new Class[0]));
                            break;
                        default:
                            LOG.warn(String.format("Unsupported sourceType=%s in actionClass=%s", actionDetail.getSourceType().value(), actionDetail.getClassName()));
                            break;
                        }
                    }
                }

                if(method == null) {
                    throw new AtlasException(String.format("Unable to locate field action className=%s method=%s sourceType=%s", actionDetail.getClassName(), actionDetail.getMethod(), actionDetail.getSourceType()));
                }

                if(Modifier.isStatic(method.getModifiers())) {
                    // TODO eliminate Action parameter even for OOTB
                    // we can use annotation also for the parameters instead
                    // cf. https://github.com/atlasmap/atlasmap/issues/536
                    if (actionDetail.isCustom() != null && actionDetail.isCustom()) {
                        targetObject = method.invoke(null, sourceObject);
                    } else {
                        targetObject = method.invoke(null, action, sourceObject);
                    }
                } else {
                    if (actionDetail.isCustom() != null && actionDetail.isCustom()) {
                        targetObject = method.invoke(actionObject, sourceObject);
                    } else {
                        targetObject = method.invoke(actionObject, action, sourceObject);
                    }
                }
            } catch (Throwable e) {
                throw new AtlasException(String.format("Error processing action %s", actionDetail.getName()), e);
            }
            return targetObject;
        }
        return sourceObject;
    }

    public AtlasConversionService getConversionService() {
        return this.conversionService;
    }

    public static String camelize(String parameter) {
        if (parameter == null || parameter.length() == 0) {
            return parameter;
        }
        char c[] = parameter.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
