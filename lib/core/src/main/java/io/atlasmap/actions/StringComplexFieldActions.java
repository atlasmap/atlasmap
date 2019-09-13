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

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.v2.Append;
import io.atlasmap.v2.Concatenate;
import io.atlasmap.v2.EndsWith;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Format;
import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.IndexOf;
import io.atlasmap.v2.LastIndexOf;
import io.atlasmap.v2.PadStringLeft;
import io.atlasmap.v2.PadStringRight;
import io.atlasmap.v2.Prepend;
import io.atlasmap.v2.ReplaceAll;
import io.atlasmap.v2.ReplaceFirst;
import io.atlasmap.v2.Split;
import io.atlasmap.v2.StartsWith;
import io.atlasmap.v2.SubString;
import io.atlasmap.v2.SubStringAfter;
import io.atlasmap.v2.SubStringBefore;

public class StringComplexFieldActions implements AtlasFieldAction {

    public static final String STRING_SEPARATOR_REGEX = "^\\s+:_+=";
    public static final Pattern STRING_SEPARATOR_PATTERN = Pattern.compile(STRING_SEPARATOR_REGEX);

    @AtlasActionProcessor
    public static String append(Append append, String input) {
        if (append == null) {
            throw new IllegalArgumentException("Append must be specified with a string");
        }
        String string = append.getString();
        if (input == null && string == null) {
            return null;
        }
        if (string == null) {
            return input.toString();
        }
        return input == null ? string : input.toString().concat(string);
    }

    @AtlasActionProcessor(sourceType = FieldType.ANY)
    public static String concatenate(Concatenate concat, List<String> inputs) {
        if (concat == null) {
            throw new IllegalArgumentException("Concatenate must be specified with a delimiter");
        }
        if (inputs == null) {
            return null;
        }

        String delim = concat.getDelimiter() == null ? "" : concat.getDelimiter();
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (String entry : inputs) {
            if (!isFirst) {
                builder.append(delim);
            }
            if (entry != null) {
                builder.append(entry);
            }
            isFirst =false;
        }

        return builder.toString();
    }

    @AtlasActionProcessor
    public static Boolean endsWith(EndsWith endsWith, String input) {
        if (endsWith == null || endsWith.getString() == null) {
            throw new IllegalArgumentException("EndsWith must be specified with a string");
        }

        return input == null ? false : input.endsWith(endsWith.getString());
    }

    @AtlasActionProcessor
    public static String format(Format format, List<Object> input) {
        if (format == null || format.getTemplate() == null) {
            throw new IllegalArgumentException("Format must be specified with a template");
        }

        return String.format(Locale.ROOT, format.getTemplate(), input == null ? null : input.toArray(new Object[0]));
    }

    @AtlasActionProcessor
    public static String genareteUUID(GenerateUUID action) {
        return UUID.randomUUID().toString();
    }

    @AtlasActionProcessor
    public static Number indexOf(IndexOf indexOf, String input) {
        if (indexOf == null || indexOf.getString() == null) {
            throw new IllegalArgumentException("IndexOf must be specified with a string");
        }

        return input == null ? -1 : input.indexOf(indexOf.getString());
    }

    @AtlasActionProcessor
    public static Number lastIndexOf(LastIndexOf lastIndexOf, String input) {
        if (lastIndexOf == null || lastIndexOf.getString() == null) {
            throw new IllegalArgumentException("LastIndexOf must be specified with a string");
        }

        return input == null ? -1 : input.lastIndexOf(lastIndexOf.getString());
    }

    @AtlasActionProcessor
    public static String padStringRight(PadStringRight padStringRight, String input) {
        if (padStringRight == null || padStringRight.getPadCharacter() == null
                || padStringRight.getPadCount() == null) {
            throw new IllegalArgumentException("PadStringRight must be specified with padCharacter and padCount");
        }

        StringBuilder builder = new StringBuilder();
        if (input != null) {
            builder.append(input);
        }
        for (int i = 0; i < padStringRight.getPadCount(); i++) {
            builder.append(padStringRight.getPadCharacter());
        }

        return builder.toString();
    }

    @AtlasActionProcessor
    public static String padStringLeft(PadStringLeft padStringLeft, String input) {
        if (padStringLeft == null || padStringLeft.getPadCharacter() == null
                || padStringLeft.getPadCount() == null) {
            throw new IllegalArgumentException("PadStringLeft must be specified with padCharacter and padCount");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < padStringLeft.getPadCount(); i++) {
            builder.append(padStringLeft.getPadCharacter());
        }
        if (input != null) {
            builder.append(input);
        }

        return builder.toString();
    }

    @AtlasActionProcessor
    public static String prepend(Prepend action, String input) {
        String string = action.getString();
        if (input == null) {
            return string;
        }
        if (string == null) {
            return input;
        }
        return string.concat(input);
    }

    @AtlasActionProcessor
    public static String replaceAll(ReplaceAll replaceAll, String input) {
        if (replaceAll == null || replaceAll.getMatch() == null || replaceAll.getMatch().isEmpty()) {
            throw new IllegalArgumentException("ReplaceAll action must be specified with a non-empty old string");
        }
        String match = replaceAll.getMatch();
        String newString = replaceAll.getNewString();
        return input == null ? null : input.replaceAll(match, newString == null ? "" : newString);
    }

    @AtlasActionProcessor
    public static String replaceFirst(ReplaceFirst replaceFirst, String input) {
        if (replaceFirst == null || replaceFirst.getMatch() == null || replaceFirst.getMatch().isEmpty()) {
            throw new IllegalArgumentException("ReplaceFirst action must be specified with a non-empty old string");
        }
        String match = replaceFirst.getMatch();
        String newString = replaceFirst.getNewString();
        return input == null ? null : input.replaceFirst(match, newString == null ? "" : newString);
    }

    @AtlasActionProcessor(sourceType = FieldType.ANY)
    public static String[] split(Split split, String input) {
        if (split == null || split.getDelimiter() == null) {
            throw new IllegalArgumentException("Split must be specified with a delimiter");
        }

        return input == null ? null : input.toString().split(split.getDelimiter());
    }

    @AtlasActionProcessor
    public static Boolean startsWith(StartsWith startsWith, String input) {
        if (startsWith == null || startsWith.getString() == null) {
            throw new IllegalArgumentException("StartsWith must be specified with a string");
        }

        return input == null ? false : input.startsWith(startsWith.getString());
    }

    @AtlasActionProcessor
    public static String subString(SubString subString, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        if (subString == null || subString.getStartIndex() == null || subString.getStartIndex() < 0) {
            throw new IllegalArgumentException("SubString action must be specified with a positive startIndex");
        }

        return doSubString(input, subString.getStartIndex(), subString.getEndIndex());
    }

    @AtlasActionProcessor
    public static String subStringAfter(SubStringAfter subStringAfter, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        if (subStringAfter == null || subStringAfter.getStartIndex() == null
                || subStringAfter.getStartIndex() < 0 || subStringAfter.getMatch() == null
                || (subStringAfter.getEndIndex() != null
                        && subStringAfter.getEndIndex() < subStringAfter.getStartIndex())) {
            throw new IllegalArgumentException(
                    "SubStringAfter action must be specified with a positive startIndex and a string to match");
        }

        int idx = input.indexOf(subStringAfter.getMatch());
        if (idx < 0) {
            return input;
        }
        idx = idx + subStringAfter.getMatch().length();
        return doSubString(input.substring(idx), subStringAfter.getStartIndex(), subStringAfter.getEndIndex());
    }

    @AtlasActionProcessor
    public static String subStringBefore(SubStringBefore subStringBefore, String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        if (subStringBefore == null || subStringBefore.getStartIndex() == null
                || subStringBefore.getStartIndex() < 0 || subStringBefore.getMatch() == null
                || (subStringBefore.getEndIndex() != null
                        && subStringBefore.getEndIndex() < subStringBefore.getStartIndex())) {
            throw new IllegalArgumentException(
                    "SubStringBefore action must be specified with a positive startIndex and a string to match");
        }

        int idx = input.indexOf(subStringBefore.getMatch());
        if (idx < 0) {
            return input;
        }

        return doSubString(input.substring(0, idx), subStringBefore.getStartIndex(), subStringBefore.getEndIndex());
    }

    private static String doSubString(String input, Integer startIndex, Integer endIndex) {
        if (endIndex == null) {
            return input.substring(startIndex);
        }

        return input.substring(startIndex, endIndex);
    }
}
