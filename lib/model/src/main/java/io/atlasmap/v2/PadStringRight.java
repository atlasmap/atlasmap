package io.atlasmap.v2;

import io.atlasmap.spi.AtlasFieldActionInfo;

public class PadStringRight extends BasePadString {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "PadStringRight", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String padStringRight(String input) {
        if (getPadCharacter() == null) {
            throw new IllegalArgumentException("PadStringRight must be specified with a padCharacter");
        }

        StringBuilder builder = new StringBuilder();
        if (input != null) {
            builder.append(input);
        }
        int count = getPadCount() == null ? 0 : getPadCount();
        for (int i = 0; i < count; i++) {
            builder.append(getPadCharacter());
        }

        return builder.toString();
    }
}
