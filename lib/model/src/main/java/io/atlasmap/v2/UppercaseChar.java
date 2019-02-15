package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class UppercaseChar extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Uppercase", sourceType = FieldType.CHAR, targetType = FieldType.CHAR, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Character uppercaseChar(Character input) {
        return input == null ? null : String.valueOf(input).toUpperCase().charAt(0);
    }
}
