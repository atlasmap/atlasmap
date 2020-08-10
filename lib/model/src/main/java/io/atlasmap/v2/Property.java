package io.atlasmap.v2;

import java.io.Serializable;

public class Property implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String name;

    protected String value;

    protected FieldType fieldType;

    protected String scope;

    protected DataSourceType dataSourceType;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
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
     * Gets the value of the scope property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     *
     * @param scope
     *     allowed object is
     *     {@link String }
     *
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Gets the value of the dataSourceType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    /**
     * Sets the value of the dataSourceType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDataSourceType(DataSourceType value) {
        this.dataSourceType = value;
    }

}
