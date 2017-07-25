/**
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
package io.atlasmap.java.module;

import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasModuleSupport;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.inspect.ClassHelper;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.inspect.ConstructException;
import io.atlasmap.java.inspect.JavaConstructService;
import io.atlasmap.java.inspect.JdkPackages;
import io.atlasmap.java.inspect.StringUtil;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaCollection;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.java.inspect.JavaPath;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.Validation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AtlasModuleDetail(name = "JavaModule", uri = "atlas:java", modes = { "SOURCE", "TARGET" }, dataFormats = { "java" }, configPackages = { "io.atlasmap.java.v2" })
public class JavaModule extends BaseAtlasModule {
    private static final Logger logger = LoggerFactory.getLogger(JavaModule.class);
    private ClassInspectionService javaInspectionService = null;
    private JavaConstructService javaConstructService = null;

    private AtlasConversionService atlasConversionService = null;
    private AtlasModuleMode atlasModuleMode = null;
    
    public static final String DEFAULT_LIST_CLASS = "java.util.ArrayList";
    
    @Override
    public void init() {
        javaInspectionService = new ClassInspectionService();
        javaInspectionService.setConversionService(getConversionService());
        setJavaInspectionService(javaInspectionService);
        
        javaConstructService = new JavaConstructService();
        javaConstructService.setConversionService(getConversionService());
        setJavaConstructService(javaConstructService);
    }

    @Override
    public void destroy() {
        javaInspectionService = null;
        javaConstructService = null;
    }

    // TODO: Support runtime class inspection
    @Override
    public void processPreInputExecution(AtlasSession atlasSession) throws AtlasException {

        if(atlasSession == null || atlasSession.getMapping() == null 
                || atlasSession.getMapping().getMappings() == null 
                || atlasSession.getMapping().getMappings().getMapping() == null) {
            logger.error("AtlasSession not properly intialized with a mapping that contains field mappings");
            return;
        }
        
        if(javaInspectionService == null) {
            javaInspectionService = new ClassInspectionService();
            javaInspectionService.setConversionService(getConversionService());
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("processPreInputExcution completed");
        }
    }
    
    @Override
    public void processPreOutputExecution(AtlasSession atlasSession) throws AtlasException {
              
        if(atlasSession == null || atlasSession.getMapping() == null 
                || atlasSession.getMapping().getMappings() == null 
                || atlasSession.getMapping().getMappings().getMapping() == null) {
            logger.error("AtlasSession not properly intialized with a mapping that contains field mappings");
            return;
        }
        
        if(javaInspectionService == null) {
            javaInspectionService = new ClassInspectionService();
            javaInspectionService.setConversionService(getConversionService());
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("processPreOutputExcution completed");
        }
    }

    @Override
    public void processPreValidation(AtlasSession atlasSession) throws AtlasException {
        
        if(atlasSession == null || atlasSession.getMapping() == null) {
            logger.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }
        
        JavaValidationService javaValidator = new JavaValidationService(getConversionService());
        List<Validation> javaValidations = javaValidator.validateMapping(atlasSession.getMapping());
        atlasSession.getValidations().getValidation().addAll(javaValidations);
        
        if(logger.isDebugEnabled()) {
            logger.debug("Detected " + javaValidations.size() + " java validation notices");
        }
               
        if(logger.isDebugEnabled()) {
            logger.debug("processPreValidation completed");
        }
    }
    
    @Override
    public void processInputMapping(AtlasSession session, Mapping mapping) throws AtlasException {
        if(mapping.getInputField() == null || mapping.getInputField().isEmpty() || mapping.getInputField().size() != 1) {
            Audit audit = new Audit();
            audit.setStatus(AuditStatus.WARN);
            audit.setMessage(String.format("Mapping does not contain exactly one input field alias=%s desc=%s", mapping.getAlias(), mapping.getDescription()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        Field field = mapping.getInputField().get(0);
        
        if(!isSupportedField(field)) {
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unsupported input field type=%s", field.getClass().getName()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        if(field instanceof PropertyField) {
            processPropertyField(session, mapping, session.getAtlasContext().getContextFactory().getPropertyStrategy());
            if(logger.isDebugEnabled()) {
                logger.debug("Processed input propertyField sPath=" + field.getPath() + " sV=" + field.getValue() + " sT=" + field.getFieldType() + " docId: " + field.getDocId());
            }
            return;
        }
        
        Object sourceObject = null;
        if(field.getDocId() != null) {
            sourceObject = session.getInput(field.getDocId());
        } else {
            sourceObject = session.getInput();
        }
        
        try {
            processMapping((JavaField)field, sourceObject, session);
            
            if(logger.isDebugEnabled()) {
                logger.debug("Processed input field sPath=" + field.getPath() + " sV=" + field.getValue() + " sT=" + field.getFieldType() + " docId: " + field.getDocId());
            }
        } catch (Exception e) {
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unexpected error occured msg=%s", e.getMessage()));
            session.getAudits().getAudit().add(audit);
            logger.error("Unexpected error occured msg=" + e.getMessage(), e);
            return;
        }
    }
    
    @Override
    public void processInputCollection(AtlasSession session, Collection collection) throws AtlasException {
        
        if(collection == null || collection.getMappings() == null || collection.getMappings().getMapping() == null || collection.getMappings().getMapping().size() < 1) {
            if(logger.isDebugEnabled()) {
                logger.debug("Empty collection mapping detected");
            }
            return;
        }
        
        logger.debug("Processing input for collection mapping items: " + collection.getMappings().getMapping().size());
        
        Object sourceObject = session.getInput();
        Object collectionObject = null;
        
        boolean firstFound = false;
        for(BaseMapping cFieldMapping : collection.getMappings().getMapping()) {            
            if(MappingType.MAP.equals(cFieldMapping.getMappingType())) {
                firstFound = true;
                Field cMapSourceField = ((Mapping)cFieldMapping).getInputField().get(0);
                if(!cMapSourceField.getClass().isAssignableFrom(JavaField.class)) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("non-JavaField detected within collection p="+cMapSourceField.getPath());
                    }
                    continue;
                }
                if(collectionObject == null) {
                    try {
                        JavaPath javaPath = new JavaPath(cMapSourceField.getPath());
                        collectionObject = ClassHelper.parentObjectForPath(sourceObject, javaPath); 

                        switch(collection.getCollectionType()) {
                        case LIST: processInputCollectionList(collection, (List<?>)collectionObject, session); break;
                        case ARRAY: throw new AtlasUnsupportedException("Arrays are not currently supported for field p=" + cMapSourceField.getPath());
                        default: throw new AtlasUnsupportedException("Unsupported collectionType=" + collection.getMappingType() + " for field p=" + cMapSourceField.getPath());
                        }
                    } catch (Exception e) {
                        logger.error("Error processing collection: " + e.getMessage(), e);
                        Audit audit = new Audit();
                        audit.setDocId(cMapSourceField.getDocId());
                        audit.setPath(cMapSourceField.getPath());
                        audit.setStatus(AuditStatus.ERROR);
                        audit.setMessage(String.format("Unexpected error occured msg=%s", e.getMessage()));
                        session.getAudits().getAudit().add(audit);
                        return;
                    }
                }
            } else {
                if(logger.isDebugEnabled()) {
                    logger.debug("Detected unsupported Collection member mapping type=" + cFieldMapping.getMappingType().value());
                } 
            }
            
            if(firstFound) {
                break;
            }
        }
    }
    
    protected void processInputCollectionList(Collection mapping, List<?> collectionObject, AtlasSession session) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        int i=0;
        Mappings collectionInstanceMappings = new Mappings();
        for(Object collectionItem : collectionObject) {
            logger.info("Handle to object: " + collectionItem.getClass().getCanonicalName());
            for(BaseMapping bm : mapping.getMappings().getMapping()) {
                if(MappingType.MAP.equals(bm.getMappingType())) {
                    Mapping m = (Mapping)bm;
                    Field in = m.getInputField().get(0);
                    if(in instanceof JavaField) {
                        JavaPath inPath = new JavaPath(in.getPath());
                        inPath.setCollectionIndex(inPath.getCollectionSegment(), i);

                        Mapping collectionInstanceMapping = AtlasModelFactory.cloneMapping(m);
                        collectionInstanceMappings.getMapping().add(collectionInstanceMapping);

                        JavaField cloneField = AtlasJavaModelFactory.cloneJavaField((JavaField)in);
                        cloneField.setPath(inPath.toString());
                        populateSourceFieldValue(cloneField, collectionItem);
                        collectionInstanceMapping.getInputField().add(cloneField);
                        collectionInstanceMapping.getOutputField().addAll(m.getOutputField());

                        logger.info("Processing input collection member p=" + cloneField.getPath() + " v=" + cloneField.getValue());
                    }
                }
            }
            i++;
        }
        
        mapping.getMappings().getMapping().addAll(collectionInstanceMappings.getMapping());
    }
    
    protected void processMapping(JavaField sourceField, Object source, AtlasSession session) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method getter = null;
        if((sourceField).getFieldType() == null) {
            getter = resolveGetMethod(source, (JavaField)sourceField, false);
            if(getter == null) {
                logger.warn("Unable to auto-detect sourceField type p=" + ((JavaField)sourceField).getPath() + " d=" + ((JavaField)sourceField).getDocId());
                return;
            }
            Class<?> returnType = getter.getReturnType();
            sourceField.setFieldType(getConversionService().fieldTypeFromClass(returnType));
            if(logger.isTraceEnabled()) {
                logger.trace("Auto-detected sourceField type p=" + ((JavaField)sourceField).getPath() + " t=" + ((JavaField)sourceField).getFieldType());
            }
        }
            
        if(getter != null) {
            populateSourceFieldValue((JavaField) sourceField, source, getter);
        } else {
            populateSourceFieldValue((JavaField) sourceField, source);
        }
    }

    protected void populateSourceFieldValue(JavaField javaField, Object source) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        populateSourceFieldValue(javaField, source, null);
    }
    
    protected void populateSourceFieldValue(JavaField javaField, Object source, Method getter) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Object sourceValue = null;
        JavaPath javaPath = new JavaPath(javaField.getPath());
        
        if(javaPath.hasCollection()) {
            JavaPath collectionItemPath = javaPath.deCollectionify(javaPath.getCollectionSegment());
            Object parentObject = ClassHelper.parentObjectForPath(source, collectionItemPath);
            if(getter == null) {
                getter = resolveGetMethod(parentObject, javaField, true);
            }
            
            if(getter != null) {
                sourceValue = getter.invoke(parentObject);
            } else {
                // we are ourselves
                sourceValue = source;
            }
        } else if (javaPath.hasParent()) {
            Object parentObject = ClassHelper.parentObjectForPath(source, javaPath);
            if(getter == null) {
                getter = resolveGetMethod(parentObject, javaField, true);
            }
            sourceValue = getter.invoke(parentObject);
        } else {
            if(getter == null) {
                getter = resolveGetMethod(source, javaField, false);
            }
            sourceValue = getter.invoke(source);
        }
        
        // TODO: support doing parent stuff at field level vs getter
        if(sourceValue == null) {
            try {
                java.lang.reflect.Field field = source.getClass().getField(javaPath.getLastSegment());
                field.setAccessible(true);
                sourceValue = field.get(source);
            } catch (NoSuchFieldException nsfe) {
                // TODO: Add audit entry
            }
        }
        
        if(sourceValue == null) {
            // TODO: Add audit entry
            javaField.setValue(null);
        } else if(getConversionService().isPrimitive(sourceValue.getClass()) || getConversionService().isBoxedPrimitive(sourceValue.getClass())) {
            javaField.setValue(getConversionService().copyPrimitive(sourceValue));
        } else {
            javaField.setValue(sourceValue);
        }
    }

    protected Object initializeTargetObject(AtlasMapping atlasMapping) throws ClassNotFoundException, IllegalAccessException, InstantiationException, ConstructException {
        
        String targetUri = null;
        for(DataSource ds : atlasMapping.getDataSource()) {
            if(DataSourceType.TARGET.equals(ds.getDataSourceType())) {
                targetUri = ds.getUri();
            }
        }
        
        String targetClassName = AtlasUtil.getUriParameterValue(targetUri, "className");
        return instantiateJavaObject(targetClassName, atlasMapping.getMappings().getMapping());
    }
    
    protected Object instantiateJavaObject(String targetClassName, List<BaseMapping> mappings) throws ClassNotFoundException, IllegalAccessException, InstantiationException, ConstructException {
        JavaClass inspectClass = getJavaInspectionService().inspectClass(targetClassName);
        merge(inspectClass, mappings);
        List<String> targetPaths = AtlasModuleSupport.listTargetPaths(mappings);
        return getJavaConstructService().constructClass(inspectClass, targetPaths);
    }
    
    protected void populateTargetObjectValue(Object targetObject, JavaField targetField, Object targetValue) throws Exception {
        
        JavaPath javaPath = new JavaPath(targetField.getPath());
        Method targetMethod = resolveSetMethod(targetObject, targetField, targetValue.getClass());
        
        Object parentObject = targetObject;
        if (javaPath.hasParent()) {
            parentObject = ClassHelper.parentObjectForPath(parentObject, javaPath);
        }
        
        if(targetMethod != null) {
            targetMethod.invoke(parentObject, targetValue);
        } else {
            try {
                java.lang.reflect.Field field = parentObject.getClass().getField(javaPath.getLastSegment());
                field.setAccessible(true);
                targetField.setValue(field.get(parentObject));
            } catch (NoSuchFieldException nsfe) {
                // 
            }
        }
    }

    @Override
    public void processOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException {

        if(mapping.getOutputField() == null || mapping.getOutputField().isEmpty() || mapping.getOutputField().size() != 1) {
            Audit audit = new Audit();
            audit.setStatus(AuditStatus.WARN);
            audit.setMessage(String.format("Mapping does not contain exactly one output field alias=%s desc=%s", mapping.getAlias(), mapping.getDescription()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        Field outputField = mapping.getOutputField().get(0);
        
        if(!(outputField instanceof JavaField)) {
            Audit audit = new Audit();
            audit.setDocId(outputField.getDocId());
            audit.setPath(outputField.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unsupported output field type=%s", outputField.getClass().getName()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        JavaField outputJavaField = (JavaField)outputField;
        
        try {
            Object targetObject = null;
            if(session.getOutput() == null) {
                targetObject = initializeTargetObject(session.getMapping());
                session.setOutput(targetObject);
            } else {
                targetObject = session.getOutput();
            }
            
            internalProcessOutputMapping(targetObject, mapping.getInputField().get(0), outputJavaField);
           
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AtlasException(e.getMessage(), e.getCause());
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("processOutputMapping completed");
        }
    }
    
    protected void internalProcessOutputMapping(Object parentObject, Field inputField, JavaField outputField) throws Exception {

        FieldType inputType = inputField.getFieldType();
        Object inputValue = inputField.getValue();
        
        Object outputValue = null;
        FieldType outputType = outputField.getFieldType();;
        
        if(logger.isDebugEnabled()) {
            logger.debug("processOutputMapping iPath=" + inputField.getPath() + " iV=" + inputValue + " iT=" + inputType + " oPath=" + outputField.getPath() + " docId: " + outputField.getDocId());
        }
        
        if(inputValue == null) {
            // TODO: Finish targetValue = null processing
            logger.warn("Null sourceValue for field: " + outputField.getPath() + " docId: " + outputField.getDocId());
            return;
        }
        
        if(outputType == null) {
            try {
                Method setter = resolveSetMethod(parentObject, outputField, null);
                if(setter != null && setter.getParameterCount() == 1) {
                    outputType = getConversionService().fieldTypeFromClass(setter.getParameterTypes()[0]);
                    outputField.setFieldType(outputType);
                    if(logger.isTraceEnabled()) {
                        logger.trace("Auto-detected targetType as {} for class={} path={}", outputType, parentObject.toString(), outputField.getPath());
                    }
                }
            } catch (NoSuchMethodException e) {
                logger.debug("Unable to auto-detect targetType for class={} path={}", parentObject.toString(), outputField.getPath());
            }
        }
             
        if(inputType != null && inputType.equals(outputType)) {
            outputValue = inputValue;
        } else {
            try {
                outputValue = getConversionService().convertType(inputValue, inputType, outputType);
            } catch (AtlasConversionException e) {
                logger.error(String.format("Unable to auto-convert for sT=%s tT=%s tF=%s msg=%s", inputType, outputType, outputField.getPath(), e.getMessage()), e);
                return;
            }
        }
                
        if(FieldType.COMPLEX.equals(outputType)) {
            logger.debug("Skipping complex outputType iT={} tT={} tF={}", inputType, outputType, outputField.getPath());
        } else {
            logger.debug("Populating primitive type sT={} tT={} tF={}", inputType, outputType, outputField.getPath());
            populateTargetObjectValue(parentObject, outputField, outputValue);
        }
    }
    
    @Override
    public void processOutputCollection(AtlasSession session, Collection collection) throws AtlasException {
        
        if(collection == null || collection.getMappings() == null || collection.getMappings().getMapping() == null || collection.getMappings().getMapping().isEmpty()) {
            if(logger.isDebugEnabled()) {
                logger.debug("Empty output collection mapping detected");
            }
            Audit audit = new Audit();
            audit.setStatus(AuditStatus.WARN);
            audit.setMessage(String.format("Collection does not contain any mapping entries alias=%s, desc=%s", collection.getAlias(), collection.getDescription()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        logger.debug("Processing output for collection mapping items: " + collection.getMappings().getMapping().size());
        
        JavaCollection javaCollection = null;
        if(collection instanceof JavaCollection) {
            javaCollection = (JavaCollection)collection;
            // do javaCollection.getCollectionClassName() stuff
        }
        
        Object targetObject = null;
        Object targetCollectionObject = null;

        if(session.getOutput() == null) {
            try {
                targetObject = initializeTargetObject(session.getMapping());
            } catch (Exception e) {
                logger.error(String.format("Error initializing targetObject msg=%s", e.getMessage()), e);
                Audit audit = new Audit();
                audit.setStatus(AuditStatus.ERROR);
                audit.setMessage(String.format("Error initializing targetObject msg=%s", e.getMessage()));
                session.getAudits().getAudit().add(audit);
                return;
            }
            session.setOutput(targetObject);
        } else {
            targetObject = session.getOutput();
        }
            
        Class<?> targetClazz = targetObject.getClass();
        String collectionMemberClassName = null;
        List<BaseMapping> collectionItems = new ArrayList<BaseMapping>();    
        for(BaseMapping baseMapping : javaCollection.getMappings().getMapping()) {
            switch(baseMapping.getMappingType()) {
            case MAP: 
                Mapping mapping = (Mapping)baseMapping;
                JavaField outputField = null;
                JavaPath outputFieldPath = null;
                
                Field inputField = mapping.getInputField().get(0);
                outputField = (JavaField)mapping.getOutputField().get(0);
                outputFieldPath = new JavaPath(outputField.getPath());
                
                // Setup initial collection object
                try {
//                    targetCollectionObject = ClassHelper.parentObjectForPath(targetObject, outputFieldPath);
//                    if(targetCollectionObject == null) {
//                        String collectionClassName = javaCollection.getCollectionClassName();
//                        if(collectionClassName == null) { 
//                            collectionClassName = DEFAULT_LIST_CLASS;
//                        }
//                        Class<?> collectionClass = Class.forName(collectionClassName);
//                        targetCollectionObject = collectionClass.newInstance();
//                        Method collectionSetter = ClassHelper.detectSetterMethod(targetObject.getClass(), ClassHelper.setMethodNameFromFieldName(JavaPath.cleanPathSegment(outputFieldPath.getCollectionSegment())), null);
//                        if(collectionSetter != null) {
//                            collectionSetter.invoke(targetObject, targetCollectionObject);
//                        }
//                    }
                    
                } catch (Exception e) {
                    logger.error(String.format("Error initializing target List object msg=%s", e.getMessage()), e);
                    Audit audit = new Audit();
                    audit.setStatus(AuditStatus.ERROR);
                    audit.setMessage(String.format("Error initializing target List object msg=%s", e.getMessage()));
                    session.getAudits().getAudit().add(audit);
                    return;
                }
                
                if(outputFieldPath == null) {
                    // This is a collectionMember source field
                    if(logger.isDebugEnabled()) {
                        logger.debug(String.format("Found collectionMember entry sPath=%s tPath=%s", inputField.getPath(), outputField.getPath()));
                    }
                    continue;
                }
                // Detect that there is something to map.. aka the inputField is indexed
                if(outputFieldPath.isCollectionRoot() && !outputFieldPath.isIndexedCollection()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug(String.format("Found collectionRoot mapping entry sPath=%s tPath=%s clz=%s", inputField.getPath(), outputField.getPath(), outputField.getClassName()));
                    }
                    
                    // Instantiate a class for a given path inside a collection
                    if(outputField.getClassName() == null) {
                        Audit audit = new Audit();
                        audit.setStatus(AuditStatus.ERROR);
                        audit.setMessage(String.format("Collection mapping must contain a collection root entry with className specified tPath=%s", outputField.getPath()));
                        session.getAudits().getAudit().add(audit);
                        continue;
                    }
                    
                    collectionMemberClassName = outputField.getClassName();
                    continue;
                }
                
                if(outputFieldPath.isIndexedCollection()) {
                    
                }

                
                logger.info(String.format("Processing output mapping for inputField=%s outputField=%s", inputField.getPath(), outputFieldPath));
                break;
            case COLLECTION: 
                Audit audit = new Audit();
                audit.setStatus(AuditStatus.ERROR);
                audit.setMessage(String.format("Collection mappings with a collection are unsupported"));
                session.getAudits().getAudit().add(audit);
                break;
            case COMBINE: break;
            case SEPARATE: break;
            case LOOKUP: break;
            default: 
                Audit audit2 = new Audit();
                audit2.setStatus(AuditStatus.ERROR);
                audit2.setMessage(String.format("Unsupported mapping type within a collection type=%s", baseMapping.getMappingType()));
                session.getAudits().getAudit().add(audit2);
                break;
            }
        }
        
        try {
            instantiateJavaObject(collectionMemberClassName, collectionItems);
        } catch (ConstructException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.error(String.format("Error constructing colletionMemberClass=%s msg=%s", collectionMemberClassName, e.getMessage()), e);
        }
        
        
        if(logger.isDebugEnabled()) {
            logger.debug("processOutputCollectionMapping completed");
        }
        
    }
    
    @Override
    public void processPostInputExecution(AtlasSession session) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostInputExecution completed");
        }
    }
    
    @Override
    public void processPostOutputExecution(AtlasSession session) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostOutputExecution completed");
        }
    }

    @Override
    public void processPostValidation(AtlasSession arg0) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostValidation completed");
        }
    }
    
    protected List<String> separateValue(AtlasSession session, String value, String delimiter)
            throws AtlasConversionException {

        AtlasContextFactory contextFactory = session.getAtlasContext().getContextFactory();
        if (contextFactory instanceof DefaultAtlasContextFactory) {
            return ((DefaultAtlasContextFactory) contextFactory).getSeparateStrategy().separateValue(value, delimiter,
                    null);
        } else {
            throw new AtlasConversionException("No supported SeparateStrategy found");
        }
    }
    
    protected void merge(JavaClass inspectionClass, List<BaseMapping> mappings) {
        if(inspectionClass == null || inspectionClass.getJavaFields() == null || inspectionClass.getJavaFields().getJavaField() == null) {
            return;
        }
        
        if(mappings == null || mappings.size() == 0) {
            return;
        }
        
        for(BaseMapping fm :mappings) {
            if(fm instanceof Mapping) {
                if(((Mapping)fm).getOutputField() != null) {
                    JavaField f = (JavaField)((Mapping)fm).getOutputField().get(0);
                    if(f.getPath() != null) {
                        JavaField inspectField = findFieldByPath(inspectionClass, f.getPath());
                        if(inspectField != null) {
                            // Support mapping overrides className
                            if(f.getClassName() != null && !f.getClassName().equals(inspectField.getClassName())) {
                                inspectField.setClassName(f.getClassName());
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected Method resolveGetMethod(Object sourceObject, JavaField javaField, boolean objectIsParent) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Object parentObject = sourceObject;
        JavaPath javaPath = new JavaPath(javaField.getPath());
        Method getter = null;
        
        if(javaPath.hasParent() && !objectIsParent) {
            parentObject = ClassHelper.parentObjectForPath(sourceObject, javaPath);
        }
        
        List<Class<?>> classTree = resolveMappableClasses(parentObject.getClass());
        
        for(Class<?> clazz : classTree) {
            try {
                if(javaField.getGetMethod() != null) { 
                    getter = clazz.getMethod(javaField.getGetMethod());
                    getter.setAccessible(true);
                    return getter;
                }
            } catch (NoSuchMethodException e) {
                // no getter method specified in mapping file   
            }

            for(String m : Arrays.asList("get", "is")) {
                String getterMethod = m + capitalizeFirstLetter(javaPath.getLastSegment());
                try {
                    getter = clazz.getMethod(getterMethod);
                    getter.setAccessible(true);
                    return getter;
                } catch (NoSuchMethodException e) {
                    // method does not exist
                }
            }
        }
        return null;
    }
    
    protected Method resolveSetMethod(Object sourceObject, JavaField javaField, Class<?> targetType) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        JavaPath javaPath = new JavaPath(javaField.getPath());
        Object parentObject = sourceObject;
        
        if(javaPath.hasParent()) {
            parentObject = ClassHelper.parentObjectForPath(parentObject, javaPath);
        }
        List<Class<?>> classTree = resolveMappableClasses(parentObject.getClass());
        
        for(Class<?> clazz : classTree) {
            try {
                String setterMethodName = javaField.getSetMethod();
                if(setterMethodName == null) { 
                    setterMethodName = "set" + capitalizeFirstLetter(javaPath.getLastSegment());
                } 
                return ClassHelper.detectSetterMethod(clazz, setterMethodName, targetType);
            } catch (NoSuchMethodException e) {
                // method does not exist
            }

            // Try the boxUnboxed version
            if(getConversionService().isPrimitive(targetType) || getConversionService().isBoxedPrimitive(targetType)) {
                try {
                    String setterMethodName = javaField.getSetMethod();
                    if(setterMethodName == null) { 
                        setterMethodName = "set" + capitalizeFirstLetter(javaPath.getLastSegment());
                    } 
                    return ClassHelper.detectSetterMethod(clazz, setterMethodName, getConversionService().boxOrUnboxPrimitive(targetType));
                } catch (NoSuchMethodException e) {
                    // method does not exist
                }
            }
        }
        
        throw new NoSuchMethodException(String.format("Unable to resolve setter for path=%s", javaField.getPath()));
    }
    
    protected List<Class<?>> resolveMappableClasses(Class<?> className) {
        
        List<Class<?>> classTree = new ArrayList<Class<?>>();
        classTree.add(className);
        
        Class<?> superClazz = className.getSuperclass();
        while (superClazz != null) {
            if (JdkPackages.contains(superClazz.getPackage().getName())) {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Ignoring SuperClass " + superClazz.getName() + " which is a Jdk core class");
//                }
                superClazz = null;
            } else {
                classTree.add(superClazz);
                superClazz = superClazz.getSuperclass();
            }
        }
        
        // DON'T reverse.. prefer child -> parent -> grandparent
        //List<Class<?>> reverseTree = classTree.subList(0, classTree.size());
        //Collections.reverse(reverseTree);
        //return reverseTree;
        return classTree;
    }
    
    protected JavaField findFieldByPath(JavaClass javaClass, String javaPath) {
        if(javaClass == null || javaClass.getJavaFields() == null || javaClass.getJavaFields().getJavaField() == null) {
            return null;
        }
        
        for(JavaField jf : javaClass.getJavaFields().getJavaField()) {
            if(jf.getPath().equals(javaPath)) {
                return jf;
            }
            if(jf instanceof JavaClass) {
                JavaField childJavaField = findFieldByPath((JavaClass)jf, javaPath);
                if(childJavaField != null) {
                    return childJavaField;
                }
            }
        }
        
        return null;
    }
    
    private static String capitalizeFirstLetter(String sentence) {
        if (StringUtil.isEmpty(sentence)) {
            return sentence;
        }
        if (sentence.length() == 1) {
            return String.valueOf(sentence.charAt(0)).toUpperCase();
        }
        return String.valueOf(sentence.charAt(0)).toUpperCase() + sentence.substring(1);
    }
    
    @Override
	public AtlasConversionService getConversionService() {
        return atlasConversionService;
    }

    @Override
	public void setConversionService(AtlasConversionService atlasConversionService) {
        this.atlasConversionService = atlasConversionService;
    }
    
    public ClassInspectionService getJavaInspectionService() {
        return javaInspectionService;
    }

    public void setJavaInspectionService(ClassInspectionService javaInspectionService) {
        this.javaInspectionService = javaInspectionService;
    }

    public JavaConstructService getJavaConstructService() {
        return javaConstructService;
    }

    public void setJavaConstructService(JavaConstructService javaConstructService) {
        this.javaConstructService = javaConstructService;
    }
    
    @Override
    public AtlasModuleMode getMode() {
        return this.atlasModuleMode;
    }

    @Override
    public void setMode(AtlasModuleMode atlasModuleMode) {
        this.atlasModuleMode = atlasModuleMode;
    }

    @Override
    public List<AtlasModuleMode> listSupportedModes() {
        return Arrays.asList(AtlasModuleMode.SOURCE, AtlasModuleMode.TARGET);
    }

    @Override
    public Boolean isStatisticsSupported() {
        return false;
    }

    @Override
    public Boolean isStatisticsEnabled() {
        return false;
    }

    @Override
    public Boolean isSupportedField(Field field) {
        if (field instanceof JavaField) {
            return true;
        } else if (field instanceof PropertyField) {
            return true;
        } else if (field instanceof ConstantField) {
            return true;
        }
        return false;
    }
}
