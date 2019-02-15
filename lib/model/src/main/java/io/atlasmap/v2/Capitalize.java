package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Capitalize extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Capitalize", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String capitalize(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        if (input.length() == 1) {
            return String.valueOf(input.charAt(0)).toUpperCase();
        }
        return String.valueOf(input.charAt(0)).toUpperCase() + input.substring(1);
    }
}
