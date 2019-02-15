package io.atlasmap.v2;

public enum ValidationStatus {

    ALL("All"),
    INFO("Info"),
    WARN("Warn"),
    ERROR("Error"),
    NONE("None");

    private final String value;

    ValidationStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ValidationStatus fromValue(String v) {
        for (ValidationStatus c: ValidationStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
