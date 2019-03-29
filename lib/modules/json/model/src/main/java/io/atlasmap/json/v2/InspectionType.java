package io.atlasmap.json.v2;

public enum InspectionType {

    ALL("All"),
    INSTANCE("Instance"),
    SCHEMA("Schema"),
    NONE("None");

    private final String value;

    InspectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static InspectionType fromValue(String v) {
        for (InspectionType c: InspectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
