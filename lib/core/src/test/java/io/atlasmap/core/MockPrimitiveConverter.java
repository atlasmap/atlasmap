package io.atlasmap.core;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

/**
 */
public class MockPrimitiveConverter {

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.RANGE)
    public Boolean convertToBoolean(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if ("0".equals(value) || "f".equalsIgnoreCase(value) || "false".equals(value)) {
            return Boolean.FALSE;
        } else if ("1".equals(value) || "t".equalsIgnoreCase(value) || "true".equals(value)) {
            return Boolean.TRUE;
        }
        throw new AtlasConversionException("String " + value + " cannot be converted to a Boolean");
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(String value) throws AtlasConversionException {
        throw new AtlasConversionException(new AtlasUnsupportedException("not supported"));
    }

    public Character convertToCharacter(String value) {
        return 'a';
    }

    public Double convertToDouble(String value) {
        return Double.MAX_VALUE;
    }

    public Float convertToFloat(String value) {
        return Float.MAX_VALUE;
    }

    public Integer convertToInteger(String value) {
        return Integer.MAX_VALUE;
    }

    public Long convertToLong(String value) {
        return Long.MAX_VALUE;
    }

    public Short convertToShort(String value) {
        return Short.MAX_VALUE;
    }

    public String convertToString(String value) {
        return "aString";
    }
}
