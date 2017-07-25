package io.atlasmap.core;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

/**
 */
public class MockPrimitiveConverter implements AtlasPrimitiveConverter<String> {

    @Override
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

    @Override
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(String value) throws AtlasConversionException {
        throw new AtlasConversionException(new AtlasUnsupportedException("not supported"));
    }

    @Override
    public Character convertToCharacter(String value) throws AtlasConversionException {
        return 'a';
    }

    @Override
    public Double convertToDouble(String value) throws AtlasConversionException {
        return Double.MAX_VALUE;
    }

    @Override
    public Float convertToFloat(String value) throws AtlasConversionException {
        return Float.MAX_VALUE;
    }

    @Override
    public Integer convertToInteger(String value) throws AtlasConversionException {
        return Integer.MAX_VALUE;
    }

    @Override
    public Long convertToLong(String value) throws AtlasConversionException {
        return Long.MAX_VALUE;
    }

    @Override
    public Short convertToShort(String value) throws AtlasConversionException {
        return Short.MAX_VALUE;
    }

    @Override
    public String convertToString(String value) throws AtlasConversionException {
        return "aString";
    }
}
