package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonRootName("AtlasMapping")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class AtlasMapping implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<DataSource> dataSource;

    protected Mappings mappings;

    protected LookupTables lookupTables;

    protected Constants constants;

    protected Properties properties;

    protected String name;

    /**
     * Gets the value of the dataSource property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataSource property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataSource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSource }
     * 
     * 
     */
    public List<DataSource> getDataSource() {
        if (dataSource == null) {
            dataSource = new ArrayList<DataSource>();
        }
        return this.dataSource;
    }

    /**
     * Gets the value of the mappings property.
     * 
     * @return
     *     possible object is
     *     {@link Mappings }
     *     
     */
    public Mappings getMappings() {
        return mappings;
    }

    /**
     * Sets the value of the mappings property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mappings }
     *     
     */
    public void setMappings(Mappings value) {
        this.mappings = value;
    }

    /**
     * Gets the value of the lookupTables property.
     * 
     * @return
     *     possible object is
     *     {@link LookupTables }
     *     
     */
    public LookupTables getLookupTables() {
        return lookupTables;
    }

    /**
     * Sets the value of the lookupTables property.
     * 
     * @param value
     *     allowed object is
     *     {@link LookupTables }
     *     
     */
    public void setLookupTables(LookupTables value) {
        this.lookupTables = value;
    }

    /**
     * Gets the value of the constants property.
     * 
     * @return
     *     possible object is
     *     {@link Constants }
     *     
     */
    public Constants getConstants() {
        return constants;
    }

    /**
     * Sets the value of the constants property.
     * 
     * @param value
     *     allowed object is
     *     {@link Constants }
     *     
     */
    public void setConstants(Constants value) {
        this.constants = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link Properties }
     *     
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties }
     *     
     */
    public void setProperties(Properties value) {
        this.properties = value;
    }

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

}
