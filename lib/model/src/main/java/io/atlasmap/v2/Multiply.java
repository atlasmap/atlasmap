package io.atlasmap.v2;

import java.math.BigDecimal;
import java.util.Collection;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class Multiply extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "Multiply", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public Number multiply(Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = ActionUtil.collection(input);

        Number product = 1L;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (product instanceof BigDecimal) {
                    product = ((BigDecimal) product).multiply(BigDecimal.valueOf(((Number) entry).doubleValue()));
                } else if (entry instanceof BigDecimal) {
                    product = BigDecimal.valueOf(product.doubleValue()).multiply((BigDecimal) entry);
                } else if (ActionUtil.requiresDoubleResult(product) || ActionUtil.requiresDoubleResult(entry)) {
                    product = product.doubleValue() * ((Number) entry).doubleValue();
                } else {
                    product = product.longValue() * ((Number) entry).longValue();
                }
            } else {
                throw new IllegalArgumentException(ActionUtil.COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return product;
    }
}
