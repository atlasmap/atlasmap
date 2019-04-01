package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class Mapping extends BaseMapping implements Serializable {

    private final static long serialVersionUID = 1L;

    protected FieldGroup inputFieldGroup;

    protected List<Field> inputField;

    protected List<Field> outputField;

    protected String id;

    protected String delimiter;

    protected String delimiterString;

    protected String lookupTableName;

    protected String strategy;

    protected String strategyClassName;

    /**
     * Gets the value of the inputFieldGroup property.
     * 
     * @return
     *     possible object is
     *     {@link FieldGroup }
     *     
     */
    public FieldGroup getInputFieldGroup() {
        return inputFieldGroup;
    }

    /**
     * Sets the value of the inputFieldGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldGroup }
     *     
     */
    public void setInputFieldGroup(FieldGroup value) {
        this.inputFieldGroup = value;
    }

    /**
     * Gets the value of the inputField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inputField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInputField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     * 
     * 
     */
    public List<Field> getInputField() {
        if (inputField == null) {
            inputField = new ArrayList<Field>();
        }
        return this.inputField;
    }

    /**
     * Gets the value of the outputField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outputField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutputField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     * 
     * 
     */
    public List<Field> getOutputField() {
        if (outputField == null) {
            outputField = new ArrayList<Field>();
        }
        return this.outputField;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the delimiter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the value of the delimiter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelimiter(String value) {
        this.delimiter = value;
    }

    /**
     * Gets the value of the delimiterString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelimiterString() {
        return delimiterString;
    }

    /**
     * Sets the value of the delimiterString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelimiterString(String value) {
        this.delimiterString = value;
    }

    /**
     * Gets the value of the lookupTableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLookupTableName() {
        return lookupTableName;
    }

    /**
     * Sets the value of the lookupTableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLookupTableName(String value) {
        this.lookupTableName = value;
    }

    /**
     * Gets the value of the strategy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrategy() {
        return strategy;
    }

    /**
     * Sets the value of the strategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrategy(String value) {
        this.strategy = value;
    }

    /**
     * Gets the value of the strategyClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrategyClassName() {
        return strategyClassName;
    }

    /**
     * Sets the value of the strategyClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrategyClassName(String value) {
        this.strategyClassName = value;
    }

}
