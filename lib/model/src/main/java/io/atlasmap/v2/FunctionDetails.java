package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("FunctionDetails")
public class FunctionDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<FunctionDetail> details;

    /**
     * Gets the value of the details property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the details property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getDetails().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FunctionDetail }
     *
     * @return A list of {@link FunctionDetail}
     */
    public List<FunctionDetail> getDetails() {
        if (details == null) {
            details = new ArrayList<FunctionDetail>();
        }
        return details;
    }

}
