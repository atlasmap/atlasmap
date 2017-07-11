package io.atlasmap.xml.v2;

import java.util.Collections;
import java.util.Map;

/**
 */
public class XmlPathCoordinate {

    private Integer index;
    private String elementName;
    private Map<String, String> namespace;

    XmlPathCoordinate(Integer index, String elementName) {
        this.index = index;
        this.elementName = elementName;
    }

    Integer getIndex() {
        return index;
    }

    String getElementName() {
        return elementName;
    }

    public Map<String, String> getNamespace() {
        return namespace;
    }

    void setNamespace(String uri, String prefix) {
        namespace = Collections.singletonMap(uri, prefix);
    }

    @Override
    public String toString() {
        return "XmlPathCoordinate{" +
            "index=" + index +
            ", elementName='" + elementName + '\'' +
            ", namespace=" + namespace +
            '}';
    }
}
