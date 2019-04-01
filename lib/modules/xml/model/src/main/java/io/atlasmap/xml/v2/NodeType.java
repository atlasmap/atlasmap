package io.atlasmap.xml.v2;

public enum NodeType {

    ALL("All"),
    ELEMENT("Element"),
    ATTRIBUTE("Attribute"),
    NONE("None");

    private final String value;

    NodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NodeType fromValue(String v) {
        for (NodeType c: NodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
