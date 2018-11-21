package io.atlasmap.service.my;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

public class MyFieldActions implements AtlasFieldAction {

    @AtlasFieldActionInfo(name = "MyCustomFieldAction", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String myCustomFieldAction(Action action, String input) {
        return "Hello " + input;
    }

}
