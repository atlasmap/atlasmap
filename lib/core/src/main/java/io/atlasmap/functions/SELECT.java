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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.BaseFunctionFactory;
import io.atlasmap.expression.Expression;
import io.atlasmap.expression.parser.ParseException;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

public class SELECT extends BaseFunctionFactory {

    @Override
    public Expression create(List<Expression> args) throws ParseException {
        if (args.size() != 2) {
            throw new ParseException("SELECT expects 2 arguments.");
        }
        Expression parentExpression = args.get(0);
        Expression selectExpression =args.get(1);
        return (ctx) -> {
            Field parent = (Field) parentExpression.evaluate(ctx);
            List<Field> collection = parent instanceof FieldGroup ? ((FieldGroup)parent).getField() : Arrays.asList(parent);
            List<Field> selected = new ArrayList<>();
            final FieldGroup answer = AtlasModelFactory.createFieldGroupFrom(parent, true);
            answer.setPath(FUNCTION_PATH);
            for (Field f : collection) {
                Field fs = (Field) selectExpression.evaluate((subCtx) -> {
                    if (subCtx != null && FUNCTION_PATH.equals(answer.getPath())) {
                        answer.setPath(parent.getPath() +
                            (subCtx.startsWith(AtlasPath.PATH_SEPARATOR) ? subCtx : (AtlasPath.PATH_SEPARATOR + subCtx)));
                    }
                    return AtlasPath.extractChildren(f, subCtx);
                });
                selected.add(fs);
            }
            if (selected.size() == 1) {
                return selected.get(0);
            }
            answer.getField().addAll(selected);
            return answer;
        };
    }

}