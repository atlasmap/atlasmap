/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.converters;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

public class StringConverter implements AtlasPrimitiveConverter<String> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.CONVENTION)
    public Boolean convertToBoolean(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value != null && ("1".equals(value) || "t".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value)
                || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value))) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.RANGE)
    public Byte convertToByte(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException nfex) {
            throw new AtlasConversionException(String
                    .format("String %s cannont be converted to a Byte as it is not in a numerical format", value));
        }
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character convertToCharacter(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // empty or greater than 1 char String throws Exception
        if (value.isEmpty() || value.length() > 1) {
            throw new AtlasConversionException(
                    String.format("String '%s' is either empty or greater than one character long", value));
        } else if (value.charAt(0) < Character.MIN_VALUE || value.charAt(0) > Character.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("String %s is greater than Character.MAX_VALUE  or less than Character.MIN_VALUE", value));
        }
        return value.charAt(0);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DOUBLE, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Double convertToDouble(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }

        if (Double.valueOf(value) == 0.0d || Double.valueOf(value) == -0.0d) {
            return Double.valueOf(value);
        }
        if (Double.valueOf(value) < Double.MIN_VALUE || Double.valueOf(value) > Double.MAX_VALUE) {
            throw new AtlasConversionException(
                    String.format("String %s is greater than Double.MAX_VALUE  or less than Double.MIN_VALUE", value));
        }

        return Double.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.FLOAT, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Float convertToFloat(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // check we can make a float of the String
        try {
            Float.parseFloat(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }

        BigDecimal bd = new BigDecimal(value);

        // handle 0.0f && -0.0 (floats suck)
        if (bd.floatValue() == 0.0f || bd.floatValue() == -0.0) {
            return Float.valueOf(value);
        }

        if (bd.floatValue() < Float.MIN_VALUE || bd.floatValue() > Float.MAX_VALUE) {
            throw new AtlasConversionException(
                    String.format("String %s is greater than Float.MAX_VALUE  or less than Float.MIN_VALUE", value));
        }

        return Float.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.INTEGER, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Integer convertToInteger(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        BigDecimal bd = null;
        Integer i = null;
        try {
            i = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            try {
                bd = new BigDecimal(value);
                i = bd.intValue();
            } catch (NumberFormatException nfe2) {
                throw new AtlasConversionException(nfe);
            }
        }

        if (bd != null && bd.compareTo(BigDecimal.valueOf(i)) != 0) {
            throw new AtlasConversionException(String
                    .format("String %s is greater than Integer.MAX_VALUE  or less than Integer.MIN_VALUE", value));
        }

        return i;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.LONG, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Long convertToLong(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        BigDecimal bd = null;
        Long l = null;
        try {
            l = Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            try {
                bd = new BigDecimal(value);
                l = bd.longValue();
            } catch (NumberFormatException nfe2) {
                throw new AtlasConversionException(nfe);
            }
        }

        if (bd != null && bd.compareTo(BigDecimal.valueOf(l)) != 0) {
            throw new AtlasConversionException(
                    String.format("String %s is greater than Long.MAX_VALUE  or less than Long.MIN_VALUE", value));
        }

        return l;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.SHORT, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Short convertToShort(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // check we can make a short of the String
        Short shortty;
        try {
            shortty = Short.parseShort(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }
        return Short.valueOf(shortty);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public String convertToString(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // we want a copy of value
        return new String(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.NUMBER, concerns = {
            AtlasConversionConcern.FORMAT })
    public Number convertToNumber(String value) throws AtlasConversionException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if (value.matches("\\d+")) {
            return new BigInteger(value);
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new AtlasConversionException(e);
        }
    }
}
