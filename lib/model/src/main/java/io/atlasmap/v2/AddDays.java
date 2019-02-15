package io.atlasmap.v2;

import java.time.ZonedDateTime;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class AddDays extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter(type = FieldType.INTEGER)
    private Integer days;

    @AtlasFieldActionInfo(name = "AddDays", sourceType = FieldType.ANY_DATE, targetType = FieldType.ANY_DATE, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public ZonedDateTime addDays(ZonedDateTime input) {
        if (input == null) {
            return null;
        }
        return input.plusDays(getDays() == null ? 0L : getDays());
    }

    /**
     * Gets the value of the days property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getDays() {
        return days;
    }

    /**
     * Sets the value of the days property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setDays(Integer value) {
        this.days = value;
    }

}
