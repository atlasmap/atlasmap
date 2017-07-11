package io.atlasmap.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.ActionDetails;
import io.atlasmap.v2.Actions;
import io.atlasmap.v2.Properties;
import io.atlasmap.v2.Property;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;

public class DefaultAtlasFieldActionService implements AtlasFieldActionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAtlasFieldActionService.class);
    private ActionDetails actionDetails = new ActionDetails();
    private AtlasConversionService conversionService = null;
    
    public DefaultAtlasFieldActionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    public void init() {
        loadFieldActions();
    }

    protected void loadFieldActions() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        final ServiceLoader<AtlasFieldAction> fieldActionServiceLoader = ServiceLoader.load(AtlasFieldAction.class, classLoader);
        for (final AtlasFieldAction atlasFieldAction : fieldActionServiceLoader) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading FieldAction class: " + atlasFieldAction.getClass().getCanonicalName());
            }
            
            Class<?> clazz = atlasFieldAction.getClass();             
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                AtlasFieldActionInfo annotation = method.getAnnotation(AtlasFieldActionInfo.class);
                if (annotation != null) {
                    ActionDetail det = new ActionDetail();
                    det.setClassName(clazz.getName());
                    det.setMethod(method.getName());
                    det.setName(annotation.name());
                    det.setSourceType(annotation.sourceType());
                    det.setTargetType(annotation.targetType());
                    det.setSourceCollectionType(annotation.sourceCollectionType());
                    det.setTargetCollectionType(annotation.targetCollectionType());

                    try {
                        det.setParameters(detectFieldActionParameters("io.atlasmap.v2." + annotation.name()));
                    } catch (ClassNotFoundException e) {
                        logger.error(String.format("Error detecting parameters for field action=%s msg=%s", annotation.name(), e.getMessage()), e);
                    }
                    
                    if (logger.isTraceEnabled()) {
                        logger.trace("Loaded FieldAction: " + det.getName());
                    }
                    listActionDetails().add(det);
                }
            }
        }
    
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Loaded %s Field Actions", listActionDetails().size()));
        }
    }

    public List<ActionDetail> listActionDetails() {
        return actionDetails.getActionDetail();
    }
    
    protected ActionDetail getActionDetailByActionName(String actionName) {
        for(ActionDetail actionDetail : listActionDetails()) {
            if(actionDetail.getName().equals(actionName)) {
                return actionDetail;
            }
        }
        
        return null;
    }

    @Override // TODO: Wire in auto-conversion service
    public Object processActions(Actions actions, Object sourceObject) throws AtlasException {
        if(actions == null || actions.getActions() == null || actions.getActions().isEmpty()) {
            return sourceObject;
        }
              
        Object targetObject = null;

        for(Action action : actions.getActions()) {
            targetObject = processAction(action, getActionDetailByActionName(action.getClass().getSimpleName()), sourceObject);
        }
        
        return targetObject;
    }
    
    protected Object processAction(Action action, ActionDetail actionDetail, Object sourceObject) throws AtlasException {
        Object targetObject = null;
        if(actionDetail != null) {
            Object actionObject = null;
            try {
                Class<?> actionClazz = Class.forName(actionDetail.getClassName());
                actionObject = actionClazz.newInstance();
                
                Method method =  null;
                if(actionDetail.getSourceType() != null) {
                    switch(actionDetail.getSourceType()) {
                    case BOOLEAN: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Boolean.class); break;
                    case BYTE: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Byte.class); break;
                    case BYTE_ARRAY: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Byte[].class); break;
                    case CHAR: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Character.class); break;
                    case DOUBLE: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Double.class); break;
                    case FLOAT: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Float.class); break;
                    case INTEGER: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Integer.class); break;
                    case LONG: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Long.class); break;
                    case SHORT: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, Short.class); break;
                    case STRING: method = actionClazz.getMethod(actionDetail.getMethod(), Action.class, String.class); break;
                    default: 
                        logger.warn(String.format("Unsupported sourceType=%s in actionClass=%s", actionDetail.getSourceType().value(), actionDetail.getClassName()));
                        break;
                    }
                }
                
                if(method == null) {
                    throw new AtlasException(String.format("Unable to locate field action className=%s method=%s sourceType=%s", actionDetail.getClassName(), actionDetail.getMethod(), actionDetail.getSourceType().value()));
                }
               
                if(Modifier.isStatic(method.getModifiers())) {
                    targetObject = method.invoke(null, action, sourceObject);
                } else {
                    targetObject = method.invoke(actionObject, action, sourceObject);
                }
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException e) {
                throw new AtlasException(String.format("Error processing action %s", actionDetail.getName()), e);
            }
            return targetObject;
        }
        return sourceObject;
    }
    
    protected Properties detectFieldActionParameters(String actionClassName) throws ClassNotFoundException {
        Class<?> actionClazz = Class.forName(actionClassName);
        
        Properties props = null;
        for(Method method : actionClazz.getMethods()) {
            // Find setters to avoid the get / is confusion
            if(method.getParameterCount() == 1 && method.getName().startsWith("set")) {
                // We have a parameter
                if(props == null) {
                    props = new Properties();
                }
                
                Property prop = null;
                for(Parameter param : method.getParameters()) {
                    prop = new Property();
                    prop.setName(camelize(method.getName().substring("set".length())));
                    prop.setFieldType(getConversionService().fieldTypeFromClass(param.getType()));
                    props.getProperty().add(prop);
                }
            }
        }
        
        return props;
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
