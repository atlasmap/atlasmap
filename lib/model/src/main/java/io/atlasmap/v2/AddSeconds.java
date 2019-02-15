package io.atlasmap.v2;

import java.time.ZonedDateTime;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class AddSeconds extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter(type = FieldType.INTEGER)
    private Integer seconds;

    @AtlasFieldActionInfo(name = "AddSeconds", sourceType = FieldType.ANY_DATE, targetType = FieldType.ANY_DATE, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public ZonedDateTime addSeconds(ZonedDateTime input) {
        if (input == null) {
            return null;
        }
        return input.plusSeconds(getSeconds() == null ? 0L : getSeconds());
    }

    /**
     * Gets the value of the seconds property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getSeconds() {
        return seconds;
    }

    /**
     * Sets the value of the seconds property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setSeconds(Integer value) {
        this.seconds = value;
    }

}
