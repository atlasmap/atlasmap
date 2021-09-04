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
package io.atlasmap.java.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.StateEnumClassLong;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetOrderArray;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.java.test.TestListOrders;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

public abstract class BaseJavaFieldWriterTest {
    protected static final String DEFAULT_VALUE = "Some string.";

    protected JavaFieldWriterUtil writerUtil = null;
    protected JavaFieldWriter writer = null;
    protected List<SegmentContext> segmentContexts = new LinkedList<>();
    protected SegmentContext lastSegmentContext = null;
    protected JavaField field = null;
    protected TargetTestClass targetTestClassInstance = null;
    protected TestListOrders targetOrderListInstance = null;
    protected TargetOrderArray targetOrderArrayInstance = null;
    protected TargetValueConverter valueConverter = null;
    protected AtlasConversionService conversionService = DefaultAtlasConversionService.getInstance();
    protected ClassLoader classLoader;
    protected Audits audits;

    @BeforeEach
    public void reset() {
        classLoader = Thread.currentThread().getContextClassLoader();
        writerUtil = new JavaFieldWriterUtil(conversionService);
        writer = new JavaFieldWriter(writerUtil);
        writer.setTargetValueConverter(new TargetValueConverter(classLoader, conversionService, writerUtil));
        field = null;
        segmentContexts = new LinkedList<>();

        targetTestClassInstance = new TargetTestClass();
        targetTestClassInstance.setContact(new TargetContact());
        targetTestClassInstance.setAddress(new TargetAddress());

        targetOrderListInstance = new TestListOrders();
        targetOrderListInstance.setOrders(new LinkedList<>());
        targetOrderListInstance.getOrders().add(new TargetOrder());
        targetOrderListInstance.getOrders().add(new TargetOrder());
        targetTestClassInstance.setListOrders(targetOrderListInstance);

        targetOrderArrayInstance = new TargetOrderArray();
        targetOrderArrayInstance.setOrders(new BaseOrder[2]);
        targetOrderArrayInstance.getOrders()[0] = new TargetOrder();
        targetOrderArrayInstance.getOrders()[1] = new TargetOrder();
        targetTestClassInstance.setOrderArray(targetOrderArrayInstance);
        audits = new Audits();
    }

    public void setupPath(String fieldPath) {
        AtlasPath path = new AtlasPath(fieldPath);
        this.segmentContexts = path.getSegments(true);
        this.lastSegmentContext = segmentContexts.get(segmentContexts.size() - 1);
        this.field = createField(fieldPath, DEFAULT_VALUE);
    }

    public JavaField createField(String path, Object value, FieldType fieldType, String className) {
        JavaField field = new JavaField();
        field.setFieldType(fieldType);
        field.setClassName(className);
        field.setValue(value);
        field.setPath(path);
        return field;
    }

    public JavaField createField(String path, Object value, FieldType fieldType) {
        return createField(path, value, fieldType, null);
    }

    public JavaField createField(String path, String value) {
        return createField(path, value, FieldType.STRING);
    }

    public JavaEnumField createEnumField(String path, Enum<?> value) {
        JavaEnumField f = new JavaEnumField();
        f.setPath(path);
        f.setName(null == value ? null : value.name());
        f.setOrdinal(null == value ? null : value.ordinal());
        f.setFieldType(FieldType.COMPLEX);
        f.setValue(value);
        return f;
    }

    public JavaField createIntField(String path, int value) {
        return createField(path, value, FieldType.INTEGER);
    }

    public JavaField createLongField(String path, long value) {
        return createField(path, value, FieldType.LONG);
    }

    protected void write(Field field) throws AtlasException {
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.getAudits()).thenReturn(audits);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getTargetField()).thenReturn(field);
        when(session.head().getSourceField()).thenReturn(field);
        writer.write(session);
    }

    protected void write(String path, String targetValue) throws AtlasException {
        Field field = createField(path, targetValue);
        write(field);
    }

    protected void write(String path, int targetValue) throws AtlasException {
        Field field = createIntField(path, targetValue);
        write(field);
    }

    protected void write(String path, StateEnumClassLong targetValue) throws AtlasException {
        Field field = createEnumField(path, targetValue);
        write(field);
    }

    protected void writeComplex(String path, Object targetValue) throws AtlasException {
        Field field = createField(path, targetValue, FieldType.COMPLEX);
        write(field);
    }
}
