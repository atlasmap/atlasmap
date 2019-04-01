package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class LookupTable implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<LookupEntry> lookupEntry;

    protected String name;

    protected String description;

    /**
     * Gets the value of the lookupEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lookupEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLookupEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LookupEntry }
     * 
     * 
     */
    public List<LookupEntry> getLookupEntry() {
        if (lookupEntry == null) {
            lookupEntry = new ArrayList<LookupEntry>();
        }
        return this.lookupEntry;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

}
