package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Audits implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<Audit> audit;

    /**
     * Gets the value of the audit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the audit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAudit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Audit }
     * 
     * 
     */
    public List<Audit> getAudit() {
        if (audit == null) {
            audit = new ArrayList<Audit>();
        }
        return this.audit;
    }

}
