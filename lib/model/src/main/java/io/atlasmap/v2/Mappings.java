package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Mappings implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<BaseMapping> mapping;

    /**
     * Gets the value of the mapping property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapping property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapping().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BaseMapping }
     * 
     * @return A list of {@link BaseMapping}
     */
    public List<BaseMapping> getMapping() {
        if (mapping == null) {
            mapping = new ArrayList<BaseMapping>();
        }
        return this.mapping;
    }

}
