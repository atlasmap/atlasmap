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
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasNotFoundException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.core.StringDelimiter;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.javapath.JavaPath;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AtlasModuleDetail(name="JavaModule", uri="atlas:java", modes={"SOURCE", "TARGET"}, dataFormats={"java"}, configPackages={"io.atlasmap.java.v2"})
public class JavaModule implements AtlasModule {

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processInput(AtlasSession session) throws AtlasException {
		AtlasMapping atlasMapping = session.getAtlasMapping();
		FieldMappings fieldMappings = atlasMapping.getFieldMappings();
		List<FieldMapping> fieldMappingList = fieldMappings.getFieldMapping();
		Object source = session.getInput();
		try { 
			MappedField sourceMappedField = null;
			Field sourceField = null;
			for(FieldMapping fieldMapping : fieldMappingList) {
				if(fieldMapping instanceof MapFieldMapping) {
					sourceMappedField = ((MapFieldMapping)fieldMapping).getInputField();
					sourceField = sourceMappedField.getField();
					if(sourceField instanceof JavaField) {
						populateValue((JavaField)sourceField, source);
					}
				} else if(fieldMapping instanceof SeparateFieldMapping) {
					sourceMappedField = ((SeparateFieldMapping)fieldMapping).getInputField();
					sourceField = sourceMappedField.getField();
					if(sourceField instanceof JavaField) {
						populateValue((JavaField)sourceField, source);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new AtlasNotFoundException(e.getMessage(), e.getCause());
		} catch (Exception e) {
			throw new AtlasException(e.getMessage(), e.getCause());
		}
		
	}
	
	protected void populateValue(JavaField javaField, Object source) throws Exception {
		Object sourceValue = null;
		if(javaField.getPath().contains(JavaPath.JAVAPATH_SEPARATOR)) {
			Object parentObject = getParentObject(new JavaPath(javaField.getPath()), source);
			Method parentGet = parentObject.getClass().getDeclaredMethod(javaField.getGetMethod());
			parentGet.setAccessible(true);
			sourceValue = parentGet.invoke(parentObject);
			javaField.setValue(sourceValue);
		} else {
			Method getText = source.getClass().getMethod(javaField.getGetMethod());
			getText.setAccessible(true);
			javaField.setValue(getText.invoke(source));
		}
	}

	@Override
	public void processOutput(AtlasSession session) throws AtlasException {
		String targetClassName = AtlasUtil.getUriParameterValue(session.getAtlasMapping().getTargetUri(), "className");
		AtlasMapping atlasMapping = session.getAtlasMapping();
		FieldMappings fieldMappings = atlasMapping.getFieldMappings();
		List<FieldMapping> fieldMappingList = fieldMappings.getFieldMapping();
		
		try {
			Class<?> targetClazz = this.getClass().getClassLoader().loadClass(targetClassName);
			Object targetObject = targetClazz.newInstance();
			
			MappedField sourceMappedField = null;
			MappedField targetMappedField = null;
			Field sourceField = null;
			JavaField targetField = null;
			Object sourceValue = null;
			
			for(FieldMapping fieldMapping : fieldMappingList) {
				if(fieldMapping instanceof MapFieldMapping) {
					sourceMappedField = ((MapFieldMapping)fieldMapping).getInputField();
					targetMappedField = ((MapFieldMapping)fieldMapping).getOutputField();
					sourceField = sourceMappedField.getField();
					sourceValue = sourceField.getValue();
					
					targetField = (JavaField)targetMappedField.getField();
					Method targetMethod = targetClazz.getDeclaredMethod(targetField.getSetMethod(), AtlasModelFactory.classFromFieldType(targetField.getType()));
					targetMethod.setAccessible(true);
					targetMethod.invoke(targetObject, sourceValue);				
				} else if(fieldMapping instanceof SeparateFieldMapping) {
					sourceMappedField = ((SeparateFieldMapping)fieldMapping).getInputField();
					sourceField = sourceMappedField.getField();
					sourceValue = sourceField.getValue();
					
					// TODO wire-in runtime audit logger
					if(!(sourceValue instanceof String)) {
						
					}
					
					List<String> sourceValues = separateValue(session, (String)sourceValue, ((SeparateFieldMapping)fieldMapping).getDelimiterValue());
					
					for(MappedField tmpTargetMappedField : ((SeparateFieldMapping)fieldMapping).getOutputFields().getMappedField()) {
						targetField = (JavaField)tmpTargetMappedField.getField();
						FieldActions fieldActions = tmpTargetMappedField.getFieldActions();
							
						for(FieldAction fieldAction : fieldActions.getFieldAction()) {
							if(fieldAction instanceof MapAction) {
								Method targetMethod = targetClazz.getDeclaredMethod(targetField.getSetMethod(), AtlasModelFactory.classFromFieldType(targetField.getType()));
								targetMethod.setAccessible(true);
								targetMethod.invoke(targetObject, sourceValues.get(((MapAction)fieldAction).getIndex()));
							}
						}
					}				
				}
			}
			session.setOutput(targetObject);

		} catch (Exception e) {
			throw new AtlasException(e.getMessage(), e.getCause());
		}
	}
	
	protected Object getParentObject(JavaPath javaPath, Object sourceObject) throws Exception {
		Object tmpObject = sourceObject;
		
		if(javaPath.getSegments() == null || javaPath.getSegments().size() < 2) {
			return sourceObject;
		}
		
		Class<?> tmpClass = sourceObject.getClass();
		int limit = javaPath.getSegments().size()-1;
		for(int i=0; i < limit; i++) {
			Method tmpMethod = tmpClass.getDeclaredMethod("get"+javaPath.getSegments().get(i));
			tmpMethod.setAccessible(true);
			tmpObject = tmpMethod.invoke(tmpObject);
		}
		
		return tmpObject;
	}
	
	protected List<String> separateValue(AtlasSession session, String value, String delimiter) throws AtlasConversionException {
		
		AtlasContextFactory contextFactory = session.getAtlasContext().getContextFactory();
		if(contextFactory instanceof DefaultAtlasContextFactory) {
			return ((DefaultAtlasContextFactory)contextFactory).getSeparateStrategy().separateValue(value, delimiter, null);
		} else {
			throw new AtlasConversionException("No supported SeparateStrategy found");
		}

	}

	@Override
	public AtlasModuleMode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMode(AtlasModuleMode atlasModuleMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AtlasModuleMode> listSupportedModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isStatisticsSupported() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isStatisticsEnabled() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isSupportedField(Field field) {
		if(field instanceof JavaField) {
			return true;
		}
		return false;
	}
}
