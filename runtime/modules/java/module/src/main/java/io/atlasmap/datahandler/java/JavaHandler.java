/**
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.atlasmap.datahandler.java;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.function.Function;

import io.atlasmap.api.v3.Message.Scope;
import io.atlasmap.api.v3.Message.Status;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.spi.v3.DataHandler;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;

/**
 *
 */
public class JavaHandler extends DataHandler {

    /**
     * @see DataHandler#supportedDataFormats()
     */
    @Override
    public String[] supportedDataFormats() {
        return new String[] {"java"};
    }

    /**
     * @see DataHandler#value(String)
     */
    @Override
    public Object value(String path) throws AtlasException {
           return member(path, (type, member, pathIndex) -> {
               try {
                   return ((Field) member).get(type);
               } catch (IllegalArgumentException | IllegalAccessException e) {
                   throw new AtlasRuntimeException(e, "Unable to retrieve value of field at index %d in path %s", pathIndex, path);
               }
           });
    }

    /**
     * @see DataHandler#setValue(String, Object, Parameter)
     */
    @Override
    public void setValue(String path, Object value, Parameter parameter) throws AtlasException {
        member(path, (type, member, pathIndex) -> {
            try {
                Field field = (Field) member;
                field.set(type, convertTo(value, field, parameter));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new AtlasRuntimeException(e, "Unable to set value of field at index %d in path %s", pathIndex, path);
            }
            return null;
        });
    }

    private Object member(String path, MemberConsumer consumer) throws AtlasException {
        Object type = document();
        if (type == null) {
            return null;
        }
        int startNdx = 0;
        int endNdx = path.indexOf('/');
        String segment = endNdx < 0 ? path : path.substring(0, endNdx);
        for (Field field : type.getClass().getDeclaredFields()) {
            if (segment.equals(field.getName())) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return consumer.apply(type, field, startNdx);
            }
        }
        throw new AtlasException("Unknown field at index %d in path %s", startNdx, path);
    }

    private Object convertTo(Object value, Field field, Parameter parameter) {
        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
            if (value == null) {
                return false;
            }
            if (value instanceof Number) {
                double number = ((Number)value).doubleValue();
                boolean convertedBoolean = number == 0.0 ? false : true;
                addMessage(Status.WARNING, Scope.DATA_HANDLER, parameter,
                           "The %s %s was automatically converted to the boolean %s",
                           value.getClass().getSimpleName().toLowerCase(), value, convertedBoolean);
                return convertedBoolean;
            }
//            switch (stringValue.trim().toLowerCase()) {
//                case "false":
//                case "f":
//                case "no":
//                case "n":
//                    value = Boolean.FALSE;
//                    break;
//                default:
//                    try {
//                        value = Double.valueOf(stringValue).intValue() != 0;
//                    } catch (NumberFormatException e) {
//                        value = Boolean.TRUE;
//                    }
            return false;
        }
        if (field.getType() == byte.class || field.getType() == Byte.class) {
            return convertToNumber(value, parameter, number -> number.byteValue());
        }
        if (field.getType() == short.class || field.getType() == Short.class) {
            return convertToNumber(value, parameter, number -> number.shortValue());
        }
        if (field.getType() == int.class || field.getType() == Integer.class) {
            return convertToNumber(value, parameter, number -> number.intValue());
        }
        if (field.getType() == long.class || field.getType() == Long.class) {
            return convertToNumber(value, parameter, number -> number.longValue());
        }
        if (field.getType() == float.class || field.getType() == Float.class) {
            return convertToNumber(value, parameter, number -> number.floatValue());
        }
        if (field.getType() == double.class || field.getType() == Double.class) {
            return convertToNumber(value, parameter, number -> number.doubleValue());
        }
        if (field.getType() == String.class) {
            return value == null ? null : value.toString();
        }
        return value;
    }

    private Object convertToNumber(Object value, Parameter parameter, Function<Number, Number> function) {
        if (value == null) {
            value = 0;
        }
        if (value instanceof Number) {
            Number number = (Number)value;
            Number convertedNumber = function.apply(number);
            if (number.doubleValue() != convertedNumber.doubleValue()) {
                addMessage(Status.WARNING, Scope.DATA_HANDLER, parameter,
                           "The %s %s was automatically converted to the %s %s, resulting in a loss of precision or a truncated value",
                           value.getClass().getSimpleName().toLowerCase(), value,
                           convertedNumber.getClass().getSimpleName().toLowerCase(), convertedNumber);
            }
            return convertedNumber;
        }
        addMessage(Status.WARNING, Scope.DATA_HANDLER, parameter,
                   "The %s %s was automatically converted to the byte 0",
                   value.getClass().getSimpleName().toLowerCase(), value instanceof String ? "'" + value + "'": value);
        return (byte)0;
    }

    private interface MemberConsumer {

        Object apply(Object type, Member member, int pathIndex);
    }
}
