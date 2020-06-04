package io.atlasmap.service.my;

import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

public class MyFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static String myCustomFieldAction(MyFieldActionsModel action, String input) {
        return "Hello " + input;
    }

}
