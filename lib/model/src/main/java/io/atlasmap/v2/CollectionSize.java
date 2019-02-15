package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class CollectionSize extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "CollectionSize", sourceType = FieldType.ANY, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Integer collectionSize(Object input) {
        if (input == null) {
            return 0;
        }
        Object[] array = ActionUtil.collection(input).toArray(new Object[0]);
        return array.length;
    }
}
