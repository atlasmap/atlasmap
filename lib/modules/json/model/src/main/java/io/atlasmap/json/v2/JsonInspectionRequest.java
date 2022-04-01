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
package io.atlasmap.json.v2;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.v2.BaseInspectionRequest;

/**
 * The top container object of JSON Document inspection request that AtlasMap UI sends
 * to the backend.
 */
@JsonRootName("JsonInspectionRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JsonInspectionRequest extends BaseInspectionRequest {

    /** Raw JSON schema/instance data to inspect. */
    private String jsonData;

    /**
     * Gets the value of the jsonData property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getJsonData() {
        return jsonData;
    }

    /**
     * Sets the value of the jsonData property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setJsonData(String value) {
        this.jsonData = value;
    }

    @Override
    public boolean equals(Object object) {
        if (!super.equals(object)) {
            return false;
        }
        final JsonInspectionRequest that = ((JsonInspectionRequest) object);
        String leftJsonData;
        leftJsonData = this.getJsonData();
        String rightJsonData;
        rightJsonData = that.getJsonData();
        if (this.jsonData != null) {
            if (that.jsonData != null) {
                if (!leftJsonData.equals(rightJsonData)) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (that.jsonData != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int currentHashCode = (super.hashCode() * 31);
        String theJsonData;
        theJsonData = this.getJsonData();
        if (this.jsonData != null) {
            currentHashCode += theJsonData.hashCode();
        }
        return currentHashCode;
    }

}
