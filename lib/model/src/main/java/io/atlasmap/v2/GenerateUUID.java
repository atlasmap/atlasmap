package io.atlasmap.v2;

import java.util.UUID;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class GenerateUUID extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "GenerateUUID", sourceType = FieldType.NONE, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String genareteUUID(Object input) {
        return UUID.randomUUID().toString();
    }
}
