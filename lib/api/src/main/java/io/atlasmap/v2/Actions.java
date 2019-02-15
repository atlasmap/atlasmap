package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonSerialize(using = ActionsJsonSerializer.class)
@JsonDeserialize(using = ActionsJsonDeserializer.class)
public class Actions implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<Action> actions;

    /**
     * Gets the value of the actions property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actions property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActions().add(newItem);
     * </pre>
     *
     *
     * <p>
     */
    public List<Action> getActions() {
        if (actions == null) {
            actions = new ArrayList<>();
        }
        return this.actions;
    }

}
