package io.atlasmap.java.v2;

import java.io.Serializable;
import io.atlasmap.v2.Collection;

public class JavaCollection extends Collection implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String collectionClassName;

    /**
     * Gets the value of the collectionClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionClassName() {
        return collectionClassName;
    }

    /**
     * Sets the value of the collectionClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionClassName(String value) {
        this.collectionClassName = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!super.equals(object)) {
            return false;
        }
        final JavaCollection that = ((JavaCollection) object);
        {
            String leftCollectionClassName;
            leftCollectionClassName = this.getCollectionClassName();
            String rightCollectionClassName;
            rightCollectionClassName = that.getCollectionClassName();
            if (this.collectionClassName!= null) {
                if (that.collectionClassName!= null) {
                    if (!leftCollectionClassName.equals(rightCollectionClassName)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.collectionClassName!= null) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        currentHashCode = ((currentHashCode* 31)+ super.hashCode());
        {
            currentHashCode = (currentHashCode* 31);
            String theCollectionClassName;
            theCollectionClassName = this.getCollectionClassName();
            if (this.collectionClassName!= null) {
                currentHashCode += theCollectionClassName.hashCode();
            }
        }
        return currentHashCode;
    }

}
