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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.atlasmap.expression.Expression;
import io.atlasmap.expression.ExpressionContext;
import io.atlasmap.expression.ExpressionException;
import io.atlasmap.v2.Field;


/**
 * An expression which performs an operation on two expression values.
 * 
 * @version $Revision: 1.3 $
 */
public abstract class UnaryExpression implements Expression {

    private static final BigDecimal BD_LONG_MIN_VALUE = BigDecimal.valueOf(Long.MIN_VALUE);
    protected Expression right;

    public UnaryExpression(Expression left) {
        this.right = left;
    }

    public static Expression createNegate(Expression left) {
        return new UnaryExpression(left) {
            public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {
                Object rvalue = right.evaluate(expressionContext).getValue();
                if (rvalue == null) {
                    return wrapWithField(null);
                }
                if (rvalue instanceof Number) {
                    return wrapWithField(negate((Number)rvalue));
                }
                return null;
            }

            public String getExpressionSymbol() {
                return "-";
            }
        };
    }

    public static BooleanExpression createInExpression(VariableExpression right, List<Object> elements, final boolean not) {

        // Use a HashSet if there are many elements.
        Collection<Object> t;
        if (elements.size() == 0) {
            t = null;
        } else if (elements.size() < 5) {
            t = elements;
        } else {
            t = new HashSet<Object>(elements);
        }
        final Collection<Object> inList = t;

        return new BooleanUnaryExpression(right) {
            public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {

                Object rvalue = right.evaluate(expressionContext).getValue();
                if (rvalue == null) {
                    return wrapWithField(null);
                }
                if (rvalue.getClass() != String.class) {
                    return wrapWithField(null);
                }

                if ((inList != null && inList.contains(rvalue)) ^ not) {
                    return wrapWithField(Boolean.TRUE);
                } else {
                    return wrapWithField(Boolean.FALSE);
                }

            }

            public String toString() {
                StringBuffer answer = new StringBuffer();
                answer.append(right);
                answer.append(" ");
                answer.append(getExpressionSymbol());
                answer.append(" ( ");

                int count = 0;
                for (Iterator<Object> i = inList.iterator(); i.hasNext();) {
                    Object o = (Object)i.next();
                    if (count != 0) {
                        answer.append(", ");
                    }
                    answer.append(o);
                    count++;
                }

                answer.append(" )");
                return answer.toString();
            }

            public String getExpressionSymbol() {
                if (not) {
                    return "NOT IN";
                } else {
                    return "IN";
                }
            }
        };
    }

    abstract static class BooleanUnaryExpression extends UnaryExpression implements BooleanExpression {
        BooleanUnaryExpression(Expression left) {
            super(left);
        }

        public boolean matches(ExpressionContext message) throws ExpressionException {
            Object object = evaluate(message).getValue();
            return object != null && object == Boolean.TRUE;
        }
    };

    public static BooleanExpression createNOT(BooleanExpression left) {
        return new BooleanUnaryExpression(left) {
            public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {
                Boolean lvalue = (Boolean)right.evaluate(expressionContext).getValue();
                if (lvalue == null) {
                    return wrapWithField(null);
                }
                return wrapWithField(lvalue.booleanValue() ? Boolean.FALSE : Boolean.TRUE);
            }

            public String getExpressionSymbol() {
                return "!";
            }
        };
    }

    public static BooleanExpression createBooleanCast(Expression left) {
        return new BooleanUnaryExpression(left) {
            public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {
                Object rvalue = right.evaluate(expressionContext).getValue();
                if (rvalue == null) {
                    return null;
                }
                if (!rvalue.getClass().equals(Boolean.class)) {
                    return wrapWithField(Boolean.FALSE);
                }
                return wrapWithField(((Boolean)rvalue).booleanValue() ? Boolean.TRUE : Boolean.FALSE);
            }

            public String toString() {
                return right.toString();
            }

            public String getExpressionSymbol() {
                return "";
            }
        };
    }

    private static Number negate(Number left) {
        Class clazz = left.getClass();
        if (clazz == Integer.class) {
            return new Integer(-left.intValue());
        } else if (clazz == Long.class) {
            return new Long(-left.longValue());
        } else if (clazz == Float.class) {
            return new Float(-left.floatValue());
        } else if (clazz == Double.class) {
            return new Double(-left.doubleValue());
        } else if (clazz == BigDecimal.class) {
            // We ussually get a big deciamal when we have Long.MIN_VALUE
            // constant in the
            // Selector. Long.MIN_VALUE is too big to store in a Long as a
            // positive so we store it
            // as a Big decimal. But it gets Negated right away.. to here we try
            // to covert it back
            // to a Long.
            BigDecimal bd = (BigDecimal)left;
            bd = bd.negate();

            if (BD_LONG_MIN_VALUE.compareTo(bd) == 0) {
                return Long.valueOf(Long.MIN_VALUE);
            }
            return bd;
        } else {
            throw new RuntimeException("Don't know how to negate: " + left);
        }
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression expression) {
        right = expression;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "(" + getExpressionSymbol() + " " + right.toString() + ")";
    }

    /**
     * {@inheritDoc}
     * TODO: more efficient hashCode()
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * {@inheritDoc}
     * TODO: more efficient hashCode()
     */
    public boolean equals(Object o) {

        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return toString().equals(o.toString());

    }

    /**
     * Returns the symbol that represents this binary expression. For example,
     * addition is represented by "+"
     * 
     * @return expression symbol string
     */
    public abstract String getExpressionSymbol();

}
