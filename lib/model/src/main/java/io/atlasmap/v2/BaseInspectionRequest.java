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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base class for Document inspection request that AtlasMap UI sends
 * to the backend.
 */
public abstract class BaseInspectionRequest implements Serializable {

    /** Document ID. */
    private String documentId;
    /** Document Name. */
    private String documentName;
    /** Document Description. */
    private String documentDescription;
    /** URI. */
    private String uri;
    /** DataSource type */
    private DataSourceType dataSourceType;
    /** Document type */
    private DocumentType documentType;
    /** Inspection type. */
    private InspectionType inspectionType;
    /** Inspection options. */
    private Map<String, String> options = new HashMap<>();
    /** Field name exclusions. */
    private StringList fieldNameExclusions;
    /** Type name exclusions. */
    private StringList typeNameExclusions;
    /** Namespace exclusions. */
    private StringList namespaceExclusions;
    /** Inspection Paths. */
    private List<String> inspectPaths;
    /** Search Paths. */
    private String searchPhrase;

    /**
     * Gets the Document ID.
     * @return Document ID
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the Document ID.
     * @param documentId Document ID
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the Document name.
     * @return Document name
     */
    public String getDocumentName() {
        return documentName;
    }

    /**
     * Sets the Document name.
     * @param documentName Document name
     */
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    /**
     * Gets the Document description.
     * @return Document description
     */
    public String getDocumentDescription() {
        return documentDescription;
    }

    /**
     * Sets the Document description.
     * @param documentDescription Document description
     */
    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    /**
     * Gets the value of the uri property.
     *
     * @return
     *     possible object is
     *     {@link String }
     * @deprecated https://github.com/atlasmap/atlasmap/issues/3907
     */
    @Deprecated
    public String getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     * @deprecated https://github.com/atlasmap/atlasmap/issues/3907
     */
    @Deprecated
    public void setUri(String value) {
        this.uri = value;
    }

    /**
     * Gets the DataSourceType.
     * @return DataSourceType
     */
    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    /**
     * Sets the DataSourceType.
     * @param dataSourceType DataSourceType
     */
    public void setDataSourceType(DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    /**
     * Gets the DocumentType.
     * @return DocumentType
     */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /**
     * Sets the DocumentType.
     * @param documentType DocumentType
     */
    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    /**
     * Gets the value of the inspectionType property.
     *
     * @return
     *     possible object is
     *     {@link InspectionType }
     *
     */
    public InspectionType getInspectionType() {
        return inspectionType;
    }

    /**
     * Sets the value of the inspectionType property.
     *
     * @param value
     *     allowed object is
     *     {@link InspectionType }
     *
     */
    public void setInspectionType(InspectionType value) {
        this.inspectionType = value;
    }

    /**
     * Gets the value of the options property.
     * 
     * @return
     *     possible object is
     *     {@link Map }
     *     
     */
    public Map<String, String> getOptions() {
        return this.options;
    }

    /**
     * Sets the value of the options property.
     * 
     * @param options
     *     allowed object is
     *     {@link Map }
     *     
     */
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    /**
     * Gets the value of the fieldNameExclusions property.
     *
     * @return
     *     possible object is
     *     {@link StringList }
     *
     */
    public StringList getFieldNameExclusions() {
        return fieldNameExclusions;
    }

    /**
     * Sets the value of the fieldNameExclusions property.
     *
     * @param value
     *     allowed object is
     *     {@link StringList }
     *
     */
    public void setFieldNameExclusions(StringList value) {
        this.fieldNameExclusions = value;
    }

    /**
     * Gets the value of the typeNameExclusions property.
     *
     * @return
     *     possible object is
     *     {@link StringList }
     *
     */
    public StringList getTypeNameExclusions() {
        return typeNameExclusions;
    }

    /**
     * Sets the value of the typeNameExclusions property.
     *
     * @param value
     *     allowed object is
     *     {@link StringList }
     *
     */
    public void setTypeNameExclusions(StringList value) {
        this.typeNameExclusions = value;
    }

    /**
     * Gets the value of the namespaceExclusions property.
     *
     * @return
     *     possible object is
     *     {@link StringList }
     *
     */
    public StringList getNamespaceExclusions() {
        return namespaceExclusions;
    }

    /**
     * Sets the value of the namespaceExclusions property.
     *
     * @param value
     *     allowed object is
     *     {@link StringList }
     *
     */
    public void setNamespaceExclusions(StringList value) {
        this.namespaceExclusions = value;
    }

    /**
     * Gets the paths to inspect.
     * @return paths
     * 
     * @deprecated https://github.com/atlasmap/atlasmap/issues/3909
     */
    public List<String> getInspectPaths() {
        return inspectPaths;
    }

    /**
     * Sets the paths to inspect.
     * @param inspectPaths paths
     * 
     * @deprecated https://github.com/atlasmap/atlasmap/issues/3909
     */
    public void setInspectPaths(List<String> inspectPaths) {
        this.inspectPaths = inspectPaths;
    }

    /**
     * Gets the search phrase to limit the inspection.
     * @return searchPhrase
     * 
     * @deprecated https://github.com/atlasmap/atlasmap/issues/3908
     */
    public String getSearchPhrase() {
        return searchPhrase;
    }

    /**
     * Gets the search phrase to limit the inspection.
     * @param searchPhrase phrase
     * 
     * @deprecated https://github.com/atlasmap/atlasmap/issues/3908
     */
    public void setSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }

    @Override
    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final BaseInspectionRequest that = ((BaseInspectionRequest) object);
        {
            StringList leftFieldNameExclusions;
            leftFieldNameExclusions = this.getFieldNameExclusions();
            StringList rightFieldNameExclusions;
            rightFieldNameExclusions = that.getFieldNameExclusions();
            if (this.fieldNameExclusions!= null) {
                if (that.fieldNameExclusions!= null) {
                    if (!leftFieldNameExclusions.equals(rightFieldNameExclusions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.fieldNameExclusions!= null) {
                    return false;
                }
            }
        }
        {
            StringList leftTypeNameExclusions;
            leftTypeNameExclusions = this.getTypeNameExclusions();
            StringList rightTypeNameExclusions;
            rightTypeNameExclusions = that.getTypeNameExclusions();
            if (this.typeNameExclusions!= null) {
                if (that.typeNameExclusions!= null) {
                    if (!leftTypeNameExclusions.equals(rightTypeNameExclusions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.typeNameExclusions!= null) {
                    return false;
                }
            }
        }
        {
            StringList leftNamespaceExclusions;
            leftNamespaceExclusions = this.getNamespaceExclusions();
            StringList rightNamespaceExclusions;
            rightNamespaceExclusions = that.getNamespaceExclusions();
            if (this.namespaceExclusions!= null) {
                if (that.namespaceExclusions!= null) {
                    if (!leftNamespaceExclusions.equals(rightNamespaceExclusions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.namespaceExclusions!= null) {
                    return false;
                }
            }
        }
        {
            String leftUri;
            leftUri = this.getUri();
            String rightUri;
            rightUri = that.getUri();
            if (this.uri!= null) {
                if (that.uri!= null) {
                    if (!leftUri.equals(rightUri)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.uri!= null) {
                    return false;
                }
            }
        }
        {
            InspectionType leftType;
            leftType = this.getInspectionType();
            InspectionType rightType;
            rightType = that.getInspectionType();
            if (this.inspectionType!= null) {
                if (that.inspectionType!= null) {
                    if (!leftType.equals(rightType)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.inspectionType!= null) {
                    return false;
                }
            }
        }
        {
            Map<String, String> leftOptions;
            leftOptions = this.getOptions();
            Map<String, String> rightOptions;
            rightOptions = that.getOptions();
            if (leftOptions !=  null) {
                if (rightOptions != null) {
                    if (!leftOptions.equals(rightOptions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (rightOptions != null) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int currentHashCode = 1;
        {
            currentHashCode = (currentHashCode* 31);
            StringList theFieldNameExclusions;
            theFieldNameExclusions = this.getFieldNameExclusions();
            if (this.fieldNameExclusions!= null) {
                currentHashCode += theFieldNameExclusions.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            StringList theTypeNameExclusions;
            theTypeNameExclusions = this.getTypeNameExclusions();
            if (this.typeNameExclusions!= null) {
                currentHashCode += theTypeNameExclusions.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            StringList theNamespaceExclusions;
            theNamespaceExclusions = this.getNamespaceExclusions();
            if (this.namespaceExclusions!= null) {
                currentHashCode += theNamespaceExclusions.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theUri;
            theUri = this.getUri();
            if (this.uri!= null) {
                currentHashCode += theUri.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            InspectionType theType;
            theType = this.getInspectionType();
            if (this.inspectionType!= null) {
                currentHashCode += theType.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Map<String, String> theOptions = this.getOptions();
            if (this.options != null) {
                currentHashCode += theOptions.hashCode();
            }
        }
        return currentHashCode;
    }
}
