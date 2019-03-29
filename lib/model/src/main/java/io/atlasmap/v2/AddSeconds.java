package io.atlasmap.v2;

import java.io.Serializable;

public class AddSeconds extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected Integer seconds;

    /**
     * Gets the value of the seconds property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSeconds() {
        return seconds;
    }

    /**
     * Sets the value of the seconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSeconds(Integer value) {
        this.seconds = value;
    }

}
