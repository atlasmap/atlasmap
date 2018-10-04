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
package io.atlasmap.v2;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ActionsJsonSerializer extends JsonSerializer<Actions> {

    public static final String NAME = "name";
    public static final String CLASS_NAME = "className";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String DAYS = "days";
    public static final String DELIMITER = "delimiter";
    public static final String END_INDEX = "endIndex";
    public static final String FROM_UNIT = "fromUnit";
    public static final String MATCH = "match";
    public static final String METHOD_NAME = "methodName";
    public static final String NEW_STRING = "newString";
    public static final String PAD_CHARACTER = "padCharacter";
    public static final String PAD_COUNT = "padCount";
    public static final String SECONDS = "seconds";
    public static final String START_INDEX = "startIndex";
    public static final String STRING = "string";
    public static final String TEMPLATE = "template";
    public static final String TO_UNIT = "toUnit";
    public static final String VALUE = "value";
    public static final String INDEX = "index";

    @Override
    public void serialize(Actions actions, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeStartArray();

        if (actions == null || actions.getActions() == null || actions.getActions().isEmpty()) {
            gen.writeEndArray();
            return;
        }

        for (Action a : actions.getActions()) {
            writeActionField(gen, a);
        }

        gen.writeEndArray();
    }

    protected void writeActionField(JsonGenerator gen, Action action) throws IOException {

        switch (action.getClass().getSimpleName()) {
            case "AddDays":
                writeAddDays(gen, (AddDays) action);
                break;
            case "AddSeconds":
                writeAddSeconds(gen, (AddSeconds) action);
                break;
            case "Append":
                writeAppend(gen, (Append) action);
                break;
            case "Concatenate":
                writeConcatenate(gen, (Concatenate) action);
                break;
            case "Contains":
                writeContains(gen, (Contains) action);
                break;
            case "ConvertAreaUnit":
                writeConvertAreaUnit(gen, (ConvertAreaUnit) action);
                break;
            case "ConvertDistanceUnit":
                writeConvertDistanceUnit(gen, (ConvertDistanceUnit) action);
                break;
            case "ConvertMassUnit":
                writeConvertMassUnit(gen, (ConvertMassUnit) action);
                break;
            case "ConvertVolumeUnit":
                writeConvertVolumeUnit(gen, (ConvertVolumeUnit) action);
                break;
            case "CustomAction":
                writeCustomAction(gen, (CustomAction) action);
                break;
            case "EndsWith":
                writeEndsWith(gen, (EndsWith) action);
                break;
            case "Equals":
                writeEquals(gen, (Equals) action);
                break;
            case "Format":
                writeFormat(gen, (Format) action);
                break;
            case "IndexOf":
                writeIndexOf(gen, (IndexOf) action);
                break;
            case "ItemAt":
                writeItemAt(gen, (ItemAt) action);
                break;
            case "LastIndexOf":
                writeLastIndexOf(gen, (LastIndexOf) action);
                break;
            case "PadStringLeft":
                writePadStringLeft(gen, (PadStringLeft) action);
                break;
            case "PadStringRight":
                writePadStringRight(gen, (PadStringRight) action);
                break;
            case "Prepend":
                writePrepend(gen, (Prepend) action);
                break;
            case "ReplaceAll":
                writeReplaceAll(gen, (ReplaceAll) action);
                break;
            case "ReplaceFirst":
                writeReplaceFirst(gen, (ReplaceFirst) action);
                break;
            case "Split":
                writeSplit(gen, (Split) action);
                break;
            case "StartsWith":
                writeStartsWith(gen, (StartsWith) action);
                break;
            case "SubString":
                writeSubString(gen, (SubString) action);
                break;
            case "SubStringAfter":
                writeSubStringAfter(gen, (SubStringAfter) action);
                break;
            case "SubStringBefore":
                writeSubStringBefore(gen, (SubStringBefore) action);
                break;
            default:
                gen.writeStartObject();
                gen.writeNullField(action.getClass().getSimpleName());
                gen.writeEndObject();
                break;
        }
    }

    protected void writeAddDays(JsonGenerator gen, AddDays action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("AddDays");
        gen.writeStartObject();
        if (action.getDays() != null) {
            gen.writeNumberField(DAYS, action.getDays());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeAddSeconds(JsonGenerator gen, AddSeconds action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("AddSeconds");
        gen.writeStartObject();
        if (action.getSeconds() != null) {
            gen.writeNumberField(SECONDS, action.getSeconds());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeAppend(JsonGenerator gen, Append action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Append");
        gen.writeStartObject();
        gen.writeStringField(STRING, action.getString());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConcatenate(JsonGenerator gen, Concatenate action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Concatenate");
        gen.writeStartObject();
        gen.writeStringField(DELIMITER, action.getDelimiter());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeContains(JsonGenerator gen, Contains action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Contains");
        gen.writeStartObject();
        gen.writeStringField(VALUE, action.getValue());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConvertAreaUnit(JsonGenerator gen, ConvertAreaUnit action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ConvertAreaUnit");
        gen.writeStartObject();
        gen.writeStringField(FROM_UNIT, action.getFromUnit().value());
        gen.writeStringField(TO_UNIT, action.getToUnit().value());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConvertDistanceUnit(JsonGenerator gen, ConvertDistanceUnit action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ConvertDistanceUnit");
        gen.writeStartObject();
        gen.writeStringField(FROM_UNIT, action.getFromUnit().value());
        gen.writeStringField(TO_UNIT, action.getToUnit().value());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConvertMassUnit(JsonGenerator gen, ConvertMassUnit action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ConvertMassUnit");
        gen.writeStartObject();
        gen.writeStringField(FROM_UNIT, action.getFromUnit().value());
        gen.writeStringField(TO_UNIT, action.getToUnit().value());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConvertVolumeUnit(JsonGenerator gen, ConvertVolumeUnit action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ConvertVolumeUnit");
        gen.writeStartObject();
        gen.writeStringField(FROM_UNIT, action.getFromUnit().value());
        gen.writeStringField(TO_UNIT, action.getToUnit().value());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeCustomAction(JsonGenerator gen, CustomAction customAction) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("CustomAction");

        boolean objectStarted = false;
        if (customAction.getClassName() != null && customAction.getClassName().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField(NAME, customAction.getName().trim());
            gen.writeEndObject();
            objectStarted = true;
        }

        if (customAction.getClassName() != null && customAction.getClassName().trim().length() > 0) {
            if (!objectStarted) {
                gen.writeStartObject();
            }
            gen.writeStringField(CLASS_NAME, customAction.getClassName().trim());
            if (!objectStarted) {
                gen.writeEndObject();
            }
        }

        if (customAction.getMethodName() != null && customAction.getMethodName().trim().length() > 0) {
            if (!objectStarted) {
                gen.writeStartObject();
            }
            gen.writeStringField(METHOD_NAME, customAction.getMethodName().trim());
            if (!objectStarted) {
                gen.writeEndObject();
            }
        }

        gen.writeEndObject();
    }

    protected void writeEndsWith(JsonGenerator gen, EndsWith action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("EndsWith");
        gen.writeStartObject();
        gen.writeStringField(STRING, action.getString());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeEquals(JsonGenerator gen, Equals action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Equals");
        gen.writeStartObject();
        gen.writeStringField(VALUE, action.getValue());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeFormat(JsonGenerator gen, Format action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Format");
        gen.writeStartObject();
        gen.writeStringField(TEMPLATE, action.getTemplate());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeIndexOf(JsonGenerator gen, IndexOf action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("IndexOf");
        gen.writeStartObject();
        gen.writeStringField(STRING, action.getString());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeItemAt(JsonGenerator gen, ItemAt action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ItemAt");
        gen.writeStartObject();
        gen.writeNumberField(INDEX, action.getIndex());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeLastIndexOf(JsonGenerator gen, LastIndexOf action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("LastIndexOf");
        gen.writeStartObject();
        gen.writeStringField(STRING, action.getString());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writePadStringLeft(JsonGenerator gen, PadStringLeft padStringLeft) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("PadStringLeft");
        gen.writeStartObject();
        gen.writeStringField(PAD_CHARACTER, padStringLeft.getPadCharacter());
        gen.writeNumberField(PAD_COUNT, padStringLeft.getPadCount());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writePadStringRight(JsonGenerator gen, PadStringRight padStringRight) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("PadStringRight");
        gen.writeStartObject();
        gen.writeStringField(PAD_CHARACTER, padStringRight.getPadCharacter());
        gen.writeNumberField(PAD_COUNT, padStringRight.getPadCount());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writePrepend(JsonGenerator gen, Prepend action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Prepend");
        gen.writeStartObject();
        gen.writeStringField(STRING, action.getString());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeReplaceAll(JsonGenerator gen, ReplaceAll action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ReplaceAll");
        gen.writeStartObject();
        gen.writeStringField(MATCH, action.getMatch());
        gen.writeStringField(NEW_STRING, action.getNewString());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeReplaceFirst(JsonGenerator gen, ReplaceFirst action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ReplaceFirst");
        gen.writeStartObject();
        gen.writeStringField(MATCH, action.getMatch());
        gen.writeStringField(NEW_STRING, action.getNewString());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeSplit(JsonGenerator gen, Split action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Split");
        gen.writeStartObject();
        gen.writeStringField(STRING, action.getDelimiter());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeStartsWith(JsonGenerator gen, StartsWith action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("StartsWith");
        gen.writeStartObject();
        gen.writeStringField(STRING, action.getString());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeSubString(JsonGenerator gen, SubString subString) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubString");
        gen.writeStartObject();
        gen.writeNumberField(START_INDEX, subString.getStartIndex());
        if (subString.getEndIndex() != null) {
            gen.writeNumberField(END_INDEX, subString.getEndIndex());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeSubStringAfter(JsonGenerator gen, SubStringAfter subStringAfter) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubStringAfter");
        gen.writeStartObject();
        gen.writeStringField(MATCH, subStringAfter.getMatch());
        gen.writeNumberField(START_INDEX, subStringAfter.getStartIndex());
        if (subStringAfter.getEndIndex() != null) {
            gen.writeNumberField(END_INDEX, subStringAfter.getEndIndex());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeSubStringBefore(JsonGenerator gen, SubStringBefore subStringBefore) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubStringBefore");
        gen.writeStartObject();
        gen.writeStringField(MATCH, subStringBefore.getMatch());
        gen.writeNumberField(START_INDEX, subStringBefore.getStartIndex());
        if (subStringBefore.getEndIndex() != null) {
            gen.writeNumberField(END_INDEX, subStringBefore.getEndIndex());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
