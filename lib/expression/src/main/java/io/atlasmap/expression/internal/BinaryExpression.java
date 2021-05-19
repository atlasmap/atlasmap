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

/**
 * An expression which performs an operation on two expression values.
 * 
 * @version $Revision: 1.2 $
 */
public abstract class BinaryExpression implements Expression {
    protected Expression left;
    protected Expression right;

    public BinaryExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }


    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "(" + left.toString() + " " + getExpressionSymbol() + " " + right.toString() + ")";
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
     */
    public boolean equals(Object o) {

        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return toString().equals(o.toString());

    }

    /**
     * Returns the symbol that represents this binary expression.  For example, addition is
     * represented by "+"
     *
     * @return expression symbol string
     */
    public abstract String getExpressionSymbol();

    /**
     * @param expression right {@link Expression}
     */
    public void setRight(Expression expression) {
        right = expression;
    }

    /**
     * @param expression left {@link Expression}
     */
    public void setLeft(Expression expression) {
        left = expression;
    }
    
}
