package io.atlasmap.mock.v2;

import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

@AtlasFieldActionInfo(name = "DayOfWeek", sourceType = FieldType.INTEGER, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
public class DayOfWeekInteger extends Action {

    private static final long serialVersionUID = 6401903284974777325L;

    private Integer integerValue;

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }
   
    @Override
    public String getDisplayName() {
        return "DayOfWeek";
    }
}
