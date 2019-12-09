package io.atlasmap.v2;

public enum MappingFileType {
    ADM("adm"),
    GZ("gz"),
    ZIP("zip"),
    JSON("json"),
    XML("xml");

    private String value;

    MappingFileType(String mappingFormat) {
        value = mappingFormat;
    }

    public String value(){
        return this.value;
    }
}
