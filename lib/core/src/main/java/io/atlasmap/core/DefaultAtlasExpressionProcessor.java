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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.expression.Expression;
import io.atlasmap.expression.ExpressionException;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.SimpleField;

public class DefaultAtlasExpressionProcessor {
    private static Logger LOG = LoggerFactory.getLogger(DefaultAtlasExpressionProcessor.class);

    public static void processExpression(DefaultAtlasSession session, io.atlasmap.v2.Expression action) {
        if (action.getExpression() == null || action.getExpression().trim().isEmpty()) {
            return;
        }

        try {
            Expression parsedExpression = Expression.parse(action.getExpression(),
                    DefaultAtlasFunctionResolver.getInstance());
            Object answer = parsedExpression.evaluate((path) -> {
                if (path == null || path.isEmpty()) {
                    return null;
                }
                try {
                    String[] splitted = path.split(":", 2);
                    Field f = new SimpleField();
                    f.setDocId(splitted[0]);
                    f.setPath(splitted[1]);
                    AtlasFieldReader reader = session.getFieldReader(splitted[0]);
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
                    action.getExpression(), AuditStatus.ERROR, null);
            if (LOG.isDebugEnabled()) {
                LOG.debug("", e);
            }
        }
    }

}
