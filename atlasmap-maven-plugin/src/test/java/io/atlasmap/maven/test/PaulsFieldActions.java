package io.atlasmap.maven.test;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.AtlasActionProperty;
import io.atlasmap.v2.FieldType;

public class PaulsFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static String myCustomFieldAction(PaulsFieldActionsModel myCustomFieldAction, String input) {
        return "Paul's custom field action: " + myCustomFieldAction.getPaulsParam() + " payload: " + input;
    }

}
