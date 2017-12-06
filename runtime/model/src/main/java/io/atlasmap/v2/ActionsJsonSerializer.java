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

    private static final String DATE_FORMAT = "dateFormat";
    private static final String END_INDEX = "endIndex";
    private static final String FROM_UNIT = "fromUnit";
    private static final String START_INDEX = "startIndex";
    private static final String STRING = "string";
    private static final String TO_UNIT = "toUnit";

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
<<<<<<< 47ddb07e1b3541f5aea5c4007ff93368151f40bf
            case "Concatentate":
                writeConcatenate(gen, (Concatenate) action);
                break;
=======
>>>>>>> Issue #151: Implement Number-related p0 field actions
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
            case "CurrentDate":
                writeCurrentDate(gen, (CurrentDate) action);
                break;
            case "CurrentDateTime":
                writeCurrentDateTime(gen, (CurrentDateTime) action);
                break;
            case "CurrentTime":
                writeCurrentTime(gen, (CurrentTime) action);
                break;
            case "CustomAction":
                writeCustomAction(gen, (CustomAction) action);
                break;
<<<<<<< 47ddb07e1b3541f5aea5c4007ff93368151f40bf
            case "EndsWith":
                writeEndsWith(gen, (EndsWith) action);
                break;
            case "Format":
                writeFormat(gen, (Format) action);
                break;
            case "IndexOf":
                writeIndexOf(gen, (IndexOf) action);
                break;
            case "LastIndexOf":
                writeLastIndexOf(gen, (LastIndexOf) action);
                break;
=======
>>>>>>> Issue #151: Implement Number-related p0 field actions
            case "PadStringLeft":
                writePadStringLeft(gen, (PadStringLeft) action);
                break;
            case "PadStringRight":
                writePadStringRight(gen, (PadStringRight) action);
                break;
<<<<<<< 47ddb07e1b3541f5aea5c4007ff93368151f40bf
            case "ReplaceAll":
                writeReplaceAll(gen, (ReplaceAll) action);
                break;
            case "ReplaceFirst":
                writeReplaceFirst(gen, (ReplaceFirst) action);
                break;
            case "StartsWith":
                writeStartsWith(gen, (StartsWith) action);
=======
            case "Replace":
                writeReplace(gen, (Replace) action);
>>>>>>> Issue #151: Implement Number-related p0 field actions
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

    protected void writeConcatenate(JsonGenerator gen, Concatenate action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Concatenate");
        if (action.getDelimiter() != null && action.getDelimiter().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField("delimiter", action.getDelimiter().trim());
            gen.writeEndObject();
        }
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

    protected void writeCurrentDate(JsonGenerator gen, CurrentDate currentDate) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("CurrentDate");
        if (currentDate.getDateFormat() != null && currentDate.getDateFormat().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField(DATE_FORMAT, currentDate.getDateFormat().trim());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    protected void writeCurrentDateTime(JsonGenerator gen, CurrentDateTime currentDateTime) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("CurrentDateTime");
        if (currentDateTime.getDateFormat() != null && currentDateTime.getDateFormat().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField(DATE_FORMAT, currentDateTime.getDateFormat().trim());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    protected void writeCurrentTime(JsonGenerator gen, CurrentTime currentTime) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("CurrentTime");
        if (currentTime.getDateFormat() != null && currentTime.getDateFormat().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField(DATE_FORMAT, currentTime.getDateFormat().trim());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    protected void writeCustomAction(JsonGenerator gen, CustomAction customAction) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("CustomAction");

        boolean objectStarted = false;
        if (customAction.getClassName() != null && customAction.getClassName().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField("className", customAction.getClassName().trim());
            gen.writeEndObject();
            objectStarted = true;
        }

        if (customAction.getMethodName() != null && customAction.getMethodName().trim().length() > 0) {
            if (!objectStarted) {
                gen.writeStartObject();
            }

            gen.writeStringField("methodName", customAction.getMethodName().trim());

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

    protected void writeFormat(JsonGenerator gen, Format action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Format");
        gen.writeStartObject();
        gen.writeStringField("template", action.getTemplate());
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
        gen.writeStringField("padCharacter", padStringLeft.getPadCharacter());
        gen.writeNumberField("padCount", padStringLeft.getPadCount());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writePadStringRight(JsonGenerator gen, PadStringRight padStringRight) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("PadStringRight");
        gen.writeStartObject();
        gen.writeStringField("padCharacter", padStringRight.getPadCharacter());
        gen.writeNumberField("padCount", padStringRight.getPadCount());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeReplaceAll(JsonGenerator gen, ReplaceAll action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ReplaceAll");
        gen.writeStartObject();
        gen.writeStringField("oldString", action.getOldString());
        if (action.getNewString() != null) {
            gen.writeStringField("newString", action.getNewString());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeReplaceFirst(JsonGenerator gen, ReplaceFirst action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ReplaceFirst");
        gen.writeStartObject();
        gen.writeStringField("oldString", action.getOldString());
        if (action.getNewString() != null) {
            gen.writeStringField("newString", action.getNewString());
        }
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
        gen.writeStringField("match", subStringAfter.getMatch());
        gen.writeNumberField(START_INDEX, subStringAfter.getStartIndex());
        if (subStringAfter.getEndIndex() != null) {
            gen.writeNumberField(END_INDEX, subStringAfter.getEndIndex());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }
<<<<<<< 47ddb07e1b3541f5aea5c4007ff93368151f40bf

    protected void writeSubStringBefore(JsonGenerator gen, SubStringBefore subStringBefore) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubStringBefore");
        gen.writeStartObject();
        gen.writeStringField("match", subStringBefore.getMatch());
        gen.writeNumberField(START_INDEX, subStringBefore.getStartIndex());
        if (subStringBefore.getEndIndex() != null) {
            gen.writeNumberField(END_INDEX, subStringBefore.getEndIndex());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }
=======
>>>>>>> Issue #151: Implement Number-related p0 field actions
}
