package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

@JsonRootName("FunctionDetail")
public class FunctionDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    protected FunctionParameters parameters;

    protected String name;

    protected Boolean custom;

    protected String className;

    protected String method;

    protected FieldType returnType;

    protected ObjectSchema functionSchema;

    /**
     * Gets the value of the parameters property.
     *
     * @return possible object is {@link FunctionParameters }
     *
     */
    public FunctionParameters getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     *
     * @param value allowed object is {@link FunctionParameters }
     *
     */
    public void setParameters(FunctionParameters value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the custom property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isCustom() {
        return custom;
    }

    /**
     * Sets the value of the custom property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setCustom(Boolean value) {
        this.custom = value;
    }

    /**
     * Gets the value of the className property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the method property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setMethod(String value) {
        this.method = value;
    }

    /**
     * Gets the value of the returnType property.
     *
     * @return possible object is {@link FieldType }
     *
     */
    public FieldType getResultType() {
        return returnType;
    }

    /**
     * Sets the value of the returnType property.
     *
     * @param value allowed object is {@link FieldType }
     *
     */
    public void setReturnType(FieldType value) {
        this.returnType = value;
    }

    public JsonSchema getFunctionSchema() {
        return functionSchema;
    }

    public void setFunctionSchema(ObjectSchema functionSchema) {
        this.functionSchema = functionSchema;
    }

    public void setFunctionSchema(Class<? extends BaseFunction> clazz) throws JsonMappingException {

        if (clazz == null) {
            setFunctionSchema((ObjectSchema) null);
            return;
        }

        setClassName(clazz.getName());
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        AtlasSchemaFactoryWrapper visitor = new AtlasSchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(clazz, visitor);
        JsonSchema schema = visitor.finalSchema();
        ObjectSchema objectSchema = schema.asObjectSchema();

        // see:
        // https://json-schema.org/understanding-json-schema/reference/generic.html#constant-values
        String id = FunctionResolver.getInstance().toId(clazz);
        objectSchema.setId(id);
        AtlasSchemaFactoryWrapper.ExtendedJsonSchema keyField = (AtlasSchemaFactoryWrapper.ExtendedJsonSchema) objectSchema
                .getProperties().get("@type");
        keyField.getMetadata().put("const", id);
        setFunctionSchema(objectSchema);
    }

}
