package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class IsNull extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "IsNull", sourceType = FieldType.ANY, targetType = FieldType.BOOLEAN, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Boolean isNull(Object input) {
        return input == null;
    }
}
