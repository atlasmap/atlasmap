package io.atlasmap.v2;

public enum FieldStatus {

    SUPPORTED("Supported"),
    UNSUPPORTED("Unsupported"),
    CACHED("Cached"),
    ERROR("Error"),
    NOT_FOUND("NotFound"),
    EXCLUDED("Excluded");
    private final String value;

    FieldStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FieldStatus fromValue(String v) {
        for (FieldStatus c: FieldStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
