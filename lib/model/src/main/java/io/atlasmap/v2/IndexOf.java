package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class IndexOf extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String string;

    @AtlasFieldActionInfo(name = "IndexOf", sourceType = FieldType.STRING, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Number indexOf(String input) {
        if (getString() == null) {
            throw new IllegalArgumentException("IndexOf must be specified with a string");
        }
        return input == null ? -1 : input.indexOf(getString());
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
