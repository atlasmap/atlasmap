package io.atlasmap.java.module;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.PathUtil;
import io.atlasmap.core.PathUtil.SegmentContext;
import io.atlasmap.java.module.DocumentJavaFieldWriter.JavaFieldWriterValueConverter;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetOrderArray;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.java.test.TestListOrders;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

public abstract class BaseDocumentWriterTest {
    protected static final String DEFAULT_VALUE = "Some string.";

    protected DocumentJavaFieldWriter writer = null;
    protected List<SegmentContext> segmentContexts = new LinkedList<>();
    protected SegmentContext lastSegmentContext = null;
    protected JavaField field = null;
    protected TargetTestClass targetTestClassInstance = null;
    protected TestListOrders targetOrderListInstance = null;
    protected TargetOrderArray targetOrderArrayInstance = null;

    protected JavaFieldWriterValueConverter valueConverter = new JavaFieldWriterValueConverter() {
        @Override
        public Object convertValue(Object parentObject, Field outputField) throws AtlasException {
            return outputField.getValue();
        }
    };

    @Before
    public void reset() {
        writer = new DocumentJavaFieldWriter();
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
    }

    public void setupPath(String fieldPath) {
        this.segmentContexts = new PathUtil(fieldPath).getSegmentContexts(true);
        for (SegmentContext ctx : this.segmentContexts) {
            writer.addClassForFieldPath(ctx.getSegmentPath(), String.class);
        }
        this.lastSegmentContext = segmentContexts.get(segmentContexts.size() - 1);
        this.field = createField(fieldPath, DEFAULT_VALUE);
    }

    public JavaField createField(String path, Object value, FieldType fieldType) {
        JavaField field = new JavaField();
        field.setFieldType(fieldType);
        field.setValue(value);
        field.setPath(path);
        return field;
    }

    public JavaField createField(String path, String value) {
        return createField(path, value, FieldType.STRING);
    }

    public JavaEnumField createEnumField(String path, Enum<?> value) {
        JavaEnumField f = new JavaEnumField();
        f.setPath(path);
        f.setName(value.name());
        f.setOrdinal(value.ordinal());
        f.setFieldType(FieldType.NONE);
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
        writer.write(field, valueConverter);
    }
}
