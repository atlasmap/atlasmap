package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActionParameters implements Serializable {

    private final static long serialVersionUID = 1L;
    protected List<ActionParameter> parameter;

    /**
     * Gets the value of the parameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActionParameter }
     * 
     * @return A list of {@link ActionParameter}
     */
    public List<ActionParameter> getParameter() {
        if (parameter == null) {
            parameter = new ArrayList<ActionParameter>();
        }
        return this.parameter;
    }

}
