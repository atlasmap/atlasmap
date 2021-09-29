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

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 
 * @deprecated Expression field action is deprecated. {@link Mapping} level
 * expression has to be used instead.
 **/
@Deprecated
public class Expression extends Action {

    private static final long serialVersionUID = 1L;

    protected String expression;

    /**
     * Gets the value of the string property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Sets the value of the string property.
     * 
     * @param expression
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The expression string to evaluate")
    @AtlasActionProperty(title = "Expression", type = FieldType.STRING)
    public void setExpression(String expression) {
        this.expression = expression;
    }

}
