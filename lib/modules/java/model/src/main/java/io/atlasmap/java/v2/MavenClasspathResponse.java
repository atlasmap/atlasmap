package io.atlasmap.java.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonRootName("MavenClasspathResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class MavenClasspathResponse implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String classpath;

    protected String errorMessage;

    protected Long executionTime;

    /**
     * Gets the value of the classpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClasspath() {
        return classpath;
    }

    /**
     * Sets the value of the classpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClasspath(String value) {
        this.classpath = value;
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
        final MavenClasspathResponse that = ((MavenClasspathResponse) object);
        {
            String leftClasspath;
            leftClasspath = this.getClasspath();
            String rightClasspath;
            rightClasspath = that.getClasspath();
            if (this.classpath!= null) {
                if (that.classpath!= null) {
                    if (!leftClasspath.equals(rightClasspath)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.classpath!= null) {
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
            String theClasspath;
            theClasspath = this.getClasspath();
            if (this.classpath!= null) {
                currentHashCode += theClasspath.hashCode();
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
