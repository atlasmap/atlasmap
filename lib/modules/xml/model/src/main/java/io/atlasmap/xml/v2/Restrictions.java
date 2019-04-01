package io.atlasmap.xml.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Restrictions implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<Restriction> restriction;

    /**
     * Gets the value of the restriction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the restriction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRestriction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Restriction }
     * 
     * 
     */
    public List<Restriction> getRestriction() {
        if (restriction == null) {
            restriction = new ArrayList<Restriction>();
        }
        return this.restriction;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final Restrictions that = ((Restrictions) object);
        {
            List<Restriction> leftRestriction;
            leftRestriction = (((this.restriction!= null)&&(!this.restriction.isEmpty()))?this.getRestriction():null);
            List<Restriction> rightRestriction;
            rightRestriction = (((that.restriction!= null)&&(!that.restriction.isEmpty()))?that.getRestriction():null);
            if ((this.restriction!= null)&&(!this.restriction.isEmpty())) {
                if ((that.restriction!= null)&&(!that.restriction.isEmpty())) {
                    if (!leftRestriction.equals(rightRestriction)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.restriction!= null)&&(!that.restriction.isEmpty())) {
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
            List<Restriction> theRestriction;
            theRestriction = (((this.restriction!= null)&&(!this.restriction.isEmpty()))?this.getRestriction():null);
            if ((this.restriction!= null)&&(!this.restriction.isEmpty())) {
                currentHashCode += theRestriction.hashCode();
            }
        }
        return currentHashCode;
    }

}
