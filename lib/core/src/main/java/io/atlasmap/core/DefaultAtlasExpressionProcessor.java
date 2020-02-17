/**
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
package io.atlasmap.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.expression.Expression;
import io.atlasmap.expression.ExpressionException;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

public class DefaultAtlasExpressionProcessor {
    private static Logger LOG = LoggerFactory.getLogger(DefaultAtlasExpressionProcessor.class);

    public static void processExpression(DefaultAtlasSession session, String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }

        try {
            Map<String, Field> sourceFieldMap = new HashMap<>();
            Field parent = session.head().getSourceField();
            if (parent != null && !AtlasUtil.isEmpty(parent.getDocId()) && !AtlasUtil.isEmpty(parent.getPath())) {
                 sourceFieldMap.put(parent.getDocId() + ":" + parent.getPath(), parent);
            }
            // Anonymous FieldGroup is just a wrapping, peel it off
            if (parent instanceof FieldGroup && AtlasUtil.isEmpty(parent.getPath())) {
                FieldGroup parentGroup = FieldGroup.class.cast(parent);
                for (Field child : parentGroup.getField()) {
                    if (!(AtlasUtil.isEmpty(child.getDocId()) && AtlasUtil.isEmpty(child.getPath()))) {
                         sourceFieldMap.put(child.getDocId() + ":" + child.getPath(), child);
                    }
                }
            }

            Expression parsedExpression = Expression.parse(expression, DefaultAtlasFunctionResolver.getInstance());
            Object answer = parsedExpression.evaluate((path) -> {
                if (path == null || path.isEmpty()) {
                    return null;
                }
                try {
                    String[] splitted = path.split(":", 2);
                    AtlasFieldReader reader = session.getFieldReader(splitted[0]);
                    Field f = sourceFieldMap.get(path);
                    if (f == null) {
                        return null;
                    }
                    session.head().setSourceField(f);
                    return reader.read(session);
                } catch (Exception e) {
                    throw new ExpressionException(e);
                }
            });
            session.head().setSourceField((Field)answer);
        } catch (Exception e) {
            AtlasUtil.addAudit(session, null,
                    String.format("Expression processing error: %s", e.getMessage()),
                    expression, AuditStatus.ERROR, null);
            if (LOG.isDebugEnabled()) {
                LOG.debug("", e);
            }
        }
    }

}
