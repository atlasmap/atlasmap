package io.atlasmap.service.my;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;

public class MyFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static String myCustomFieldAction(MyFieldActionsModel action, String input) {
        return String.format("%s %s", action.getParam(), input);
    }

}
