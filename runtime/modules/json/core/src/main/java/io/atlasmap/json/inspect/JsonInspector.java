package io.atlasmap.json.inspect;

import io.atlasmap.json.v2.JsonDocument;

public interface JsonInspector {

    JsonDocument inspect(String inspectee) throws JsonInspectionException;

}
