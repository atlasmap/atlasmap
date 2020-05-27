package io.atlasmap.v2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class CopyTo extends Action implements Serializable {

    private static final long serialVersionUID = 1L;

    private String index;

    public CopyTo(String index) {
        this.index = index;
    }

    public CopyTo() {
    }

    /**
     * Gets the value of the string property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public String getIndex() {
        return index;
    }

    /**
     * Sets the value of the string property.
     *
     * @param index allowed object is
     *              {@link Integer }
     */
    @JsonPropertyDescription("The comma-separated indexes of the item in target collections.")
    @AtlasActionProperty(title = "Indexes", type = FieldType.STRING)
    public void setIndex(String index) {
        this.index = index;
    }

    @JsonIgnore
    public List<Integer> getIndexes() {
        // indexes coming from the ui are 1-based while we work with 0-based indexes
        return Arrays.stream(index.trim().split(",")).map(i -> Integer.parseInt(i) - 1).collect(Collectors.toList());
    }
}
