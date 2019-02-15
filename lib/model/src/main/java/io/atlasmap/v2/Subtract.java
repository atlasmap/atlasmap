package io.atlasmap.v2;

import java.math.BigDecimal;
import java.util.Collection;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Subtract extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Subtract", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Number subtract(Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = ActionUtil.collection(input);

        Number difference = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (difference == null) {
                    difference = (Number) entry;
                } else if (difference instanceof BigDecimal) {
                    difference = ((BigDecimal) difference).subtract(BigDecimal.valueOf(((Number) entry).doubleValue()));
                } else if (entry instanceof BigDecimal) {
                    difference = BigDecimal.valueOf(difference.doubleValue()).subtract((BigDecimal) entry);
                } else if (ActionUtil.requiresDoubleResult(difference) || ActionUtil.requiresDoubleResult(entry)) {
                    difference = difference.doubleValue() - ((Number) entry).doubleValue();
                } else {
                    difference = difference.longValue() - ((Number) entry).longValue();
                }
            } else {
                throw new IllegalArgumentException(ActionUtil.COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return difference;
    }
}
