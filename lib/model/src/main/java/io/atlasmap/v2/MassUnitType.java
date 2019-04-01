package io.atlasmap.v2;

public enum MassUnitType {

    KILOGRAM_KG("Kilogram (kg)"),
    POUND_LB("Pound (lb)");

    private final String value;

    MassUnitType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MassUnitType fromValue(String v) {
        for (MassUnitType c: MassUnitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
