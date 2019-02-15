package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class TrimRight extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "TrimRight", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String trimRight(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        int i = input.length() - 1;
        while (i >= 0 && Character.isWhitespace(input.charAt(i))) {
            i--;
        }
        return input.substring(0, i + 1);
    }
}
