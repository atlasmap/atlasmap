package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class Append extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String suffix;

    @AtlasFieldActionInfo(name = "Append", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String append(Object input) {
        String suffix = getSuffix();
        if (input == null && suffix == null) {
            return null;
        }
        if (suffix == null) {
            return input.toString();
        }
        return input == null ? suffix : input.toString().concat(suffix);
    }

    /**
     * Gets the value of the suffix property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the value of the suffix property.
     *
     * @param suffix
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

}
