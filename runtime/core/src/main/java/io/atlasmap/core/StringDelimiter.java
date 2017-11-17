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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum StringDelimiter {
    SPACE("Space", "\\s"), MULTISPACE("MultiSpace", "\\s+"), COMMA("Comma", ","), COLON("Colon", ":");

    private String name;
    private String value;

    StringDelimiter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static List<StringDelimiter> getAll() {
        return Arrays.asList(SPACE, MULTISPACE, COMMA, COLON);
    }

    public static List<String> getAllValues() {
        List<String> values = new ArrayList<String>();
        List<StringDelimiter> enums = getAll();
        for (StringDelimiter en : enums) {
            values.add(en.getValue());
        }
        return values;
    }

    public static List<String> getAllNames() {
        List<String> names = new ArrayList<String>();
        List<StringDelimiter> enums = getAll();
        for (StringDelimiter en : enums) {
            names.add(en.getName());
        }
        return names;
    }
}
