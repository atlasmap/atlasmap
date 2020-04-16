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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;

import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;
import io.atlasmap.xml.core.AtlasXmlConstants;
import io.atlasmap.xml.core.AtlasXmlNamespaceContext;
import io.atlasmap.xml.core.XmlComplexTypeFactory;
import io.atlasmap.xml.core.schema.AtlasXmlSchemaSetParser;
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

public class XmlSchemaInspector {

    static final Logger LOG = LoggerFactory.getLogger(XmlSchemaInspector.class);
    private static final Map<String, FieldType> XS_TYPE_TO_FIELD_TYPE_MAP;
    private static final Map<String, FieldType> BLACKLISTED_TYPES;

    static {
        XS_TYPE_TO_FIELD_TYPE_MAP = new HashMap<>();
        XS_TYPE_TO_FIELD_TYPE_MAP.put("int", FieldType.INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("integer", FieldType.BIG_INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("negativeInteger", FieldType.BIG_INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("nonNegativeInteger", FieldType.BIG_INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("positiveInteger", FieldType.BIG_INTEGER);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("nonPositiveInteger", FieldType.BIG_INTEGER);
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

    private XmlDocument xmlDocument;
    private AtlasXmlNamespaceContext namespaceContext;
    private String rootNamespace;
    private ClassLoader classLoader;

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    public XmlDocument getXmlDocument() {
        return xmlDocument;
    }

    public void inspect(File schemaFile) throws XmlInspectionException {
        try {
            doInspect(new FileInputStream(schemaFile));
        } catch (Exception e) {
            throw new XmlInspectionException(e);
        }
    }

    public void inspect(String schemaAsString) throws XmlInspectionException {
        try {
            doInspect(new ByteArrayInputStream(schemaAsString.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new XmlInspectionException(e);
        }
    }

    private void doInspect(InputStream is) throws Exception {
        xmlDocument = AtlasXmlModelFactory.createXmlDocument();
        Fields fields = new Fields();
        xmlDocument.setFields(fields);

        AtlasXmlSchemaSetParser parser = new AtlasXmlSchemaSetParser(this.classLoader);
        XSSchemaSet schemaSet = parser.parse(is);
        this.namespaceContext = parser.getNamespaceContext();
        this.rootNamespace = parser.getRootNamespace();
        printSchemaSet(schemaSet);
        populateNamespaces();
    }

    private String getNameNS(XSDeclaration decl) {
        String targetNamespace = decl.getTargetNamespace();
        if (targetNamespace == null || targetNamespace.isEmpty()) {
            targetNamespace = decl.getOwnerSchema().getTargetNamespace();
        }
        if (targetNamespace != null && !targetNamespace.isEmpty()) {
            String prefix = namespaceContext.getPrefix(targetNamespace);
            if (prefix == null || prefix.isEmpty()) {
                prefix = namespaceContext.addWithIndex(targetNamespace);
            }
            return String.format("%s:%s", prefix, decl.getName());
        }
        return decl.getName();
    }

    private void populateNamespaces() {
        for (Entry<String, String> entry : namespaceContext.getNamespaceMap().entrySet()) {
            String prefix = entry.getKey();
            String uri = entry.getValue();
            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(uri)
                    || AtlasXmlConstants.ATLAS_XML_SCHEMASET_NAMESPACE.equals(uri)) {
                continue;
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("adding a namespace >>> prefix={}, uri={}", prefix, uri);
            }
            if (xmlDocument.getXmlNamespaces() == null) {
                xmlDocument.setXmlNamespaces(new XmlNamespaces());
            }
            XmlNamespace namespace = new XmlNamespace();
            namespace.setAlias(prefix);
            namespace.setUri(uri);
            xmlDocument.getXmlNamespaces().getXmlNamespace().add(namespace);
        }
    }

    private void printSchemaSet(XSSchemaSet schemaSet) throws Exception {
        if (schemaSet == null) {
            throw new XmlInspectionException("Schema set is null");
        }

        XSSchema schema = rootNamespace != null ? schemaSet.getSchema(rootNamespace) : schemaSet.getSchema("");
        // we only care about declared elements...
        Iterator<XSElementDecl> jtr = schema.iterateElementDecls();
        while (jtr.hasNext()) {
            Set<String> cachedComplexType = new HashSet<>();
            XSElementDecl e = jtr.next();
            String rootName = getNameNS(e);

            if (e.getType().isComplexType()) {
                XmlComplexType rootComplexType = getXmlComplexType();
                rootComplexType.setName(rootName);
                rootComplexType.setPath("/" + rootName);
                rootComplexType.setFieldType(FieldType.COMPLEX);
                xmlDocument.getFields().getField().add(rootComplexType);
                printComplexType(e.getType().asComplexType(), "/" + rootName, rootComplexType, cachedComplexType);
            } else if (e.getType().isSimpleType()) {
                XmlField xmlField = AtlasXmlModelFactory.createXmlField();
                xmlField.setName(rootName);
                xmlField.setPath("/" + rootName);
                xmlDocument.getFields().getField().add(xmlField);
                printSimpleType(e.getType().asSimpleType(), xmlField);
            }
        }
    }

    private void printComplexType(XSComplexType complexType, String rootName, XmlComplexType xmlComplexType,
            Set<String> cachedComplexType) throws Exception {
        printAttributes(complexType, rootName, xmlComplexType);
        XSParticle particle = complexType.getContentType().asParticle();
        if (particle != null) {
            printParticle(particle, rootName, xmlComplexType, cachedComplexType);
        }
    }

    private void printParticle(XSParticle particle, String rootName, XmlComplexType xmlComplexType,
            Set<String> cachedComplexType) throws Exception {
        XSTerm term = particle.getTerm();
        if (term.isModelGroup()) {
            XSModelGroup group = term.asModelGroup();
            printGroup(group, rootName, xmlComplexType, cachedComplexType);
        } else if (term.isModelGroupDecl()) {
            printGroupDecl(term.asModelGroupDecl(), rootName, xmlComplexType, cachedComplexType);
        } else if (term.isElementDecl()) {
            CollectionType collectionType = getCollectionType(particle);
            printElement(term.asElementDecl(), rootName, xmlComplexType, collectionType, cachedComplexType);
        }
    }

    private void printGroup(XSModelGroup modelGroup, String rootName, XmlComplexType xmlComplexType,
            Set<String> cachedComplexType) throws Exception {
        // this is the parent of the group
        for (XSParticle particle : modelGroup.getChildren()) {
            // Don't cache siblings to avoid https://github.com/atlasmap/atlasmap/issues/255
            Set<String> cachedTypeCopy = new HashSet<>(cachedComplexType);
            printParticle(particle, rootName, xmlComplexType, cachedTypeCopy);
        }
    }

    private void printGroupDecl(XSModelGroupDecl modelGroupDecl, String rootName, XmlComplexType parentXmlComplexType,
            Set<String> cachedComplexType) throws Exception {
        printGroup(modelGroupDecl.getModelGroup(), rootName, parentXmlComplexType, cachedComplexType);
    }

    private void printElement(XSElementDecl element, String root, XmlComplexType xmlComplexType,
            CollectionType collectionType, Set<String> cachedComplexType) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Element: {}/{}", root, getNameNS(element));
        }
        String rootName = root;
        if (element.getType().isComplexType()) {
            XmlComplexType complexType = getXmlComplexType();
            rootName = rootName + "/" + getNameNS(element);
            complexType.setName(getNameNS(element));
            complexType.setPath(rootName);
            complexType.setCollectionType(collectionType);
            xmlComplexType.getXmlFields().getXmlField().add(complexType);
            String typeName = getNameNS(element.getType());
            if (typeName != null && !typeName.isEmpty() && cachedComplexType.contains(typeName)) {
                complexType.setStatus(FieldStatus.CACHED);
            } else {
                cachedComplexType.add(typeName);
                printComplexType(element.getType().asComplexType(), rootName, complexType, cachedComplexType);
            }
        } else {
            if (element.getType() != null && element.getType().asSimpleType() != null) {
                XmlField xmlField = AtlasXmlModelFactory.createXmlField();
                xmlField.setName(getNameNS(element));
                xmlField.setPath(rootName + "/" + getNameNS(element));
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
            xmlField.setName(getNameNS(attributeDecl));
            if (attributeDecl.getDefaultValue() != null) {
                xmlField.setValue(attributeDecl.getDefaultValue().value);
            } else if (attributeDecl.getFixedValue() != null) {
                xmlField.setValue(attributeDecl.getFixedValue().value);
            }
            xmlField.setPath(rootName + "/" + "@" + getNameNS(attributeDecl));
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
        XmlComplexType rootComplexType = XmlComplexTypeFactory.createXmlComlexField();
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
                    if (value instanceof String[]) {
                        String[] values = (String[]) value;
                        for (String v : values) {
                            Restriction restriction = new Restriction();
                            restriction.setValue(v.toString());
                            restriction.setType(RestrictionType.fromValue(field.getName()));
                            xmlField.getRestrictions().getRestriction().add(restriction);
                        }
                    } else if (value != null) {
                        Restriction restriction = new Restriction();
                        restriction.setValue(value.toString());
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
