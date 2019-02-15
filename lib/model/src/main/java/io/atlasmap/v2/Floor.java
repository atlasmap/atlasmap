package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Floor extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Floor", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Number floor(Number input) {
        return input == null ? 0L : (long)Math.floor(input.doubleValue());
    }
}
