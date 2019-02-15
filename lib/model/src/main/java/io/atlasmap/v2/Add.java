package io.atlasmap.v2;

import java.math.BigDecimal;
import java.util.Collection;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Add extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Add", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Number add(Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = ActionUtil.collection(input);

        Number sum = 0L;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (sum instanceof BigDecimal) {
                    sum = ((BigDecimal) sum).add(BigDecimal.valueOf(((Number) entry).doubleValue()));
                } else if (entry instanceof BigDecimal) {
                    sum = BigDecimal.valueOf(sum.doubleValue()).add((BigDecimal) entry);
                } else if (ActionUtil.requiresDoubleResult(sum) || ActionUtil.requiresDoubleResult(entry)) {
                    sum = sum.doubleValue() + ((Number) entry).doubleValue();
                } else {
                    sum = sum.longValue() + ((Number) entry).longValue();
                }
            } else {
                throw new IllegalArgumentException(ActionUtil.COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return sum;
    }
}
