package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class FileExtension extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "FileExtension", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String fileExtension(String input) {
        if (input == null) {
            return null;
        }

        int ndx = input.lastIndexOf('.');
        return ndx < 0 ? null : input.substring(ndx + 1);
    }
}
