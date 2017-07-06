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
package io.atlasmap.v2;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AtlasModelFactory {

	public static final Set<String> primitiveClasses = new HashSet<String>(Arrays.asList("byte", "short", "int", "long", "float", "double", "boolean", "char"));
	public static final Set<String> boxedPrimitiveClasses = new HashSet<String>(Arrays.asList("java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Boolean", "java.lang.Character", "java.lang.String"));
		
	public static FieldType fieldTypeFromClassName(String className) {
		if(className == null || className.isEmpty()) {
			return null;
		}
		
		switch(className) {
		case "boolean": return FieldType.BOOLEAN;
		case "java.lang.Boolean": return FieldType.BOOLEAN;
		case "byte": return FieldType.BYTE;
		case "java.lang.Byte": return FieldType.BYTE;
		case "char": return FieldType.CHAR;
		case "java.lang.Character": return FieldType.CHAR;
		case "double": return FieldType.DOUBLE;
		case "java.lang.Double": return FieldType.DOUBLE;
		case "float": return FieldType.FLOAT;
		case "java.lang.Float": return FieldType.FLOAT;
		case "int": return FieldType.INTEGER;
		case "java.lang.Integer": return FieldType.INTEGER;
		case "long": return FieldType.LONG;
		case "java.lang.Long": return FieldType.LONG;
		case "short": return FieldType.SHORT;
		case "java.lang.Short": return FieldType.SHORT;
		case "java.lang.String": return FieldType.STRING;
		default: return FieldType.UNSUPPORTED;
		}
	}
	
	public static Class<?> classFromFieldType(FieldType fieldType) {
		if(fieldType == null) {
			return null;
		}
		
		switch(fieldType) {
		case BOOLEAN: return Boolean.class;
		case BYTE: return Byte.class;
		case CHAR: return java.lang.Character.class;
		case DOUBLE: return java.lang.Double.class;
		case FLOAT: return java.lang.Float.class;
		case INTEGER: return java.lang.Integer.class;
		case LONG: return java.lang.Long.class;
		case SHORT: return java.lang.Short.class;
		case STRING: return java.lang.String.class;
		// TODO: need to fix the default return type for non-primitive
		default: return null;
		}
	}
	
	public static MockDocument createMockDocument() {
		MockDocument mockDocument = new MockDocument();
		mockDocument.setFields(new Fields());
		return mockDocument;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends FieldMapping> T createFieldMapping(Class <T> clazz) {
		T fm = null;
		if(clazz == null) {
			return null;
		}
		
		if(clazz.isAssignableFrom(SeparateFieldMapping.class)) {
			fm = (T) new SeparateFieldMapping();
			((SeparateFieldMapping)fm).setOutputFields(new MappedFields());
			return fm;
		} else if(clazz.isAssignableFrom(CombineFieldMapping.class)) {
			fm = (T) new CombineFieldMapping();
			((CombineFieldMapping)fm).setInputFields(new MappedFields());
			return fm;
		} else if(clazz.isAssignableFrom(MapFieldMapping.class)) {
			fm = (T) new MapFieldMapping();
			return fm;
		} else if(clazz.isAssignableFrom(LookupFieldMapping.class)) {
			fm = (T) new LookupFieldMapping();
			return fm;
		} else {
			throw new IllegalStateException("Unsupported class: " + clazz.getName());
		}
	}
	
	public static AtlasMapping createAtlasMapping() {
		AtlasMapping mapping = new AtlasMapping();
		mapping.setFieldMappings(new FieldMappings());
		mapping.setProperties(new Properties());
		mapping.setLookupTables(new LookupTables());
		return mapping;
	}
	
	public static MappedField createMappedField() {
		MappedField mappedField = new MappedField();
		mappedField.setFieldActions(new FieldActions());
		return mappedField;
	}
	
	public static LookupTable createLookupTable() {
		LookupTable lookupTable = new LookupTable();
		lookupTable.setLookupEntryList(new LookupEntryList());
		return lookupTable;
	}
	
	public static CollectionFieldMapping createCollectionFieldMapping() {
		CollectionFieldMapping collectionFieldMapping = new CollectionFieldMapping();
		collectionFieldMapping.getFieldMappings().add(new FieldMappings());
		return collectionFieldMapping;
	}
}
