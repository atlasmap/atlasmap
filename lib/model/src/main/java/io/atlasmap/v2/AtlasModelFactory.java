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

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("squid:S3776")
public class AtlasModelFactory {

    private AtlasModelFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends BaseMapping> T createMapping(MappingType type) {
        T fm = null;
        if (type == null) {
            return null;
        }

        switch (type) {
        case COLLECTION:
            fm = (T) new Collection();
            ((Collection) fm).setMappingType(type);
            return fm;
        case COMBINE:
        case LOOKUP:
        case MAP:
        case SEPARATE:
            fm = (T) new Mapping();
            ((Mapping) fm).setMappingType(type);
            return fm;
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
                clone.setMappings(new Mappings());
                for (BaseMapping m : mapping.getMappings().getMapping()) {
                    clone.getMappings().getMapping().add(cloneMapping(m, deepClone));
                }
            }
            return clone;
        }
        // Non-collection mapping
        Mapping mapping = (Mapping) baseMapping;
        Mapping clone = new Mapping();
        clone.setAlias(mapping.getAlias());
        clone.setDelimiter(mapping.getDelimiter());
        clone.setDelimiterString(mapping.getDelimiterString());
        clone.setDescription(mapping.getDescription());
        clone.setLookupTableName(mapping.getLookupTableName());
        if (mapping.getMappingType() != null) {
            clone.setMappingType(MappingType.fromValue(mapping.getMappingType().value()));
        }
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

    public static Field cloneField(Field f) {
        throw new IllegalArgumentException("Use module specific factories to clone fields");
    }

    /**
     * This is a shallow copy, it doesn't handle children.
     * Each module should handle their own deep clone.
     * @param fg {@code FieldGroup}
     * @return copied FieldGroup
     */
    public static FieldGroup copyFieldGroup(FieldGroup fg) {
        if (fg == null) {
            return null;
        }
        FieldGroup newfg = new FieldGroup();
        copyField(fg, newfg, true);
        return newfg;
    }

    public static SimpleField cloneFieldToSimpleField(Field field) {
        if (field == null) {
            return null;
        }

        SimpleField f = new SimpleField();
        copyField(field, f, true);
        return f;
    }

    public static void copyField(Field from, Field to, boolean withActions) {
        if (withActions) {
            to.setActions(cloneFieldActions(from.getActions()));
        }
        if (from.getArrayDimensions() != null) {
            to.setArrayDimensions(Integer.valueOf(from.getArrayDimensions()));
        }
        if (from.getArraySize() != null) {
            to.setArraySize(Integer.valueOf(from.getArraySize()));
        }
        if (from.getCollectionType() != null) {
            to.setCollectionType(CollectionType.fromValue(from.getCollectionType().value()));
        }
        if (from.getDocId() != null) {
            to.setDocId(from.getDocId());
        }
        if (from.getFieldType() != null) {
            to.setFieldType(FieldType.fromValue(from.getFieldType().value()));
        }
        if (from.getIndex() != null) {
            to.setIndex(Integer.valueOf(from.getIndex()));
        }
        if (from.getPath() != null) {
            to.setPath(from.getPath());
        }
        if (from.isRequired() != null) {
            to.setRequired(Boolean.valueOf(from.isRequired()));
        }
        if (from.getStatus() != null) {
            to.setStatus(FieldStatus.fromValue(from.getStatus().value()));
        }
        if (from.getName() != null) {
            to.setName(from.getName());
        }
        // We can't clone so don't set value
    }

    public static FieldGroup createFieldGroupFrom(Field field, boolean withActions) {
        FieldGroup answer = new FieldGroup();
        copyField(field, answer, withActions);
        return answer;
    }

    public static ArrayList<Action> cloneFieldActions(ArrayList<Action> actions) {
        if (actions == null) {
            return null;
        }

        ArrayList<Action> n = new ArrayList<Action>();

        if (actions == null || actions.isEmpty()) {
            return n;
        }

        for (Action a : actions) {
            n.add(cloneAction(a));
        }
        return n;
    }

    public static Action cloneAction(Action action) {
        if (action == null) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String s = mapper.writeValueAsString(action);
            System.out.println(s);
            return mapper.readerFor(Action.class).readValue(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String baseFieldToString(Field f) {
        if (f == null) {
            return "";
        }
        StringBuilder tmp = new StringBuilder();
        tmp.append(" arrayDimensions=" + f.getArrayDimensions());
        tmp.append(" arraySize=" + f.getArraySize());
        tmp.append(" collectionType=" + (f.getCollectionType() != null ? f.getCollectionType().value() : null));
        tmp.append(" docId=" + f.getDocId());
        if (f.getActions() != null && f.getActions() != null) {
            if (!f.getActions().isEmpty()) {
                tmp.append(" fieldActions#=" + f.getActions().size());
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
        if (f != null && f.getName() != null) {
            tmp.append(f.getName());
        }
        tmp.append(baseFieldToString(f));
        tmp.append("]");
        return tmp.toString();
    }

    public static String toString(Field f) {
        StringBuilder tmp = new StringBuilder("Field [name=");
        if (f != null) {
            tmp.append(f.getClass().getSimpleName());
        }
        tmp.append(baseFieldToString(f));
        tmp.append("]");
        return tmp.toString();
    }
}
