package io.atlasmap.v2;

public enum InspectionType {
    SCHEMA("SCHEMA"),
    INSTANCE("INSTANCE"),
    JAVA_CLASS("JAVA_CLASS");

    private String value;

    InspectionType(String inspectionType) {
        value = inspectionType;
    }

    public String value(){
        return this.value;
    }

}
