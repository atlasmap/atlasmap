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

import static io.atlasmap.v2.AtlasModelFactory.wrapWithField;

import io.atlasmap.expression.ExpressionContext;
import io.atlasmap.expression.ExpressionException;
import io.atlasmap.v2.Field;

/**
 * A filter performing a comparison of two objects.
 * 
 * @version $Revision: 1.2 $
 */
public abstract class LogicExpression extends BinaryExpression implements BooleanExpression {

    /**
     * @param left left {@link BooleanExpression}
     * @param right right {@link BooleanExpression}
     */
    public LogicExpression(BooleanExpression left, BooleanExpression right) {
        super(left, right);
    }

    public static BooleanExpression createOR(BooleanExpression lvalue, BooleanExpression rvalue) {
        return new LogicExpression(lvalue, rvalue) {

            public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {

                Boolean lv = (Boolean)left.evaluate(expressionContext).getValue();
                // Can we do an OR shortcut??
                if (lv != null && lv.booleanValue()) {
                    return wrapWithField(Boolean.TRUE);
                }

                Boolean rv = (Boolean)right.evaluate(expressionContext).getValue();
                return wrapWithField(rv == null ? null : rv);
            }

            public String getExpressionSymbol() {
                return "||";
            }
        };
    }

    public static BooleanExpression createAND(BooleanExpression lvalue, BooleanExpression rvalue) {
        return new LogicExpression(lvalue, rvalue) {

            public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {

                Boolean lv = (Boolean)left.evaluate(expressionContext).getValue();

                // Can we do an AND shortcut??
                if (lv == null) {
                    return null;
                }
                if (!lv.booleanValue()) {
                    return wrapWithField(Boolean.FALSE);
                }

                Boolean rv = (Boolean)right.evaluate(expressionContext).getValue();
                return wrapWithField(rv == null ? null : rv);
            }

            public String getExpressionSymbol() {
                return "&&";
            }
        };
    }

    public abstract Field evaluate(ExpressionContext expressionContext) throws ExpressionException;

    public boolean matches(ExpressionContext message) throws ExpressionException {
        Object object = evaluate(message).getValue();
        return object != null && object == Boolean.TRUE;
    }

}
