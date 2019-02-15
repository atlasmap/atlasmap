package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class Prepend extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String prefix;

    @AtlasFieldActionInfo(name = "Prepend", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String prepend(Object input) {
        if (input == null && getPrefix() == null) {
            return null;
        }
        if (getPrefix() == null) {
            return input.toString();
        }
        return input == null ? getPrefix() : getPrefix().concat(input.toString());
    }

    /**
     * Gets the value of the prefix property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the value of the prefix property.
     *
     * @param prefix
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
