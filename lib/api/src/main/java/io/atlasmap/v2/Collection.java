package io.atlasmap.v2;

import java.io.Serializable;
import java.math.BigInteger;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Deprecated
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class Collection
    extends BaseMapping
    implements Serializable
{

    private final static long serialVersionUID = 1L;

    protected Mappings mappings;

    protected BigInteger collectionSize;

    protected CollectionType collectionType;

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
     * Gets the value of the collectionSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCollectionSize() {
        return collectionSize;
    }

    /**
     * Sets the value of the collectionSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCollectionSize(BigInteger value) {
        this.collectionSize = value;
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

}
