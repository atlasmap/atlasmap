package io.atlasmap.v2;

import java.util.Collection;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Average extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    private Add addAction = new Add();

    @AtlasFieldActionInfo(name = "Average", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Number average(Object input) {
        if (input == null) {
            return 0;
        }
        Collection<?> inputs = ActionUtil.collection(input);
        return addAction.add(input).doubleValue() / inputs.size();
    }
}
