package io.atlasmap.v2;

import java.util.Collection;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class Concatenate extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String delimiter;

    @AtlasFieldActionInfo(name = "Concatenate", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public String concatenate(Object input) {
        if (input == null) {
            return null;
        }

        String delim = getDelimiter() == null ? "" : getDelimiter();

        Collection<?> inputs = ActionUtil.collection(input);

        StringBuilder builder = new StringBuilder();
        for (Object entry : inputs) {
            if (builder.length() > 0) {
                builder.append(delim);
            }
            if (entry != null) {
                builder.append(entry.toString());
            }
        }

        return builder.toString();
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
