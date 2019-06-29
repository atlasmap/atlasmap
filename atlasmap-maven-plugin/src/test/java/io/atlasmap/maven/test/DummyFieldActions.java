package io.atlasmap.maven.test;

import java.util.List;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.CustomAction;

public class DummyFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static Number dummy(Dummy action, List<Object> input) {
        return 0;
    }

}
