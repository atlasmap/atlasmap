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
package io.atlasmap.json.v2;

import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;

public class AtlasJsonModelFactory {
	
	public static final String URI_FORMAT = "atlas:json";

	public static JsonDocument createJsonDocument() {
		JsonDocument jsonDocument = new JsonDocument();
		jsonDocument.setFields(new Fields());
		return jsonDocument;
	}
	
	public static JsonField createJsonField() {
	    JsonField jsonField = new JsonField();
		return jsonField;
	}
	
	public static String toString(JsonField f) {
		return "JsonField [name=" + f.getName() + ", primitive=" + f.isPrimitive() + ", typeName=" + f.getTypeName() + ", userCreated="
			+ f.isUserCreated() + ", actions=" + f.getActions() + ", value=" + f.getValue() + ", arrayDimensions=" + f.getArrayDimensions()
			+ ", arraySize=" + f.getArraySize() + ", collectionType=" + f.getCollectionType() + ", docId=" + f.getDocId() + ", index="
			+ f.getIndex() + ", path=" + f.getPath() + ", required=" + f.isRequired() + ", status=" + f.getStatus() + ", fieldType="
			+ f.getFieldType() + "]";
	}
        
    public static Field cloneField(Field field) {
        JsonField clone = new JsonField();
        JsonField that = (JsonField)field;
        
        //generic from Field
        if(field.getActions() != null) { clone.setActions(AtlasModelFactory.cloneFieldActions(field.getActions())); }
        if(field.getArrayDimensions() != null) { clone.setArrayDimensions(Integer.valueOf(field.getArrayDimensions())); }
        if(field.getArraySize() != null) { clone.setArraySize(Integer.valueOf(field.getArraySize())); }
        if(field.getCollectionType() != null) { clone.setCollectionType(CollectionType.fromValue(field.getCollectionType().value())); }
        if(field.getDocId() != null) { clone.setDocId(new String(field.getDocId())); }
        if(field.getFieldType() != null) { clone.setFieldType(FieldType.fromValue(field.getFieldType().value())); }
        if(field.getIndex() != null) { clone.setIndex(Integer.valueOf(field.getIndex())); }
        if(field.getPath() != null) { clone.setPath(new String(field.getPath())); }
        if(field.isRequired() != null) { clone.setRequired(Boolean.valueOf(field.isRequired())); }
        if(field.getStatus() != null) { clone.setStatus(FieldStatus.fromValue(field.getStatus().value())); }  
                
        //json specific
        clone.setName(that.getName());
        clone.setPrimitive(that.isPrimitive());
        clone.setTypeName(that.getTypeName());
        clone.setUserCreated(that.isUserCreated());
        
        return clone;
    }
}
