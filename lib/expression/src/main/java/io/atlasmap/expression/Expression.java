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

import java.io.StringReader;

import io.atlasmap.expression.internal.LRUCache;
import io.atlasmap.expression.parser.ParseException;
import io.atlasmap.expression.parser.Parser;
import io.atlasmap.v2.Field;

/**
 *
 * Parses and evaluates a simple expression language.  This was originally based
 * on the selector expression language found in ActiveMQ.  It was modified so that
 * it's supports custom functions and it's comparison expressions were less SQL like,
 * and more script like.
 *
 */
public interface Expression {

    LRUCache cache = new LRUCache(100);

    /**
     * Execute the expression against the given context.
     *
     * @param expressionContext
     * @return
     * @throws ExpressionException
     */
    Field evaluate(ExpressionContext expressionContext) throws ExpressionException;

    static Expression parse(String expessionText, FunctionResolver functionResolver) throws ExpressionException {
        if (functionResolver == null) {
            functionResolver = (name, args) -> {
                throw new ParseException("Function not found: " + name);
            };
        }
        Object result = cache.get(expessionText);
        if (result instanceof ExpressionException) {
            throw (ExpressionException) result;
        } else if (result instanceof Expression) {
            return (Expression) result;
        } else {
            String actual = expessionText;
            try {
                Parser parser = new Parser(new StringReader(actual));
                parser.functionResolver = functionResolver;
                Expression e = parser.parse();
                cache.put(expessionText, e);
                return e;
            } catch (Throwable e) {
                ExpressionException fe = new ExpressionException(actual, e);
                cache.put(expessionText, fe);
                throw fe;
            }
        }
    }

    static void clearCache() {
        cache.clear();
    }

}
