package io.atlasmap.spi;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConverter;

/**
 */
public interface AtlasPrimitiveConverter<T> extends AtlasConverter<T> {

    Boolean convertToBoolean(T value) throws AtlasConversionException;

    Byte convertToByte(T value) throws AtlasConversionException;

    Character convertToCharacter(T value) throws AtlasConversionException;

    Double convertToDouble(T value) throws AtlasConversionException;

    Float convertToFloat(T value) throws AtlasConversionException;

    Integer convertToInteger(T value) throws AtlasConversionException;

    Long convertToLong(T value) throws AtlasConversionException;

    Short convertToShort(T value) throws AtlasConversionException;

    String convertToString(T value) throws AtlasConversionException;

}
