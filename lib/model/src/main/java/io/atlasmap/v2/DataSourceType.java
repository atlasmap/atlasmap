package io.atlasmap.v2;

public enum DataSourceType {

    SOURCE("Source"),
    TARGET("Target");

    private final String value;

    DataSourceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DataSourceType fromValue(String v) {
        for (DataSourceType c: DataSourceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
