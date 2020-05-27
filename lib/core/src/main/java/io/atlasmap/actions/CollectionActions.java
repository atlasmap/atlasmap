package io.atlasmap.actions;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.CopyTo;
import io.atlasmap.v2.FieldType;

public class CollectionActions implements AtlasFieldAction {

    @AtlasActionProcessor(sourceType = FieldType.ANY)
    public static Object[] copyTo(CopyTo action, Object input) {
        // This a noop processor. Nevertheless it's signature is important to signal that's a one-to-many action.
        // It's behavior is implemented directly into DefaultAtlasContext
        return new Object[]{};
    }

}
