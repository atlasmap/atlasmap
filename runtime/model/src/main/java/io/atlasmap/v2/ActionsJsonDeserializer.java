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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

@SuppressWarnings({"squid:S1118", // Add private constructor
    "squid:S1226", // Introduce new variable
    "squid:S1301", // Replace switch with if
    "squid:S1479", // Reduce number of switch cases
    "squid:S3358", // Extract nested ternary
    "squid:S3776", }) // Cognitive complexity of method
public class ActionsJsonDeserializer extends JsonDeserializer<Actions> {

    @Override
    public Actions deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Actions actions = null;
        if (jp != null && jp.isExpectedStartArrayToken()) {
            actions = new Actions();
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                JsonToken jsonToken = jp.nextToken();

                if (jsonToken == JsonToken.END_ARRAY) {
                    break;
                }

                Action action = processActionJsonToken(jp);
                if (action != null) {
                    actions.getActions().add(action);
                }
            }
        } else {
            throw new IOException(
                    "Invalid JSON where array expected: " + (jp != null ? jp.getCurrentToken().asString() : null));
        }

        return actions;
    }

    protected Action processActionJsonToken(JsonParser jsonToken) throws IOException {

        if (jsonToken.getCurrentName() == null) {
            return null;
        }

        switch (jsonToken.getCurrentName()) {
            case "AbsoluteValue":
                return new AbsoluteValue();
            case "Add":
                return new Add();
            case "AddDays":
                return processAddDaysJsonToken(jsonToken);
            case "AddSeconds":
                return processAddSecondsJsonToken(jsonToken);
            case "Append":
                return processAppendJsonToken(jsonToken);
            case "Average":
                return new Average();
            case "Camelize":
                return new Camelize();
            case "Capitalize":
                return new Capitalize();
            case "Ceiling":
                return new Ceiling();
            case "Concatenate":
                return processConcatenateJsonToken(jsonToken);
            case "ConvertAreaUnit":
                return processConvertAreaUnitJsonToken(jsonToken);
            case "ConvertDistanceUnit":
                return processConvertDistanceUnitJsonToken(jsonToken);
            case "ConvertMassUnit":
                return processConvertMassUnitJsonToken(jsonToken);
            case "ConvertVolumeUnit":
                return processConvertVolumeUnitJsonToken(jsonToken);
            case "CurrentDate":
                return new CurrentDate();
            case "CurrentDateTime":
                return new CurrentDateTime();
            case "CurrentTime":
                return new CurrentTime();
            case "CustomAction":
                return processCustomActionJsonToken(jsonToken);
            case "DayOfWeek":
                return new DayOfWeek();
            case "DayOfYear":
                return new DayOfYear();
            case "Divide":
                return new Divide();
            case "EndsWith":
                return processEndsWithJsonToken(jsonToken);
            case "FileExtension":
                return new FileExtension();
            case "Floor":
                return new Floor();
            case "Format":
                return processFormatJsonToken(jsonToken);
            case "GenerateUUID":
                return new GenerateUUID();
            case "IndexOf":
                return processIndexOfJsonToken(jsonToken);
            case "LastIndexOf":
                return processLastIndexOfJsonToken(jsonToken);
            case "Length":
                return new Length();
            case "Lowercase":
                return new Lowercase();
            case "LowercaseChar":
                return new LowercaseChar();
            case "Maximum":
                return new Maximum();
            case "Minimum":
                return new Minimum();
            case "Multiply":
                return new Multiply();
            case "Normalize":
                return new Normalize();
            case "PadStringLeft":
                return processPadStringLeftJsonToken(jsonToken);
            case "PadStringRight":
                return processPadStringRightJsonToken(jsonToken);
            case "Prepend":
                return processPrependJsonToken(jsonToken);
            case "RemoveFileExtension":
                return new RemoveFileExtension();
            case "ReplaceAll":
                return processReplaceAllJsonToken(jsonToken);
            case "ReplaceFirst":
                return processReplaceFirstJsonToken(jsonToken);
            case "Round":
                return new Round();
            case "SeparateByDash":
                return new SeparateByDash();
            case "SeparateByUnderscore":
                return new SeparateByUnderscore();
            case "StartsWith":
                return processStartsWithJsonToken(jsonToken);
            case "SubString":
                return processSubStringJsonToken(jsonToken);
            case "SubStringAfter":
                return processSubStringAfterJsonToken(jsonToken);
            case "SubStringBefore":
                return processSubStringBeforeJsonToken(jsonToken);
            case "Subtract":
                return new Subtract();
            case "Trim":
                return new Trim();
            case "TrimLeft":
                return new TrimLeft();
            case "TrimRight":
                return new TrimRight();
            case "Uppercase":
                return new Uppercase();
            case "UppercaseChar":
                return new UppercaseChar();
            default:
                // ref: https://github.com/atlasmap/atlasmap/issues/6
                // TODO: Logger not required in model module
                // logger.warn("Unsupported action named: " + jsonToken.getCurrentName());
        }

        return null;
    }

    protected AddDays processAddDaysJsonToken(JsonParser jsonToken) throws IOException {
        AddDays action = new AddDays();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.DAYS:
                jsonToken.nextToken();
                action.setDays(jsonToken.getIntValue());
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected AddSeconds processAddSecondsJsonToken(JsonParser jsonToken) throws IOException {
        AddSeconds action = new AddSeconds();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.SECONDS:
                jsonToken.nextToken();
                action.setSeconds(jsonToken.getIntValue());
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected Append processAppendJsonToken(JsonParser jsonToken) throws IOException {
        Append action = new Append();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.STRING:
                    jsonToken.nextToken();
                    action.setString(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected Concatenate processConcatenateJsonToken(JsonParser jsonToken) throws IOException {
        Concatenate action = new Concatenate();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.DELIMITER:
                jsonToken.nextToken();
                action.setDelimiter(jsonToken.getValueAsString());
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected ConvertAreaUnit processConvertAreaUnitJsonToken(JsonParser jsonToken) throws IOException {
        ConvertAreaUnit action = new ConvertAreaUnit();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.FROM_UNIT:
                jsonToken.nextToken();
                action.setFromUnit(AreaUnitType.fromValue(jsonToken.getValueAsString()));
                break;
            case ActionsJsonSerializer.TO_UNIT:
                jsonToken.nextToken();
                action.setToUnit(AreaUnitType.fromValue(jsonToken.getValueAsString()));
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));

        return action;

    }

    protected ConvertDistanceUnit processConvertDistanceUnitJsonToken(JsonParser jsonToken) throws IOException {
        ConvertDistanceUnit action = new ConvertDistanceUnit();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.FROM_UNIT:
                jsonToken.nextToken();
                action.setFromUnit(DistanceUnitType.fromValue(jsonToken.getValueAsString()));
                break;
            case ActionsJsonSerializer.TO_UNIT:
                jsonToken.nextToken();
                action.setToUnit(DistanceUnitType.fromValue(jsonToken.getValueAsString()));
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));

        return action;

    }

    protected ConvertMassUnit processConvertMassUnitJsonToken(JsonParser jsonToken) throws IOException {
        ConvertMassUnit action = new ConvertMassUnit();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.FROM_UNIT:
                jsonToken.nextToken();
                action.setFromUnit(MassUnitType.fromValue(jsonToken.getValueAsString()));
                break;
            case ActionsJsonSerializer.TO_UNIT:
                jsonToken.nextToken();
                action.setToUnit(MassUnitType.fromValue(jsonToken.getValueAsString()));
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));

        return action;

    }

    protected ConvertVolumeUnit processConvertVolumeUnitJsonToken(JsonParser jsonToken)  throws IOException {
        ConvertVolumeUnit action = new ConvertVolumeUnit();
        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.FROM_UNIT:
                jsonToken.nextToken();
                action.setFromUnit(VolumeUnitType.fromValue(jsonToken.getValueAsString()));
                break;
            case ActionsJsonSerializer.TO_UNIT:
                jsonToken.nextToken();
                action.setToUnit(VolumeUnitType.fromValue(jsonToken.getValueAsString()));
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));

        return action;
    }

    protected CustomAction processCustomActionJsonToken(JsonParser jsonToken) throws IOException {
        CustomAction action = new CustomAction();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.CLASS_NAME:
                jsonToken.nextToken();
                action.setClassName(jsonToken.getValueAsString());
                break;
            case ActionsJsonSerializer.METHOD_NAME:
                jsonToken.nextToken();
                action.setMethodName(jsonToken.getValueAsString());
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected EndsWith processEndsWithJsonToken(JsonParser jsonToken) throws IOException {
        EndsWith action = new EndsWith();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.STRING:
                    jsonToken.nextToken();
                    action.setString(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected Format processFormatJsonToken(JsonParser jsonToken) throws IOException {
        Format action = new Format();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.TEMPLATE:
                    jsonToken.nextToken();
                    action.setTemplate(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected IndexOf processIndexOfJsonToken(JsonParser jsonToken) throws IOException {
        IndexOf action = new IndexOf();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.STRING:
                    jsonToken.nextToken();
                    action.setString(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected LastIndexOf processLastIndexOfJsonToken(JsonParser jsonToken) throws IOException {
        LastIndexOf action = new LastIndexOf();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.STRING:
                    jsonToken.nextToken();
                    action.setString(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected PadStringLeft processPadStringLeftJsonToken(JsonParser jsonToken) throws IOException {
        PadStringLeft action = new PadStringLeft();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.PAD_CHARACTER:
                jsonToken.nextToken();
                action.setPadCharacter(jsonToken.getValueAsString());
                break;
            case ActionsJsonSerializer.PAD_COUNT:
                jsonToken.nextToken();
                action.setPadCount(jsonToken.getIntValue());
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected PadStringRight processPadStringRightJsonToken(JsonParser jsonToken) throws IOException {
        PadStringRight action = new PadStringRight();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.PAD_CHARACTER:
                jsonToken.nextToken();
                action.setPadCharacter(jsonToken.getValueAsString());
                break;
            case ActionsJsonSerializer.PAD_COUNT:
                jsonToken.nextToken();
                action.setPadCount(jsonToken.getIntValue());
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected Prepend processPrependJsonToken(JsonParser jsonToken) throws IOException {
        Prepend action = new Prepend();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.STRING:
                    jsonToken.nextToken();
                    action.setString(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected ReplaceAll processReplaceAllJsonToken(JsonParser jsonToken) throws IOException {
        ReplaceAll action = new ReplaceAll();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.MATCH:
                    jsonToken.nextToken();
                    action.setMatch(jsonToken.getValueAsString());
                    break;
                case ActionsJsonSerializer.NEW_STRING:
                    jsonToken.nextToken();
                    action.setNewString(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected ReplaceFirst processReplaceFirstJsonToken(JsonParser jsonToken) throws IOException {
        ReplaceFirst action = new ReplaceFirst();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.MATCH:
                    jsonToken.nextToken();
                    action.setMatch(jsonToken.getValueAsString());
                    break;
                case ActionsJsonSerializer.NEW_STRING:
                    jsonToken.nextToken();
                    action.setNewString(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected StartsWith processStartsWithJsonToken(JsonParser jsonToken) throws IOException {
        StartsWith action = new StartsWith();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
                case ActionsJsonSerializer.STRING:
                    jsonToken.nextToken();
                    action.setString(jsonToken.getValueAsString());
                    break;
                default:
                    break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected SubStringAfter processSubStringAfterJsonToken(JsonParser jsonToken) throws IOException {
        SubStringAfter action = new SubStringAfter();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.START_INDEX:
                jsonToken.nextToken();
                action.setStartIndex(jsonToken.getIntValue());
                break;
            case ActionsJsonSerializer.END_INDEX:
                jsonToken.nextToken();
                action.setEndIndex(jsonToken.getIntValue());
                break;
            case ActionsJsonSerializer.MATCH:
                jsonToken.nextToken();
                action.setMatch(jsonToken.getValueAsString());
                break;

            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected SubStringBefore processSubStringBeforeJsonToken(JsonParser jsonToken) throws IOException {
        SubStringBefore action = new SubStringBefore();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.START_INDEX:
                jsonToken.nextToken();
                action.setStartIndex(jsonToken.getIntValue());
                break;
            case ActionsJsonSerializer.END_INDEX:
                jsonToken.nextToken();
                action.setEndIndex(jsonToken.getIntValue());
                break;
            case ActionsJsonSerializer.MATCH:
                jsonToken.nextToken();
                action.setMatch(jsonToken.getValueAsString());
                break;

            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }

    protected SubString processSubStringJsonToken(JsonParser jsonToken) throws IOException {
        SubString action = new SubString();

        if (JsonToken.END_ARRAY.equals(jsonToken.currentToken())
                || JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
            return action;
        }

        JsonToken nextToken = null;
        do {
            if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                jsonToken.nextToken();
            }
            switch (jsonToken.getCurrentName()) {
            case ActionsJsonSerializer.START_INDEX:
                jsonToken.nextToken();
                action.setStartIndex(jsonToken.getIntValue());
                break;
            case ActionsJsonSerializer.END_INDEX:
                jsonToken.nextToken();
                action.setEndIndex(jsonToken.getIntValue());
                break;
            default:
                break;
            }

            nextToken = jsonToken.nextToken();
        } while (!JsonToken.END_ARRAY.equals(nextToken) && !JsonToken.END_OBJECT.equals(nextToken));
        return action;
    }
}
