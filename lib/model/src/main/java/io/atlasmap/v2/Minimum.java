package io.atlasmap.v2;

import java.math.BigDecimal;
import java.util.Collection;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Minimum  extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Minimum", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Number minimum(Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = ActionUtil.collection(input);

        Number min = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (min instanceof BigDecimal && entry instanceof BigDecimal) {
                    min = ((BigDecimal) entry).min((BigDecimal)min);
                } else if (min == null || ((Number) entry).doubleValue() < min.doubleValue()) {
                    min = (Number) entry;
                }
            } else {
                throw new IllegalArgumentException(ActionUtil.COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return min;
    }
}
