/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.csv.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CsvFields implements Serializable {

    private final static long serialVersionUID = 1L;

    protected List<CsvField> csvField;

    /**
     * Gets the value of the csvField property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the csvField property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCsvField().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CsvField }
     *
     *
     */
    public List<CsvField> getCsvField() {
        if (csvField == null) {
            csvField = new ArrayList<CsvField>();
        }
        return this.csvField;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final CsvFields that = ((CsvFields) object);
        {
            List<CsvField> leftCsvField;
            leftCsvField = (((this.csvField != null)&&(!this.csvField.isEmpty()))?this.getCsvField():null);
            List<CsvField> rightCsvField;
            rightCsvField = (((that.csvField!= null)&&(!that.csvField.isEmpty()))?that.getCsvField():null);
            if ((this.csvField != null)&&(!this.csvField.isEmpty())) {
                if ((that.csvField!= null)&&(!that.csvField.isEmpty())) {
                    if (!leftCsvField.equals(rightCsvField)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.csvField!= null)&&(!that.csvField.isEmpty())) {
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
            List<CsvField> theCsvField;
            theCsvField = (((this.csvField != null)&&(!this.csvField.isEmpty()))?this.getCsvField():null);
            if ((this.csvField != null)&&(!this.csvField.isEmpty())) {
                currentHashCode += theCsvField.hashCode();
            }
        }
        return currentHashCode;
    }

}
