/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.functions;

import java.util.List;

import io.atlasmap.core.BaseFunctionFactory;
import io.atlasmap.expression.Expression;
import io.atlasmap.expression.ExpressionContext;
import io.atlasmap.expression.ExpressionException;
import io.atlasmap.expression.internal.BooleanExpression;
import io.atlasmap.expression.parser.ParseException;

public class ISEMPTY extends BaseFunctionFactory {

    @Override
    public Expression create(List<Expression> args) throws ParseException {
        if (args.size() != 1) {
            throw new ParseException("ISEMPTY expects 1 argument.");
        }
        final Expression arg = args.get(0);
        return new BooleanExpression() {
            public Object evaluate(ExpressionContext ctx) throws ExpressionException {
                Object value = arg.evaluate(ctx);
                if (value == null || value.toString().isEmpty()) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            };
            public boolean matches(ExpressionContext ctx) throws ExpressionException {
                Object answer = evaluate(ctx);
                return answer != null && answer == Boolean.TRUE;
            }
        };
    }

}
