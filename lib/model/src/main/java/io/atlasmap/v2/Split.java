package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class Split extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String delimiter;

    @AtlasFieldActionInfo(name = "Split", sourceType = FieldType.STRING, targetType = FieldType.ANY, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.ALL)
    public String[] split(String input) {
        if (getDelimiter() == null) {
            throw new IllegalArgumentException("Split must be specified with a delimiter");
        }
        return input == null ? null : input.split(getDelimiter());
    }

    /**
     * Gets the value of the delimiter property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the value of the delimiter property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDelimiter(String value) {
        this.delimiter = value;
    }

}
