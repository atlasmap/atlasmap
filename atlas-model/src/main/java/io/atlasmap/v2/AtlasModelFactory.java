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

import io.atlasmap.v2.MockField;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.Properties;

public class AtlasModelFactory {

    @SuppressWarnings("unchecked")
    public static <T extends BaseMapping> T createMapping(MappingType type) {
        T fm = null;
        if (type == null) {
            return null;
        }

        switch(type) {
        case COLLECTION: fm = (T) new Collection(); ((Collection)fm).setMappingType(type); return fm;
        case COMBINE: fm = (T) new Mapping(); ((Mapping)fm).setMappingType(type); return fm;
        case LOOKUP: fm = (T) new Mapping(); ((Mapping)fm).setMappingType(type); return fm;
        case MAP: fm = (T) new Mapping(); ((Mapping)fm).setMappingType(type); return fm;
        case SEPARATE: fm = (T) new Mapping(); ((Mapping)fm).setMappingType(type); return fm;
        default:
            throw new IllegalStateException(String.format("Unsupported mappingType=%s", type.value()));
        }
    }

    public static AtlasMapping createAtlasMapping() {
        AtlasMapping mapping = new AtlasMapping();
        mapping.setMappings(new Mappings());
        mapping.setProperties(new Properties());
        mapping.setLookupTables(new LookupTables());
        return mapping;
    }

    public static Collection createCollection() {
        Collection collectionMapping = new Collection();
        collectionMapping.setMappings(new Mappings());
        collectionMapping.setMappingType(MappingType.COLLECTION);
        return collectionMapping;
    }

    public static MockDocument createMockDocument() {
        MockDocument mockDocument = new MockDocument();
        mockDocument.setFields(new Fields());
        return mockDocument;
    }

    public static MockField createMockField() {
        return new MockField();
    }
    
    public static PropertyField createPropertyField() {
        return new PropertyField();
    }
    
    public static BaseMapping cloneMapping(BaseMapping baseMapping, boolean deepClone) {
        if (baseMapping.getMappingType().equals(MappingType.COLLECTION)) {
            Collection mapping = (Collection) baseMapping;
            Collection clone = new Collection();
            clone.setAlias(mapping.getAlias());
            clone.setDescription(mapping.getDescription());
            clone.setMappingType(mapping.getMappingType());
            if (deepClone) {
                for (BaseMapping m : mapping.getMappings().getMapping()) {
                    clone.getMappings().getMapping().add(cloneMapping(m, deepClone));
                }
            }
            return clone;
        } else { //non-collection mapping
            Mapping mapping = (Mapping) baseMapping;
            Mapping clone = new Mapping();
            clone.setAlias(mapping.getAlias());
            clone.setDelimiter(mapping.getDelimiter());
            clone.setDelimiterString(mapping.getDelimiterString());
            clone.setDescription(mapping.getDescription());
            clone.setLookupTableName(mapping.getLookupTableName());
            clone.setMappingType(MappingType.fromValue(mapping.getMappingType().value()));
            clone.setStrategy(mapping.getStrategy());
            clone.setStrategyClassName(mapping.getStrategyClassName());
            if (deepClone) {
                for (Field f : mapping.getInputField()) {
                    clone.getInputField().add(cloneField(f));
                }
                for (Field f : mapping.getOutputField()) {
                    clone.getOutputField().add(cloneField(f));
                }
            }
            return clone;
        }        
    }
    
    public static Field cloneField(Field f) {
        return null;
    }
    
    public static SimpleField cloneFieldToSimpleField(Field field) {
        if(field == null) {
            return null;
        }
        
        SimpleField f = new SimpleField();
        f.setActions(cloneFieldActions(field.getActions()));
        if(field.getArrayDimensions() != null) { f.setArrayDimensions(Integer.valueOf(field.getArrayDimensions())); }
        if(field.getArraySize() != null) { f.setArraySize(Integer.valueOf(field.getArraySize())); }
        if(field.getCollectionType() != null) { f.setCollectionType(CollectionType.fromValue(field.getCollectionType().value())); }
        if(field.getDocId() != null) { f.setDocId(new String(field.getDocId())); }
        if(field.getFieldType() != null) { f.setFieldType(FieldType.fromValue(field.getFieldType().value())); }
        if(field.getIndex() != null) { f.setIndex(Integer.valueOf(field.getIndex())); }
        if(field.getPath() != null) { f.setPath(new String(field.getPath())); }
        if(field.isRequired() != null) { f.setRequired(Boolean.valueOf(field.isRequired())); }
        if(field.getStatus() != null) { f.setStatus(FieldStatus.fromValue(field.getStatus().value())); }
        // We can't clone so don't set value
        // f.setValue(field.getValue());
        return f;
    }
    
    public static Actions cloneFieldActions(Actions actions) {
        if(actions == null) {
            return null;
        }
        
        Actions n = new Actions();
        
        if(actions.getActions() == null || actions.getActions().isEmpty()) {
            return n;
        }
        
        for(Action a : actions.getActions()) {
            n.getActions().add(cloneAction(a));
        }
        return n;
    }
    
    public static Action cloneAction(Action action) {
        if(action == null) {
            return null;
        }
        
        Action a = null;
        if(action instanceof Camelize) { return new Camelize(); }
        if(action instanceof Capitalize) { return new Capitalize(); }
        if(action instanceof CurrentDate) { return new CurrentDate(); }
        if(action instanceof CurrentDateTime) { return new CurrentDateTime(); }
        if(action instanceof CurrentTime) { return new CurrentTime(); }
        if(action instanceof CustomAction) { 
            a = new CustomAction();
            ((CustomAction)a).setClassName(new String(((CustomAction)action).getClassName()));
            ((CustomAction)a).setMethodName(new String(((CustomAction)action).getMethodName()));
            if(((CustomAction)a).getInputFieldType() != null) {
                ((CustomAction)a).setInputFieldType(FieldType.fromValue(((CustomAction)action).getInputFieldType().value()));
            }
            if(((CustomAction)a).getOutputFieldType() != null) {
                ((CustomAction)a).setOutputFieldType(FieldType.fromValue(((CustomAction)action).getOutputFieldType().value()));
            }
        }
        return a;
    }
    
    protected static String baseFieldToString(Field f) {
        if(f == null) {
            return "";
        }
        String tmp = new String(); 
        tmp.concat(" arrayDimensions=" + f.getArrayDimensions());        
        tmp.concat(" arraySize=" + f.getArraySize());
        tmp.concat(" collectionType=" + (f.getCollectionType() != null ? f.getCollectionType().value() : null));
        tmp.concat(" docId=" + f.getDocId());
        if(f.getActions() != null && f.getActions().getActions() != null) {
            if(!f.getActions().getActions().isEmpty()) {
                tmp.concat(" fieldActions#=" + f.getActions().getActions().size());
            } else {
                tmp.concat(" fieldActions#=0");
            }
        } else {
            tmp.concat(" fieldActions#=");
        }
        tmp.concat(" fieldType=" + (f.getFieldType() != null ? f.getFieldType().value() : null));
        tmp.concat(" index=" + f.getIndex());
        tmp.concat(" path=" + f.getPath());
        tmp.concat(" fieldStatus=" + (f.getStatus() != null ? f.getStatus().value() : null));
        tmp.concat(" value=" + f.getValue());
        return tmp;
    }
    
    public static String toString(PropertyField f) {
        String tmp = new String("PropertyField [name=");
        if(f != null && f.getName() != null) {
            tmp.concat(f.getName());
        }
        tmp.concat(baseFieldToString(f));
        tmp.concat("]");
        return tmp;
    }
}
