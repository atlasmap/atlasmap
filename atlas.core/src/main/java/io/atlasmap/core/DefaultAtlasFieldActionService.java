package io.atlasmap.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.ActionDetails;
import io.atlasmap.v2.Actions;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;

public class DefaultAtlasFieldActionService implements AtlasFieldActionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAtlasFieldActionService.class);
    private ActionDetails actionDetails = new ActionDetails();

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
            targetObject = processAction(getActionDetailByActionName(action.getClass().getSimpleName()), sourceObject);
        }
        
        return targetObject;
    }
    
    protected Object processAction(ActionDetail actionDetail, Object sourceObject) throws AtlasException {
        Object targetObject = null;
        if(actionDetail != null) {
            Object actionObject = null;
            try {
                Class<?> actionClazz = Class.forName(actionDetail.getClassName());
                actionObject = actionClazz.newInstance();
                
                Method method =  null;
                if(actionDetail.getSourceType() != null) {
                    switch(actionDetail.getSourceType()) {
                    case BOOLEAN: method = actionClazz.getMethod(actionDetail.getMethod(), Boolean.class); break;
                    case BYTE: method = actionClazz.getMethod(actionDetail.getMethod(), Byte.class); break;
                    case BYTE_ARRAY: method = actionClazz.getMethod(actionDetail.getMethod(), Byte[].class); break;
                    case CHAR: method = actionClazz.getMethod(actionDetail.getMethod(), Character.class); break;
                    case DOUBLE: method = actionClazz.getMethod(actionDetail.getMethod(), Double.class); break;
                    case FLOAT: method = actionClazz.getMethod(actionDetail.getMethod(), Float.class); break;
                    case INTEGER: method = actionClazz.getMethod(actionDetail.getMethod(), Integer.class); break;
                    case LONG: method = actionClazz.getMethod(actionDetail.getMethod(), Long.class); break;
                    case SHORT: method = actionClazz.getMethod(actionDetail.getMethod(), Short.class); break;
                    case STRING: method = actionClazz.getMethod(actionDetail.getMethod(), String.class); break;
                    default: break;
                    }
                }
                
                if(method == null) {
                    throw new AtlasException(String.format("Unable to locate field action className=%s method=%s sourceType=%s", actionDetail.getClassName(), actionDetail.getMethod(), actionDetail.getSourceType().value()));
                }
               
                if(Modifier.isStatic(method.getModifiers())) {
                    targetObject = method.invoke(null, sourceObject);
                } else {
                    targetObject = method.invoke(actionObject, sourceObject);
                }
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException e) {
                throw new AtlasException(String.format("Error processing action %s", actionDetail.getName()), e);
            }
            return targetObject;
        }
        return sourceObject;
    }
}
