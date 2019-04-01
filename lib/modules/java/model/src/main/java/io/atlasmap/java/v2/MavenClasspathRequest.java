package io.atlasmap.java.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonRootName("MavenClasspathRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class MavenClasspathRequest implements Serializable {

    private final static long serialVersionUID = 1L;
    protected String pomXmlData;

    protected Long executeTimeout;

    protected String workingDirectory;

    /**
     * Gets the value of the pomXmlData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPomXmlData() {
        return pomXmlData;
    }

    /**
     * Sets the value of the pomXmlData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPomXmlData(String value) {
        this.pomXmlData = value;
    }

    /**
     * Gets the value of the executeTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getExecuteTimeout() {
        return executeTimeout;
    }

    /**
     * Sets the value of the executeTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setExecuteTimeout(Long value) {
        this.executeTimeout = value;
    }

    /**
     * Gets the value of the workingDirectory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets the value of the workingDirectory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkingDirectory(String value) {
        this.workingDirectory = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final MavenClasspathRequest that = ((MavenClasspathRequest) object);
        {
            String leftPomXmlData;
            leftPomXmlData = this.getPomXmlData();
            String rightPomXmlData;
            rightPomXmlData = that.getPomXmlData();
            if (this.pomXmlData!= null) {
                if (that.pomXmlData!= null) {
                    if (!leftPomXmlData.equals(rightPomXmlData)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.pomXmlData!= null) {
                    return false;
                }
            }
        }
        {
            Long leftExecuteTimeout;
            leftExecuteTimeout = this.getExecuteTimeout();
            Long rightExecuteTimeout;
            rightExecuteTimeout = that.getExecuteTimeout();
            if (this.executeTimeout!= null) {
                if (that.executeTimeout!= null) {
                    if (!leftExecuteTimeout.equals(rightExecuteTimeout)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.executeTimeout!= null) {
                    return false;
                }
            }
        }
        {
            String leftWorkingDirectory;
            leftWorkingDirectory = this.getWorkingDirectory();
            String rightWorkingDirectory;
            rightWorkingDirectory = that.getWorkingDirectory();
            if (this.workingDirectory!= null) {
                if (that.workingDirectory!= null) {
                    if (!leftWorkingDirectory.equals(rightWorkingDirectory)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.workingDirectory!= null) {
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
            String thePomXmlData;
            thePomXmlData = this.getPomXmlData();
            if (this.pomXmlData!= null) {
                currentHashCode += thePomXmlData.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Long theExecuteTimeout;
            theExecuteTimeout = this.getExecuteTimeout();
            if (this.executeTimeout!= null) {
                currentHashCode += theExecuteTimeout.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theWorkingDirectory;
            theWorkingDirectory = this.getWorkingDirectory();
            if (this.workingDirectory!= null) {
                currentHashCode += theWorkingDirectory.hashCode();
            }
        }
        return currentHashCode;
    }

}
