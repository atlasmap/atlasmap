package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class SeparateByUnderscore extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "SeparateByUnderscore", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String separateByUnderscore(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        return ActionUtil.STRING_SEPARATOR_PATTERN.matcher(input).replaceAll("_");
    }
}
