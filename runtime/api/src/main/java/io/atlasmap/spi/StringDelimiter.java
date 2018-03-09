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
package io.atlasmap.spi;

import java.util.ArrayList;
import java.util.List;

public enum StringDelimiter {
    AMPERSAND("Ampersand", "&", "&"),
    AT_SIGN("AtSign", "@", "@"),
    BACKSLASH("Backslash", "\\\\", "\\"),
    COLON("Colon", ":", ":"),
    COMMA("Comma", ",", ","),
    DASH("Dash", "-", "-"),
    EQUAL("Equal", "=", "="),
    HASH("Hash", "#", "#"),
    MULTI_SPACE("MultiSpace", "\\s+", "    "),
    PERIOD("Period", "\\.", "."),
    PIPE("Pipe", "\\|", "|"),
    SEMICOLON("Semicolon", ";", ";"),
    SLASH("Slash", "/", "/"),
    SPACE("Space", "\\s", " "),
    UNDERSCORE("Underscore", "_", "_");

    private String name;
    private String regex;
    private String value;

    StringDelimiter(String name, String regex, String value) {
        this.name = name;
        this.regex = regex;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public String getValue() {
        return value;
    }

    public static StringDelimiter fromName(String name) {
        for (StringDelimiter entry : values()) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    public static List<String> getAllRegexes() {
        List<String> values = new ArrayList<String>();
        for (StringDelimiter en : values()) {
            values.add(en.getRegex());
        }
        return values;
    }

    public static List<String> getAllNames() {
        List<String> names = new ArrayList<String>();
        for (StringDelimiter en : values()) {
            names.add(en.getName());
        }
        return names;
    }
}
