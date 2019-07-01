package io.atlasmap.maven.test;

import java.util.Arrays;
import java.util.List;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.CustomAction;

public class DummyFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static Number dummyOneToOne(DummyOneToOne action, String input) {
        return 0;
    }

    @AtlasActionProcessor
    public static List<Number> dummyOneToMany(DummyOneToMany action, String input) {
        return Arrays.asList(0);
    }

    @AtlasActionProcessor
    public static Number dummyManyToOne(DummyManyToOne action, List<String> input) {
        return 0;
    }

    @AtlasActionProcessor
    public static Number dummyZeroToOne(DummyZeroToOne action) {
        return 0;
    }

}
