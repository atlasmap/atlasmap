package io.atlasmap.xml.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class XmlFields implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<XmlField> xmlField;

    /**
     * Gets the value of the xmlField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xmlField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXmlField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XmlField }
     * 
     * 
     */
    public List<XmlField> getXmlField() {
        if (xmlField == null) {
            xmlField = new ArrayList<XmlField>();
        }
        return this.xmlField;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final XmlFields that = ((XmlFields) object);
        {
            List<XmlField> leftXmlField;
            leftXmlField = (((this.xmlField!= null)&&(!this.xmlField.isEmpty()))?this.getXmlField():null);
            List<XmlField> rightXmlField;
            rightXmlField = (((that.xmlField!= null)&&(!that.xmlField.isEmpty()))?that.getXmlField():null);
            if ((this.xmlField!= null)&&(!this.xmlField.isEmpty())) {
                if ((that.xmlField!= null)&&(!that.xmlField.isEmpty())) {
                    if (!leftXmlField.equals(rightXmlField)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.xmlField!= null)&&(!that.xmlField.isEmpty())) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        {
            currentHashCode = (currentHashCode* 31);
            List<XmlField> theXmlField;
            theXmlField = (((this.xmlField!= null)&&(!this.xmlField.isEmpty()))?this.getXmlField():null);
            if ((this.xmlField!= null)&&(!this.xmlField.isEmpty())) {
                currentHashCode += theXmlField.hashCode();
            }
        }
        return currentHashCode;
    }

}
