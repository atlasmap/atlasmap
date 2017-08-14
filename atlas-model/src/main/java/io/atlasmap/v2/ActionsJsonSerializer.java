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
        case "PadStringLeft":
            writePadStringLeft(gen, (PadStringLeft) action);
            break;
        case "PadStringRight":
            writePadStringRight(gen, (PadStringRight) action);
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
        case "SumUp":
            writeSumUp(gen, (SumUp) action);
            break;
        default:
            gen.writeStartObject();
            gen.writeNullField(action.getClass().getSimpleName());
            gen.writeEndObject();
            break;
        }
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

    protected void writeCurrentDate(JsonGenerator gen, CurrentDate currentDate) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("CurrentDate");
        if (currentDate.getDateFormat() != null && currentDate.getDateFormat().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField("dateFormat", currentDate.getDateFormat().trim());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    protected void writeCurrentDateTime(JsonGenerator gen, CurrentDateTime currentDateTime) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("CurrentDateTime");
        if (currentDateTime.getDateFormat() != null && currentDateTime.getDateFormat().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField("dateFormat", currentDateTime.getDateFormat().trim());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }

    protected void writeCurrentTime(JsonGenerator gen, CurrentTime currentTime) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("CurrentTime");
        if (currentTime.getDateFormat() != null && currentTime.getDateFormat().trim().length() > 0) {
            gen.writeStartObject();
            gen.writeStringField("dateFormat", currentTime.getDateFormat().trim());
            gen.writeEndObject();
        }
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

    protected void writeSubString(JsonGenerator gen, SubString subString) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubString");
        gen.writeStartObject();
        gen.writeNumberField("startIndex", subString.getStartIndex());
        if (subString.getEndIndex() != null) {
            gen.writeNumberField("endIndex", subString.getEndIndex());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeSubStringAfter(JsonGenerator gen, SubStringAfter subStringAfter) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubStringAfter");
        gen.writeStartObject();
        gen.writeStringField("match", subStringAfter.getMatch());
        gen.writeNumberField("startIndex", subStringAfter.getStartIndex());
        if (subStringAfter.getEndIndex() != null) {
            gen.writeNumberField("endIndex", subStringAfter.getEndIndex());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeSubStringBefore(JsonGenerator gen, SubStringBefore subStringBefore) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubStringBefore");
        gen.writeStartObject();
        gen.writeStringField("match", subStringBefore.getMatch());
        gen.writeNumberField("startIndex", subStringBefore.getStartIndex());
        if (subStringBefore.getEndIndex() != null) {
            gen.writeNumberField("endIndex", subStringBefore.getEndIndex());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConvertAreaUnit(JsonGenerator gen, ConvertAreaUnit action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ConvertAreaUnit");
        gen.writeStartObject();
        gen.writeStringField("fromUnit", action.getFromUnit().value());
        gen.writeStringField("toUnit", action.getToUnit().value());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConvertDistanceUnit(JsonGenerator gen, ConvertDistanceUnit action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ConvertDistanceUnit");
        gen.writeStartObject();
        gen.writeStringField("fromUnit", action.getFromUnit().value());
        gen.writeStringField("toUnit", action.getToUnit().value());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConvertMassUnit(JsonGenerator gen, ConvertMassUnit action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ConvertMassUnit");
        gen.writeStartObject();
        gen.writeStringField("fromUnit", action.getFromUnit().value());
        gen.writeStringField("toUnit", action.getToUnit().value());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeConvertVolumeUnit(JsonGenerator gen, ConvertVolumeUnit action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("ConvertVolumeUnit");
        gen.writeStartObject();
        gen.writeStringField("fromUnit", action.getFromUnit().value());
        gen.writeStringField("toUnit", action.getToUnit().value());
        gen.writeEndObject();
        gen.writeEndObject();
    }

    protected void writeSumUp(JsonGenerator gen, SumUp action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SumUp");
        if (action.getNumberType() != null) {
            gen.writeStartObject();
            gen.writeStringField("numberType", action.getNumberType().value());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }
}
