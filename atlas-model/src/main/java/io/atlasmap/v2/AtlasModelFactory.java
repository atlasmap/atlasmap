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
        if(action instanceof ConvertAreaUnit) {
            ConvertAreaUnit cau = new ConvertAreaUnit();
            cau.setFromUnit(((ConvertAreaUnit) action).getFromUnit());
            cau.setToUnit(((ConvertAreaUnit) action).getToUnit());
            a = cau;
        }
        if(action instanceof ConvertDistanceUnit) {
            ConvertDistanceUnit cdu = new ConvertDistanceUnit();
            cdu.setFromUnit(((ConvertDistanceUnit) action).getFromUnit());
            cdu.setToUnit(((ConvertDistanceUnit) action).getToUnit());
            a = cdu;
        }
        if(action instanceof ConvertMassUnit) {
            ConvertMassUnit cmu = new ConvertMassUnit();
            cmu.setFromUnit(((ConvertMassUnit) action).getFromUnit());
            cmu.setToUnit(((ConvertMassUnit) action).getToUnit());
            a = cmu;
        }
        if(action instanceof ConvertVolumeUnit) {
            ConvertVolumeUnit cvu = new ConvertVolumeUnit();
            cvu.setFromUnit(((ConvertVolumeUnit) action).getFromUnit());
            cvu.setToUnit(((ConvertVolumeUnit) action).getToUnit());
            a = cvu;
        }
        if(action instanceof CurrentDate) { return new CurrentDate(); }
        if(action instanceof CurrentDateTime) { return new CurrentDateTime(); }
        if(action instanceof CurrentTime) { return new CurrentTime(); }
        if(action instanceof CustomAction) { 
            a = new CustomAction();
            if(((CustomAction)action).getClassName() != null) { ((CustomAction)a).setClassName(new String(((CustomAction)action).getClassName())); }
            if(((CustomAction)action).getMethodName() != null) { ((CustomAction)a).setMethodName(new String(((CustomAction)action).getMethodName())); }
            if(((CustomAction)a).getInputFieldType() != null) {
                ((CustomAction)a).setInputFieldType(FieldType.fromValue(((CustomAction)action).getInputFieldType().value()));
            }
            if(((CustomAction)a).getOutputFieldType() != null) {
                ((CustomAction)a).setOutputFieldType(FieldType.fromValue(((CustomAction)action).getOutputFieldType().value()));
            }
        }
        if(action instanceof GenerateUUID) { return new GenerateUUID(); }
        if(action instanceof Lowercase) { return new Lowercase(); }
        if(action instanceof PadStringLeft) { 
            a = new PadStringLeft();
            if(((PadStringLeft)action).getPadCharacter() != null) { ((PadStringLeft)a).setPadCharacter(new String(((PadStringLeft)action).getPadCharacter())); }
            if(((PadStringLeft)action).getPadCount() != null) { ((PadStringLeft)a).setPadCount(Integer.valueOf(((PadStringLeft)action).getPadCount())); }
        }
        if(action instanceof PadStringRight) { 
            a = new PadStringRight();
            if(((PadStringRight)action).getPadCharacter() != null) { ((PadStringRight)a).setPadCharacter(new String(((PadStringRight)action).getPadCharacter())); }
            if(((PadStringRight)action).getPadCount() != null) { ((PadStringRight)a).setPadCount(Integer.valueOf(((PadStringRight)action).getPadCount())); }
        }
        if(action instanceof SeparateByDash) { return new SeparateByDash(); }
        if(action instanceof SeparateByUnderscore) { return new SeparateByUnderscore(); }
        if(action instanceof StringLength) { return new StringLength(); }
        if(action instanceof SubString) { 
            a = new SubString();
            if(((SubString)action).getStartIndex() != null) { ((SubString)a).setStartIndex(Integer.valueOf(((SubString)action).getStartIndex())); }
            if(((SubString)action).getEndIndex() != null) { ((SubString)a).setEndIndex(Integer.valueOf(((SubString)action).getEndIndex())); }
        }
        if(action instanceof SubStringAfter) { 
            a = new SubStringAfter();
            if(((SubStringAfter)action).getStartIndex() != null) { ((SubStringAfter)a).setStartIndex(Integer.valueOf(((SubStringAfter)action).getStartIndex())); }
            if(((SubStringAfter)action).getEndIndex() != null) { ((SubStringAfter)a).setEndIndex(Integer.valueOf(((SubStringAfter)action).getEndIndex())); }
        }
        if(action instanceof SubStringBefore) { 
            a = new SubStringBefore();
            if(((SubStringBefore)action).getStartIndex() != null) { ((SubStringBefore)a).setStartIndex(Integer.valueOf(((SubStringBefore)action).getStartIndex())); }
            if(((SubStringBefore)action).getEndIndex() != null) { ((SubStringBefore)a).setEndIndex(Integer.valueOf(((SubStringBefore)action).getEndIndex())); }
        }
        if (action instanceof SumUp) {
            SumUp su = new SumUp();
            su.setNumberType(((SumUp) action).getNumberType());
            a = su;
        }
        if(action instanceof Trim) { return new Trim(); }
        if(action instanceof TrimLeft) { return new TrimLeft(); }
        if(action instanceof TrimRight) { return new TrimRight(); }
        if(action instanceof Uppercase) { return new Uppercase(); }
        return a;
    }
    
    protected static String baseFieldToString(Field f) {
        if(f == null) {
            return "";
        }
        StringBuilder tmp = new StringBuilder(); 
        tmp.append(" arrayDimensions=" + f.getArrayDimensions());        
        tmp.append(" arraySize=" + f.getArraySize());
        tmp.append(" collectionType=" + (f.getCollectionType() != null ? f.getCollectionType().value() : null));
        tmp.append(" docId=" + f.getDocId());
        if(f.getActions() != null && f.getActions().getActions() != null) {
            if(!f.getActions().getActions().isEmpty()) {
                tmp.append(" fieldActions#=" + f.getActions().getActions().size());
            } else {
                tmp.append(" fieldActions#=0");
            }
        } else {
            tmp.append(" fieldActions#=");
        }
        tmp.append(" fieldType=" + (f.getFieldType() != null ? f.getFieldType().value() : null));
        tmp.append(" index=" + f.getIndex());
        tmp.append(" path=" + f.getPath());
        tmp.append(" fieldStatus=" + (f.getStatus() != null ? f.getStatus().value() : null));
        tmp.append(" value=" + f.getValue());
        return tmp.toString();
    }
    
    public static String toString(PropertyField f) {
        StringBuilder tmp = new StringBuilder("PropertyField [name=");
        if(f != null && f.getName() != null) {
            tmp.append(f.getName());
        }
        tmp.append(baseFieldToString(f));
        tmp.append("]");
        return tmp.toString();
    }
    
    public static String toString(Field f) {
        StringBuilder tmp = new StringBuilder("Field [name=");
        if(f != null) {
            tmp.append(f.getClass().getSimpleName());
        }
        tmp.append(baseFieldToString(f));
        tmp.append("]");
        return tmp.toString();
    }
}
