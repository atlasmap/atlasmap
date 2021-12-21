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

/**
 * The enumeration of the string delimters.
 */
public enum StringDelimiter {
    /** Ampersand(&amp;). */
    AMPERSAND("Ampersand", "&", "&"),
    /** At sign(@). */
    AT_SIGN("AtSign", "@", "@"),
    /** Backslash(\). */
    BACKSLASH("Backslash", "\\\\", "\\"),
    /** Colon(:). */
    COLON("Colon", ":", ":"),
    /** Comma(,). */
    COMMA("Comma", ",", ","),
    /** Dash(-). */
    DASH("Dash", "-", "-"),
    /** Equal(=). */
    EQUAL("Equal", "=", "="),
    /** Hash(#). */
    HASH("Hash", "#", "#"),
    /** Multispace(  ). */
    MULTI_SPACE("MultiSpace", "\\s+", "    "),
    /** Period(.). */
    PERIOD("Period", "\\.", "."),
    /** Pipe(|). */
    PIPE("Pipe", "\\|", "|"),
    /** Semicolon(;). */
    SEMICOLON("Semicolon", ";", ";"),
    /** Slash(/). */
    SLASH("Slash", "/", "/"),
    /** Space( ). */
    SPACE("Space", "\\s", " "),
    /** Underscore(_). */
    UNDERSCORE("Underscore", "_", "_");

    private String name;
    private String regex;
    private String value;

    /**
     * A constructor.
     * @param name name
     * @param regex regular expression
     * @param value value
     */
    StringDelimiter(String name, String regex, String value) {
        this.name = name;
        this.regex = regex;
        this.value = value;
    }

    /**
     * Gets the name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the regular expression.
     * @return regular expression
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Gets the value.
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the enum from the name.
     * @param name name
     * @return the enum
     */
    public static StringDelimiter fromName(String name) {
        for (StringDelimiter entry : values()) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Gets all regular expressions.
     * @return a list of regular expression strings
     */
    public static List<String> getAllRegexes() {
        List<String> values = new ArrayList<String>();
        for (StringDelimiter en : values()) {
            values.add(en.getRegex());
        }
        return values;
    }

    /**
     * Gets all names.
     * @return a list of names.
     */
    public static List<String> getAllNames() {
        List<String> names = new ArrayList<String>();
        for (StringDelimiter en : values()) {
            names.add(en.getName());
        }
        return names;
    }
}
