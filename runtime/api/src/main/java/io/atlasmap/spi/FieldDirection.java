package io.atlasmap.spi;

public enum FieldDirection {
    SOURCE("Source"),
    TARGET("Target");

    private String value;

    FieldDirection(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
