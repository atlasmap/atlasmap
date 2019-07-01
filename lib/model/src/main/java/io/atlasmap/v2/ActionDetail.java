package io.atlasmap.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

@JsonRootName("ActionDetail")
public class ActionDetail implements Serializable {

    private final static long serialVersionUID = 1L;

    protected ActionParameters parameters;

    protected String name;

    protected Boolean custom;

    protected String className;

    protected String method;

    protected FieldType sourceType;

    protected FieldType targetType;

    protected Multiplicity multiplicity;

    protected ObjectSchema actionSchema;

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link ActionParameters }
     *     
     */
    public ActionParameters getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActionParameters }
     *     
     */
    public void setParameters(ActionParameters value) {
        this.parameters = value;
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

    /**
     * Gets the value of the custom property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCustom() {
        return custom;
    }

    /**
     * Sets the value of the custom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustom(Boolean value) {
        this.custom = value;
    }

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethod(String value) {
        this.method = value;
    }

    /**
     * Gets the value of the sourceType property.
     * 
     * @return
     *     possible object is
     *     {@link FieldType }
     *     
     */
    public FieldType getSourceType() {
        return sourceType;
    }

    /**
     * Sets the value of the sourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldType }
     *     
     */
    public void setSourceType(FieldType value) {
        this.sourceType = value;
    }

    /**
     * Gets the value of the targetType property.
     * 
     * @return
     *     possible object is
     *     {@link FieldType }
     *     
     */
    public FieldType getTargetType() {
        return targetType;
    }

    /**
     * Sets the value of the targetType property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldType }
     *     
     */
    public void setTargetType(FieldType value) {
        this.targetType = value;
    }

    /**
     * Gets the value of the multiplicity property.
     * 
     * @return
     *     possible object is
     *     {@link Multiplicity }
     *     
     */
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    /**
     * Sets the value of the multiplicity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Multiplicity }
     *     
     */
    public void setMultiplicity(Multiplicity value) {
        this.multiplicity = value;
    }

    public JsonSchema getActionSchema() {
        return actionSchema;
    }

    public void setActionSchema(ObjectSchema actionSchema) {
        this.actionSchema = actionSchema;
    }

    public void setActionSchema(Class<? extends Action> clazz) throws JsonMappingException {

        if (clazz == null) {
            setActionSchema((ObjectSchema)null);
            return;
        }

        setClassName(clazz.getName());
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        AtlasSchemaFactoryWrapper visitor = new AtlasSchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(clazz, visitor);
        JsonSchema schema = visitor.finalSchema();
        ObjectSchema objectSchema = schema.asObjectSchema();

        // see: https://json-schema.org/understanding-json-schema/reference/generic.html#constant-values
        String id = ActionResolver.getInstance().toId(clazz);
        objectSchema.setId(id);
        AtlasSchemaFactoryWrapper.ExtendedJsonSchema keyField = (AtlasSchemaFactoryWrapper.ExtendedJsonSchema) objectSchema.getProperties().get("@type");
        keyField.getMetadata().put("const", id);
        setActionSchema(objectSchema);
    }



}
