package io.atlasmap.v2;

public enum Multiplicity {

    ONE_TO_ONE("OneToOne"),

    ONE_TO_MANY("OneToMany"),

    MANY_TO_ONE("ManyToOne"),

    ZERO_TO_ONE("ZeroToOne"),

    MANY_TO_MANY("ManyToMany");

    Multiplicity(String v) {
        value = v;
    }

    private final String value;

    public String value() {
        return value;
    }

    public static Multiplicity fromValue(String v) {
        for (Multiplicity c: Multiplicity.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
