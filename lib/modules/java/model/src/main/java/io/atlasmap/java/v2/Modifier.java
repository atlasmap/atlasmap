package io.atlasmap.java.v2;

public enum Modifier {

    ALL("All"),
    ABSTRACT("Abstract"),
    FINAL("Final"),
    INTERFACE("Interface"),
    NATIVE("Native"),
    PACKAGE_PRIVATE("Package Private"),
    PUBLIC("Public"),
    PROTECTED("Protected"),
    PRIVATE("Private"),
    STATIC("Static"),
    STRICT("Strict"),
    SYNCHRONIZED("Synchronized"),
    TRANSIENT("Transient"),
    VOLATILE("Volatile"),
    NONE("None");

    private final String value;

    Modifier(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Modifier fromValue(String v) {
        for (Modifier c: Modifier.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
