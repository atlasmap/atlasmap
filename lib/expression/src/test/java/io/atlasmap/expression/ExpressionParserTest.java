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
package io.atlasmap.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.atlasmap.expression.internal.ComparisonExpression;
import io.atlasmap.expression.internal.LogicExpression;
import io.atlasmap.expression.internal.VariableExpression;

import org.junit.jupiter.api.Test;


public class ExpressionParserTest {

    @Test
    public void testParseWithParensAround() throws Exception {
        String[] values = {"${x} == 1 && ${y} == 2", "(${x} == 1) && (${y} == 2)", "((${x} == 1) && (${y} == 2))"};

        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            info("Parsing: " + value);

            Expression andExpression = parse(value);
            assertTrue(andExpression instanceof LogicExpression, "Created LogicExpression expression");
            LogicExpression logicExpression = (LogicExpression)andExpression;
            Expression left = logicExpression.getLeft();
            Expression right = logicExpression.getRight();

            assertTrue(left instanceof ComparisonExpression, "Left is a binary filter");
            assertTrue(right instanceof ComparisonExpression, "Right is a binary filter");
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
        assertTrue(expression instanceof VariableExpression, message + ". Must be PropertyExpression");
        VariableExpression propExp = (VariableExpression)expression;
        assertEquals(expected, propExp.getName(), message + ". Property name");
    }

    protected Expression parse(String text) throws Exception {
        return Expression.parse(text, null);
    }
}
