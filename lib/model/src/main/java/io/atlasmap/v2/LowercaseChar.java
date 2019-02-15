package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class LowercaseChar extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Lowercase", sourceType = FieldType.CHAR, targetType = FieldType.CHAR, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Character lowercaseChar(Character input) {
        return input == null ? null : String.valueOf(input).toLowerCase().charAt(0);
    }
}
