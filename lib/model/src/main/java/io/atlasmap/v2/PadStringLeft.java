package io.atlasmap.v2;

import io.atlasmap.spi.AtlasFieldActionInfo;

public class PadStringLeft extends BasePadString
{
    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "PadStringLeft", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String padStringLeft(String input) {
        if (getPadCharacter() == null) {
            throw new IllegalArgumentException("PadStringLeft must be specified with a padCharacter");
        }

        StringBuilder builder = new StringBuilder();
        int count = getPadCount() == null ? 0 : getPadCount();
        for (int i = 0; i < count; i++) {
            builder.append(getPadCharacter());
        }
        if (input != null) {
            builder.append(input);
        }

        return builder.toString();
    }
}
