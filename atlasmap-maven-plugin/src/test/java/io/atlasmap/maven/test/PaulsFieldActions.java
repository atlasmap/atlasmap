package io.atlasmap.maven.test;

import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.v2.*;

public class PaulsFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor(sourceType = FieldType.STRING)
    public static String myCustomFieldAction(PaulsFieldActionsModel myCustomFieldAction, String input) {
        return myCustomFieldAction.getPaulsParam() + input;
    }

}
