package io.atlasmap.v2;

import java.time.ZonedDateTime;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class DayOfMonth extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "DayOfMonth", sourceType = FieldType.ANY_DATE, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Integer dayOfMonth(ZonedDateTime input) {
        return input == null ? null : input.getDayOfMonth();
    }
}
