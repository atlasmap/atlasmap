package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class EndsWith extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String string;

    @AtlasFieldActionInfo(name = "EndsWith", sourceType = FieldType.STRING, targetType = FieldType.BOOLEAN, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Boolean endsWith(String input) {
        if (getString() == null) {
            throw new IllegalArgumentException("EndsWith must be specified with a string");
        }

        return input == null ? false : input.endsWith(getString());
    }

    /**
     * Gets the value of the string property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getString() {
        return string;
    }

    /**
     * Sets the value of the string property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setString(String value) {
        this.string = value;
    }

}
