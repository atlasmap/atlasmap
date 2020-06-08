package io.atlasmap.maven.test;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;

public class PaulsFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static String myCustomFieldAction(PaulsFieldActionsModel myCustomFieldAction, String input) {
        return "Paul's custom field action: " + myCustomFieldAction.getPaulsParam() + " payload: " + input;
    }

}
