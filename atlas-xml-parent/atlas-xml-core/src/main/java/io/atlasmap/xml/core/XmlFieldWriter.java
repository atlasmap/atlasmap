/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.xml.core;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.PathUtil;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlField;

public class XmlFieldWriter extends XmlFieldTransformer {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(XmlFieldWriter.class);

	private Document document = null;
	private boolean enableElementNamespaces = true;
	private boolean enableAttributeNamespaces = true;
	private boolean ignoreMissingNamespaces = true;
	
    public XmlFieldWriter() throws AtlasException {
    	this(new HashMap<>(), null);
    }

    public XmlFieldWriter(Map<String, String> namespaces, String seedDocument) throws AtlasException {
        super(namespaces);
        this.document = createDocument(namespaces, seedDocument);
        //check to see if the seed document has namespaces
        seedDocumentNamespaces(document);
    }
    
    public void write(List<XmlField> fields) throws AtlasException {
    	if (fields == null) {
    		throw new AtlasException(new IllegalArgumentException("Argument 'fields' cannot be null"));
    	}
    	for (XmlField f: fields) {
    		write(f);
    	}
    }

    public void write(Field field) throws AtlasException {
        if (field == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'field' cannot be null"));
        }
        
        if (logger.isDebugEnabled()) {    		
    		    logger.debug("Now processing field p=%s t=%s v=%s", field.getPath(), field.getFieldType(), field.getValue());
        }
        
        PathUtil path = new PathUtil(field.getPath());
    	String lastSegment = path.getLastSegment();
    	Element parentNode = null;
    	String parentSegment = null;
    	for (String segment : path.getSegments()) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("Now processing segment: " + segment);
    			logger.debug("Parent element is currently: " + writeDocumentToString(true, parentNode));
    		}
    		if (parentNode == null) {
    			//processing root node
    			parentNode = document.getDocumentElement();
    			String cleanedSegment = PathUtil.cleanPathSegment(segment);
    			if (parentNode == null) {
    				if (logger.isDebugEnabled()) {
    					logger.debug("Creating root element with name: " + cleanedSegment);
    				}
    				//no root node exists yet, create root node with this segment name;
    				Element rootNode = createElement(segment);
    				addNamespacesToElement(rootNode, namespaces);
    				document.appendChild(rootNode);
    				parentNode = rootNode;
    			} else if (!(parentNode.getNodeName().equals(segment))) {
        			//make sure root element's name matches.
    				throw new AtlasException("Root element name '" + parentNode.getNodeName() 
    					+ "' does not match expected name '" + segment + "' from path: " + field.getPath());
    			}
    			parentSegment = segment;
    		} else {
    			if (logger.isDebugEnabled()) {
    				if (segment == lastSegment) {
    					logger.debug("Now processing field value segment: " + segment);
    				} else {
    					logger.debug("Now processing parent segment: " + segment);
    				}
    			}
    			
    			if (!PathUtil.isAttributeSegment(segment)) {
    				//if current segment of path isn't attribute, it refers to a child element, find it or create it..
    				Element childNode = getChildNode(parentNode, parentSegment, segment);
    				if (childNode == null) {
    					childNode = createParentNode(parentNode, parentSegment, segment);    			
    				}
    				parentNode = childNode;
    				parentSegment = segment;
    			}
    			
    			if (segment == lastSegment) {
					writeValue(parentNode, segment, field);
				}
    		}      		
    	}    
    }
    
    public static void addNamespacesToElement(Element node, Map<String,String> namespaces) {
    	for (String namespaceAlias : namespaces.keySet()) {
			String namespaceUri = namespaces.get(namespaceAlias);
			String attributeName = "xmlns";
			if (namespaceAlias != null && !namespaceAlias.equals("")) {
				attributeName += ":" + namespaceAlias;
			}
			node.setAttributeNS("http://www.w3.org/2000/xmlns/", attributeName, namespaceUri);
		}
    }
    
    public void writeValue(Element parentNode, String segment, Field field) throws AtlasException {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Writing field value in parent node '" + segment + "', parentNode: " + writeDocumentToString(true, parentNode));
    	}    	    	
    	String value = convertValue(field);
    	if (PathUtil.isAttributeSegment(segment)) {
    		String cleanedSegment = PathUtil.cleanPathSegment(segment);
    		if (this.enableAttributeNamespaces) {  
    			if (logger.isDebugEnabled()) {
    				logger.debug("Attribute namespaces are enabled, determining namespace.");
    			}
    	    	String namespaceAlias = null;
    	    	String namespaceUri = null;
    	    	if (PathUtil.isNamespaceSegment(segment)) {
    	    		namespaceAlias = PathUtil.getNamespace(segment);
    	    		namespaceUri = this.namespaces.get(namespaceAlias);    		
    	    		logger.debug("Parsed namespace alias '" + namespaceAlias + "', from segment '" + segment + "', namespaceUri: " + namespaceUri);
    	    	}
    			if (!this.ignoreMissingNamespaces && namespaceUri == null) {
    				throw new AtlasException("Cannot find namespace URI for attribute: '" + segment + "', available namespaces: " + this.namespaces);
    			}        		
    			if (namespaceUri != null) {
    				parentNode.setAttributeNS(namespaceUri, namespaceAlias + ":" + cleanedSegment, value);
    			} else {
    				parentNode.setAttribute(cleanedSegment, value);
    			}
    		} else {
    			parentNode.setAttribute(cleanedSegment, value);
    		}
    	} else { //set element value
    		parentNode.setTextContent(value);    		    		
    	}		
		
		if (logger.isDebugEnabled()) {
			logger.debug("Parent node after value written: " + writeDocumentToString(true, parentNode));
		}
    }
    
    public static Element getChildNode(Element parentNode, String parentSegment, String segment) throws AtlasException {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Looking for child node '" + segment + "' in parent '" + parentSegment + "': " + writeDocumentToString(true, parentNode));
    	}
    	if (parentNode == null) {
    		return null;
    	}
    	String cleanedSegment = PathUtil.cleanPathSegment(segment);
    	String namespaceAlias = PathUtil.getNamespace(segment);
    	if (namespaceAlias != null && !"".equals(namespaceAlias)) {
    		cleanedSegment = namespaceAlias + ":" + cleanedSegment;
    	}
    	List<Element> children = getChildrenWithName(cleanedSegment, parentNode);
    	if (logger.isDebugEnabled()) {
    		logger.debug("Found " + children.size() + " children in '" + parentSegment + "' with the name '" + cleanedSegment + "'.");
    	}
    	Element childNode = children.size() > 0 ? children.get(0) : null;
    	if (children.size() > 0 && PathUtil.isCollectionSegment(segment)) {
    		int index = PathUtil.indexOfSegment(segment);
    		childNode = null;
    		if (children.size() > index) {
    			childNode = children.get(index);
    		}
    	}
    	if (logger.isDebugEnabled()) {
    		if (childNode == null) {
    			logger.debug("Could not find child node '" + segment + "' in parent '" + parentSegment + "'.");	
    		} else {
    			logger.debug("Found child node '" + segment + "' in parent '" + parentSegment 
    					+ "', class: " + childNode.getClass().getName() + ", node: " + writeDocumentToString(true, childNode));
    		}    		
    	}
    	return childNode;
    }
    
    public Element createParentNode(Element parentNode, String parentSegment, String segment) throws AtlasException {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Creating parent node '" + segment + "' under previous parent '" + parentSegment + "'.");
    	}
    	Element childNode = null;
    	String cleanedSegment = PathUtil.cleanPathSegment(segment);
		if (PathUtil.isCollectionSegment(segment)) {
			int index = PathUtil.indexOfSegment(segment);
			String namespaceAlias = PathUtil.getNamespace(segment);
			if (namespaceAlias != null && !"".equals(namespaceAlias)) {
	    		cleanedSegment = namespaceAlias + ":" + cleanedSegment;
	    	}
			
			List<Element> children = getChildrenWithName(cleanedSegment, parentNode);
			
			if (children.size() < (index + 1)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Child Element Array is too small, resizing to accomodate index: " + index + ", current array: " + children);
				}
				//if our array doesn't have index + 1 items in it, add objects until we have the index available
				while (children.size() < (index + 1)) {
					Element child = (Element) parentNode.appendChild(createElement(segment));
					children.add(child);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Child Element Array after resizing: " + children);
				}
			}
			children = getChildrenWithName(cleanedSegment, parentNode);
			childNode = (Element) children.get(index);			
		} else {
			childNode = (Element) parentNode.appendChild(createElement(segment));
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Parent Node '" + parentSegment + "' after adding child parent node '" + segment + "':" + writeDocumentToString(true, parentNode));
		}
		return childNode;
    }
    
    public Element createElement(String segment) throws AtlasException { 
    	String cleanedSegment = PathUtil.cleanPathSegment(segment);
    	if (logger.isDebugEnabled()) {
			logger.debug("Creating element for segment '" + segment + "'.");
		}
    	if (this.enableElementNamespaces) {
    		if (logger.isDebugEnabled()) {
				logger.debug("Element namespaces are enabled, determining namespace.");
			}
    		String namespaceAlias = null;
        	String namespaceUri = null;
        	if (PathUtil.isNamespaceSegment(segment)) {
        		namespaceAlias = PathUtil.getNamespace(segment);
        		namespaceUri = this.namespaces.get(namespaceAlias);    		
        		logger.debug("Parsed namespace alias '" + namespaceAlias + "', from segment '" + segment 
        				+ "', namespaceUri: " + namespaceUri + ", known namespaces: " + this.namespaces);
        	}
			if (!this.ignoreMissingNamespaces && namespaceUri == null) {
				throw new AtlasException("Cannot find namespace URI for element: '" + segment + "', available namespaces: " + this.namespaces);
			}
			if (namespaceUri != null) {
				return document.createElementNS(namespaceUri, namespaceAlias + ":" + cleanedSegment);
			}
		}
    	return document.createElement(cleanedSegment);
    }
    
    public static List<Element> getChildrenWithName(String name, Element parentNode) {
    	List<Element> children = new LinkedList<>();
    	if (parentNode == null) {
    		return children;
    	}
    	NodeList nodeChildren = parentNode.getChildNodes();
		for (int i = 0; i < nodeChildren.getLength(); i++) {
			Node child = nodeChildren.item(i);
			if ((child instanceof Element) && child.getNodeName().equals(name)) {
				children.add((Element)child);
			}
		}
    	return children;
    }
    
    public String convertValue(Field field) throws AtlasException {
    	FieldType type = field.getFieldType();
    	Object originalValue = field.getValue();
    	String value = originalValue != null ? String.valueOf(originalValue) : null;            	
    	if (logger.isDebugEnabled()) {
    		String valueClass = originalValue == null ? "null" : originalValue.getClass().getName();
    		logger.debug("Converted field value. Type: " + type 
    				+ ", originalValue: " + originalValue + "(" + valueClass 
    				+ "), to: '" + value + "'.");
    	}
    	return value;
    } 

    private static Document createDocument(Map<String,String> namespaces, String seedDocument) throws AtlasException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            if (namespaces != null && !namespaces.isEmpty()) {
                documentBuilderFactory.setNamespaceAware(true);
            }
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            if (seedDocument != null) {
            	Document document =  documentBuilder.parse(new ByteArrayInputStream(seedDocument.getBytes("UTF-8")));
            	            	
            	Element rootNode = document.getDocumentElement();

            	//extract namespaces from seed document
            	NamedNodeMap attributes = rootNode.getAttributes();
            	if (attributes != null) {
            		for (int i = 0; i < attributes.getLength(); i++) {
            			Node n = attributes.item(i);
            			String nodeName = n.getNodeName();
            			if (nodeName != null && nodeName.startsWith("xmlns")) {
            				String namespaceAlias = "";
            				if (nodeName.contains(":")) {
            					namespaceAlias = nodeName.substring(nodeName.indexOf(":") + 1);	
            				}            		
            				if (!namespaces.containsKey(namespaceAlias)) {
            					namespaces.put(namespaceAlias, n.getNodeValue());
            				}
            			}
            		}
            	}
            	
        		//rewrite root element to contain user-specified namespaces
            	if (namespaces.size() > 0) {
            		Element oldRootNode = rootNode;
            		rootNode = (Element) oldRootNode.cloneNode(true);
            		addNamespacesToElement(rootNode, namespaces);
            		document.removeChild(oldRootNode);
            		document.appendChild(rootNode);
            	}
            	
            	return document;
            }
            return documentBuilder.newDocument();
        } catch (Exception e) {
            throw new AtlasException(e);
        }        
    }    
    
    public static String writeDocumentToString(boolean stripSpaces, Node node) throws AtlasException {
    	try {
    		if (node == null) {
    			return "";
    		}
	    	TransformerFactory tf = TransformerFactory.newInstance();
	    	Transformer transformer = tf.newTransformer();
	    	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    	StringWriter writer = new StringWriter();
	    	transformer.transform(new DOMSource(node), new StreamResult(writer));
	    	
	    	String result = writer.getBuffer().toString();
	    	if (stripSpaces) {
	    		result = result.replaceAll("\n|\r", ""); 
	    		result = result.replaceAll("> *?<", "><");
	    	}
	    	return result;
    	} catch (Exception e) {
    		throw new AtlasException(e);
    	}
    }
    
    public Document getDocument() {
		return document;
	}

	public boolean isEnableElementNamespaces() {
		return enableElementNamespaces;
	}

	public void setEnableElementNamespaces(boolean enableElementNamespaces) {
		this.enableElementNamespaces = enableElementNamespaces;
	}

	public boolean isEnableAttributeNamespaces() {
		return enableAttributeNamespaces;
	}

	public void setEnableAttributeNamespaces(boolean enableAttributeNamespaces) {
		this.enableAttributeNamespaces = enableAttributeNamespaces;
	}

	public boolean isIgnoreMissingNamespaces() {
		return ignoreMissingNamespaces;
	}

	public void setIgnoreMissingNamespaces(boolean ignoreMissingNamespaces) {
		this.ignoreMissingNamespaces = ignoreMissingNamespaces;
	}        
}
