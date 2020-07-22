package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FunctionParameters implements Serializable {

    private static final long serialVersionUID = 1L;
    protected List<FunctionParameter> parameters;

    /**
     * Gets the value of the parameter property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the parameter property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getParameter().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FunctionParameter }
     *
     * @return A list of {@link FunctionParameter}
     */
    public List<FunctionParameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<FunctionParameter>();
        }
        return parameters;
    }

}
