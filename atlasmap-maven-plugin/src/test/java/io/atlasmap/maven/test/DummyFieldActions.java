package io.atlasmap.maven.test;

import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

public class DummyFieldActions implements AtlasFieldAction {

    @AtlasFieldActionInfo(name = "DummyFieldAction", sourceType = FieldType.BYTE, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.MAP, targetCollectionType = CollectionType.ARRAY)
    public static Number dummy(Action action, Object input) {
        return 0;
    }

}
