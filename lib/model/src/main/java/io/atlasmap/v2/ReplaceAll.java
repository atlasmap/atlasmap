package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class ReplaceAll extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String match;
    @AtlasFieldActionParameter
    private String newString;

    @AtlasFieldActionInfo(name = "ReplaceAll", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String replaceAll(String input) {
        if (match == null || match.length() == 0) {
            throw new IllegalArgumentException("ReplaceAll action must be specified with a non-empty old string");
        }

        return input == null ? null : input.replaceAll(match, newString == null ? "" : newString);
    }

    /**
     * Gets the value of the match property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMatch() {
        return match;
    }

    /**
     * Sets the value of the match property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMatch(String value) {
        this.match = value;
    }

    /**
     * Gets the value of the newString property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNewString() {
        return newString;
    }

    /**
     * Sets the value of the newString property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNewString(String value) {
        this.newString = value;
    }

}
