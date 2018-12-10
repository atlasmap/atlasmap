package io.atlasmap.java.inspect;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.v2.CollectionType;

public class ClassInspectionServiceITest {

    private ClassInspectionService classInspectionService = null;

    @Before
    public void setUp() {
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() {
        classInspectionService = null;
    }

    @Test
    public void testInspectClassClassNameClassPath() throws InspectionException {
        JavaClass javaClazz = classInspectionService.inspectClass(
                "io.atlasmap.java.test.v2.FlatPrimitiveClass",
                CollectionType.NONE,
                "target/reference-jars/atlas-java-test-model-1.16.0-SNAPSHOT.jar");
        assertNotNull(javaClazz);
    }

    @Test
    public void testInspectClassClassNameClassPath45() throws InspectionException {
        JavaClass javaClazz = classInspectionService.inspectClass(
                "io.syndesis.connector.salesforce.Contact",
                CollectionType.NONE,
                "target/reference-jars/salesforce-upsert-contact-connector-0.4.5.jar:target/reference-jars/camel-salesforce-2.19.0.jar");
        assertNotNull(javaClazz);
    }
}
