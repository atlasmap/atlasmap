package io.atlasmap.v2;

public enum AuditStatus {

    ALL("All"),
    INFO("Info"),
    WARN("Warn"),
    ERROR("Error"),
    NONE("None");

    private final String value;

    AuditStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AuditStatus fromValue(String v) {
        for (AuditStatus c: AuditStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
