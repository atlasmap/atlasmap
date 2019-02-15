package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Length extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Length", sourceType = FieldType.ANY, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Integer length(Object input) {
        return input == null ? -1 : input.toString().length();
    }
}
