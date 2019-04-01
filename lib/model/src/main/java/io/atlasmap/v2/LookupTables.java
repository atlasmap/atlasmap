package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class LookupTables implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<LookupTable> lookupTable;

    /**
     * Gets the value of the lookupTable property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lookupTable property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLookupTable().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LookupTable }
     * 
     * 
     */
    public List<LookupTable> getLookupTable() {
        if (lookupTable == null) {
            lookupTable = new ArrayList<LookupTable>();
        }
        return this.lookupTable;
    }

}
