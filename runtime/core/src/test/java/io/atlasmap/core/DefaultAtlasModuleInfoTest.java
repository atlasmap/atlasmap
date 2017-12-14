package io.atlasmap.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.atlasmap.spi.AtlasModule;

public class DefaultAtlasModuleInfoTest {

    private static DefaultAtlasModuleInfo module = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        List<String> formats = new ArrayList<>();
        formats.add("java");
        List<String> packageNames = new ArrayList<>();
        packageNames.add("io.atlasmap.core");

        module = new DefaultAtlasModuleInfo("name", "atlas:java", AtlasModule.class, null, formats, packageNames);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        module = null;
    }

    @Test
    public void testGetModuleClass() {
        assertNotNull(module.getModuleClass());
    }

    @Test
    public void testGetConstructor() {
        assertNull(module.getConstructor());
    }

    @Test
    public void testGetFormats() {
        assertNotNull(module.getFormats());
    }

    @Test
    public void testGetDataFormats() {
        assertNotNull(module.getDataFormats());

        List<String> packageNames = new ArrayList<>();
        packageNames.add("io.atlasmap.core");

        DefaultAtlasModuleInfo moduleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", AtlasModule.class, null,
                null, packageNames);
        assertNotNull(moduleInfo.getDataFormats());
    }

    @Test
    public void testGetModuleClassName() {
        assertNotNull(module.getModuleClassName());

        List<String> formats = new ArrayList<>();
        formats.add("java");
        List<String> packageNames = new ArrayList<>();
        packageNames.add("io.atlasmap.core");

        DefaultAtlasModuleInfo moduleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", null, null, formats,
                packageNames);
        assertNull(moduleInfo.getModuleClassName());
    }

    @Test
    public void testGetPackageNames() {
        assertNotNull(module.getPackageNames());

        List<String> formats = new ArrayList<>();
        formats.add("java");
        List<String> packageNames = new ArrayList<>();

        DefaultAtlasModuleInfo moduleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", null, null, formats,
                packageNames);
        assertNotNull(moduleInfo.getPackageNames());

        moduleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", null, null, formats, null);
        assertNotNull(moduleInfo.getPackageNames());
    }

    @Test
    public void testGetName() {
        assertNotNull(module.getName());
    }

    @Test
    public void testGetUri() {
        assertNotNull(module.getUri());
    }

    @Test
    public void testIsSourceSupported() {
        assertNull(module.isSourceSupported());
    }

    @Test
    public void testIsTargetSupported() {
        assertNull(module.isTargetSupported());
    }

    @Test
    public void testGetClassName() {
        assertNotNull(module.getClassName());
    }

    @Test
    public void testGetVersion() {
        assertNull(module.getVersion());
    }

    @Test
    public void testToString() {
        assertNotNull(module.toString());
    }

}
