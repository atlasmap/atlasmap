package io.atlasmap.v2;

import java.math.BigDecimal;
import java.util.Collection;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Divide extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Divide", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Number divide(Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = ActionUtil.collection(input);

        Number quotient = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (quotient == null) {
                    quotient = (Number) entry;
                } else if (quotient instanceof BigDecimal) {
                    quotient = ((BigDecimal) quotient).divide(BigDecimal.valueOf(((Number) entry).doubleValue()));
                } else if (entry instanceof BigDecimal) {
                    quotient = BigDecimal.valueOf(quotient.doubleValue()).divide((BigDecimal) entry);
                } else {
                    quotient = quotient.doubleValue() / ((Number) entry).doubleValue();
                }
            } else {
                throw new IllegalArgumentException(ActionUtil.COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return quotient;
    }
}
