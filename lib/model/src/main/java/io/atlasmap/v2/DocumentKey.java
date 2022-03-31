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

/**
 * The unique key to identify the Document which is a combination of {@link DataSourceType}
 * to indicate whether it's a source Document or target Document, and <code>Document ID</code>.
 */
public class DocumentKey implements Serializable {
    private DataSourceType dataSourceType;
    private String documentId;

    /**
     * A constructor.
     * @param dsType SOURCE or TARGET
     * @param docId
     */
    public DocumentKey(DataSourceType dsType, String docId) {
        dataSourceType = dsType;
        documentId = docId;
    }

    /**
     * Gets the {@link DataSourceType}.
     * @return DataSourceType
     */
    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    /**
     * Sets the {@link DataSourceType}.
     * @param dataSourceType DataSourceType
     */
    public void setDataSourceType(DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

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

    @Override
    public String toString() {
        return dataSourceType + ":" + documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DocumentKey)) {
            return false;
        }
        DocumentKey other = (DocumentKey)o;
        if (dataSourceType != other.dataSourceType || (documentId == null ^ other.documentId == null)) {
            return false;
        }
        return documentId == null || documentId.equals(other.documentId);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
