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
package io.atlasmap.java.v2;

import io.atlasmap.v2.Field;

public class AtlasJavaModelFactory {
	
	public static final String URI_FORMAT = "atlas:java?className=%s";

	public static JavaClass createJavaClass() {
		JavaClass javaClass = new JavaClass();
		javaClass.setJavaEnumFields(new JavaEnumFields());
		javaClass.setJavaFields(new JavaFields());
		return javaClass;
	}
	
	public static JavaField createJavaField() {
		JavaField javaField = new JavaField();
		javaField.setModifiers(new ModifierList());
		return javaField;
	}
	
    public static Field cloneJavaField(Field field) {
        
        if (field instanceof JavaField) {
            JavaField that = (JavaField) field;
            JavaField clone = new JavaField();
            
            //generic from Field
            clone.setActions(that.getActions());
            clone.setArrayDimensions(that.getArrayDimensions());
            clone.setArraySize(that.getArraySize());
            clone.setCollectionType(that.getCollectionType());
            clone.setDocId(that.getDocId());
            clone.setFieldType(that.getFieldType());
            clone.setIndex(that.getIndex());        
            clone.setPath(that.getPath());
            clone.setRequired(that.isRequired());
            clone.setStatus(that.getStatus());
            clone.setValue(that.getValue());
            
            //defined by JavaField
            clone.setAnnotations(that.getAnnotations());
            clone.setClassName(that.getClassName());
            clone.setCollectionClassName(that.getCollectionClassName());
            clone.setGetMethod(that.getGetMethod());
            clone.setModifiers(that.getModifiers());
            clone.setName(that.getName());
            clone.setParameterizedTypes(that.getParameterizedTypes());
            clone.setPrimitive(that.isPrimitive());
            clone.setSetMethod(that.getSetMethod());
            clone.setSynthetic(that.isSynthetic());
            
            return clone;
        } else if (field instanceof JavaEnumField) {
            JavaEnumField that = (JavaEnumField) field;
            JavaEnumField clone = new JavaEnumField();
            
            //generic from Field
            clone.setActions(that.getActions());
            clone.setArrayDimensions(that.getArrayDimensions());
            clone.setArraySize(that.getArraySize());
            clone.setCollectionType(that.getCollectionType());
            clone.setDocId(that.getDocId());
            clone.setFieldType(that.getFieldType());
            clone.setIndex(that.getIndex());        
            clone.setPath(that.getPath());
            clone.setRequired(that.isRequired());
            clone.setStatus(that.getStatus());
            clone.setValue(that.getValue());
            
            //defined by JavaEnumField
            clone.setClassName(that.getClassName());            
            clone.setName(that.getName());
            clone.setOrdinal(that.getOrdinal());            
            
            return clone;
            
        }
        //TODO: needs to be atlasexception, but that's not a dependency for some reason on this project.
        throw new RuntimeException("Unsupported field type to clone: " + field);
    }
}
