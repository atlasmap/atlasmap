package io.atlasmap.v2;

public class CustomFunction extends BaseFunction {

    private static final long serialVersionUID = 1L;

    protected String name;

    protected String className;

    protected String methodName;

    protected FieldType outputFieldType;

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
     * Gets the value of the methodName property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the value of the methodName property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setMethodName(String value) {
        this.methodName = value;
    }

    /**
     * Gets the value of the outputFieldType property.
     *
     * @return possible object is {@link FieldType }
     *
     */
    public FieldType getOutputFieldType() {
        return outputFieldType;
    }

    /**
     * Sets the value of the outputFieldType property.
     *
     * @param value allowed object is {@link FieldType }
     *
     */
    public void setOutputFieldType(FieldType value) {
        this.outputFieldType = value;
    }

}
