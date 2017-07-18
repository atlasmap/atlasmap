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

import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

import java.math.BigDecimal;

public class StringConverter implements AtlasPrimitiveConverter<String> {


    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.RANGE)
    public Boolean convertToBoolean(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value.equals("0") || value.equalsIgnoreCase("f") || value.equals("false")) {
            return Boolean.FALSE;
        } else if (value.equals("1") || value.equalsIgnoreCase("t") || value.equals("true")) {
            return Boolean.TRUE;
        }
        throw new AtlasConversionException("String " + value + " cannot be converted to a Boolean");
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("String to Byte conversion is not supported"));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character convertToCharacter(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // empty or greater than 1 char String throws Exception
        if (value.isEmpty() || value.length() > 1) {
            throw new AtlasConversionException("String is either empty or greater than one character long");
        } else if (value.charAt(0) < Character.MIN_VALUE || value.charAt(0) > Character.MAX_VALUE) {
            throw new AtlasConversionException("String " + value + " is greater than Character.MAX_VALUE  or less than Character.MIN_VALUE");
        }
        return value.charAt(0);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DOUBLE, concerns = {AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE})
    public Double convertToDouble(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        try {
            Double d = Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }

        if (Double.valueOf(value) == 0.0d || Double.valueOf(value) == -0.0d) {
            return Double.valueOf(value);
        }
        if (Double.valueOf(value) < Double.MIN_VALUE || Double.valueOf(value) > Double.MAX_VALUE) {
            throw new AtlasConversionException("String " + value + " is greater than Double.MAX_VALUE  or less than Double.MIN_VALUE");
        }

        return Double.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.FLOAT, concerns = {AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE})
    public Float convertToFloat(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        //check we can make a float of the String
        try {
            Float.parseFloat(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }

        BigDecimal bd = new BigDecimal(value);

        //handle 0.0f && -0.0  (floats suck)
        if (bd.floatValue() == 0.0f || bd.floatValue() == -0.0) {
            return Float.valueOf(value);
        }

        if (bd.floatValue() < Float.MIN_VALUE || bd.floatValue() > Float.MAX_VALUE) {
            throw new AtlasConversionException("String " + value + " is greater than Float.MAX_VALUE  or less than Float.MIN_VALUE");
        }

        return Float.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.INTEGER, concerns = {AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE})
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
        
        if(bd != null && bd.compareTo(BigDecimal.valueOf(i)) != 0) {
            throw new AtlasConversionException("String " + value + " is greater than Integer.MAX_VALUE  or less than Integer.MIN_VALUE");
        }
         
        return i;        
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.LONG, concerns = {AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE})
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
        
        if(bd != null && bd.compareTo(BigDecimal.valueOf(l)) != 0) {
            throw new AtlasConversionException("String " + value + " is greater than Long.MAX_VALUE  or less than Long.MIN_VALUE");
        }
         
        return l;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.SHORT, concerns = {AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE})
    public Short convertToShort(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        //check we can make a short of the String
        try {
            Short.parseShort(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }
        return Short.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public String convertToString(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        //we want a copy of value
        return new String(value);
    }
}
