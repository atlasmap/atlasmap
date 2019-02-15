package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class TrimLeft extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "TrimLeft", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String trimLeft(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        int i = 0;
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        return input.substring(i);
    }
}
