
package io.atlasmap.json.v2;

import java.io.Serializable;
import io.atlasmap.v2.DataSource;

public class JsonDataSource extends DataSource implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String template;

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplate(String value) {
        this.template = value;
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
        final JsonDataSource that = ((JsonDataSource) object);
        {
            String leftTemplate;
            leftTemplate = this.getTemplate();
            String rightTemplate;
            rightTemplate = that.getTemplate();
            if (this.template!= null) {
                if (that.template!= null) {
                    if (!leftTemplate.equals(rightTemplate)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.template!= null) {
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
            String theTemplate;
            theTemplate = this.getTemplate();
            if (this.template!= null) {
                currentHashCode += theTemplate.hashCode();
            }
        }
        return currentHashCode;
    }

}
