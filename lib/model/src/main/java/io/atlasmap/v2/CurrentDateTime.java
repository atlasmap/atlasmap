package io.atlasmap.v2;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class CurrentDateTime extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "CurrentDateTime", sourceType = FieldType.NONE, targetType = FieldType.ANY_DATE, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public ZonedDateTime currentDateTime(Object input) {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault());
    }
}
