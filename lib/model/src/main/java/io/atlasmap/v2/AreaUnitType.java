package io.atlasmap.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AreaUnitType {

    SQUARE_METER("Square Meter"),
    SQUARE_MILE("Square Mile"),
    SQUARE_FOOT("Square Foot");

    private final String value;

    AreaUnitType(String v) {
        value = v;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static AreaUnitType fromValue(String v) {
        for (AreaUnitType c: AreaUnitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
