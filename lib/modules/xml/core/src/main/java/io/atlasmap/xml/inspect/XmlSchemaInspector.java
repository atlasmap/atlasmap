/*
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
import java.util.List;
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
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;

import io.atlasmap.core.AtlasPath;
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
import io.atlasmap.xml.v2.XmlEnumField;
import io.atlasmap.xml.v2.XmlEnumFields;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlFields;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

public class XmlSchemaInspector {

    static final Logger LOG = LoggerFactory.getLogger(XmlSchemaInspector.class);
    private static final Map<String, FieldType> XS_TYPE_TO_FIELD_TYPE_MAP;
    private static final Map<String, FieldType> EXCLUDED_TYPES;

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
        XS_TYPE_TO_FIELD_TYPE_MAP.put("NMTOKEN", FieldType.STRING);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("anyURI", FieldType.STRING);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("base64Binary", FieldType.STRING);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("hexBinary", FieldType.STRING);
        XS_TYPE_TO_FIELD_TYPE_MAP.put("QName", FieldType.STRING);

        EXCLUDED_TYPES = new HashMap<>();
        EXCLUDED_TYPES.put("NOTATION", FieldType.UNSUPPORTED);
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
        if (decl.getName() == null) {
            return null;
        }
        String targetNamespace = decl.getTargetNamespace();
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
            XSElementDecl e = jtr.next();
            String rootName = getNameNS(e);

            if (e.getType().isComplexType()) {
                XmlComplexType rootComplexType = getXmlComplexType();
                rootComplexType.setName(rootName);
                rootComplexType.setPath("/" + rootName);
                rootComplexType.setFieldType(FieldType.COMPLEX);
                xmlDocument.getFields().getField().add(rootComplexType);
                printComplexType(e.getType().asComplexType(), rootComplexType, new HashSet<>());
            } else if (e.getType().isSimpleType()) {
                XmlField xmlField = AtlasXmlModelFactory.createXmlField();
                xmlField.setName(rootName);
                xmlField.setPath("/" + rootName);
                xmlDocument.getFields().getField().add(xmlField);
                printSimpleType(e.getType().asSimpleType(), xmlField);
            }
        }
    }

    private void printComplexType(XSComplexType complexType, XmlComplexType parentXmlComplexType,
            Set<String> cachedComplexType) throws Exception {
        printAttributes(complexType, parentXmlComplexType);
        XSParticle particle = complexType.getContentType().asParticle();
        if (particle != null) {
            printParticle(particle, parentXmlComplexType, cachedComplexType);
        }
    }

    private void printParticle(XSParticle particle, XmlComplexType parentXmlComplexType,
            Set<String> cachedComplexType) throws Exception {
        XSTerm term = particle.getTerm();
        if (term.isModelGroup()) {
            XSModelGroup group = term.asModelGroup();
            printGroup(group, parentXmlComplexType, cachedComplexType);
        } else if (term.isModelGroupDecl()) {
            printGroupDecl(term.asModelGroupDecl(), parentXmlComplexType, cachedComplexType);
        } else if (term.isElementDecl()) {
            CollectionType collectionType = getCollectionType(particle);
            printElement(term.asElementDecl(), parentXmlComplexType, collectionType,
                    cachedComplexType);
        }
    }

    private void printGroup(XSModelGroup modelGroup, XmlComplexType parentXmlComplexType,
            Set<String> cachedComplexType) throws Exception {
        // this is the parent of the group
        for (XSParticle particle : modelGroup.getChildren()) {
            // cache applies only vertically to avoid recursion
            Set<String> cachedTypeCopy = new HashSet<>(cachedComplexType);
            printParticle(particle, parentXmlComplexType, cachedTypeCopy);
        }
    }

    private void printGroupDecl(XSModelGroupDecl modelGroupDecl, XmlComplexType parentXmlComplexType,
            Set<String> cachedComplexType) throws Exception {
        printGroup(modelGroupDecl.getModelGroup(), parentXmlComplexType, cachedComplexType);
    }

    private void printElement(XSElementDecl element, XmlComplexType parentXmlComplexType,
            CollectionType collectionType, Set<String> cachedComplexType) throws Exception {
        String parentPath = parentXmlComplexType.getPath();
        String elementName = getNameNS(element);
        String typeName = getNameNS(element.getType());
        XSType elementType = element.getType();
        if (elementType == null) {
            return;
        }
        if (elementType.isComplexType()) {
            XmlComplexType complexType = getXmlComplexType();
            String path = parentPath + "/" + elementName + getCollectionPathSuffix(collectionType);
            complexType.setName(elementName);
            complexType.setPath(path);
            complexType.setCollectionType(collectionType);
            parentXmlComplexType.getXmlFields().getXmlField().add(complexType);

            if (typeName != null && !typeName.isEmpty() && cachedComplexType.contains(typeName)) {
                complexType.setStatus(FieldStatus.CACHED);
            } else if (typeName != null) {
                cachedComplexType.add(typeName);
            }

            if (complexType.getStatus() != FieldStatus.CACHED) {
                printComplexType(element.getType().asComplexType(), complexType, cachedComplexType);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Element: {}/{}", parentPath, getNameNS(element));
                }
            }
        } else if (elementType.asSimpleType() != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Element: {}/{}", parentPath, getNameNS(element));
            }

            XSRestrictionSimpleType restrictionType = elementType.asSimpleType().asRestriction();
            List<XSFacet> enumerations = restrictionType != null ? restrictionType.getFacets("enumeration") : null;
            if (enumerations != null && !enumerations.isEmpty()) {
                XmlComplexType complexType = getXmlComplexType();
                String path = parentPath + "/" + elementName + getCollectionPathSuffix(collectionType);
                complexType.setName(elementName);
                complexType.setPath(path);
                complexType.setCollectionType(collectionType);
                complexType.setEnumeration(true);
                parentXmlComplexType.getXmlFields().getXmlField().add(complexType);
                XmlEnumFields enums = new XmlEnumFields();
                complexType.setXmlEnumFields(enums);
                for (XSFacet enumFacet : enumerations) {
                    XmlEnumField f = new XmlEnumField();
                    f.setName(enumFacet.getValue().toString());
                    enums.getXmlEnumField().add(f);
                }
                return;
            }

            XmlField xmlField = AtlasXmlModelFactory.createXmlField();
            xmlField.setName(elementName);
            xmlField.setPath(parentPath + "/" + elementName + getCollectionPathSuffix(collectionType));
            xmlField.setCollectionType(collectionType);
            parentXmlComplexType.getXmlFields().getXmlField().add(xmlField);
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

    private String getCollectionPathSuffix(CollectionType type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case ARRAY:
                return AtlasPath.PATH_ARRAY_SUFFIX;
            case LIST:
                return AtlasPath.PATH_LIST_SUFFIX;
            case MAP:
                return AtlasPath.PATH_MAP_SUFFIX;
            default:
                return "";
        }
    }

    private void printAttributes(XSComplexType xsComplexType, XmlComplexType parentXmlComplexType) {
        Collection<? extends XSAttributeUse> c = xsComplexType.getDeclaredAttributeUses();
        for (XSAttributeUse aC : c) {
            XmlField xmlField = AtlasXmlModelFactory.createXmlField();
            XSAttributeDecl attributeDecl = aC.getDecl();
            xmlField.setName(getNameNS(attributeDecl));
            xmlField.setAttribute(true);
            if (attributeDecl.getDefaultValue() != null) {
                xmlField.setValue(attributeDecl.getDefaultValue().value);
            } else if (attributeDecl.getFixedValue() != null) {
                xmlField.setValue(attributeDecl.getFixedValue().value);
            }
            xmlField.setPath(parentXmlComplexType.getPath() + "/" + "@" + getNameNS(attributeDecl));
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
            parentXmlComplexType.getXmlFields().getXmlField().add(xmlField);
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
        // check the exclusions
        FieldType attrType = EXCLUDED_TYPES.get(name);
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
