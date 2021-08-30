/*
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
package io.atlasmap.kafkaconnect.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KafkaConnectFields implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<KafkaConnectField> kafkaConnectField;

    /**
     * Gets the value of the jsonField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jsonField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJsonField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link KafkaConnectField }
     * @return A list of {@link KafkaConnectField}
     * 
     */
    public List<KafkaConnectField> getKafkaConnectField() {
        if (kafkaConnectField == null) {
            kafkaConnectField = new ArrayList<KafkaConnectField>();
        }
        return this.kafkaConnectField;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final KafkaConnectFields that = ((KafkaConnectFields) object);
        {
            List<KafkaConnectField> leftKafkaConnectField;
            leftKafkaConnectField = (((this.kafkaConnectField!= null)&&(!this.kafkaConnectField.isEmpty()))?this.getKafkaConnectField():null);
            List<KafkaConnectField> rightKafkaConnectField;
            rightKafkaConnectField = (((that.kafkaConnectField!= null)&&(!that.kafkaConnectField.isEmpty()))?that.getKafkaConnectField():null);
            if ((this.kafkaConnectField!= null)&&(!this.kafkaConnectField.isEmpty())) {
                if ((that.kafkaConnectField!= null)&&(!that.kafkaConnectField.isEmpty())) {
                    if (!leftKafkaConnectField.equals(rightKafkaConnectField)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.kafkaConnectField!= null)&&(!that.kafkaConnectField.isEmpty())) {
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
            List<KafkaConnectField> theKafkaConnectField;
            theKafkaConnectField = (((this.kafkaConnectField!= null)&&(!this.kafkaConnectField.isEmpty()))?this.getKafkaConnectField():null);
            if ((this.kafkaConnectField!= null)&&(!this.kafkaConnectField.isEmpty())) {
                currentHashCode += theKafkaConnectField.hashCode();
            }
        }
        return currentHashCode;
    }

}
