package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("StringMap")
public class StringMap implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<StringMapEntry> stringMapEntry;

    /**
     * Gets the value of the stringMapEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stringMapEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStringMapEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StringMapEntry }
     * 
     * 
     */
    public List<StringMapEntry> getStringMapEntry() {
        if (stringMapEntry == null) {
            stringMapEntry = new ArrayList<StringMapEntry>();
        }
        return this.stringMapEntry;
    }

}
