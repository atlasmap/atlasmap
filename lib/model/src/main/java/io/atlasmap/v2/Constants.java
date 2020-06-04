package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Constants implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<Constant> constant;

    /**
     * Gets the value of the constant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the constant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConstant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Constant }
     * 
     * @return A list of {@link Constant}
     */
    public List<Constant> getConstant() {
        if (constant == null) {
            constant = new ArrayList<Constant>();
        }
        return this.constant;
    }

}
