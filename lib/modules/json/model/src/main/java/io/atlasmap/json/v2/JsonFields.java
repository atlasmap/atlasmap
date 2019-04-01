package io.atlasmap.json.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JsonFields implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<JsonField> jsonField;

    /**
     * Gets the value of the jsonField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jsonField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJsonField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JsonField }
     * 
     * 
     */
    public List<JsonField> getJsonField() {
        if (jsonField == null) {
            jsonField = new ArrayList<JsonField>();
        }
        return this.jsonField;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final JsonFields that = ((JsonFields) object);
        {
            List<JsonField> leftJsonField;
            leftJsonField = (((this.jsonField!= null)&&(!this.jsonField.isEmpty()))?this.getJsonField():null);
            List<JsonField> rightJsonField;
            rightJsonField = (((that.jsonField!= null)&&(!that.jsonField.isEmpty()))?that.getJsonField():null);
            if ((this.jsonField!= null)&&(!this.jsonField.isEmpty())) {
                if ((that.jsonField!= null)&&(!that.jsonField.isEmpty())) {
                    if (!leftJsonField.equals(rightJsonField)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.jsonField!= null)&&(!that.jsonField.isEmpty())) {
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
            List<JsonField> theJsonField;
            theJsonField = (((this.jsonField!= null)&&(!this.jsonField.isEmpty()))?this.getJsonField():null);
            if ((this.jsonField!= null)&&(!this.jsonField.isEmpty())) {
                currentHashCode += theJsonField.hashCode();
            }
        }
        return currentHashCode;
    }

}
