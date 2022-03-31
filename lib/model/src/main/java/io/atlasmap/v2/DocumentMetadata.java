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
package io.atlasmap.v2;

import java.io.Serializable;
import java.util.Map;

/**
 * The Document metadata.
 * @see DocumentCatalog
 */
public class DocumentMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Document ID. */
    private String id;
    /** Document name. */
    private String name;
    /** Document description. */
    private String description;
    /** URI. */
    private String uri;
    /** DataSource type */
    private DataSourceType dataSourceType;
    /** Document type */
    private DocumentType documentType;
    /** inspection type */
    private InspectionType inspectionType;
    /** inspection parameters */
    private Map<String,String> inspectionParameters;
    /** Field name exclusions. */
    private StringList fieldNameExclusions;
    /** Type name exclusions. */
    private StringList typeNameExclusions;
    /** Namespace exclusions. */
    private StringList namespaceExclusions;

    /**
     * Gets the ID.
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID.
     * @param id ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the URI.
     * @return URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI.
     * @param uri URI
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the Document type.
     * @return Document type
     */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /**
     * Sets the Document type.
     * @param documentType Document type
     */
    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    /**
     * Gets the inspection type.
     * @return inspection type
     */
    public InspectionType getInspectionType() {
        return inspectionType;
    }

    /**
     * Sets the inspection type.
     * @param inspectionType inspection type
     */
    public void setInspectionType(InspectionType inspectionType) {
        this.inspectionType = inspectionType;
    }

    /**
     * Gets the DataSource type.
     * @return DataSource type
     */
    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    /**
     * Sets the DataSource type.
     * @param dataSourceType DataSource type.
     */
    public void setDataSourceType(DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    /**
     * Gets the inspection parameters.
     * @return inspection parameters
     */
    public Map<String,String> getInspectionParameters() {
        return inspectionParameters;
    }

    /**
     * Sets the inspection parameters.
     * @param inspectionParameters inspection parameters
     */
    public void setInspectionParameters(Map<String,String> inspectionParameters) {
        this.inspectionParameters = inspectionParameters;
    }

    public StringList getFieldNameExclusions() {
        return fieldNameExclusions;
    }

    public void setFieldNameExclusions(StringList fieldNameExclusions) {
        this.fieldNameExclusions = fieldNameExclusions;
    }

    public StringList getTypeNameExclusions() {
        return typeNameExclusions;
    }

    public void setTypeNameExclusions(StringList typeNameExclusions) {
        this.typeNameExclusions = typeNameExclusions;
    }

    public StringList getNamespaceExclusions() {
        return namespaceExclusions;
    }

    public void setNamespaceExclusions(StringList namespaceExclusions) {
        this.namespaceExclusions = namespaceExclusions;
    }

}
