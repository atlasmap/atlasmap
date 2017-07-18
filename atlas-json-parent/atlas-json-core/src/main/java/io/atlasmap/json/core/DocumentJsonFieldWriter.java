package io.atlasmap.json.core;

import java.math.BigDecimal;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.atlasmap.api.AtlasException;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.FieldType;

/**
 */
public class DocumentJsonFieldWriter {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DocumentJsonFieldWriter.class);

    private ObjectMapper objectMapper = null;
    private ObjectNode rootNode = null;

    public DocumentJsonFieldWriter() {
    	this.objectMapper = new ObjectMapper();
    	objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        rootNode = objectMapper.createObjectNode();
    }
    
    public DocumentJsonFieldWriter(ObjectMapper objectMapper) {
    	this.objectMapper = objectMapper;
    	this.rootNode = objectMapper.createObjectNode();
    }
    
    public ObjectNode getRootNode() {
		return rootNode;		
	}
    
    public ObjectMapper getObjectMapper() {
		return objectMapper;
	}        
    
    public void write(JsonField field) throws AtlasException {
    	if (field == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'jsonField' cannot be null"));
        }
    	if (logger.isDebugEnabled()) {    		
    		logger.debug("Now processing field: " + AtlasJsonModelFactory.toString(field));
    		logger.debug("Field type: " + field.getFieldType());
    		logger.debug("Field path: " + field.getPath());
    		logger.debug("Field value: " + field.getValue());    		
    	}
    	JsonPath path = new JsonPath(field.getPath());
    	String lastSegment = path.getLastSegment();
    	ObjectNode parentNode = this.rootNode;
    	String parentSegment = null;
    	for (String segment : path.getSegments()) {    		
    		if (segment != lastSegment) { //this is a parent node.
    			if (logger.isDebugEnabled()) {
    				logger.debug("Now processing parent segment: " + segment);
    			}
    			JsonNode childNode = getChildNode(parentNode, parentSegment, segment);
    			if (childNode == null) {
    				childNode = createParentNode(parentNode, parentSegment, segment);
    			} else if (childNode instanceof ArrayNode) {
    				int index = JsonPath.indexOfSegment(segment);
    				ArrayNode arrayChild = (ArrayNode) childNode;
    				if (arrayChild.size() < (index + 1)) {
    					if (logger.isDebugEnabled()) {
    						logger.debug("Object Array is too small, resizing to accomodate index: " + index + ", current array: " + arrayChild);
    					}
    					//if our array doesn't have index + 1 items in it, add nulls until we have the index available
    					while (arrayChild.size() < (index + 1)) {
    						arrayChild.addObject();
    					}
    					if (logger.isDebugEnabled()) {
    						logger.debug("Object Array after resizing: " + arrayChild);
    					}
    				}
    				childNode = arrayChild.get(index);
    			}
    			parentNode = (ObjectNode) childNode;
    			parentSegment = segment;
    		} else { //this is the last segment of the path, write the value
    			if (logger.isDebugEnabled()) {
    				logger.debug("Now processing field value segment: " + segment);
    			}
    			writeValue(parentNode, parentSegment, segment, field);
    		}    		
    	}                        
    }
    
    public void writeValue(ObjectNode parentNode, String parentSegment, String segment, JsonField field) throws AtlasException {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Writing field value '" + segment + "' in parent node '" + parentSegment + "', parentNode: " + parentNode);
    	}
    	JsonNode valueNode = createValueNode(field);
		String cleanedSegment = JsonPath.cleanPathSegment(segment);
		if (JsonPath.isCollectionSegment(segment)) {
			//if this field is a collection, we need to place our value in an array
			
			//get or construct the array the value will be placed in
			if (logger.isDebugEnabled()) {
				logger.debug("Field type is collection. Fetching array '" + segment + 
						"' from parent '" + parentSegment + "': " + parentNode);
			}

			ArrayNode arrayChild = (ArrayNode) getChildNode(parentNode, parentSegment, segment);
			if (arrayChild == null) {
				arrayChild = parentNode.putArray(cleanedSegment);
				if (logger.isDebugEnabled()) {
					logger.debug("Could not find array to place value in, created it in parent: " + parentNode);
				}
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("Array before placing value: " + arrayChild);
			}
						
			//determine where in the array our value will go
			int index = JsonPath.indexOfSegment(segment);
			
			if (arrayChild.size() < (index + 1)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Value Array is too small, resizing to accomodate index: " + index + ", current array: " + arrayChild);
				}
				//if our array doesn't have index + 1 items in it, add nulls until we have the index available
				while (arrayChild.size() < (index + 1)) {
					arrayChild.addNull();
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Value Array after resizing: " + arrayChild);
				}
			}
			
			//set the value in the array
			arrayChild.set(index, valueNode);
		} else {
			//on a regular primitive value, just set it in the object node parent
			parentNode.replace(cleanedSegment, valueNode);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Parent node after value written: " + parentNode);
		}
    }
    
    public JsonNode getChildNode(ObjectNode parentNode, String parentSegment, String segment) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Looking for child node '" + segment + "' in parent '" + parentSegment + "': " + parentNode);
    	}
    	String cleanedSegment = JsonPath.cleanPathSegment(segment);
    	JsonNode childNode = parentNode.path(cleanedSegment);
    	if (JsonNodeType.MISSING.equals(childNode.getNodeType())) {
    		childNode = null;
    	}
    	if (logger.isDebugEnabled()) {
    		if (childNode == null) {
    			logger.debug("Could not find child node '" + segment + "' in parent '" + parentSegment + "'.");	
    		} else {
    			logger.debug("Found child node '" + segment + "' in parent '" + parentSegment + "', class: " + childNode.getClass().getName() + ", node: " + childNode);
    		}    		
    	}
    	return childNode;
    }
    
    public ObjectNode createParentNode(ObjectNode parentNode, String parentSegment, String segment) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Creating parent node '" + segment + "' under previous parent '" + parentSegment + "' (" + parentNode.getClass().getName() + ")");
    	}
    	ObjectNode childNode = null;
    	String cleanedSegment = JsonPath.cleanPathSegment(segment);
		if (JsonPath.isCollectionSegment(segment)) {
			ArrayNode arrayChild = parentNode.putArray(cleanedSegment);
			int index = JsonPath.indexOfSegment(segment);
			
			if (arrayChild.size() < (index + 1)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Object Array is too small, resizing to accomodate index: " + index + ", current array: " + arrayChild);
				}
				//if our array doesn't have index + 1 items in it, add objects until we have the index available
				while (arrayChild.size() < (index + 1)) {
					arrayChild.addObject();
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Object Array after resizing: " + arrayChild);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Created wrapper parent array node '" + segment + "': " + arrayChild);
			}
			childNode = (ObjectNode) arrayChild.get(index);			
		} else {   				
			childNode = parentNode.putObject(cleanedSegment);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Parent Node '" + parentSegment + "' after adding child parent node '" + segment + "':" + parentNode);
		}
		return childNode;
    }
    
    public JsonNode createValueNode(JsonField jsonField) throws AtlasException {
    	FieldType type = jsonField.getFieldType();
    	Object value = jsonField.getValue();
    	JsonNode valueNode = null;
    	if (FieldType.STRING.equals(type)) {
            valueNode = rootNode.textNode((String)value);
        } else if (FieldType.CHAR.equals(type)) {
        	valueNode = rootNode.textNode(Character.toString((char) value));            
        } else if (FieldType.BOOLEAN.equals(type)) {
        	valueNode = rootNode.booleanNode((Boolean)value);
        } else if (FieldType.INTEGER.equals(type)) {
        	valueNode = rootNode.numberNode((Integer)value);            
        } else if (FieldType.DOUBLE.equals(type) || FieldType.FLOAT.equals(type)) {
        	valueNode = rootNode.numberNode(new BigDecimal(String.valueOf(value)));
        } else if (FieldType.SHORT.equals(type)) {
        	valueNode = rootNode.numberNode(Short.valueOf(String.valueOf(value)));
        } else if (FieldType.LONG.equals(type)) {
        	valueNode = rootNode.numberNode(Long.valueOf(String.valueOf(value)));
        } else {
            throw new AtlasException("Cannot set value for " + jsonField.getPath() + " --> " + value + " for field type " + type);
        }    	
    	if (logger.isDebugEnabled()) {
    		String valueClass = value == null ? "null" : value.getClass().getName();
    		logger.debug("Converted JsonField value to ValueNode. Type: " + type 
    				+ ", value: " + value + "(" + valueClass 
    				+ "), node class: " + valueNode.getClass().getName() + ", node: "+ valueNode);
    	}
    	return valueNode;
    }    
}
