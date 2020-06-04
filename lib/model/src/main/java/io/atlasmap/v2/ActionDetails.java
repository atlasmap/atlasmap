package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("ActionDetails")
public class ActionDetails implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<ActionDetail> actionDetail;

    /**
     * Gets the value of the actionDetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actionDetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActionDetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActionDetail }
     * 
     * @return A list of {@link ActionDetail}
     */
    public List<ActionDetail> getActionDetail() {
        if (actionDetail == null) {
            actionDetail = new ArrayList<ActionDetail>();
        }
        return this.actionDetail;
    }

}
