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

@JsonRootName("CsvInspectionResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class CsvInspectionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Document csvDocument;
    private String errorMessage;
    private long executionTime;

    public Document getCsvDocument() {
        return csvDocument;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setCsvDocument(Document csvDocument) {
        this.csvDocument = csvDocument;
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}
