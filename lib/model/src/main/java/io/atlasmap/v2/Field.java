package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public abstract class Field
    implements Serializable
{

    private final static long serialVersionUID = 1L;

    protected ArrayList<Action> actions;

    protected Object value;

    protected Integer arrayDimensions;

    protected Integer arraySize;

    protected CollectionType collectionType;

    protected String docId;

    protected Integer index;

    protected String path;

    protected Boolean required;

    protected FieldStatus status;

    protected FieldType fieldType;

    protected String format;

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
    public ArrayList<Action> getActions() {
        return this.actions;
    }

    /**
     * Sets the value of the actions property.
     * 
     * @param actions
     *     allowed object is
     *     {@link List<Action> }
     *     
     */
    @JsonDeserialize(using = ActionListUpgradeDeserializer.class)
    public void setActions(ArrayList<Action> actions) {
        this.actions = actions;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the value of the arrayDimensions property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getArrayDimensions() {
        return arrayDimensions;
    }

    /**
     * Sets the value of the arrayDimensions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setArrayDimensions(Integer value) {
        this.arrayDimensions = value;
    }

    /**
     * Gets the value of the arraySize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getArraySize() {
        return arraySize;
    }

    /**
     * Sets the value of the arraySize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setArraySize(Integer value) {
        this.arraySize = value;
    }

    /**
     * Gets the value of the collectionType property.
     * 
     * @return
     *     possible object is
     *     {@link CollectionType }
     *     
     */
    public CollectionType getCollectionType() {
        return collectionType;
    }

    /**
     * Sets the value of the collectionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectionType }
     *     
     */
    public void setCollectionType(CollectionType value) {
        this.collectionType = value;
    }

    /**
     * Gets the value of the docId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocId() {
        return docId;
    }

    /**
     * Sets the value of the docId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocId(String value) {
        this.docId = value;
    }

    /**
     * Gets the value of the index property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIndex(Integer value) {
        this.index = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the required property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRequired() {
        return required;
    }

    /**
     * Sets the value of the required property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRequired(Boolean value) {
        this.required = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link FieldStatus }
     *     
     */
    public FieldStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldStatus }
     *     
     */
    public void setStatus(FieldStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the fieldType property.
     * 
     * @return
     *     possible object is
     *     {@link FieldType }
     *     
     */
    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * Sets the value of the fieldType property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldType }
     *     
     */
    public void setFieldType(FieldType value) {
        this.fieldType = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

}
