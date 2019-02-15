package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class Equals extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String value;

    @AtlasFieldActionInfo(name = "Equals", sourceType = FieldType.ANY, targetType = FieldType.BOOLEAN, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Boolean execute(Object input) {
        if (input == null) {
            return getValue() == null;
        }

        return input.toString().equals(getValue());
    }

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

}
