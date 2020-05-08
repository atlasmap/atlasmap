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

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.csv.v2.CsvComplexType;
import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.csv.v2.CsvFields;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Document;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import sun.misc.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * It accepts InputStream as a document in order to process big files efficiently.
 * It uses the mark operation of the InputStream to reset the stream and read consecutive fields.
 * If InputStream does not support the mark operation it is wrapped in BufferedInputStream.
 */
public class CsvFieldReader implements AtlasFieldReader {

    private final CsvConfig csvConfig;
    private InputStream document;

    public CsvFieldReader(CsvConfig csvConfig) {
        this.csvConfig = csvConfig;
    }

    public void setDocument(InputStream inputStream) {
        if (inputStream != null && !inputStream.markSupported()) {
            this.document = new BufferedInputStream(inputStream);
        } else {
            this.document = inputStream;
        }
    }

    @Override
    public Field read(AtlasInternalSession session) throws AtlasException {
        Field field = session.head().getSourceField();

        if (document == null) {
            AtlasUtil.addAudit(session, field.getDocId(),
                String.format("Cannot read field '%s' of document '%s', document is null",
                    field.getPath(), field.getDocId()),
                field.getPath(), AuditStatus.ERROR, null);
            return field;
        }
        if (field == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'field' cannot be null"));
        }
        if (!(field instanceof CsvField) && !(field instanceof FieldGroup)) {
            throw new AtlasException(String.format("Unsupported field type '%s'", field.getClass()));
        }

        if (field instanceof FieldGroup) {
            //complex field
            FieldGroup fieldGroup = (FieldGroup) field;
            List<Field> fields = fieldGroup.getField();

            FieldGroup readFieldGroup = AtlasModelFactory.copyFieldGroup(fieldGroup);

            for (Field subField: fields) {
                if (subField instanceof FieldGroup) {
                    //support only one level grouping
                    subField = ((FieldGroup) subField).getField().get(0);
                }

                if (subField instanceof CsvField) {
                    Field readSubField = readFields((CsvField) subField);
                    readFieldGroup.getField().add(readSubField);
                }
            }

            session.head().setSourceField(readFieldGroup);
            return readFieldGroup;
        } else {
            Field readField = readFields((CsvField) field);

            session.head().setSourceField(readField);
            return readField;
        }
    }

    private Field readFields(CsvField field) throws AtlasException {
        List<Field> fields = new ArrayList<>();
        CsvField csvField = field;
        CSVFormat csvFormat = csvConfig.newCsvFormat();
        try {
            document.mark(Integer.MAX_VALUE);

            CSVParser parser = csvFormat.parse(new InputStreamReader(document));

            AtlasPath atlasPath = new AtlasPath(csvField.getPath());
            int i = 0;
            Integer fieldIndex = atlasPath.getRootSegment().getCollectionIndex();
            if (fieldIndex != null) {
                for (CSVRecord record: parser) {
                    if (i == fieldIndex) {
                        CsvField newField = CsvField.cloneOf(csvField);
                        String value;
                        if (csvField.getColumn() != null) {
                            value = record.get(csvField.getColumn());
                        } else {
                            value = record.get(csvField.getName());
                        }
                        newField.setValue(value);
                        fields.add(newField);
                        break;
                    }
                    i++;
                }
            } else {
                for (CSVRecord record: parser) {
                    CsvField collectionField = CsvField.cloneOf(csvField);
                    String value;
                    if (csvField.getColumn() != null) {
                        value = record.get(csvField.getColumn());
                    } else {
                        value = record.get(csvField.getName());
                    }
                    collectionField.setValue(value);
                    AtlasPath collectionFieldPath = new AtlasPath(collectionField.getPath());
                    collectionFieldPath.setCollectionIndex(0, i);
                    collectionField.setPath(collectionFieldPath.toString());
                    fields.add(collectionField);
                    i++;
                }
            }

            document.reset();
        } catch (IOException e) {
            throw new AtlasException(e);
        }

        if (fields.size() == 1) {
            return fields.get(0);
        } else {
            FieldGroup fieldGroup = AtlasModelFactory.createFieldGroupFrom(field, true);
            fieldGroup.getField().addAll(fields);
            return fieldGroup;
        }

    }

    /**
     * Reads only the first row of the document.
     *
     * If firstRecordAsHeader is set to true it uses column names for field names, otherwise it uses an index
     * starting from 0.
     *
     * @return
     * @throws AtlasException
     */
    public Document readSchema() throws AtlasException {
        CSVFormat csvFormat = csvConfig.newCsvFormat();
        CSVParser parser;
        try {
            document.mark(Integer.MAX_VALUE);
            parser = csvFormat.parse(new InputStreamReader(document));

        } catch (IOException e) {
            throw new AtlasException(e);
        }

        List<CsvField> fields = new ArrayList<>();

        if (csvConfig.isFirstRecordAsHeader()) {
            int i = 0;
            for (String headerName : parser.getHeaderNames()) {
                CsvField field = new CsvField();
                field.setColumn(i);
                field.setName(headerName);
                field.setPath("/<>/" + headerName);
                field.setFieldType(FieldType.STRING);
                fields.add(field);
                i++;
            }
        } else {
            CSVRecord record = parser.iterator().next();
            for (int i = 0; i < record.size(); i++) {
                CsvField field = new CsvField();
                field.setColumn(i);
                field.setName(String.valueOf(i));
                field.setPath("/<>/" + field.getName());
                field.setFieldType(FieldType.STRING);
                fields.add(field);
            }
        }

        try {
            document.reset();
        } catch (IOException e) {
            throw new AtlasException(e);
        }

        CsvFields csvFields = new CsvFields();
        csvFields.getCsvField().addAll(fields);

        CsvComplexType csvComplexType = new CsvComplexType();
        csvComplexType.setFieldType(FieldType.COMPLEX);
        csvComplexType.setCollectionType(CollectionType.LIST);
        csvComplexType.setPath("/<>");
        csvComplexType.setName("");
        csvComplexType.setCsvFields(csvFields);

        Fields documentFields = new Fields();
        documentFields.getField().add(csvComplexType);

        Document document = new Document();
        document.setFields(documentFields);
        return document;
    }
}
