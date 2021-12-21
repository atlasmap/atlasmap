/*
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
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The factory class for common AtlasMap model objects.
 */
public class AtlasModelFactory {

    /** The path expression for the generated field. */
    public static final String GENERATED_PATH = "$ATLASMAP";

    private AtlasModelFactory() {
    }

    /**
     * Creates a {@link Mapping} model object.
     * @param <T> type
     * @return {@link Mapping} model object
     */
    public static <T extends BaseMapping> T createMapping() {
        return createMapping(MappingType.MAP);
    }

    /**
     * Creates a {@link Mapping} model object.
     * @param <T> type
     * @param type type
     * @return {@link Mapping} model object
     */
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

    /**
     * Creates an {@link AtlasMapping} model object.
     * @return {@link AtlasMapping} model object
     */
    public static AtlasMapping createAtlasMapping() {
        AtlasMapping mapping = new AtlasMapping();
        mapping.setMappings(new Mappings());
        mapping.setProperties(new Properties());
        mapping.setLookupTables(new LookupTables());
        return mapping;
    }

    /**
     * Creates a {@link Collection} model object.
     * @return {@link Collection} model object
     */
    public static Collection createCollection() {
        Collection collectionMapping = new Collection();
        collectionMapping.setMappings(new Mappings());
        collectionMapping.setMappingType(MappingType.COLLECTION);
        return collectionMapping;
    }

    /**
     * Creates a {@link MockDocument}.
     * @return {@link MockDocument}
     */
    public static MockDocument createMockDocument() {
        MockDocument mockDocument = new MockDocument();
        mockDocument.setFields(new Fields());
        return mockDocument;
    }

    /**
     * Creates a {@link MockField}.
     * @return {@link MockField}
     */
    public static MockField createMockField() {
        return new MockField();
    }

    /**
     * Creates a {@link PropertyField}.
     * @return {@link PropertyField}
     */
    public static PropertyField createPropertyField() {
        return new PropertyField();
    }

    /**
     * Clones a {@link BaseMapping}.
     * @param baseMapping {@link BaseMapping} to clone
     * @param deepClone true to deep clone
     * @return cloned {@link BaseMapping}
     */
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

    /**
     * Clones a {@link Field}.
     * @param f {@link Field} to clone
     * @return cloned {@link Field}
     */
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

    /**
     * Clones a {@link Field} to a {@link SimpleField}.
     * @param field {@link Field} to clone
     * @return cloned {@link SimpleField}
     */
    public static SimpleField cloneFieldToSimpleField(Field field) {
        if (field == null) {
            return null;
        }

        SimpleField f = new SimpleField();
        copyField(field, f, true);
        return f;
    }

    /**
     * Copies {@link Field} properties.
     * @param from from
     * @param to to
     * @param withActions true to copy actions
     */
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

    /**
     * Creates a {@link FieldGroup} from the {@link Field} passed in.
     * @param field a source {@link Field} to copy from
     * @param withActions true to copy actions
     * @return created {@link FieldGroup}
     */
    public static FieldGroup createFieldGroupFrom(Field field, boolean withActions) {
        FieldGroup answer = new FieldGroup();
        copyField(field, answer, withActions);
        return answer;
    }

    /**
     * Clones a list of {@link Action}.
     * @param actions a list of {@link Action} to clone
     * @return cloned
     */
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

    /**
     * Clones an {@link Action}.
     * @param action {@link Action}
     * @return cloned
     */
    public static Action cloneAction(Action action) {
        if (action == null) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper()
                .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES);
            String s = mapper.writeValueAsString(action);
            System.out.println(s);
            return mapper.readerFor(Action.class).readValue(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a string description of the {@link Field}.
     * @param f field
     * @return string description
     */
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

    /**
     * Gets a string description of the {@link PropertyField}.
     * @param f property field
     * @return string description
     */
    public static String toString(PropertyField f) {
        StringBuilder tmp = new StringBuilder("PropertyField [name=");
        if (f != null && f.getName() != null) {
            tmp.append(f.getName());
        }
        tmp.append(baseFieldToString(f));
        tmp.append("]");
        return tmp.toString();
    }

    /**
     * Gets a string description of the {@link Field}.
     * @param f field
     * @return string description
     */
    public static String toString(Field f) {
        StringBuilder tmp = new StringBuilder("Field [name=");
        if (f != null) {
            tmp.append(f.getClass().getSimpleName());
        }
        tmp.append(baseFieldToString(f));
        tmp.append("]");
        return tmp.toString();
    }

    /**
     * Wraps the passed in object with a {@link Field}.
     * @param val object
     * @return wrapped {@link Field}
     */
    public static Field wrapWithField(Object val) {
        return wrapWithField(val, "/");
    }

    /**
     * Wraps the passed in object with a {@link Field}.
     * @param val object
     * @param parentPath parent path
     * @return wrapped {@link Field}
     */
    public static Field wrapWithField(Object val, String parentPath) {
        if (val instanceof java.util.Collection) {
            Object[] collection = ((java.util.Collection)val).toArray();
            FieldGroup group = new FieldGroup();
            group.setPath(parentPath + GENERATED_PATH);
            for (int i=0; i<collection.length; i++) {
                Field sub = wrapWithField(collection[i], group.getPath());
                sub.setPath(sub.getPath() + "[" + i + "]");
                group.getField().add(sub);
            }
            return group;
        }
        SimpleField answer = new SimpleField();
        answer.setPath(GENERATED_PATH);
        answer.setValue(val);
        return answer;
    }

    /**
     * Unwraps the field and return a value. It returns a {@link List} if it's a collection.
     * @param f field
     * @return value
     */
    public static Object unwrapField(Field f) {
        if (f == null) {
            return null;
        }
        if (f instanceof FieldGroup) {
            List<Object> l = new LinkedList<>();
            for (Field sub : ((FieldGroup)f).getField()) {
                l.add(unwrapField(sub));
            }
            return l;
        } else {
            return f.getValue();
        }
    }

}
