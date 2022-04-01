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
package io.atlasmap.csv.core;

import static io.atlasmap.csv.v2.CsvConstants.OPTION_ALLOW_DUPLICATE_HEADER_NAMES;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_ALLOW_MISSING_COLUMN_NAMES;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_COMMENT_MARKER;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_DELIMITER;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_ESCAPE;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_FIRST_RECORD_AS_HEADER;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_FORMAT;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_HEADERS;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_IGNORE_EMPTY_LINES;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_IGNORE_HEADER_CASE;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_IGNORE_SURROUNDING_SPACES;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_NULL_STRING;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_QUOTE;
import static io.atlasmap.csv.v2.CsvConstants.OPTION_SKIP_HEADER_RECORD;

import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CSV configuration.
 */
public class CsvConfig {
    private static final Logger LOG = LoggerFactory.getLogger(CsvConfig.class);

    private String format;
    private Character delimiter;
    private Boolean firstRecordAsHeader;
    private Boolean skipHeaderRecord;
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

    /**
     * A constructor.
     */
    public CsvConfig() {
        this(null);
    }

    /**
     * A constructor.
     * @param format format
     */
    public CsvConfig(String format) {
        if (format == null) {
            this.format = "Default";
        } else {
            this.format = format;
        }
        this.delimiter = CSVFormat.valueOf(this.format).getDelimiter();
    }

    /**
     * Gets the new config from the specified options.
     * @param config options
     * @return config
     */
    public static CsvConfig newConfig(Map<String, String> config) {
        CsvConfig csvConfig = new CsvConfig(config.get(OPTION_FORMAT));
        String delimiter = config.get(OPTION_DELIMITER);

        for (Map.Entry<String, String> entry: config.entrySet()) {
            switch (entry.getKey()) {
                case OPTION_DELIMITER:
                    csvConfig.delimiter = delimiter.charAt(0);
                    break;
                case OPTION_FIRST_RECORD_AS_HEADER:
                    csvConfig.firstRecordAsHeader = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case OPTION_SKIP_HEADER_RECORD:
                    csvConfig.skipHeaderRecord = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case OPTION_COMMENT_MARKER:
                    csvConfig.commentMarker = entry.getValue().charAt(0);
                    break;
                case OPTION_HEADERS:
                    csvConfig.headers = entry.getValue();
                    break;
                case OPTION_ESCAPE:
                    csvConfig.escape = entry.getValue().charAt(0);
                    break;
                case OPTION_IGNORE_EMPTY_LINES:
                    csvConfig.ignoreEmptyLines = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case OPTION_IGNORE_HEADER_CASE:
                    csvConfig.ignoreHeaderCase = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case OPTION_IGNORE_SURROUNDING_SPACES:
                    csvConfig.ignoreSurroundingSpaces = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case OPTION_NULL_STRING:
                    csvConfig.nullString = entry.getValue();
                    break;
                case OPTION_QUOTE:
                    csvConfig.quote = entry.getValue().charAt(0);
                    break;
                case OPTION_ALLOW_DUPLICATE_HEADER_NAMES:
                    csvConfig.allowDuplicateHeaderNames = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
                case OPTION_ALLOW_MISSING_COLUMN_NAMES:
                    csvConfig.allowMissingColumnNames = entry.getValue() == null || Boolean.valueOf(entry.getValue());
                    break;
            }
        }
        return csvConfig;
    }

    /**
     * Gets the {@link CSVFormat}.
     * @return CSVFormat
     */
    CSVFormat newCsvFormat() {
        CSVFormat csvFormat;
        csvFormat = (format != null) ? CSVFormat.valueOf(format) : CSVFormat.DEFAULT;
        csvFormat = (delimiter != null) ? csvFormat.withDelimiter(delimiter) : csvFormat;
        csvFormat = (Boolean.TRUE.equals(firstRecordAsHeader)) ? csvFormat.withFirstRecordAsHeader() : csvFormat;
        csvFormat = (skipHeaderRecord != null) ? csvFormat.withSkipHeaderRecord(skipHeaderRecord) : csvFormat;
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

    /**
     * Gets the format.
     * @return format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the delimiter.
     * @param delimiter delimiter
     */
    public void setDelimiter(Character delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Gets the delimiter.
     * @return delimiter
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Sets if it uses the first record as a header.
     * @param firstRecordAsHeader true to use the first record as a header, or false
     */
    public void setFirstRecordAsHeader(Boolean firstRecordAsHeader) {
        this.firstRecordAsHeader = firstRecordAsHeader;
    }

    /**
     * Gets if it uses the first record as a header.
     * @return true if the first record as a header, or false
     */
    public boolean isFirstRecordAsHeader() {
        return Boolean.TRUE.equals(firstRecordAsHeader);
    }

    /**
     * Gets if it uses the first record as a header.
     * @return true if the first record as a header, or false
     */
    public Boolean getFirstRecordAsHeader() {
        return firstRecordAsHeader;
    }

    /**
     * Sets if it skips the header record.
     * @param skipHeaderRecord true to skip the header, or false
     */
    public void setSkipHeaderRecord(Boolean skipHeaderRecord) {
        this.skipHeaderRecord = skipHeaderRecord;
    }

    /**
     * Gets if it skips the header record.
     * @return true if it skips the header record, or false
     */
    public Boolean getSkipHeaderRecord() {
        return skipHeaderRecord;
    }

    /**
     * Sets the header.
     * @param headers header
     */
    public void setHeaders(String headers) {
        this.headers = headers;
    }

    /**
     * Gets the header.
     * @return header
     */
    public String getHeaders() {
        return headers;
    }

    /**
     * Gets the parsed header.
     * @return parsed header
     */
    public String[] getParsedHeaders() {
        return headers != null ? headers.split(delimiter.toString()) : null;
    }

    /**
     * Sets the comment marker character.
     * @param commentMarker comment marker character
     */
    public void setCommentMarker(Character commentMarker) {
        this.commentMarker = commentMarker;
    }

    /**
     * Gets the comment marker character.
     * @return comment marker character
     */
    public Character getCommentMarker() {
        return commentMarker;
    }

    /**
     * Gest the escape character.
     * @return escape character
     */
    public Character getEscape() {
        return escape;
    }

    /**
     * Sets the escape character.
     * @param escape escape character
     */
    public void setEscape(Character escape) {
        this.escape = escape;
    }

    /**
     * gets if it ignores the empty lines.
     * @return true if it ignores the empty lines, or false
     */
    public Boolean getIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    /**
     * Sets if it ignores the empty lines.
     * @param ignoreEmptyLines true to ignore the empty lines, or false
     */
    public void setIgnoreEmptyLines(Boolean ignoreEmptyLines) {
        this.ignoreEmptyLines = ignoreEmptyLines;
    }

    /**
     * Gets if it ignores the header case.
     * @return true if it ignores the header case, or false
     */
    public Boolean getIgnoreHeaderCase() {
        return ignoreHeaderCase;
    }

    /**
     * Sets if it ignores the header case.
     * @param ignoreHeaderCase true to ignore header case, or false
     */
    public void setIgnoreHeaderCase(Boolean ignoreHeaderCase) {
        this.ignoreHeaderCase = ignoreHeaderCase;
    }

    /**
     * Gets if it ignores the surrouunding spaces.
     * @return true if it ignores the surrounding spaces, or false
     */
    public Boolean getIgnoreSurroundingSpaces() {
        return ignoreSurroundingSpaces;
    }

    /**
     * Sets if it ignores the surrounding spaces.
     * @param ignoreSurroundingSpaces true to ignore the surrounding spaces, or false
     */
    public void setIgnoreSurroundingSpaces(Boolean ignoreSurroundingSpaces) {
        this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
    }

    /**
     * Gets the null string.
     * @return null string
     */
    public String getNullString() {
        return nullString;
    }

    /**
     * Sets the null string.
     * @param nullString null string
     */
    public void setNullString(String nullString) {
        this.nullString = nullString;
    }

    /**
     * Gets the quote character.
     * @return quote character
     */
    public Character getQuote() {
        return quote;
    }

    /**
     * Sets the quote character.
     * @param quote quote character
     */
    public void setQuote(Character quote) {
        this.quote = quote;
    }

    /**
     * Gets if it allows the duplicate header names.
     * @return true if it allows duplicate the header names, or false
     */
    public Boolean getAllowDuplicateHeaderNames() {
        return allowDuplicateHeaderNames;
    }

    /**
     * Sets if it allows the duplicate header names.
     * @param allowDuplicateHeaderNames true to allow the duplicate header names, or false
     */
    public void setAllowDuplicateHeaderNames(Boolean allowDuplicateHeaderNames) {
        this.allowDuplicateHeaderNames = allowDuplicateHeaderNames;
    }

    /**
     * Gets if it allows the missing column names.
     * @return true if it allows the missing column names, or false
     */
    public Boolean getAllowMissingColumnNames() {
        return allowMissingColumnNames;
    }

    /**
     * Sets if it allows the missing column names.
     * @param allowMissingColumnNames true to allow the missing column names, or false
     */
    public void setAllowMissingColumnNames(Boolean allowMissingColumnNames) {
        this.allowMissingColumnNames = allowMissingColumnNames;
    }
}
