package io.atlasmap.v2;

@Deprecated
public enum MappingType {

    ALL("All"),
    COLLECTION("Collection"),
    COMBINE("Combine"),
    LOOKUP("Lookup"),
    MAP("Map"),
    SEPARATE("Separate"),
    NONE("None");

    private final String value;

    MappingType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MappingType fromValue(String v) {
        for (MappingType c: MappingType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
