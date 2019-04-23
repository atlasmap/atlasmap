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

import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class BooleanConverter implements AtlasConverter<Boolean> {

    private static final String STRING_VALUES = "true|false";

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.DECIMAL)
    public BigDecimal toBigDecimal(Boolean value) {
        return value != null ? BigDecimal.valueOf(value ? 1 : 0) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.BIG_INTEGER)
    public BigInteger toBigInteger(Boolean value) {
        return value != null ? BigInteger.valueOf(value ? 1 : 0) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.BOOLEAN)
    public Boolean toBoolean(Boolean value, String sourceFormat, String targetFormat) {
        return value != null ? Boolean.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.BYTE)
    public Byte toByte(Boolean value) {
        return value != null ? (byte) (value ? 1 : 0) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.CHAR)
    public Character toCharacter(Boolean value) {
        return value != null ? (char) (value ? 1 : 0) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.DOUBLE)
    public Double toDouble(Boolean value) {
        return value != null ? value ? 1.0d : 0.0d : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.FLOAT)
    public Float toFloat(Boolean value) {
        return value != null ? (value ? 1.0f : 0.0f) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.INTEGER)
    public Integer toInteger(Boolean value) {
        return value != null ? (value ? 1 : 0) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.LONG)
    public Long toLong(Boolean value) {
        return value != null ? (value ? 1L : 0L) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.NUMBER)
    public Number toNumber(Boolean value) {
        return toShort(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.SHORT)
    public Short toShort(Boolean value) {
        return value != null ? (short) (value ? 1 : 0) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.STRING, concerns = {
            AtlasConversionConcern.CONVENTION })
    public String toString(Boolean value, String sourceFormat, String targetFormat) {
        if (value == null) {
            return null;
        }
        // TODO optimize/save defaults
        String format = targetFormat != null && !"".equals(targetFormat) ? targetFormat : STRING_VALUES;
        String[] values = format.split("\\|");
        String trueValue = "";
        String falseValue = "";
        if (values.length == 2) {
            trueValue = values[0];
            falseValue = values[1];
        } else if (values.length == 1) {
            trueValue = values[0];
        }
        return String.valueOf((value ? trueValue : falseValue));
    }

}
