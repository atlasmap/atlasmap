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
package io.atlasmap.csv.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.v2.Document;

/**
 * The top container object of XML Document inspection response that AtlasMap design time backend
 * service sends back to the UI in return for CSV inspection request.
 */
@JsonRootName("CsvInspectionResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class CsvInspectionResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    /** CSV Document. */
    private Document csvDocument;
    /** Error message. */
    private String errorMessage;
    /** Execution time. */
    private long executionTime;

    /**
     * Gets the CSV Document.
     * @return CSV document
     */
    public Document getCsvDocument() {
        return csvDocument;
    }

    /**
     * Gets the error message.
     * @return error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the execution time.
     * @return execution time.
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Sets the CSV Document.
     * @param csvDocument CSV Document.
     */
    public void setCsvDocument(Document csvDocument) {
        this.csvDocument = csvDocument;
    }

    /**
     * Sets the error message.
     * @param message message
     */
    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }

    /**
     * Sets the execution time.
     * @param executionTime execution time
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}
