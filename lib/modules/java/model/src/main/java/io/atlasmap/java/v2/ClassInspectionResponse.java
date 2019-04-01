package io.atlasmap.java.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonRootName("ClassInspectionResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class ClassInspectionResponse implements Serializable {

    private final static long serialVersionUID = 1L;

    protected JavaClass javaClass;

    protected String errorMessage;

    protected Long executionTime;

    /**
     * Gets the value of the javaClass property.
     * 
     * @return
     *     possible object is
     *     {@link JavaClass }
     *     
     */
    public JavaClass getJavaClass() {
        return javaClass;
    }

    /**
     * Sets the value of the javaClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaClass }
     *     
     */
    public void setJavaClass(JavaClass value) {
        this.javaClass = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    /**
     * Gets the value of the executionTime property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getExecutionTime() {
        return executionTime;
    }

    /**
     * Sets the value of the executionTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setExecutionTime(Long value) {
        this.executionTime = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final ClassInspectionResponse that = ((ClassInspectionResponse) object);
        {
            JavaClass leftJavaClass;
            leftJavaClass = this.getJavaClass();
            JavaClass rightJavaClass;
            rightJavaClass = that.getJavaClass();
            if (this.javaClass!= null) {
                if (that.javaClass!= null) {
                    if (!leftJavaClass.equals(rightJavaClass)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.javaClass!= null) {
                    return false;
                }
            }
        }
        {
            String leftErrorMessage;
            leftErrorMessage = this.getErrorMessage();
            String rightErrorMessage;
            rightErrorMessage = that.getErrorMessage();
            if (this.errorMessage!= null) {
                if (that.errorMessage!= null) {
                    if (!leftErrorMessage.equals(rightErrorMessage)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.errorMessage!= null) {
                    return false;
                }
            }
        }
        {
            Long leftExecutionTime;
            leftExecutionTime = this.getExecutionTime();
            Long rightExecutionTime;
            rightExecutionTime = that.getExecutionTime();
            if (this.executionTime!= null) {
                if (that.executionTime!= null) {
                    if (!leftExecutionTime.equals(rightExecutionTime)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.executionTime!= null) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        {
            currentHashCode = (currentHashCode* 31);
            JavaClass theJavaClass;
            theJavaClass = this.getJavaClass();
            if (this.javaClass!= null) {
                currentHashCode += theJavaClass.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theErrorMessage;
            theErrorMessage = this.getErrorMessage();
            if (this.errorMessage!= null) {
                currentHashCode += theErrorMessage.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Long theExecutionTime;
            theExecutionTime = this.getExecutionTime();
            if (this.executionTime!= null) {
                currentHashCode += theExecutionTime.hashCode();
            }
        }
        return currentHashCode;
    }

}
