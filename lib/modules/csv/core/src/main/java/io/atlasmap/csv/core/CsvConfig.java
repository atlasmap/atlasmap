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
package io.atlasmap.csv.core;

import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class CsvConfig {
    private static final Logger LOG = LoggerFactory.getLogger(CsvConfig.class);

    private String format;
    private Character delimiter;
    private boolean firstRecordAsHeader;
    private String headers;
    private Character commentMarker;
    private Character escape;
    private Boolean ignoreEmptyLines;
    private Boolean ignoreHeaderCase;
    private Boolean ignoreSurroundingSpaces;
    private String nullString;
    private Character quote;
    private Boolean allowDuplicateHeaderNames;
    private Boolean allowMissingColumnNames;

    public CsvConfig() {
        this(null);
    }

    public CsvConfig(String format) {
        if (format == null) {
            this.format = "Default";
        } else {
            this.format = format;
        }
        this.delimiter = CSVFormat.valueOf(this.format).getDelimiter();
    }

    public static CsvConfig newConfig(Map<String, String> config) {
        CsvConfig csvConfig = new CsvConfig(config.get("format"));
        String delimiter = config.get("delimiter");

        for (Map.Entry<String, String> entry: config.entrySet()) {
            switch (entry.getKey()) {
                case "delimiter":
                    csvConfig.delimiter = delimiter.charAt(0);
                    break;
                case "firstRecordAsHeader":
                    csvConfig.firstRecordAsHeader = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case "commentMarker":
                    csvConfig.commentMarker = entry.getValue().charAt(0);
                    break;
                case "headers":
                    csvConfig.headers = entry.getValue();
                    break;
                case "escape":
                    csvConfig.escape = entry.getValue().charAt(0);
                    break;
                case "ignoreEmptyLines":
                    csvConfig.ignoreEmptyLines = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case "ignoreHeaderCase":
                    csvConfig.ignoreHeaderCase = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case "ignoreSurroundingSpaces":
                    csvConfig.ignoreSurroundingSpaces = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case "nullString":
                    csvConfig.nullString = entry.getValue();
                    break;
                case "quote":
                    csvConfig.quote = entry.getValue().charAt(0);
                    break;
                case "allowDuplicateHeaderNames":
                    csvConfig.allowDuplicateHeaderNames = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case "allowMissingColumnNames":
                    csvConfig.allowMissingColumnNames = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
            }
        }
        return csvConfig;
    }

    CSVFormat newCsvFormat() {
        CSVFormat csvFormat;
        csvFormat = (format != null) ? CSVFormat.valueOf(format) : CSVFormat.DEFAULT;
        csvFormat = (delimiter != null) ? csvFormat.withDelimiter(delimiter) : csvFormat;
        csvFormat = firstRecordAsHeader ? csvFormat.withFirstRecordAsHeader() : csvFormat;
        csvFormat = (headers != null) ? csvFormat.withHeader(getParsedHeaders()) : csvFormat;
        csvFormat = (commentMarker != null) ? csvFormat.withCommentMarker(commentMarker): csvFormat;
        csvFormat = (escape != null) ? csvFormat.withEscape(escape): csvFormat;
        csvFormat = (ignoreEmptyLines != null) ? csvFormat.withIgnoreEmptyLines(ignoreEmptyLines): csvFormat;
        csvFormat = (ignoreHeaderCase != null) ? csvFormat.withIgnoreHeaderCase(ignoreHeaderCase): csvFormat;
        csvFormat = (ignoreSurroundingSpaces != null) ? csvFormat.withIgnoreSurroundingSpaces(ignoreSurroundingSpaces): csvFormat;
        csvFormat = (nullString != null) ? csvFormat.withNullString(nullString): csvFormat;
        csvFormat = (quote != null) ? csvFormat.withQuote(quote): csvFormat;
        csvFormat = (allowDuplicateHeaderNames != null) ? csvFormat.withAllowDuplicateHeaderNames(allowDuplicateHeaderNames): csvFormat;
        csvFormat = (allowMissingColumnNames != null) ? csvFormat.withAllowMissingColumnNames(allowMissingColumnNames): csvFormat;
        return csvFormat;
    }

    public String getFormat() {
        return format;
    }

    public void setDelimiter(Character delimiter) {
        this.delimiter = delimiter;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setFirstRecordAsHeader(boolean firstRecordAsHeader) {
        this.firstRecordAsHeader = firstRecordAsHeader;
    }

    public boolean isFirstRecordAsHeader() {
        return firstRecordAsHeader;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getHeaders() {
        return headers;
    }

    public String[] getParsedHeaders() {
        return headers.split(delimiter.toString());
    }

    public void setCommentMarker(Character commentMarker) {
        this.commentMarker = commentMarker;
    }

    public Character getCommentMarker() {
        return commentMarker;
    }

    public Character getEscape() {
        return escape;
    }

    public void setEscape(Character escape) {
        this.escape = escape;
    }

    public Boolean getIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    public void setIgnoreEmptyLines(Boolean ignoreEmptyLines) {
        this.ignoreEmptyLines = ignoreEmptyLines;
    }

    public Boolean getIgnoreHeaderCase() {
        return ignoreHeaderCase;
    }

    public void setIgnoreHeaderCase(Boolean ignoreHeaderCase) {
        this.ignoreHeaderCase = ignoreHeaderCase;
    }

    public Boolean getIgnoreSurroundingSpaces() {
        return ignoreSurroundingSpaces;
    }

    public void setIgnoreSurroundingSpaces(Boolean ignoreSurroundingSpaces) {
        this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
    }

    public String getNullString() {
        return nullString;
    }

    public void setNullString(String nullString) {
        this.nullString = nullString;
    }

    public Character getQuote() {
        return quote;
    }

    public void setQuote(Character quote) {
        this.quote = quote;
    }

    public Boolean getAllowDuplicateHeaderNames() {
        return allowDuplicateHeaderNames;
    }

    public void setAllowDuplicateHeaderNames(Boolean allowDuplicateHeaderNames) {
        this.allowDuplicateHeaderNames = allowDuplicateHeaderNames;
    }

    public Boolean getAllowMissingColumnNames() {
        return allowMissingColumnNames;
    }

    public void setAllowMissingColumnNames(Boolean allowMissingColumnNames) {
        this.allowMissingColumnNames = allowMissingColumnNames;
    }
}
