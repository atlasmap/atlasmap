package io.atlasmap.java.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class JavaFields implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<JavaField> javaField;

    /**
     * Gets the value of the javaField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the javaField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJavaField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JavaField }
     * 
     * 
     */
    public List<JavaField> getJavaField() {
        if (javaField == null) {
            javaField = new ArrayList<JavaField>();
        }
        return this.javaField;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final JavaFields that = ((JavaFields) object);
        {
            List<JavaField> leftJavaField;
            leftJavaField = (((this.javaField!= null)&&(!this.javaField.isEmpty()))?this.getJavaField():null);
            List<JavaField> rightJavaField;
            rightJavaField = (((that.javaField!= null)&&(!that.javaField.isEmpty()))?that.getJavaField():null);
            if ((this.javaField!= null)&&(!this.javaField.isEmpty())) {
                if ((that.javaField!= null)&&(!that.javaField.isEmpty())) {
                    if (!leftJavaField.equals(rightJavaField)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.javaField!= null)&&(!that.javaField.isEmpty())) {
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
            List<JavaField> theJavaField;
            theJavaField = (((this.javaField!= null)&&(!this.javaField.isEmpty()))?this.getJavaField():null);
            if ((this.javaField!= null)&&(!this.javaField.isEmpty())) {
                currentHashCode += theJavaField.hashCode();
            }
        }
        return currentHashCode;
    }

}
