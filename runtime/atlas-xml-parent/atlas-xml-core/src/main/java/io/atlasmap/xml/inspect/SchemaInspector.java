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
package io.atlasmap.xml.inspect;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.Restriction;
import io.atlasmap.xml.v2.RestrictionType;
import io.atlasmap.xml.v2.Restrictions;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlFields;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SchemaInspector {

    private static final XmlDocument XML_DOCUMENT = AtlasXmlModelFactory.createXmlDocument();
    private static final Map<String, FieldType> XS_TYPE_TO_FIELD_TYPE_MAP;
    private static final Map<String, FieldType> BLACKLISTED_TYPES;

    static {
        XS_TYPE_TO_FIELD_TYPE_MAP = new HashMap<>();
        XS_TYPE_TO_FIELD_TYPE_MAP.put("int", FieldType.INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("integer", FieldType.INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("negativeInteger", FieldType.INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("nonNegativeInteger", FieldType.INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("positiveInteger", FieldType.INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("nonPositiveInteger", FieldType.INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("string", FieldType.STRING);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("short", FieldType.SHORT);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("long", FieldType.LONG);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("double", FieldType.DOUBLE);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("float", FieldType.FLOAT);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("boolean", FieldType.BOOLEAN);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("date", FieldType.DATE);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("dateTime", FieldType.DATE_TIME);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("decimal", FieldType.DECIMAL);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("float", FieldType.FLOAT);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("unsignedLong", FieldType.UNSIGNED_LONG);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("unsignedInt", FieldType.UNSIGNED_INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("unsignedLong", FieldType.UNSIGNED_LONG);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("unsignedShort", FieldType.UNSIGNED_SHORT);

        BLACKLISTED_TYPES = new HashMap<>();
        BLACKLISTED_TYPES.put("NMTOKEN", FieldType.UNSUPPORTED);
        BLACKLISTED_TYPES.put("anyURI", FieldType.UNSUPPORTED);
        BLACKLISTED_TYPES.put("base64Binary", FieldType.UNSUPPORTED);
        BLACKLISTED_TYPES.put("byte", FieldType.UNSUPPORTED);
        BLACKLISTED_TYPES.put("unsignedByte", FieldType.UNSUPPORTED);
        BLACKLISTED_TYPES.put("hexBinary", FieldType.UNSUPPORTED);
        BLACKLISTED_TYPES.put("NOTATION", FieldType.UNSUPPORTED);
        BLACKLISTED_TYPES.put("QName", FieldType.UNSUPPORTED);
    }

    public XmlDocument getXmlDocument() {
        return XML_DOCUMENT;
    }

    public void inspect(File schemaFile) throws XmlInspectionException {
        Fields fields = new Fields();
        XML_DOCUMENT.setFields(fields);
        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        parser.setAnnotationParser(new DomAnnotationParserFactory());
        try {
            parser.parse(schemaFile);
            XSSchemaSet schemaSet = parser.getResult();
            printSchemaSet(schemaSet);
        } catch (SAXException | IOException e) {
            throw new XmlInspectionException(e);
        }
    }

    public void inspect(String schemaAsString) throws XmlInspectionException {
        Fields fields = new Fields();
        XML_DOCUMENT.setFields(fields);
        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        ByteArrayInputStream is;
        try {
            is = new ByteArrayInputStream(schemaAsString.getBytes("UTF-8"));
            parser.setAnnotationParser(new DomAnnotationParserFactory());
            parser.parse(is);
            XSSchemaSet schemaSet = parser.getResult();
            printSchemaSet(schemaSet);
        } catch (SAXException | UnsupportedEncodingException e) {
            throw new XmlInspectionException(e);
        }
    }

    private void printSchemaSet(XSSchemaSet schemaSet) throws XmlInspectionException {
        if (schemaSet == null) {
            throw new XmlInspectionException("Schema set is null");
        }
        Iterator<XSSchema> itr = schemaSet.iterateSchema();
        while (itr.hasNext()) {
            XSSchema s = itr.next();
            // check the target namespace where null == default ("") and needs no mapping
            if (s.getTargetNamespace() != null) {
                XML_DOCUMENT.setXmlNamespaces(new XmlNamespaces());
                XmlNamespace namespace = new XmlNamespace();
                namespace.setUri(s.getTargetNamespace());
                namespace.setAlias("tns");// default prefix for target namespace (is this the only one possible?)
                XML_DOCUMENT.getXmlNamespaces().getXmlNamespace().add(namespace);
            }
            // we only care about declared elements...
            Iterator<XSElementDecl> jtr = s.iterateElementDecls();
            while (jtr.hasNext()) {
                XSElementDecl e = jtr.next();
                String rootName = "/".concat(e.getName());
                if (e.getType().isComplexType()) {
                    XmlComplexType rootComplexType = getXmlComplexType();
                    rootComplexType.setName(e.getName());
                    rootComplexType.setPath(rootName);
                    rootComplexType.setFieldType(FieldType.COMPLEX);
                    XML_DOCUMENT.getFields().getField().add(rootComplexType);
                    printComplexType(e.getType().asComplexType(), rootName, rootComplexType);
                } else if (e.getType().isSimpleType()) {
                    XmlField xmlField = AtlasXmlModelFactory.createXmlField();
                    xmlField.setName(e.getName());
                    xmlField.setPath("/".concat(e.getName()));
                    XML_DOCUMENT.getFields().getField().add(xmlField);
                    printSimpleType(e.getType().asSimpleType(), xmlField);
                }
            }
        }
    }

    private void printComplexType(XSComplexType complexType, String rootName, XmlComplexType xmlComplexType) {
        printAttributes(complexType, rootName, xmlComplexType);
        XSParticle particle = complexType.getContentType().asParticle();
        if (particle != null) {
            printParticle(particle, rootName, xmlComplexType);
        }
    }

    private void printParticle(XSParticle particle, String rootName, XmlComplexType xmlComplexType) {
        XSTerm term = particle.getTerm();
        if (term.isModelGroup()) {
            XSModelGroup group = term.asModelGroup();
            printGroup(group, rootName, xmlComplexType);
        } else if (term.isModelGroupDecl()) {
            printGroupDecl(term.asModelGroupDecl(), rootName, xmlComplexType);
        } else if (term.isElementDecl()) {
            CollectionType collectionType = getCollectionType(particle);
            printElement(term.asElementDecl(), rootName, xmlComplexType, collectionType);
        }
    }

    private void printGroup(XSModelGroup modelGroup, String rootName, XmlComplexType xmlComplexType) {
        // this is the parent of the group
        for (XSParticle particle : modelGroup.getChildren()) {
            printParticle(particle, rootName, xmlComplexType);
        }
    }

    private void printGroupDecl(XSModelGroupDecl modelGroupDecl, String rootName, XmlComplexType parentXmlComplexType) {
        printGroup(modelGroupDecl.getModelGroup(), rootName, parentXmlComplexType);
    }

    private void printElement(XSElementDecl element, String root, XmlComplexType xmlComplexType,
            CollectionType collectionType) {
        String rootName = root;
        if (element.getType().isComplexType()) {
            XmlComplexType complexType = getXmlComplexType();
            rootName = rootName + "/" + element.getName();
            complexType.setName(element.getName());
            complexType.setPath(rootName);
            complexType.setCollectionType(collectionType);
            xmlComplexType.getXmlFields().getXmlField().add(complexType);
            printComplexType(element.getType().asComplexType(), rootName, complexType);
        } else {
            if (element.getType() != null && element.getType().asSimpleType() != null) {
                XmlField xmlField = AtlasXmlModelFactory.createXmlField();
                xmlField.setName(element.getName());
                xmlField.setPath(rootName + "/" + element.getName());
                xmlComplexType.getXmlFields().getXmlField().add(xmlField);
                if (element.getDefaultValue() != null) {
                    xmlField.setValue(element.getDefaultValue());
                } else if (element.getFixedValue() != null) {
                    xmlField.setValue(element.getFixedValue());
                }
                XSRestrictionSimpleType typeRestriction = element.getType().asSimpleType().asRestriction();
                if (typeRestriction != null) {
                    xmlField.setFieldType(XS_TYPE_TO_FIELD_TYPE_MAP.get(typeRestriction.getBaseType().getName()));
                    mapRestrictions(xmlField, typeRestriction);
                }
                printSimpleType(element.getType().asSimpleType(), xmlField);
            }
        }
    }

    private void printAttributes(XSComplexType xsComplexType, String rootName, XmlComplexType xmlComplexType) {
        Collection<? extends XSAttributeUse> c = xsComplexType.getDeclaredAttributeUses();
        for (XSAttributeUse aC : c) {
            XmlField xmlField = AtlasXmlModelFactory.createXmlField();
            XSAttributeDecl attributeDecl = aC.getDecl();
            xmlField.setName(attributeDecl.getName());
            if (attributeDecl.getDefaultValue() != null) {
                xmlField.setValue(attributeDecl.getDefaultValue().value);
            } else if (attributeDecl.getFixedValue() != null) {
                xmlField.setValue(attributeDecl.getFixedValue().value);
            }
            xmlField.setPath(rootName + "/" + "@" + attributeDecl.getName());
            FieldType attrType = getFieldType(attributeDecl.getType().getName());
            xmlField.setFieldType(attrType);
            if (xmlField.getFieldType() == null) {
                // check the simple types in the schema....
                XSSimpleType simpleType = xsComplexType.getRoot().getSimpleType(xsComplexType.getTargetNamespace(),
                        attributeDecl.getType().getName());
                if (simpleType != null) {
                    FieldType fieldType = getFieldType(simpleType.getBaseType().getName());
                    xmlField.setFieldType(fieldType);
                    xmlField.setTypeName(attributeDecl.getType().getName());
                    if (simpleType.asRestriction() != null) {
                        mapRestrictions(xmlField, simpleType.asRestriction());
                    }
                } else {
                    // cannot figure it out....
                    xmlField.setFieldType(FieldType.UNSUPPORTED);
                }
            }
            xmlComplexType.getXmlFields().getXmlField().add(xmlField);
        }
    }

    private void printSimpleType(XSSimpleType simpleType, XmlField xmlField) {
        if (xmlField.getFieldType() == null) {
            FieldType attrType = getFieldType(simpleType.getName());
            xmlField.setFieldType(attrType);
        }
    }

    private void mapRestrictions(XmlField xmlField, XSRestrictionSimpleType restrictionSimpleType) {
        SimpleTypeRestriction simpleTypeRestriction = new SimpleTypeRestriction();
        simpleTypeRestriction.initRestrictions(restrictionSimpleType);
        Restrictions restrictions = new Restrictions();
        xmlField.setRestrictions(restrictions);
        mapSimpleRestrictionToRestriction(simpleTypeRestriction, xmlField);
    }

    private XmlComplexType getXmlComplexType() {
        XmlComplexType rootComplexType = new XmlComplexType();
        rootComplexType.setFieldType(FieldType.COMPLEX);
        rootComplexType.setXmlFields(new XmlFields());
        return rootComplexType;
    }

    private FieldType getFieldType(String name) {
        // check the blacklist
        FieldType attrType = BLACKLISTED_TYPES.get(name);
        if (attrType == null) {
            attrType = XS_TYPE_TO_FIELD_TYPE_MAP.get(name);
        }
        return attrType;
    }

    private CollectionType getCollectionType(XSParticle particle) {
        if (!particle.isRepeated()) {
            return null;
        } else if (particle.isRepeated() && particle.getMaxOccurs().intValue() == -1) {
            return CollectionType.LIST;
        } else if (particle.isRepeated() && particle.getMaxOccurs().intValue() > 1) {
            return CollectionType.ARRAY;
        }
        return null;
    }

    private void mapSimpleRestrictionToRestriction(SimpleTypeRestriction simpleTypeRestriction, XmlField xmlField) {
        for (Field field : SimpleTypeRestriction.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                // do we even care about this restriction?
                if (typeRestrictionExists(field.getName())) {
                    Object value = field.get(simpleTypeRestriction);
                    if (value != null) {
                        Restriction restriction = new Restriction();
                        restriction.setValue((String) value);
                        restriction.setType(RestrictionType.fromValue(field.getName()));
                        xmlField.getRestrictions().getRestriction().add(restriction);
                    }
                }
            } catch (IllegalAccessException e) {
                // eat it...
            }
        }
    }

    private boolean typeRestrictionExists(String name) {
        for (RestrictionType restrictionType : RestrictionType.values()) {
            if (name.equals(restrictionType.value())) {
                return true;
            }
        }
        return false;
    }
}
