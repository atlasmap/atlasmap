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
        // We can't clone so don't set value
    }

    public static FieldGroup createFieldGroupFrom(Field field) {
        FieldGroup answer = new FieldGroup();
        copyField(field, answer, true);
        return answer;
    }

    public static Actions cloneFieldActions(Actions actions) {
        if (actions == null) {
            return null;
        }

        Actions n = new Actions();

        if (actions.getActions() == null || actions.getActions().isEmpty()) {
            return n;
        }

        for (Action a : actions.getActions()) {
            n.getActions().add(cloneAction(a));
        }
        return n;
    }

    // FIXME: https://github.com/atlasmap/atlasmap/issues/536
    public static Action cloneAction(Action action) {
        if (action == null) {
            return null;
        }

        if (action instanceof AbsoluteValue) {
            return new AbsoluteValue();
        }
        if (action instanceof Add) {
            return new Add();
        }
        if (action instanceof AddDays) {
            AddDays addDays = new AddDays();
            addDays.setDays(((AddDays) action).getDays());
            return addDays;
        }
        if (action instanceof AddSeconds) {
            AddSeconds addSeconds = new AddSeconds();
            addSeconds.setSeconds(((AddSeconds) action).getSeconds());
            return addSeconds;
        }
        if (action instanceof Append) {
            Append append = new Append();
            append.setString(((Append) action).getString());
            return append;
        }
        if (action instanceof Average) {
            return new Average();
        }
        if (action instanceof Camelize) {
            return new Camelize();
        }
        if (action instanceof Capitalize) {
            return new Capitalize();
        }
        if (action instanceof Ceiling) {
            return new Ceiling();
        }
        if (action instanceof Concatenate) {
            Concatenate concat = new Concatenate();
            concat.setDelimiter(((Concatenate) action).getDelimiter());
            return concat;
        }
        if (action instanceof Contains) {
            Contains contains = new Contains();
            contains.setValue(((Contains) action).getValue());
            return contains;
        }
        if (action instanceof ConvertAreaUnit) {
            ConvertAreaUnit cau = new ConvertAreaUnit();
            cau.setFromUnit(((ConvertAreaUnit) action).getFromUnit());
            cau.setToUnit(((ConvertAreaUnit) action).getToUnit());
            return cau;
        }
        if (action instanceof ConvertDistanceUnit) {
            ConvertDistanceUnit cdu = new ConvertDistanceUnit();
            cdu.setFromUnit(((ConvertDistanceUnit) action).getFromUnit());
            cdu.setToUnit(((ConvertDistanceUnit) action).getToUnit());
            return cdu;
        }
        if (action instanceof ConvertMassUnit) {
            ConvertMassUnit cmu = new ConvertMassUnit();
            cmu.setFromUnit(((ConvertMassUnit) action).getFromUnit());
            cmu.setToUnit(((ConvertMassUnit) action).getToUnit());
            return cmu;
        }
        if (action instanceof ConvertVolumeUnit) {
            ConvertVolumeUnit cvu = new ConvertVolumeUnit();
            cvu.setFromUnit(((ConvertVolumeUnit) action).getFromUnit());
            cvu.setToUnit(((ConvertVolumeUnit) action).getToUnit());
            return cvu;
        }
        if (action instanceof CurrentDate) {
            return new CurrentDate();
        }
        if (action instanceof CurrentDateTime) {
            return new CurrentDateTime();
        }
        if (action instanceof CurrentTime) {
            return new CurrentTime();
        }
        if (action instanceof CustomAction) {
            CustomAction a = new CustomAction();
            if (((CustomAction) action).getClassName() != null) {
                a.setClassName(((CustomAction) action).getClassName());
            }
            if (((CustomAction) action).getMethodName() != null) {
                a.setMethodName(((CustomAction) action).getMethodName());
            }
            if (((CustomAction) action).getInputFieldType() != null) {
                a.setInputFieldType(FieldType.fromValue(((CustomAction) action).getInputFieldType().value()));
            }
            if (((CustomAction) action).getOutputFieldType() != null) {
                a.setOutputFieldType(FieldType.fromValue(((CustomAction) action).getOutputFieldType().value()));
            }
            return a;
        }
        if (action instanceof DayOfMonth) {
            return new DayOfMonth();
        }
        if (action instanceof DayOfWeek) {
            return new DayOfWeek();
        }
        if (action instanceof DayOfYear) {
            return new DayOfYear();
        }
        if (action instanceof Divide) {
            return new Divide();
        }
        if (action instanceof EndsWith) {
            EndsWith endsWith = new EndsWith();
            endsWith.setString(((EndsWith) action).getString());
            return endsWith;
        }
        if (action instanceof Equals) {
            Equals equals = new Equals();
            equals.setValue(((Equals) action).getValue());
            return equals;
        }
        if (action instanceof Format) {
            Format format = new Format();
            format.setTemplate(((Format) action).getTemplate());
            return format;
        }
        if (action instanceof FileExtension) {
            return new FileExtension();
        }
        if (action instanceof Floor) {
            return new Floor();
        }
        if (action instanceof GenerateUUID) {
            return new GenerateUUID();
        }
        if (action instanceof IndexOf) {
            IndexOf indexOf = new IndexOf();
            indexOf.setString(((IndexOf) action).getString());
            return indexOf;
        }
        if (action instanceof IsNull) {
            return new IsNull();
        }
        if (action instanceof ItemAt) {
            ItemAt itemAt = new ItemAt();
            itemAt.setIndex(((ItemAt) action).getIndex());
            return itemAt;
        }
        if (action instanceof LastIndexOf) {
            LastIndexOf lastIndexOf = new LastIndexOf();
            lastIndexOf.setString(((LastIndexOf) action).getString());
            return lastIndexOf;
        }
        if (action instanceof Length) {
            return new Length();
        }
        if (action instanceof Lowercase) {
            return new Lowercase();
        }
        if (action instanceof LowercaseChar) {
            return new LowercaseChar();
        }
        if (action instanceof Maximum) {
            return new Maximum();
        }
        if (action instanceof Minimum) {
            return new Minimum();
        }
        if (action instanceof Multiply) {
            return new Multiply();
        }
        if (action instanceof Normalize) {
            return new Normalize();
        }
        if (action instanceof PadStringLeft) {
            PadStringLeft a = new PadStringLeft();
            if (((PadStringLeft) action).getPadCharacter() != null) {
                a.setPadCharacter(((PadStringLeft) action).getPadCharacter());
            }
            if (((PadStringLeft) action).getPadCount() != null) {
                a.setPadCount(Integer.valueOf(((PadStringLeft) action).getPadCount()));
            }
            return a;
        }
        if (action instanceof PadStringRight) {
            PadStringRight a = new PadStringRight();
            if (((PadStringRight) action).getPadCharacter() != null) {
                a.setPadCharacter(((PadStringRight) action).getPadCharacter());
            }
            if (((PadStringRight) action).getPadCount() != null) {
                a.setPadCount(((PadStringRight) action).getPadCount());
            }
            return a;
        }
        if (action instanceof Prepend) {
            Prepend append = new Prepend();
            append.setString(((Prepend) action).getString());
            return append;
        }
        if (action instanceof RemoveFileExtension) {
            return new RemoveFileExtension();
        }
        if (action instanceof ReplaceAll) {
            ReplaceAll a = new ReplaceAll();
            if (((ReplaceAll) action).getMatch() != null) {
                a.setMatch(((ReplaceAll) action).getMatch());
            }
            if (((ReplaceAll) action).getNewString() != null) {
                a.setNewString(((ReplaceAll) action).getNewString());
            }
            return a;
        }
        if (action instanceof ReplaceFirst) {
            ReplaceFirst a = new ReplaceFirst();
            if (((ReplaceFirst) action).getMatch() != null) {
                a.setMatch(((ReplaceFirst) action).getMatch());
            }
            if (((ReplaceFirst) action).getNewString() != null) {
                a.setNewString(((ReplaceFirst) action).getNewString());
            }
            return a;
        }
        if (action instanceof Round) {
            return new Round();
        }
        if (action instanceof SeparateByDash) {
            return new SeparateByDash();
        }
        if (action instanceof SeparateByUnderscore) {
            return new SeparateByUnderscore();
        }
        if (action instanceof Split) {
            Split split = new Split();
            split.setDelimiter(((Split) action).getDelimiter());
            return split;
        }
        if (action instanceof StartsWith) {
            StartsWith startsWith = new StartsWith();
            startsWith.setString(((StartsWith) action).getString());
            return startsWith;
        }
        if (action instanceof SubString) {
            SubString a = new SubString();
            if (((SubString) action).getStartIndex() != null) {
                a.setStartIndex(((SubString) action).getStartIndex());
            }
            if (((SubString) action).getEndIndex() != null) {
                a.setEndIndex(((SubString) action).getEndIndex());
            }
            return a;
        }
        if (action instanceof SubStringAfter) {
            SubStringAfter a = new SubStringAfter();
            if (((SubStringAfter) action).getStartIndex() != null) {
                a.setStartIndex(((SubStringAfter) action).getStartIndex());
            }
            if (((SubStringAfter) action).getEndIndex() != null) {
                a.setEndIndex(((SubStringAfter) action).getEndIndex());
            }
            return a;
        }
        if (action instanceof SubStringBefore) {
            SubStringBefore a = new SubStringBefore();
            if (((SubStringBefore) action).getStartIndex() != null) {
                a.setStartIndex(((SubStringBefore) action).getStartIndex());
            }
            if (((SubStringBefore) action).getEndIndex() != null) {
                a.setEndIndex(((SubStringBefore) action).getEndIndex());
            }
            return a;
        }
        if (action instanceof Subtract) {
            return new Subtract();
        }
        if (action instanceof Trim) {
            return new Trim();
        }
        if (action instanceof TrimLeft) {
            return new TrimLeft();
        }
        if (action instanceof TrimRight) {
            return new TrimRight();
        }
        if (action instanceof Uppercase) {
            return new Uppercase();
        }
        if (action instanceof UppercaseChar) {
            return new UppercaseChar();
        }
        return null;
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
        if (f.getActions() != null && f.getActions().getActions() != null) {
            if (!f.getActions().getActions().isEmpty()) {
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
