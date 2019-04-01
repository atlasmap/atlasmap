package io.atlasmap.v2;

public enum NumberType {

    BIG_INTEGER("Big Integer"),
    BYTE("Byte"),
    DECIMAL("Decimal"),
    DOUBLE("Double"),
    FLOAT("Float"),
    INTEGER("Integer"),
    LONG("Long"),
    SHORT("Short");

    private final String value;

    NumberType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NumberType fromValue(String v) {
        for (NumberType c: NumberType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
