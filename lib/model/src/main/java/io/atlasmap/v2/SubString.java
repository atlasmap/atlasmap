package io.atlasmap.v2;

import io.atlasmap.spi.AtlasFieldActionInfo;

public class SubString extends BaseSubString {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "SubString", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String subString(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        if (getStartIndex() == null || getStartIndex() < 0) {
            throw new IllegalArgumentException("SubString action must be specified with a positive startIndex");
        }

        return subString(input, getStartIndex(), getEndIndex());
    }
}
