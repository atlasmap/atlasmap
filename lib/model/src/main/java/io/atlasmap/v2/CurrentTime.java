package io.atlasmap.v2;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;

public class CurrentTime extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionInfo(name = "CurrentTime", sourceType = FieldType.NONE, targetType = FieldType.DATE_TIME, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public ZonedDateTime currentTime(Object input) {
        return LocalTime.now().atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
    }
}
