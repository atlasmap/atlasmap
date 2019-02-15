package io.atlasmap.v2;

import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class SubStringBefore extends BaseSubString {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String match;

    @AtlasFieldActionInfo(name = "SubStringBefore", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public String subStringBefore(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        if (getStartIndex() == null || getStartIndex() < 0 || getMatch() == null || (getEndIndex() != null && getEndIndex() < getStartIndex())) {
            throw new IllegalArgumentException("SubStringBefore action must be specified with a positive startIndex and a string to match");
        }

        int idx = input.indexOf(getMatch());
        if (idx < 0) {
            return input;
        }

        return subString(input.substring(0, idx), getStartIndex(), getEndIndex());
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

}
