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
     * @see DataHandler#setValue(String, Object)
     */
    @Override
    public void setValue(String path, Object value) throws AtlasException {
        member(path, (type, member, pathIndex) -> {
            try {
                Field field = (Field) member;
                field.set(type, convertTo(value, field));
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

    private Object convertTo(Object value, Field field) {
        if (field.getType() == int.class) {
            if (Number.class.isAssignableFrom(value.getClass())) {
                return Number.class.cast(value).intValue();
            }
        } else if (field.getType() == String.class) {
            return value.toString();
        }
        return value;
    }

    private interface MemberConsumer {

        Object apply(Object type, Member member, int pathIndex);
    }
}
