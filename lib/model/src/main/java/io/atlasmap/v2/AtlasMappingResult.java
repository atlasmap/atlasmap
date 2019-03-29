package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("AtlasMappingResult")
public class AtlasMappingResult implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<TargetDocument> targetDocuments;

    protected Audits audits;

    /**
     * Gets the value of the targetDocuments property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the targetDocuments property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTargetDocuments().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TargetDocument }
     * 
     * 
     */
    public List<TargetDocument> getTargetDocuments() {
        if (targetDocuments == null) {
            targetDocuments = new ArrayList<TargetDocument>();
        }
        return this.targetDocuments;
    }

    /**
     * Gets the value of the audits property.
     * 
     * @return
     *     possible object is
     *     {@link Audits }
     *     
     */
    public Audits getAudits() {
        return audits;
    }

    /**
     * Sets the value of the audits property.
     * 
     * @param value
     *     allowed object is
     *     {@link Audits }
     *     
     */
    public void setAudits(Audits value) {
        this.audits = value;
    }

}
