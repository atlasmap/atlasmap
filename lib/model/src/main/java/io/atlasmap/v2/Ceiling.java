package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Ceiling extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Ceiling", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Number ceiling(Number input) {
        return input == null ? 0L : (long)Math.ceil(input.doubleValue());
    }
}
