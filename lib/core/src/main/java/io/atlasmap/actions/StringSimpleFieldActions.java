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
package io.atlasmap.actions;

import java.util.regex.Pattern;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

@SuppressWarnings("squid:S1118")
public class StringSimpleFieldActions implements AtlasFieldAction {

    public static final String STRING_SEPARATOR_REGEX = "[\\s+\\:\\_\\+\\=\\-]+";
    public static final Pattern STRING_SEPARATOR_PATTERN = Pattern.compile(STRING_SEPARATOR_REGEX);

    @AtlasFieldActionInfo(name = "Capitalize", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String capitalize(Action action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        if (input.length() == 1) {
            return String.valueOf(input.charAt(0)).toUpperCase();
        }
        return String.valueOf(input.charAt(0)).toUpperCase() + input.substring(1);
    }

    @AtlasFieldActionInfo(name = "FileExtension", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String fileExtension(Action action, String input) {
        if (input == null) {
            return null;
        }

        int ndx = input.lastIndexOf('.');
        return ndx < 0 ? null : input.substring(ndx + 1);
    }

    @AtlasFieldActionInfo(name = "Lowercase", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String lowercase(Action action, String input) {
        if (input == null) {
            return null;
        }

        return input.toLowerCase();
    }

    @AtlasFieldActionInfo(name = "Lowercase", sourceType = FieldType.CHAR, targetType = FieldType.CHAR, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Character lowercaseChar(Action action, Character input) {
        if (input == null) {
            return null;
        }

        return String.valueOf(input).toLowerCase().charAt(0);
    }

    @AtlasFieldActionInfo(name = "Normalize", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String normalize(Action action, String input) {
        return input == null ? null : input.replaceAll("\\s+", " ").trim();
    }

    @AtlasFieldActionInfo(name = "RemoveFileExtension", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String removeFileExtension(Action action, String input) {
        if (input == null) {
            return null;
        }

        int ndx = input.lastIndexOf('.');
        return ndx < 0 ? input : input.substring(0, ndx);
    }

    @AtlasFieldActionInfo(name = "SeparateByDash", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String separateByDash(Action action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        return STRING_SEPARATOR_PATTERN.matcher(input).replaceAll("-");
    }

    @AtlasFieldActionInfo(name = "SeparateByUnderscore", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String separateByUnderscore(Action action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        return STRING_SEPARATOR_PATTERN.matcher(input).replaceAll("_");
    }

    @AtlasFieldActionInfo(name = "Trim", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String trim(Action action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        return input.trim();
    }

    @AtlasFieldActionInfo(name = "TrimLeft", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String trimLeft(Action action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        int i = 0;
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        return input.substring(i);
    }

    @AtlasFieldActionInfo(name = "TrimRight", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String trimRight(Action action, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        int i = input.length() - 1;
        while (i >= 0 && Character.isWhitespace(input.charAt(i))) {
            i--;
        }
        return input.substring(0, i + 1);
    }

    @AtlasFieldActionInfo(name = "Uppercase", sourceType = FieldType.STRING, targetType = FieldType.STRING, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static String uppercase(Action action, String input) {
        if (input == null) {
            return null;
        }

        return input.toUpperCase();
    }

    @AtlasFieldActionInfo(name = "Uppercase", sourceType = FieldType.CHAR, targetType = FieldType.CHAR, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Character uppercaseChar(Action action, Character input) {
        if (input == null) {
            return null;
        }

        return String.valueOf(input).toUpperCase().charAt(0);
    }
}
