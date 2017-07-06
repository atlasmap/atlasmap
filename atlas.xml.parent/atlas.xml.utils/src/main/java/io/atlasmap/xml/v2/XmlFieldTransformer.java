package io.atlasmap.xml.v2;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
public abstract class XmlFieldTransformer {

    protected Map<String, String> namespaces;

    public XmlFieldTransformer() {
    }

    public XmlFieldTransformer(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    protected LinkedList<String> getElementsInXmlPath(String xmlPath) {
        return Arrays.stream(xmlPath.replaceFirst("/", "").split("/"))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    protected List<XmlPathCoordinate> createXmlPathCoordinates(final List<String> elements) {
        LinkedList<XmlPathCoordinate> xmlPathCoordinates = new LinkedList<>();
        for (String element : elements) {
            XmlPathCoordinate xmlPathCoordinate;
            int index = 0;
            //indexed elements
            if (element.contains("[")) {
                String[] indexedTargetElement = element.split("\\[");
                String indexedElement = indexedTargetElement[0].substring(indexedTargetElement[0].lastIndexOf("/") + 1);
                index = Integer.valueOf(indexedTargetElement[1].replace("]", ""));
                element = indexedElement;
            }
            xmlPathCoordinate = new XmlPathCoordinate(index, element);
            if (namespaces != null && !namespaces.isEmpty()) {
                handleNamespacedElements(element, xmlPathCoordinate);
            }
            xmlPathCoordinates.addLast(xmlPathCoordinate);
        }
        return xmlPathCoordinates;
    }

    protected String findNamespaceURIFromPrefix(String prefix) {
        return namespaces.entrySet().stream()
            .filter(e -> e.getValue().equals(prefix))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    protected void seedDocumentNamespaces(Document document) {
        NodeList nodeList = document.getChildNodes();
        if (namespaces == null) {
            namespaces = new LinkedHashMap<>();
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap namedNodeMap = node.getAttributes();
                for (int x = 0; x < namedNodeMap.getLength(); x++) {
                    Node attribute = namedNodeMap.item(x);
                    if (attribute.getNamespaceURI() != null) {
                        if (attribute.getLocalName().equals("xmlns")) {
                            namespaces.put(attribute.getNodeValue(), "");
                        } else {
                            namespaces.put(attribute.getNodeValue(), attribute.getLocalName());
                        }
                    }
                }
            }
        }
    }

    private void handleNamespacedElements(String element, XmlPathCoordinate xmlPathCoordinate) {
        if (element.contains(":")) {
            String[] namespacedElement = element.split(":");
            String prefix = namespacedElement[0];
            //dealing with an attr
            if (prefix.contains("@")) {
                prefix = prefix.substring(1);
            }
            String namespaceURI = findNamespaceURIFromPrefix(prefix);
            xmlPathCoordinate.setNamespace(namespaceURI, prefix);
        } else {
            String namespaceURI = findNamespaceURIFromPrefix("");
            if (namespaceURI != null) {
                xmlPathCoordinate.setNamespace(namespaceURI, "");
            }
        }
    }
}
