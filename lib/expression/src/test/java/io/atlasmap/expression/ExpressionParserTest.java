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
package io.atlasmap.expression;

import io.atlasmap.expression.internal.ComparisonExpression;
import io.atlasmap.expression.internal.LogicExpression;
import io.atlasmap.expression.internal.VariableExpression;
import junit.framework.TestCase;


/**
 * @version $Revision: 1.2 $
 */
public class ExpressionParserTest extends TestCase {

    public void testParseWithParensAround() throws Exception {
        String[] values = {"${x} == 1 && ${y} == 2", "(${x} == 1) && (${y} == 2)", "((${x} == 1) && (${y} == 2))"};

        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            info("Parsing: " + value);

            Expression andExpression = parse(value);
            assertTrue("Created LogicExpression expression", andExpression instanceof LogicExpression);
            LogicExpression logicExpression = (LogicExpression)andExpression;
            Expression left = logicExpression.getLeft();
            Expression right = logicExpression.getRight();

            assertTrue("Left is a binary filter", left instanceof ComparisonExpression);
            assertTrue("Right is a binary filter", right instanceof ComparisonExpression);
            ComparisonExpression leftCompare = (ComparisonExpression)left;
            ComparisonExpression rightCompare = (ComparisonExpression)right;
            assertPropertyExpression("left", leftCompare.getLeft(), "x");
            assertPropertyExpression("right", rightCompare.getLeft(), "y");
        }
    }

    private void info(String s) {
        System.out.println(s);
    }

    protected void assertPropertyExpression(String message, Expression expression, String expected) {
        assertTrue(message + ". Must be PropertyExpression", expression instanceof VariableExpression);
        VariableExpression propExp = (VariableExpression)expression;
        assertEquals(message + ". Property name", expected, propExp.getName());
    }

    protected Expression parse(String text) throws Exception {
        return Expression.parse(text, null);
    }
}
