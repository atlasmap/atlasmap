/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

import io.atlasmap.expression.internal.ComparisonExpression;
import io.atlasmap.expression.parser.ParseException;
import junit.framework.TestCase;

import io.atlasmap.expression.internal.BooleanExpression;

/**
 * @version $Revision: 1.7 $
 */
@SuppressWarnings("unchecked")
public class ExpressionTest extends TestCase {

    static final FunctionResolver FUNCTION_RESOLVER = (name, args) -> {

        name = name.toUpperCase();
        if ("LT".equals(name)) {
            if (args.size() != 2) {
                throw new ParseException("LT expects 2 arguments.");
            }
            return ComparisonExpression.createLessThan(args.get(0), args.get(1));
        } else if ("IF".equals(name)) {
            if (args.size() != 3) {
                throw new ParseException("IF expects 3 argument.");
            }
            BooleanExpression conditional = BooleanExpression.asBooleanExpression(args.get(0));
            Expression trueExpression = args.get(1);
            Expression falseExpression = args.get(2);
            return (ctx) -> {
                if (conditional.matches(ctx)) {
                    return trueExpression.evaluate(ctx);
                } else {
                    return falseExpression.evaluate(ctx);
                }
            };
        } else if ("TOLOWER".equals(name)) {
            if (args.size() != 1) {
                throw new ParseException("TOLOWER expects 1 argument.");
            }
            Expression arg = args.get(0);
            return (ctx) -> {
                Object value = arg.evaluate(ctx);
                if (value == null) {
                    return null;
                }
                return value.toString().toLowerCase();
            };
        }

        throw new ParseException("Unknown function: " + name);
    };

    class MockMessage implements ExpressionContext {

        HashMap<String, Object> properties = new HashMap<String, Object>();
        private String text;
        private Object destination;
        private String messageId;
        private String type;
        private Object localConnectionId;

        public void setDestination(Object destination) {
            this.destination = destination;
        }

        public void setJMSMessageID(String messageId) {
            this.messageId = messageId;
        }

        public void setJMSType(String type) {
            this.type = type;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setBooleanProperty(String key, boolean value) {
            properties.put(key, value);
        }

        public void setStringProperty(String key, String value) {
            properties.put(key, value);
        }

        public void setByteProperty(String key, byte value) {
            properties.put(key, value);
        }

        public void setDoubleProperty(String key, double value) {
            properties.put(key, value);
        }

        public void setFloatProperty(String key, float value) {
            properties.put(key, value);
        }

        public void setLongProperty(String key, long value) {
            properties.put(key, value);
        }

        public void setIntProperty(String key, int value) {
            properties.put(key, value);
        }

        public void setShortProperty(String key, short value) {
            properties.put(key, value);
        }

        public void setObjectProperty(String key, Object value) {
            properties.put(key, value);
        }

        public void setBigIntegerProperty(String key, BigInteger value) {
            properties.put(key, value);
        }

        public void setBigDecimalProperty(String key, BigDecimal value) {
            properties.put(key, value);
        }

        public <T> T getBodyAs(Class<T> type) throws ExpressionException {
            if (type == String.class) {
                return type.cast(text);
            }
            return null;
        }

        public Object getVariable(String name) {
            if ("JMSType".equals(name)) {
                return type;
            }
            if ("JMSMessageID".equals(name)) {
                return messageId;
            }
            return properties.get(name);
        }

        public <T> T getDestination() {
            return (T) destination;
        }

        public Object getLocalConnectionId() {
            return localConnectionId;
        }


    }


    public void testBooleanSelector() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "(${trueProp} || ${falseProp}) && ${trueProp}", true);
        assertSelector(message, "(${trueProp} || ${falseProp}) && ${falseProp}", false);

    }


    public void testJMSPropertySelectors() throws Exception {
        MockMessage message = createMessage();
        message.setJMSType("selector-test");
        message.setJMSMessageID("id:test:1:1:1:1");

        assertSelector(message, "${JMSType} == 'selector-test'", true);
        assertSelector(message, "${JMSType} == 'crap'", false);

        assertSelector(message, "${JMSMessageID} == 'id:test:1:1:1:1'", true);
        assertSelector(message, "${JMSMessageID} == 'id:not-test:1:1:1:1'", false);

        message = createMessage();
        message.setJMSType("1001");

        assertSelector(message, "${JMSType}=='1001'", true);
        assertSelector(message, "${JMSType}=='1001' || ${JMSType}=='1002'", true);
        assertSelector(message, "${JMSType} == 'crap'", false);
    }

    public void testBasicSelectors() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "${name} == 'James'", true);
        assertSelector(message, "${rank} > 100", true);
        assertSelector(message, "${rank} >= 123", true);
        assertSelector(message, "${rank} >= 124", false);

    }

    public void testPropertyTypes() throws Exception {
        MockMessage message = createMessage();
        assertSelector(message, "${byteProp} == 123", true);
        assertSelector(message, "${byteProp} == 10", false);
        assertSelector(message, "${byteProp2} == 33", true);
        assertSelector(message, "${byteProp2} == 10", false);
        assertSelector(message, "${shortProp} == 123", true);
        assertSelector(message, "${shortProp} == 10", false);
        assertSelector(message, "${shortProp} == 123", true);
        assertSelector(message, "${shortProp} == 10", false);
        assertSelector(message, "${intProp} == 123", true);
        assertSelector(message, "${intProp} == 10", false);
        assertSelector(message, "${longProp} == 123", true);
        assertSelector(message, "${longProp} == 10", false);
        assertSelector(message, "${floatProp} == 123", true);
        assertSelector(message, "${floatProp} == 10", false);
        assertSelector(message, "${doubleProp} == 123", true);
        assertSelector(message, "${doubleProp} == 10", false);
        assertSelector(message, "${bigIntegerProp} == 7", true);
        assertSelector(message, "${bigIntegerProp} == 1", false);
        assertSelector(message, "${bigIntegerProp} == 7.1", false);
        assertSelector(message, "${bigDecimalProp} == 7.7", true);
        assertSelector(message, "${bigDecimalProp} == 7.8", false);
        assertSelector(message, "${bigDecimalProp} == 7", false);
        assertSelector(message, "${bigDecimalProp} == 8", false);
    }

    public void testAndSelectors() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "${name} == 'James' && ${rank} < 200", true);
        assertSelector(message, "${name} == 'James' && ${rank} > 200", false);
        assertSelector(message, "${name} == 'Foo' && ${rank} < 200", false);
        assertSelector(message, "${unknown} == 'Foo' && ${anotherUnknown} < 200", false);
    }

    public void testOrSelectors() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "${name} == 'James' || ${rank} < 200", true);
        assertSelector(message, "${name} == 'James' || ${rank} > 200", true);
        assertSelector(message, "${name} == 'Foo' || ${rank} < 200", true);
        assertSelector(message, "${name} == 'Foo' || ${rank} > 200", false);
        assertSelector(message, "${unknown} == 'Foo' || ${anotherUnknown} < 200", null);
    }

    public void testPlus() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "${rank} + 2 == 125", true);
        assertSelector(message, "(${rank} + 2) == 125", true);
        assertSelector(message, "125 == (${rank} + 2)", true);
        assertSelector(message, "${rank} + ${version} == 125", true);
        assertSelector(message, "${rank} + 2 < 124", false);
        assertSelector(message, "${name} + '!' == 'James!'", true);
    }

    public void testMinus() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "${rank} - 2 == 121", true);
        assertSelector(message, "${rank} - ${version} == 121", true);
        assertSelector(message, "${rank} - 2 > 122", false);
    }

    public void testMultiply() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "${rank} * 2 == 246", true);
        assertSelector(message, "${rank} * ${version} == 246", true);
        assertSelector(message, "${rank} * 2 < 130", false);
    }

    public void testDivide() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "${rank} / ${version} == 61.5", true);
        assertSelector(message, "${rank} / 3 > 100.0", false);
        assertSelector(message, "${rank} / 3 > 100", false);
        assertSelector(message, "${version} / 2 == 1", true);

    }


    public void testIsNull() throws Exception {
        MockMessage message = createMessage();

        assertSelector(message, "${dummy} == null", true);
        assertSelector(message, "${dummy} != null", false);
        assertSelector(message, "${name} != null", true);
        assertSelector(message, "${name} == null", false);
    }


    /**
     * Test cases from Mats Henricson
     */
    public void testMatsHenricsonUseCases() throws Exception {
        MockMessage message = createMessage();
        assertSelector(message, "${SessionserverId}==1870414179", false);

        message.setLongProperty("SessionserverId", 1870414179);
        assertSelector(message, "${SessionserverId}==1870414179", true);

        message.setLongProperty("SessionserverId", 1234);
        assertSelector(message, "${SessionserverId}==1870414179", false);
    }

    public void testFloatComparisons() throws Exception {
        MockMessage message = createMessage();

        // JMS 1.1 Section 3.8.1.1 : Approximate literals use the Java
        // floating-point literal syntax.
        // We will use the java varible x to demo valid floating point syntaxs.
        double x;

        // test decimals like x.x
        x = 1.0;
        x = -1.1;
        x = 1.0E1;
        x = 1.1E1;
        x = -1.1E1;
        assertSelector(message, "1.0 < 1.1", true);
        assertSelector(message, "-1.1 < 1.0", true);
        assertSelector(message, "1.0E1 < 1.1E1", true);
        assertSelector(message, "-1.1E1 < 1.0E1", true);

        // test decimals like x.
        x = 1.;
        x = 1.E1;
        assertSelector(message, "1. < 1.1", true);
        assertSelector(message, "-1.1 < 1.", true);
        assertSelector(message, "1.E1 < 1.1E1", true);
        assertSelector(message, "-1.1E1 < 1.E1", true);

        // test decimals like .x
        x = .5;
        x = -.5;
        x = .5E1;
        assertSelector(message, ".1 < .5", true);
        assertSelector(message, "-.5 < .1", true);
        assertSelector(message, ".1E1 < .5E1", true);
        assertSelector(message, "-.5E1 < .1E1", true);

        // test exponents
        x = 4E10;
        x = -4E10;
        x = 5E+10;
        x = 5E-10;
        assertSelector(message, "4E10 < 5E10", true);
        assertSelector(message, "5E8 < 5E10", true);
        assertSelector(message, "-4E10 < 2E10", true);
        assertSelector(message, "-5E8 < 2E2", true);
        assertSelector(message, "4E+10 < 5E+10", true);
        assertSelector(message, "4E-10 < 5E-10", true);
    }

    public void testStringQuoteParsing() throws Exception {
        MockMessage message = createMessage();
        assertSelector(message, "${quote} == '''In God We Trust'''", true);
    }

    public void testToLowerFunction() throws Exception {
        MockMessage message = createMessage();
        assertSelector(message, "ToLower(${name})", "james");
    }

    public void testIfFunction() throws Exception {
        MockMessage message = createMessage();
        assertSelector(message, "IF(.1 < .5, 'good', 'bad')", "good");
        assertSelector(message, "IF(.1 > .5, 'good', 'bad')", "bad");
    }

    public void testNestedFunction() throws Exception {
        MockMessage message = createMessage();
        assertSelector(message, "IF(ToLower(${name}) == 'james', 'good', 'bad')", "good");
        assertSelector(message, "IF(ToLower(${name}) == 'James', 'good', 'bad')", "bad");
    }


    public void testInvalidSelector() throws Exception {
        MockMessage message = createMessage();
        assertInvalidSelector(message, "True && 3+5");
        assertInvalidSelector(message, "=TEST 'test'");
    }

    protected MockMessage createMessage() {
        MockMessage message = createMessage("FOO.BAR");
        message.setJMSType("selector-test");
        message.setJMSMessageID("connection:1:1:1:1");
        message.setObjectProperty("name", "James");
        message.setObjectProperty("location", "London");

        message.setByteProperty("byteProp", (byte) 123);
        message.setByteProperty("byteProp2", (byte) 33);
        message.setShortProperty("shortProp", (short) 123);
        message.setIntProperty("intProp", (int) 123);
        message.setLongProperty("longProp", (long) 123);
        message.setFloatProperty("floatProp", (float) 123);
        message.setDoubleProperty("doubleProp", (double) 123);

        message.setIntProperty("rank", 123);
        message.setIntProperty("version", 2);
        message.setStringProperty("quote", "'In God We Trust'");
        message.setStringProperty("foo", "_foo");
        message.setStringProperty("punctuation", "!#$&()*+,-./:;<=>?@[\\]^`{|}~");
        message.setBooleanProperty("trueProp", true);
        message.setBooleanProperty("falseProp", false);

        message.setBigIntegerProperty("bigIntegerProp", new BigInteger("7"));
        message.setBigDecimalProperty("bigDecimalProp", new BigDecimal("7.7"));
        return message;
    }

    protected void assertInvalidSelector(MockMessage message, String text) {
        try {
            Expression.parse(text, FUNCTION_RESOLVER);
            fail("Created a valid selector");
        } catch (ExpressionException e) {
        }
    }

    protected void assertSelector(MockMessage message, String text, Object expected) throws ExpressionException {
        Expression selector = null;
        selector = Expression.parse(text, FUNCTION_RESOLVER);
        assertTrue("Created a valid selector", selector != null);
        Object value = selector.evaluate(message);
        assertEquals("Selector for: " + text, expected, value);
    }

    protected MockMessage createMessage(String subject) {
        MockMessage message = new MockMessage();
        message.setDestination(subject);
        return message;
    }
}
