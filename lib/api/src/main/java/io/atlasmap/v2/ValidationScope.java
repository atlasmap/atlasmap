package io.atlasmap.v2;

public enum ValidationScope {

    ALL("All"),
    DATA_SOURCE("DataSource"),
    MAPPING("Mapping"),
    LOOKUP_TABLE("LookupTable"),
    CONSTANT("Constant"),
    PROPERTY("Property");

    private final String value;

    ValidationScope(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ValidationScope fromValue(String v) {
        for (ValidationScope c: ValidationScope.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
