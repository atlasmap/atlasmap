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

import io.atlasmap.v2.Field;
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
                
        //json specific
        clone.setName(that.getName());
        clone.setPrimitive(that.isPrimitive());
        clone.setTypeName(that.getTypeName());
        clone.setUserCreated(that.isUserCreated());
        
        return clone;
    }
}
