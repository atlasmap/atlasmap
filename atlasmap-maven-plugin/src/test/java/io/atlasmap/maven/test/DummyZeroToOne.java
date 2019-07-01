package io.atlasmap.maven.test;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.atlasmap.v2.Action;
import io.atlasmap.v2.AtlasActionProperty;
import io.atlasmap.v2.FieldType;

public class DummyZeroToOne extends Action implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonPropertyDescription("The dummy action parameter")
    @AtlasActionProperty(title = "Dummy parameter", type = FieldType.STRING)
    public String dummyParameter = "dummy";

}
