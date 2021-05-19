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
package io.atlasmap.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("ActionDetails")
public class ActionDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<ActionDetail> actionDetail;

    /**
     * Gets the value of the actionDetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actionDetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActionDetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActionDetail }
     * 
     * @return A list of {@link ActionDetail}
     */
    public List<ActionDetail> getActionDetail() {
        if (actionDetail == null) {
            actionDetail = new ArrayList<ActionDetail>();
        }
        return this.actionDetail;
    }

}
