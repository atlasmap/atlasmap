/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import io.atlasmap.expression.Expression;
import io.atlasmap.expression.ExpressionContext;
import io.atlasmap.expression.ExpressionException;
import io.atlasmap.v2.Field;


/**
 * A filter performing a comparison of two objects
 * 
 * @version $Revision: 1.2 $
 */
public abstract class ComparisonExpression extends BinaryExpression implements BooleanExpression {

    public static final ThreadLocal<Boolean> CONVERT_STRING_EXPRESSIONS = new ThreadLocal<Boolean>();

    boolean convertStringExpressions = false;
    private static final Set<Character> REGEXP_CONTROL_CHARS = new HashSet<Character>();

    /**
     * @param left left {@link Expression}
     * @param right right {@link Expression}
     */
    public ComparisonExpression(Expression left, Expression right) {
        super(left, right);
        convertStringExpressions = CONVERT_STRING_EXPRESSIONS.get()!=null;
    }

    public static BooleanExpression createBetween(Expression value, Expression left, Expression right) {
        return LogicExpression.createAND(createGreaterThanEqual(value, left), createLessThanEqual(value, right));
    }

    public static BooleanExpression createNotBetween(Expression value, Expression left, Expression right) {
        return LogicExpression.createOR(createLessThan(value, left), createGreaterThan(value, right));
    }

    static {
        REGEXP_CONTROL_CHARS.add(Character.valueOf('.'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('\\'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('['));
        REGEXP_CONTROL_CHARS.add(Character.valueOf(']'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('^'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('$'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('?'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('*'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('+'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('{'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('}'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('|'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('('));
        REGEXP_CONTROL_CHARS.add(Character.valueOf(')'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf(':'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('&'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('<'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('>'));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('='));
        REGEXP_CONTROL_CHARS.add(Character.valueOf('!'));
    }

    static class LikeExpression extends UnaryExpression implements BooleanExpression {

        Pattern likePattern;

        /**
         */
        public LikeExpression(Expression right, String like, int escape) {
            super(right);

            StringBuffer regexp = new StringBuffer(like.length() * 2);
            regexp.append("\\A"); // The beginning of the input
            for (int i = 0; i < like.length(); i++) {
                char c = like.charAt(i);
                if (escape == (0xFFFF & c)) {
                    i++;
                    if (i >= like.length()) {
                        // nothing left to escape...
                        break;
                    }

                    char t = like.charAt(i);
                    regexp.append("\\x");
                    regexp.append(Integer.toHexString(0xFFFF & t));
                } else if (c == '%') {
                    regexp.append(".*?"); // Do a non-greedy match
                } else if (c == '_') {
                    regexp.append("."); // match one
                } else if (REGEXP_CONTROL_CHARS.contains(new Character(c))) {
                    regexp.append("\\x");
                    regexp.append(Integer.toHexString(0xFFFF & c));
                } else {
                    regexp.append(c);
                }
            }
            regexp.append("\\z"); // The end of the input

            likePattern = Pattern.compile(regexp.toString(), Pattern.DOTALL);
        }

        /**
         * @see org.apache.activemq.filter.UnaryExpression#getExpressionSymbol()
         */
        public String getExpressionSymbol() {
            return "LIKE";
        }

        /**
         * @see org.apache.activemq.filter.Expression#evaluate(ExpressionContext)
         */
        public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {

            Object rv = this.getRight().evaluate(expressionContext).getValue();

            if (rv == null) {
                return null;
            }

            if (!(rv instanceof String)) {
                return wrapWithField(Boolean.FALSE);
                // throw new RuntimeException("LIKE can only operate on String
                // identifiers. LIKE attemped on: '" + rv.getClass());
            }

            return wrapWithField(likePattern.matcher((String)rv).matches() ? Boolean.TRUE : Boolean.FALSE);
        }

        public boolean matches(ExpressionContext message) throws ExpressionException {
            Object object = evaluate(message).getValue();
            return object != null && object == Boolean.TRUE;
        }
    }

    public static BooleanExpression createLike(Expression left, String right, String escape) {
        if (escape != null && escape.length() != 1) {
            throw new RuntimeException("The ESCAPE string litteral is invalid.  It can only be one character.  Litteral used: " + escape);
        }
        int c = -1;
        if (escape != null) {
            c = 0xFFFF & escape.charAt(0);
        }

        return new LikeExpression(left, right, c);
    }

    public static BooleanExpression createNotLike(Expression left, String right, String escape) {
        return UnaryExpression.createNOT(createLike(left, right, escape));
    }

    public static BooleanExpression createInFilter(Expression left, List elements) {

        if (!(left instanceof VariableExpression)) {
            throw new RuntimeException("Expected a property for In expression, got: " + left);
        }
        return UnaryExpression.createInExpression((VariableExpression)left, elements, false);

    }

    public static BooleanExpression createNotInFilter(Expression left, List elements) {

        if (!(left instanceof VariableExpression)) {
            throw new RuntimeException("Expected a property for In expression, got: " + left);
        }
        return UnaryExpression.createInExpression((VariableExpression)left, elements, true);

    }

    public static BooleanExpression createIsNull(Expression left) {
        return doCreateEqual(left, ConstantExpression.NULL);
    }

    public static BooleanExpression createIsNotNull(Expression left) {
        return UnaryExpression.createNOT(doCreateEqual(left, ConstantExpression.NULL));
    }

    public static BooleanExpression createNotEqual(Expression left, Expression right) {
        return UnaryExpression.createNOT(createEqual(left, right));
    }

    public static BooleanExpression createEqual(Expression left, Expression right) {
        checkEqualOperandCompatability(left, right);
        return doCreateEqual(left, right);
    }

    private static BooleanExpression doCreateEqual(Expression left, Expression right) {
        return new ComparisonExpression(left, right) {

            public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {
                Object lv = left.evaluate(expressionContext).getValue();
                Object rv = right.evaluate(expressionContext).getValue();

                // If one of the values is null
                if (lv == null ^ rv == null) {
                    return wrapWithField(Boolean.FALSE);
                }
                if (lv == rv || lv.equals(rv)) {
                    return wrapWithField(Boolean.TRUE);
                }
                if (lv instanceof Comparable && rv instanceof Comparable) {
                    return wrapWithField(compare((Comparable)lv, (Comparable)rv));
                }
                return wrapWithField(Boolean.FALSE);
            }

            protected boolean asBoolean(int answer) {
                return answer == 0;
            }

            public String getExpressionSymbol() {
                return "==";
            }
        };
    }

    public static BooleanExpression createGreaterThan(final Expression left, final Expression right) {
        checkLessThanOperand(left);
        checkLessThanOperand(right);
        return new ComparisonExpression(left, right) {
            protected boolean asBoolean(int answer) {
                return answer > 0;
            }

            public String getExpressionSymbol() {
                return ">";
            }
        };
    }

    public static BooleanExpression createGreaterThanEqual(final Expression left, final Expression right) {
        checkLessThanOperand(left);
        checkLessThanOperand(right);
        return new ComparisonExpression(left, right) {
            protected boolean asBoolean(int answer) {
                return answer >= 0;
            }

            public String getExpressionSymbol() {
                return ">=";
            }
        };
    }

    public static BooleanExpression createLessThan(final Expression left, final Expression right) {
        checkLessThanOperand(left);
        checkLessThanOperand(right);
        return new ComparisonExpression(left, right) {

            protected boolean asBoolean(int answer) {
                return answer < 0;
            }

            public String getExpressionSymbol() {
                return "<";
            }

        };
    }

    public static BooleanExpression createLessThanEqual(final Expression left, final Expression right) {
        checkLessThanOperand(left);
        checkLessThanOperand(right);
        return new ComparisonExpression(left, right) {

            protected boolean asBoolean(int answer) {
                return answer <= 0;
            }

            public String getExpressionSymbol() {
                return "<=";
            }
        };
    }

    /**
     * Only Numeric expressions can be used in &gt;, &gt;=, &lt; or &lt;= expressions.s
     * 
     * @param expr {@link Expression}
     */
    public static void checkLessThanOperand(Expression expr) {
        if (expr instanceof ConstantExpression) {
            Object value = ((ConstantExpression)expr).getValue();
            if (value instanceof Number) {
                return;
            }

            // Else it's boolean or a String..
            throw new RuntimeException("Value '" + expr + "' cannot be compared.");
        }
        if (expr instanceof BooleanExpression) {
            throw new RuntimeException("Value '" + expr + "' cannot be compared.");
        }
    }


    /**
     * @param left left {@link Expression}
     * @param right right {@link Expression}
     */
    private static void checkEqualOperandCompatability(Expression left, Expression right) {
        if (left instanceof ConstantExpression && right instanceof ConstantExpression) {
            if (left instanceof BooleanExpression && !(right instanceof BooleanExpression)) {
                throw new RuntimeException("'" + left + "' cannot be compared with '" + right + "'");
            }
        }
    }

    public Field evaluate(ExpressionContext expressionContext) throws ExpressionException {
        Comparable<Comparable> lv = (Comparable)left.evaluate(expressionContext).getValue();
        if (lv == null) {
            return wrapWithField(null);
        }
        Comparable rv = (Comparable)right.evaluate(expressionContext).getValue();
        if (rv == null) {
            return wrapWithField(null);
        }
        return wrapWithField(compare(lv, rv));
    }

    protected Boolean compare(Comparable lv, Comparable rv) {
        Class<? extends Comparable> lc = lv.getClass();
        Class<? extends Comparable> rc = rv.getClass();
        // If the the objects are not of the same type,
        // try to convert up to allow the comparison.
        if (lc != rc) {
            try {
                if (lc == Boolean.class) {
                    if (convertStringExpressions && rc == String.class) {
                        rv = Boolean.valueOf((String)rv);
                    } else {
                        return Boolean.FALSE;
                    }
                } else if (lc == Byte.class) {
                    if (rc == Short.class) {
                        lv = Short.valueOf(((Number)lv).shortValue());
                    } else if (rc == Integer.class) {
                        lv = Integer.valueOf(((Number)lv).intValue());
                    } else if (rc == Long.class) {
                        lv = Long.valueOf(((Number)lv).longValue());
                    } else if (rc == Float.class) {
                        lv = new Float(((Number)lv).floatValue());
                    } else if (rc == Double.class) {
                        lv = new Double(((Number)lv).doubleValue());
                    } else if (rc == BigInteger.class) {
                        lv = BigInteger.valueOf((Byte)lv);
                    } else if (rc == BigDecimal.class) {
                        lv = BigDecimal.valueOf((Byte)lv);
                    } else if (convertStringExpressions && rc == String.class) {
                        rv = Byte.valueOf((String)rv);
                    } else {
                        return Boolean.FALSE;
                    }
                } else if (lc == Short.class) {
                    if (rc == Integer.class) {
                        lv = Integer.valueOf(((Number)lv).intValue());
                    } else if (rc == Long.class) {
                        lv = Long.valueOf(((Number)lv).longValue());
                    } else if (rc == Float.class) {
                        lv = new Float(((Number)lv).floatValue());
                    } else if (rc == Double.class) {
                        lv = new Double(((Number)lv).doubleValue());
                    } else if (rc == BigInteger.class) {
                        lv = BigInteger.valueOf((Short)lv);
                    } else if (rc == BigDecimal.class) {
                        lv = BigDecimal.valueOf((Short)lv);
                    } else if (convertStringExpressions && rc == String.class) {
                        rv = Short.valueOf((String)rv);
                    } else {
                        return Boolean.FALSE;
                    }
                } else if (lc == Integer.class) {
                    if (rc == Long.class) {
                        lv = Long.valueOf(((Number)lv).longValue());
                    } else if (rc == Float.class) {
                        lv = new Float(((Number)lv).floatValue());
                    } else if (rc == Double.class) {
                        lv = new Double(((Number)lv).doubleValue());
                    } else if (rc == BigInteger.class) {
                        lv = BigInteger.valueOf((Integer)lv);
                    } else if (rc == BigDecimal.class) {
                        lv = BigDecimal.valueOf((Integer)lv);
                    } else if (convertStringExpressions && rc == String.class) {
                        rv = Integer.valueOf((String)rv);
                    } else {
                        return Boolean.FALSE;
                    }
                } else if (lc == Long.class) {
                    if (rc == Integer.class) {
                        rv = Long.valueOf(((Number)rv).longValue());
                    } else if (rc == Float.class) {
                        lv = new Float(((Number)lv).floatValue());
                    } else if (rc == Double.class) {
                        lv = new Double(((Number)lv).doubleValue());
                    } else if (rc == BigInteger.class) {
                        lv = BigInteger.valueOf((Long)lv);
                    } else if (rc == BigDecimal.class) {
                        lv = BigDecimal.valueOf((Long)lv);
                    } else if (convertStringExpressions && rc == String.class) {
                        rv = Long.valueOf((String)rv);
                    } else {
                        return Boolean.FALSE;
                    }
                } else if (lc == Float.class) {
                    if (rc == Integer.class) {
                        rv = new Float(((Number)rv).floatValue());
                    } else if (rc == Long.class) {
                        rv = new Float(((Number)rv).floatValue());
                    } else if (rc == Double.class) {
                        lv = new Double(((Number)lv).doubleValue());
                    } else if (rc == BigInteger.class) {
                        rv = new BigDecimal((BigInteger)rv);
                        lv = BigDecimal.valueOf((Float)lv);
                    } else if (rc == BigDecimal.class) {
                        lv = BigDecimal.valueOf((Float)lv);
                    } else if (convertStringExpressions && rc == String.class) {
                        rv = Float.valueOf((String)rv);
                    } else {
                        return Boolean.FALSE;
                    }
                } else if (lc == Double.class) {
                    if (rc == Integer.class) {
                        rv = new Double(((Number)rv).doubleValue());
                    } else if (rc == Long.class) {
                        rv = new Double(((Number)rv).doubleValue());
                    } else if (rc == Float.class) {
                        rv = new Double(((Number)rv).doubleValue());
                    } else if (rc == BigInteger.class) {
                        rv = new BigDecimal((BigInteger)rv);
                        lv = BigDecimal.valueOf((Double)lv);
                    } else if (rc == BigDecimal.class) {
                        lv = BigDecimal.valueOf((Double)lv);
                    } else if (convertStringExpressions && rc == String.class) {
                        rv = Double.valueOf((String)rv);
                    } else {
                        return Boolean.FALSE;
                    }
                } else if (convertStringExpressions && lc == String.class) {

                    if (rc == Boolean.class) {
                        lv = Boolean.valueOf((String)lv);
                    } else if (rc == Byte.class) {
                        lv = Byte.valueOf((String)lv);
                    } else if (rc == Short.class) {
                        lv = Short.valueOf((String)lv);
                    } else if (rc == Integer.class) {
                        lv = Integer.valueOf((String)lv);
                    } else if (rc == Long.class) {
                        lv = Long.valueOf((String)lv);
                    } else if (rc == Float.class) {
                        lv = Float.valueOf((String)lv);
                    } else if (rc == Double.class) {
                        lv = Double.valueOf((String)lv);
                    } else if (rc == BigInteger.class) {
                        lv = new BigInteger((String)lv);
                    } else if (rc == BigDecimal.class) {
                        lv = new BigDecimal((String)lv);
                    } else {
                        return Boolean.FALSE;
                    }
                } else if (lc == BigInteger.class) {
                    if (rc == Byte.class) {
                        rv = BigInteger.valueOf(((Byte)rv));
                    } else if (rc == Short.class) {
                        rv = BigInteger.valueOf((Short)rv);
                    } else if (rc == Integer.class) {
                        rv = BigInteger.valueOf((Integer)rv);
                    } else if (rc == Long.class) {
                        rv = BigInteger.valueOf((Long)rv);
                    } else if (rc == Float.class) {
                        lv = new BigDecimal((BigInteger)lv);
                        rv = BigDecimal.valueOf(((Float)rv));
                    } else if (rc == Double.class) {
                        lv = new BigDecimal((BigInteger)lv);
                        rv = BigDecimal.valueOf((Double)rv);
                    } else if (rc == BigDecimal.class) {
                        lv = new BigDecimal((BigInteger)lv);
                    } else if (rc != BigInteger.class) {
                        return Boolean.FALSE;
                    }
                } else if (lc == BigDecimal.class) {
                    if (rc == Byte.class) {
                        rv = BigDecimal.valueOf((Byte)rv);
                    } else if (rc == Short.class) {
                        rv = BigDecimal.valueOf((Short)rv);
                    } else if (rc == Integer.class) {
                        rv = BigDecimal.valueOf((Integer)rv);
                    } else if (rc == Long.class) {
                        rv = BigDecimal.valueOf((Long)rv);
                    } else if (rc == Float.class) {
                        rv = BigDecimal.valueOf((Float)rv);
                    } else if (rc == Double.class) {
                        rv = BigDecimal.valueOf((Double)rv);
                    } else if (rc == BigInteger.class) {
                        rv = new BigDecimal((BigInteger)rv);
                    } else if (rc != BigDecimal.class) {
                        return Boolean.FALSE;
                    }
                } else {
                    return Boolean.FALSE;
                }
            } catch (NumberFormatException e) {
                return Boolean.FALSE;
            }
        }
        return asBoolean(lv.compareTo(rv)) ? Boolean.TRUE : Boolean.FALSE;
    }

    protected abstract boolean asBoolean(int answer);

    public boolean matches(ExpressionContext message) throws ExpressionException {
        Object object = evaluate(message).getValue();
        return object != null && object == Boolean.TRUE;
    }

}
