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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import io.atlasmap.api.AtlasException;
import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Document;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Fields;

public class CsvFieldWriter implements AtlasFieldWriter {

    private final CsvConfig csvConfig;
    private Document document;

    public CsvFieldWriter(CsvConfig csvConfig) {
        this.csvConfig = csvConfig;
        document = new Document();
        document.setFields(new Fields());
    }

    public Document getDocument() {
        return document;
    }

    /**
     * Write is not performed until after the whole target document is ready and toCsv is called.
     *
     * @param session
     * @throws AtlasException
     */
    @Override
    public void write(AtlasInternalSession session) throws AtlasException {
        Field targetField = session.head().getTargetField();
        Field sourceField = session.head().getSourceField();
        if (sourceField instanceof FieldGroup) {
            FieldGroup targetFieldGroup = AtlasModelFactory.createFieldGroupFrom(targetField, true);

            for (Field sourceSubField: ((FieldGroup) sourceField).getField()) {
                CsvField targetCsvField = (CsvField) targetField;
                CsvField targetCsvSubField = new CsvField();
                AtlasModelFactory.copyField(targetField, targetCsvSubField, false);
                targetCsvSubField.setColumn(targetCsvField.getColumn());
                targetCsvSubField.setValue(sourceSubField.getValue());
                targetFieldGroup.getField().add(targetCsvSubField);
            }

            targetField = targetFieldGroup;
            session.head().setTargetField(targetFieldGroup);
        } else {
            targetField.setValue(sourceField.getValue());
        }

        document.getFields().getField().add(targetField);
    }

    public String toCsv() throws AtlasException {
        CSVFormat csvFormat = csvConfig.newCsvFormat();

        String[] headers = csvConfig.getParsedHeaders();
        boolean ignoreHeaderCase = Boolean.TRUE.equals(csvConfig.getIgnoreHeaderCase());
        if (headers != null && ignoreHeaderCase) {
            for (int j = 0; j < headers.length; j++) {
                headers[j] = headers[j].toLowerCase();
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            CSVPrinter printer = new CSVPrinter(writer, csvFormat);

            List<Field> fields = document.getFields().getField();

            if (fields.isEmpty()) {
                return "";
            }

            if (!Boolean.TRUE.equals(csvConfig.getSkipHeaderRecord())) {
                if (csvConfig.getHeaders() == null) {
                    String[] headerRecords = new String[fields.size()];

                    int i = 0;
                    for (Field field : fields) {
                        CsvField csvField;
                        if (field instanceof FieldGroup) {
                            FieldGroup fieldGroup = (FieldGroup) field;
                            csvField = (CsvField) fieldGroup.getField().get(i);
                        } else {
                            csvField = (CsvField) field;
                        }

                        if (csvField.getColumn() != null) {
                            headerRecords[csvField.getColumn()] = csvField.getName();
                        } else {
                            headerRecords[i] = csvField.getName();
                        }
                        i++;
                    }
                    printer.printRecord(headerRecords);
                }
            }

            int recordsCount;
            if (fields.get(0) instanceof FieldGroup) {
                recordsCount = ((FieldGroup) fields.get(0)).getField().size();
            } else {
                recordsCount = 1;
            }

            for (int i = 0; i < recordsCount; i++) {
                List<String> values = new ArrayList<>();
                for (Field field: fields) {
                    CsvField csvField;
                    if (field instanceof FieldGroup) {
                        FieldGroup fieldGroup = (FieldGroup) field;
                        csvField = (CsvField) fieldGroup.getField().get(i);
                    } else {
                        csvField = (CsvField) field;
                    }

                    if (csvField.getColumn() != null) {
                        //Add missing values
                        for (int j = values.size(); j < csvField.getColumn() + 1; j++) {
                            values.add(null);
                        }
                        values.set(csvField.getColumn(), csvField.getValue().toString());
                    } else if (headers != null) {
                        for (int j = values.size(); j < headers.length; j++) {
                            values.add(null);
                        }

                        int column = findColumn(headers, ignoreHeaderCase, csvField);
                        if (column != -1) {
                            values.set(column, csvField.getValue().toString());
                        }
                    } else {
                        values.add(csvField.getValue().toString());
                    }
                }
                printer.printRecord(values);
            }

            writer.flush();
            String csv = out.toString();
            return csv;
        } catch (IOException e) {
            throw new AtlasException(e);
        }
    }

    private int findColumn(String[] headers, boolean ignoreHeaderCase, CsvField csvField) {
        String columnName = csvField.getName();
        if (ignoreHeaderCase) {
            columnName = csvField.getName().toLowerCase();
        }
        int column = 0;
        for (column = 0; column < headers.length; column++) {
            if (headers[column].equals(columnName)) {
                return column;
            }
        }
        return -1;
    }
}
