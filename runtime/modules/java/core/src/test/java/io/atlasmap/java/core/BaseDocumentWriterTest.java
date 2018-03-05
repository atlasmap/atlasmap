package io.atlasmap.java.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import io.atlasmap.api.AtlasConversionService;
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
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupTable;

public abstract class BaseDocumentWriterTest {
    protected static final String DEFAULT_VALUE = "Some string.";

    protected DocumentJavaFieldWriter writer = null;
    protected List<SegmentContext> segmentContexts = new LinkedList<>();
    protected SegmentContext lastSegmentContext = null;
    protected JavaField field = null;
    protected TargetTestClass targetTestClassInstance = null;
    protected TestListOrders targetOrderListInstance = null;
    protected TargetOrderArray targetOrderArrayInstance = null;
    protected TargetValueConverter valueConverter = null;
    protected AtlasConversionService conversionService = DefaultAtlasConversionService.getInstance();
    protected ClassLoader classLoader;

    @Before
    public void reset() {
        classLoader = Thread.currentThread().getContextClassLoader();
        writer = new DocumentJavaFieldWriter(conversionService);
        writer.setTargetValueConverter(new TargetValueConverter(classLoader, conversionService) {
            public Object convert(AtlasInternalSession session, LookupTable lookupTable, Field sourceField, Object parentObject, Field targetField) throws AtlasException {
                return targetField.getValue();
            }
        });
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
        this.segmentContexts = new AtlasPath(fieldPath).getSegmentContexts(true);
        for (SegmentContext ctx : this.segmentContexts) {
            addClassForFieldPath(ctx.getSegmentPath(), String.class);
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
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getTargetField()).thenReturn(field);
        writer.write(session);
    }

    protected void addClassForFieldPath(String fieldPath, Class<?> clz) {
        String fieldPathTrimmed = AtlasPath.removeCollectionIndexes(fieldPath);
        @SuppressWarnings("unchecked")
        Map<String, Class<?>> classesForFields = (Map<String, Class<?>>) getInternalState(writer, "classesForFields");
        classesForFields.put(fieldPathTrimmed, clz);
        setInternalState(writer, "classesForFields", classesForFields);
    }

    private Object getInternalState(Object target, String field) {
        Class<?> c = target.getClass();
        try {
            java.lang.reflect.Field f = getFieldFromHierarchy(c, field);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to get internal state on a private field.", e);
        }
    }

    private void setInternalState(Object target, String field, Object value) {
        Class<?> c = target.getClass();
        try {
            java.lang.reflect.Field f = getFieldFromHierarchy(c, field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to set internal state on a private field.", e);
        }
    }

    private java.lang.reflect.Field getFieldFromHierarchy(Class<?> clazz, String field) {
        java.lang.reflect.Field f = getField(clazz, field);
        while (f == null && clazz != Object.class) {
            Class<?> superClazz = clazz.getSuperclass();
            f = getField(superClazz, field);
        }
        if (f == null) {
            throw new IllegalArgumentException("This field: '" + field + "' on this class: '" + clazz.getSimpleName() + "' is not declared within hierarchy of this class!");
        }
        return f;
    }

    private java.lang.reflect.Field getField(Class<?> clazz, String field) {
        try {
            return clazz.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private void setTargetValue(Object targetValue) {
        writer.setTargetValueConverter(new TargetValueConverter(classLoader, conversionService) {
            @Override
            public Object convert(AtlasInternalSession session, LookupTable lookupTable, Field sourceField, Object parentObject, Field targetField) throws AtlasException {
                return targetValue;
            }
        });
    }

    protected void write(String path, String targetValue) throws AtlasException {
        Field field = createField(path, targetValue);
        setTargetValue(targetValue);
        write(field);
    }

    protected void write(String path, int targetValue) throws AtlasException {
        Field field = createIntField(path, targetValue);
        setTargetValue(targetValue);
        write(field);
    }

    protected void write(String path, StateEnumClassLong targetValue) throws AtlasException {
        Field field = createEnumField(path, targetValue);
        setTargetValue(targetValue);
        write(field);
    }

    protected Object findChildObject(Field field, SegmentContext segmentContext, Object parentObject) throws Exception {
        Class<DocumentJavaFieldWriter> clazz = DocumentJavaFieldWriter.class;
        Method method = clazz.getDeclaredMethod("findChildObject", Field.class, SegmentContext.class, Object.class);
        boolean accessible = method.isAccessible();
        if (!accessible) {
            method.setAccessible(true);
        }
        return method.invoke(writer, field, segmentContext, parentObject);
    }
}
