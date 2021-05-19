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
package io.atlasmap.expression.internal;


import io.atlasmap.expression.Expression;
import io.atlasmap.expression.ExpressionContext;
import io.atlasmap.expression.ExpressionException;
import io.atlasmap.v2.Field;

/**
 * Represents a property expression.
 *
 * @version $Revision: 1.5 $
 */
public class VariableExpression implements Expression {

    private final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {
        return expressionContext.getVariable(name);
    }

    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "${" + name + "}";
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     *{@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return name.equals(((VariableExpression) o).name);

    }

}
