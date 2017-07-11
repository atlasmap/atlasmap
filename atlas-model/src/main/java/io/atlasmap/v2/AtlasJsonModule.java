package io.atlasmap.v2;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class AtlasJsonModule extends SimpleModule {

    private static final long serialVersionUID = -2352383379765836801L;
    private static final String NAME = "AtlasJsonModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {};

    public AtlasJsonModule() {
        super(NAME, VERSION_UTIL.version());
        addSerializer(Actions.class, new ActionsJsonSerializer());
        addDeserializer(Actions.class, new ActionsJsonDeserializer());
    }
}