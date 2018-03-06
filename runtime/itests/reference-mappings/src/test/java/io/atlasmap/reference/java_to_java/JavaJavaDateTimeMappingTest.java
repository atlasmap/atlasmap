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
package io.atlasmap.reference.java_to_java;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.test.DateTimeClass;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;

public class JavaJavaDateTimeMappingTest extends AtlasMappingBaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(JavaJavaDateTimeMappingTest.class);

    private static final JavaField[] DATE_TIME_FIELDS;
    static {
        List<JavaField> list = new ArrayList<>();
        list.add(createJavaField("/calendarField", FieldType.DATE_TIME_TZ, "java.util.Calendar"));
        list.add(createJavaField("/dateField", FieldType.DATE_TIME, "java.util.Date"));
        list.add(createJavaField("/gregorianCalendarField", FieldType.DATE_TIME_TZ, "java.util.GregorianCalendar"));
        list.add(createJavaField("/localDateField", FieldType.DATE, "java.time.LocalDate"));
        list.add(createJavaField("/localDateTimeField", FieldType.DATE_TIME, "java.time.LocalDateTime"));
        list.add(createJavaField("/localTimeField", FieldType.TIME, "java.time.LocalTime"));
        list.add(createJavaField("/sqlDateField", FieldType.DATE, "java.sql.Date"));
        list.add(createJavaField("/sqlTimeField", FieldType.TIME, "java.sql.Time"));
        list.add(createJavaField("/sqlTimestampField", FieldType.DATE_TIME, "java.sql.Timestamp"));
        list.add(createJavaField("/zonedDateTimeField", FieldType.DATE_TIME_TZ, "java.time.ZonedDateTime"));
        DATE_TIME_FIELDS = list.toArray(new JavaField[0]);
    }

    private static final Map<FieldType, FieldType> UNSUPPORTED = new HashMap<>();
    static {
        UNSUPPORTED.put(FieldType.DATE, FieldType.TIME);
        UNSUPPORTED.put(FieldType.TIME, FieldType.DATE);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMapping() throws Exception {
        for (int i=0; i<DATE_TIME_FIELDS.length; i++) {
            LOG.info("#####################");
            LOG.info("# offset: {}", i);
            LOG.info("#####################");
            doTest(i);
        }
    }

    private void doTest(int offset) throws Exception {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        atlasMapping.setName("JavaJavaDateTimeMapping");
        addDataSource(atlasMapping, "atlas:java?className=io.atlasmap.java.test.DateTimeClass",
                DataSourceType.SOURCE);
        addDataSource(atlasMapping, "atlas:java?className=io.atlasmap.java.test.DateTimeClass",
                DataSourceType.TARGET);

        List<BaseMapping> mappings = atlasMapping.getMappings().getMapping();
        for (int i=0; i<DATE_TIME_FIELDS.length; i++) {
            int targetIndex = offset + i < DATE_TIME_FIELDS.length ? offset + i
                    : offset + i - DATE_TIME_FIELDS.length;
            JavaField sourceField = copyJavaField(DATE_TIME_FIELDS[i]);
            JavaField targetField = copyJavaField(DATE_TIME_FIELDS[targetIndex]);
            if (UNSUPPORTED.containsKey(sourceField.getFieldType())
                    && UNSUPPORTED.get(sourceField.getFieldType()).equals(targetField.getFieldType())) {
                sourceField = copyJavaField(targetField);
            }
            Mapping mfm = AtlasModelFactory.createMapping(MappingType.MAP);
            mfm.getInputField().add(sourceField);
            mfm.getOutputField().add(targetField);
            mappings.add(mfm);
        }

        AtlasContext context = ((DefaultAtlasContextFactory)atlasContextFactory)
                .createContext(atlasMapping);
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(createSource());
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof DateTimeClass);
        validateTarget((DateTimeClass) object);
    }

    private DateTimeClass createSource() {
        DateTimeClass sourceClass = new DateTimeClass();
        sourceClass.setCalendarField(Calendar.getInstance());
        sourceClass.setDateField(Date.from(Instant.now()));
        sourceClass.setGregorianCalendarField((GregorianCalendar)GregorianCalendar.getInstance());
        sourceClass.setLocalDateField(LocalDate.now());
        sourceClass.setLocalDateTimeField(LocalDateTime.now());
        sourceClass.setLocalTimeField(LocalTime.now());
        sourceClass.setMonthDayField(MonthDay.now());
        sourceClass.setSqlDateField(new java.sql.Date(Instant.now().toEpochMilli()));
        sourceClass.setSqlTimeField(new java.sql.Time(Instant.now().toEpochMilli()));
        sourceClass.setSqlTimestampField(new java.sql.Timestamp(Instant.now().toEpochMilli()));
        sourceClass.setYearField(Year.now());
        sourceClass.setYearMonthField(YearMonth.now());
        sourceClass.setZonedDateTimeField(ZonedDateTime.now());
        return sourceClass;
    }

    private void addDataSource(AtlasMapping mapping, String uri, DataSourceType type) {
        DataSource ds = new DataSource();
        ds.setUri(uri);
        ds.setDataSourceType(type);
        mapping.getDataSource().add(ds);
    }

    private static JavaField createJavaField(String path, FieldType type, String className) {
        JavaField javaField = AtlasJavaModelFactory.createJavaField();
        javaField.setPath(path);
        javaField.setModifiers(null);
        javaField.setFieldType(type);
        javaField.setClassName(className);
        return javaField;
    }

    private JavaField copyJavaField(JavaField source) {
        JavaField javaField = AtlasJavaModelFactory.createJavaField();
        javaField.setPath(source.getPath());
        javaField.setModifiers(source.getModifiers());
        javaField.setFieldType(source.getFieldType());
        javaField.setClassName(source.getClassName());
        return javaField;
    }

    private void validateTarget(DateTimeClass target) {
        assertNotNull(target.getCalendarField());
        assertNotNull(target.getDateField());
        assertNotNull(target.getGregorianCalendarField());
        assertNotNull(target.getLocalDateField());
        assertNotNull(target.getLocalDateTimeField());
        assertNotNull(target.getLocalTimeField());
        assertNotNull(target.getSqlDateField());
        assertNotNull(target.getSqlTimeField());
        assertNotNull(target.getSqlTimestampField());
        assertNotNull(target.getZonedDateTimeField());
    }
}
