package io.atlasmap.v2;

public enum CollectionType {

    ALL("All"),
    ARRAY("Array"),
    LIST("List"),
    MAP("Map"),
    NONE("None");

    private final String value;

    CollectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CollectionType fromValue(String v) {
        for (CollectionType c: CollectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
