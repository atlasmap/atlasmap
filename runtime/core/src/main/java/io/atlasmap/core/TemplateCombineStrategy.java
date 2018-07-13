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

import java.text.MessageFormat;
import java.text.StringCharacterIterator;
import java.util.Map;

/**
 * A template-based combine strategy that uses a template in place of a delimiter to combine the input using
 * {@link MessageFormat#format(String, Object...))}.
 * <p>
 * <strong>Warning:</strong> The indexes in the template must be one-based, unlike the
 * {@link MessageFormat#format(String, Object...))} method, which expects zero-based indexes.
 * </p>
 */
public class TemplateCombineStrategy extends DefaultAtlasCombineStrategy {

    @Override
    public String combineValues(Map<Integer, String> values, String template) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        // Default to default combine with default delimiter if template is missing
        if (template == null || template.isEmpty()) {
            return combineValues(values);
        }

        StringBuilder templateBuilder = new StringBuilder();

        // Convert indexes in template to zero-based (since specified by user as one-based)
        StringCharacterIterator iter = new StringCharacterIterator(template);
        boolean escaped = false;
        for (char chr = iter.first(); chr != StringCharacterIterator.DONE; chr = iter.next()) {
            templateBuilder.append(chr);
            if (chr == '\'' && !escaped) {
                escaped = true;
            } else {
                if (chr == '{' && !escaped) {
                    StringBuilder indexBuilder = new StringBuilder();
                    for (chr = iter.next(); Character.isDigit(chr); chr = iter.next()) {
                        indexBuilder.append(chr);
                    }
                    templateBuilder.append(Integer.valueOf(indexBuilder.toString()) - 1);
                    templateBuilder.append(chr);
                }
                escaped = false;
            }
        }

        values = DefaultAtlasCombineStrategy.sortByKey(values);
        return MessageFormat.format(templateBuilder.toString(), values.values().toArray());
    }
}
