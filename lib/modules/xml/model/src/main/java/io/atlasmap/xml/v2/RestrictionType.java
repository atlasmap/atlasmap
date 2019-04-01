package io.atlasmap.xml.v2;

public enum RestrictionType {

    ALL("All"),
    ENUMERATION("enumeration"),
    FRACTION_DIGITS("fractionDigits"),
    LENGTH("length"),
    MAX_EXCLUSIVE("maxExclusive"),
    MAX_INCLUSIVE("maxInclusive"),
    MAX_LENGTH("maxLength"),
    MIN_EXCLUSIVE("minExclusive"),
    MIN_INCLUSIVE("minInclusive"),
    MIN_LENGTH("minLength"),
    PATTERN("pattern"),
    TOTAL_DIGITS("totalDigits"),
    WHITE_SPACE("whiteSpace"),
    NONE("None");

    private final String value;

    RestrictionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RestrictionType fromValue(String v) {
        for (RestrictionType c: RestrictionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
