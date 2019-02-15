package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class RemoveFileExtension extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "RemoveFileExtension", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String removeFileExtension(String input) {
        if (input == null) {
            return null;
        }

        int ndx = input.lastIndexOf('.');
        return ndx < 0 ? input : input.substring(0, ndx);
    }
}
