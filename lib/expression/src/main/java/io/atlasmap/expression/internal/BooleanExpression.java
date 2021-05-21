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
import io.atlasmap.expression.parser.ParseException;

/**
 * A BooleanExpression is an expression that always
 * produces a Boolean result.
 *
 */
public interface BooleanExpression extends Expression {
    
    /**
     * @param message expression context
     * @return true if the expression evaluates to Boolean.TRUE.
     * @throws ExpressionException exception
     */
    boolean matches(ExpressionContext message) throws ExpressionException;

    static BooleanExpression asBooleanExpression(Expression value) throws ParseException {
        if (value instanceof BooleanExpression) {
            return (BooleanExpression) value;
        }
        if (value instanceof VariableExpression) {
            return UnaryExpression.createBooleanCast( value );
        }
        throw new ParseException("Expression will not result in a boolean value: " + value);
    }

}
