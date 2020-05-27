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

import java.util.Arrays;
import java.util.List;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.BaseFunctionFactory;
import io.atlasmap.expression.Expression;
import io.atlasmap.expression.internal.BooleanExpression;
import io.atlasmap.expression.parser.ParseException;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

public class FILTER extends BaseFunctionFactory {

    @Override
    public Expression create(List<Expression> args) throws ParseException {
        if (args.size() != 2) {
            throw new ParseException("FILTER expects 2 arguments.");
        }
        Expression parentExpression = args.get(0);
        BooleanExpression filterExpression = BooleanExpression.asBooleanExpression(args.get(1));
        return (ctx) -> {
            Field parent = (Field) parentExpression.evaluate(ctx);
            List<Field> collection = parent instanceof FieldGroup ? ((FieldGroup)parent).getField() : Arrays.asList(parent);
            FieldGroup filtered = AtlasModelFactory.createFieldGroupFrom(parent, true);
            int index = 0;
            for (Field f : collection) {
                if (
                    filterExpression.matches((subCtx) -> {
                        return AtlasPath.extractChildren(f, subCtx);
                    })
                ) {
                    adjustRootCollectionIndex(f, index);
                    index++;
                    filtered.getField().add(f);
                }
            }
            return filtered;
        };
    }

    private void adjustRootCollectionIndex(Field f, int index) {
        AtlasPath filteredPath = new AtlasPath(f.getPath());
        Integer collectionSegmentIndex = null;
        List<AtlasPath.SegmentContext> filteredSegments = filteredPath.getSegments(true);
        if (filteredSegments.get(0).getCollectionIndex() != null) {
            collectionSegmentIndex = 0;
        } else if (filteredSegments.get(1).getCollectionIndex() != null) {
            collectionSegmentIndex = 1;
        }

        if (collectionSegmentIndex != null) {
            if (f instanceof FieldGroup) {
                AtlasPath.setCollectionIndexRecursively((FieldGroup) f, collectionSegmentIndex, index);
            } else {
                filteredPath.setCollectionIndex(collectionSegmentIndex, index);
                f.setPath(filteredPath.getOriginalPath());
            }

        }
    }

}
