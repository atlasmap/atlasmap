package io.atlasmap.v2;

import java.math.BigDecimal;
import java.util.Collection;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Maximum extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Maximum", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Number maximum(Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = ActionUtil.collection(input);

        Number max = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (max instanceof BigDecimal && entry instanceof BigDecimal) {
                    max = ((BigDecimal) entry).max((BigDecimal)max);
                } else if (max == null || ((Number) entry).doubleValue() > max.doubleValue()) {
                    max = (Number) entry;
                }
            } else {
                throw new IllegalArgumentException(ActionUtil.COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return max;
    }
}
