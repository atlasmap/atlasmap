package io.atlasmap.v2;

public enum DistanceUnitType {

    METER_M("Meter (m)"),
    MILE_MI("Mile (mi)"),
    YARD_YD("Yard (yd)"),
    FOOT_FT("Foot (ft)"),
    INCH_IN("Inch (in)");

    private final String value;

    DistanceUnitType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DistanceUnitType fromValue(String v) {
        for (DistanceUnitType c: DistanceUnitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
