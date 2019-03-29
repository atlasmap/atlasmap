package io.atlasmap.v2;

public enum FieldType {

    ANY("Any"),
    ANY_DATE("Any Date"),
    BIG_INTEGER("Big Integer"),
    BOOLEAN("Boolean"),
    BYTE("Byte"),
    BYTE_ARRAY("ByteArray"),
    CHAR("Char"),
    COMPLEX("Complex"),
    DATE("Date"),
    DATE_TIME("DateTime"),
    DATE_TIME_TZ("DateTimeTZ"),
    DATE_TZ("DateTZ"),
    DECIMAL("Decimal"),
    DOUBLE("Double"),
    FLOAT("Float"),
    INTEGER("Integer"),
    LONG("Long"),
    NONE("None"),
    NUMBER("Number"),
    SHORT("Short"),
    STRING("String"),
    TIME("Time"),
    TIME_TZ("TimeTZ"),
    UNSIGNED_BYTE("Unsigned Byte"),
    UNSIGNED_INTEGER("Unsigned Integer"),
    UNSIGNED_LONG("Unsigned Long"),
    UNSIGNED_SHORT("Unsigned Short"),
    UNSUPPORTED("Unsupported");

    private final String value;

    FieldType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FieldType fromValue(String v) {
        for (FieldType c: FieldType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
