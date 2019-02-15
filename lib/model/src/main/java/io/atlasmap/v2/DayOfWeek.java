package io.atlasmap.v2;

import java.time.ZonedDateTime;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class DayOfWeek extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "DayOfWeek", sourceType = FieldType.ANY_DATE, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Integer dayOfWeek(ZonedDateTime input) {
        return input == null ? null : input.getDayOfWeek().getValue();
    }
}
