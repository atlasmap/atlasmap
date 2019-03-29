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
     * Objects of the following type(s) are allowed in the list
     * {@link AbsoluteValue }
     * {@link Add }
     * {@link AddDays }
     * {@link AddSeconds }
     * {@link Append }
     * {@link Average }
     * {@link Camelize }
     * {@link Capitalize }
     * {@link Ceiling }
     * {@link CollectionSize }
     * {@link Concatenate }
     * {@link Contains }
     * {@link ConvertAreaUnit }
     * {@link ConvertDistanceUnit }
     * {@link ConvertMassUnit }
     * {@link ConvertVolumeUnit }
     * {@link CurrentDate }
     * {@link CurrentDateTime }
     * {@link CurrentTime }
     * {@link CustomAction }
     * {@link DayOfMonth }
     * {@link DayOfWeek }
     * {@link DayOfYear }
     * {@link Divide }
     * {@link EndsWith }
     * {@link Equals }
     * {@link FileExtension }
     * {@link Floor }
     * {@link Format }
     * {@link GenerateUUID }
     * {@link IndexOf }
     * {@link IsNull }
     * {@link ItemAt }
     * {@link LastIndexOf }
     * {@link Length }
     * {@link LowercaseChar }
     * {@link Lowercase }
     * {@link Maximum }
     * {@link Minimum }
     * {@link Multiply }
     * {@link Normalize }
     * {@link PadStringLeft }
     * {@link PadStringRight }
     * {@link Prepend }
     * {@link RemoveFileExtension }
     * {@link ReplaceAll }
     * {@link ReplaceFirst }
     * {@link Round }
     * {@link SeparateByDash }
     * {@link SeparateByUnderscore }
     * {@link Split }
     * {@link StartsWith }
     * {@link SubString }
     * {@link SubStringAfter }
     * {@link SubStringBefore }
     * {@link Subtract }
     * {@link Trim }
     * {@link TrimLeft }
     * {@link TrimRight }
     * {@link Uppercase }
     * {@link UppercaseChar }
     * 
     * 
     */
    public List<Action> getActions() {
        if (actions == null) {
            actions = new ArrayList<Action>();
        }
        return this.actions;
    }

}
