package io.atlasmap.v2;

import java.math.BigDecimal;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class AbsoluteValue extends Action implements AtlasFieldAction
{

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "AbsoluteValue", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Number absoluteValue(Number input) {
        if (input == null) {
            return 0;
        }
        if (input instanceof BigDecimal) {
            return ((BigDecimal) input).abs();
        }
        if (ActionUtil.requiresDoubleResult(input)) {
            return Math.abs(input.doubleValue());
        }
        return Math.abs(input.longValue());
    }
}
