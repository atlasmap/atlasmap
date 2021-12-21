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
package io.atlasmap.actions;

import java.util.UUID;
import java.util.regex.Pattern;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.Capitalize;
import io.atlasmap.v2.FileExtension;
import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.Lowercase;
import io.atlasmap.v2.LowercaseChar;
import io.atlasmap.v2.Normalize;
import io.atlasmap.v2.RemoveFileExtension;
import io.atlasmap.v2.SeparateByDash;
import io.atlasmap.v2.SeparateByUnderscore;
import io.atlasmap.v2.Trim;
import io.atlasmap.v2.TrimLeft;
import io.atlasmap.v2.TrimRight;
import io.atlasmap.v2.Uppercase;
import io.atlasmap.v2.UppercaseChar;

/**
 * The String field actions that doesn't have any parameter.
 */
public class StringSimpleFieldActions implements AtlasFieldAction {

    /** The regular expression for the string separator. */
    public static final String STRING_SEPARATOR_REGEX = "[\\s+\\:\\_\\+\\=\\-]+";
    /** The compiled {@link Pattern} for {@link #STRING_SEPARATOR_REGEX}. */
    public static final Pattern STRING_SEPARATOR_PATTERN = Pattern.compile(STRING_SEPARATOR_REGEX);

    /**
     * Capitalize the source String.
     * @param action action model
     * @param input source
     * @return capitalized
     */
    @AtlasActionProcessor
    public static String capitalize(Capitalize action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        if (input.length() == 1) {
            return String.valueOf(input.charAt(0)).toUpperCase();
        }
        return String.valueOf(input.charAt(0)).toUpperCase() + input.substring(1);
    }

    /**
     * Gets the file extension from the source String if it has. For example,
     * if the source string is "test.zip" then "zip" is returned. It returns "null"
     * if there's no extension.
     * @param action action model
     * @param input source
     * @return file extension if exists, or null
     */
    @AtlasActionProcessor
    public static String fileExtension(FileExtension action, String input) {
        if (input == null) {
            return null;
        }

        int ndx = input.lastIndexOf('.');
        return ndx < 0 ? null : input.substring(ndx + 1);
    }

    /**
     * Gets the UUID String.
     * @param action action model
     * @return UUID String
     */
    @AtlasActionProcessor
    public static String genareteUUID(GenerateUUID action) {
        return UUID.randomUUID().toString();
    }

    /**
     * Gets the lower cased string.
     * @param action action model
     * @param input source
     * @return lower cased
     */
    @AtlasActionProcessor
    public static String lowercase(Lowercase action, String input) {
        if (input == null) {
            return null;
        }

        return input.toLowerCase();
    }

    /**
     * Gets the lower ased character.
     * @param action action model
     * @param input source
     * @return lower cased
     */
    @AtlasActionProcessor
    public static Character lowercaseChar(LowercaseChar action, Character input) {
        if (input == null) {
            return null;
        }

        return String.valueOf(input).toLowerCase().charAt(0);
    }

    /**
     * Replaces the repeating spaces to a single space.
     * @param action action model
     * @param input source
     * @return single spaced
     */
    @AtlasActionProcessor
    public static String normalize(Normalize action, String input) {
        return input == null ? null : input.replaceAll("\\s+", " ").trim();
    }

    /**
     * Removes the file extension if there is, or returns the source String as-is. For example,
     * if the source string is "test.zip", "test" is returned.
     * @param action action model
     * @param input source
     * @return processed
     */
    @AtlasActionProcessor
    public static String removeFileExtension(RemoveFileExtension action, String input) {
        if (input == null) {
            return null;
        }

        int ndx = input.lastIndexOf('.');
        return ndx < 0 ? input : input.substring(0, ndx);
    }

    /**
     * Replaces the string separator to the dash(-). The regular expression for detecting
     * separators to replace is {@link #STRING_SEPARATOR_REGEX}.
     * @param action action model
     * @param input source
     * @return processed
     */
    @AtlasActionProcessor
    public static String separateByDash(SeparateByDash action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        return STRING_SEPARATOR_PATTERN.matcher(input).replaceAll("-");
    }

    /**
     * Replaces the string separator to the underscore(_). The regular expression for detecting
     * separators to replace is {@link #STRING_SEPARATOR_REGEX}.
     * @param action action model
     * @param input source
     * @return processed
     */
    @AtlasActionProcessor
    public static String separateByUnderscore(SeparateByUnderscore action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        return STRING_SEPARATOR_PATTERN.matcher(input).replaceAll("_");
    }

    /**
     * Trims the source String by using {@link String#trim()}.
     * @param action action model
     * @param input source
     * @return processed
     */
    @AtlasActionProcessor
    public static String trim(Trim action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        return input.trim();
    }

    /**
     * Trims the white spaces at the left.
     * @param action action model
     * @param input source
     * @return processed
     */
    @AtlasActionProcessor
    public static String trimLeft(TrimLeft action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        int i = 0;
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        return input.substring(i);
    }

    /**
     * Trims the white spaces at the right.
     * @param action action model
     * @param input source
     * @return processed
     */
    @AtlasActionProcessor
    public static String trimRight(TrimRight action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        int i = input.length() - 1;
        while (i >= 0 && Character.isWhitespace(input.charAt(i))) {
            i--;
        }
        return input.substring(0, i + 1);
    }

    /**
     * Gets the upper cased string.
     * @param action action model
     * @param input source
     * @return upper cased
     */
    @AtlasActionProcessor
    public static String uppercase(Uppercase action, String input) {
        if (input == null) {
            return null;
        }

        return input.toUpperCase();
    }

    /**
     * Gets the upper cased character.
     * @param action action model
     * @param input source
     * @return upper cased
     */
    @AtlasActionProcessor
    public static Character uppercaseChar(UppercaseChar action, Character input) {
        if (input == null) {
            return null;
        }

        return String.valueOf(input).toUpperCase().charAt(0);
    }
}
