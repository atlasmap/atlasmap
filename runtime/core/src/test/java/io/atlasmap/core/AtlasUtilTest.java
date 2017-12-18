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
package io.atlasmap.core;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.Test;

import io.atlasmap.v2.ValidationStatus;

public class AtlasUtilTest {

    @Test
    public void testIsEmpty() {
        assertTrue(AtlasUtil.isEmpty(null));
        assertTrue(AtlasUtil.isEmpty(""));
        assertTrue(AtlasUtil.isEmpty("     "));
        assertTrue(AtlasUtil.isEmpty("\n\n"));
        assertTrue(AtlasUtil.isEmpty("\t\t"));
        assertTrue(AtlasUtil.isEmpty("\r\n"));
        assertTrue(AtlasUtil.isEmpty("\f\t\n\r"));
        // Note: We can live with 'backspace' not being 'empty'
        assertFalse(AtlasUtil.isEmpty("\b"));
    }

    @Test
    public void testAtlasUri() {
        String uriJavaNover = "atlas:java";
        String uriJavaNoverWParm = "atlas:java?foo=bar";
        String uriJavaNoverWParms = "atlas:java?foo=bar&bar=blah";

        String uriJavaVer1 = "atlas:java::1";
        String uriJavaVer2 = "atlas:java::2";
        String uriJavaVer2WParm = "atlas:java::2?foo=bar";
        String uriJavaVer2WParams = "atlas:java::2?foo=bar&bar=blah";

        List<String> javaUris = Arrays.asList(uriJavaNover, uriJavaNoverWParm, uriJavaNoverWParms, uriJavaVer1, uriJavaVer2, uriJavaVer2WParm, uriJavaVer2WParams);
        List<String> noverUris = Arrays.asList(uriJavaNover, uriJavaNoverWParm, uriJavaNoverWParms);
        List<String> javaVer1Uris = Arrays.asList(uriJavaVer1);
        List<String> javaVer2Uris = Arrays.asList(uriJavaVer2, uriJavaVer2WParm, uriJavaVer2WParams);
        List<String> parmUris = Arrays.asList(uriJavaNoverWParm, uriJavaVer2WParm);
        List<String> parmsUris = Arrays.asList(uriJavaNoverWParms, uriJavaVer2WParams);

        for (String uri : javaUris) {
            assertEquals("atlas", AtlasUtil.getUriScheme(uri));
        }

        for (String uri : javaUris) {
            assertEquals("java", AtlasUtil.getUriModule(uri));
        }

        for (String uri : javaUris) {
            assertNull(AtlasUtil.getUriDataType(uri));
        }

        for (String uri : noverUris) {
            assertNull(AtlasUtil.getUriModuleVersion(uri));
        }

        for (String uri : javaVer1Uris) {
            assertEquals("1", AtlasUtil.getUriModuleVersion(uri));
        }

        for (String uri : javaVer2Uris) {
            assertEquals("2", AtlasUtil.getUriModuleVersion(uri));
        }

        for (String uri : parmUris) {
            Map<String, String> params = AtlasUtil.getUriParameters(uri);
            assertNotNull(params);
            assertEquals(Integer.valueOf(1), Integer.valueOf(params.size()));
            assertEquals("bar", params.get("foo"));
            assertNull(params.get("bar"));
        }

        for (String uri : parmsUris) {
            Map<String, String> params = AtlasUtil.getUriParameters(uri);
            assertNotNull(params);
            assertEquals(Integer.valueOf(2), Integer.valueOf(params.size()));
            assertEquals("bar", params.get("foo"));
            assertEquals("blah", params.get("bar"));
            assertNull(params.get("blah"));
        }
    }

    @Test
    public void testFindClassesForPackage() {
        List<Class<?>> classes = AtlasUtil.findClassesForPackage("io.atlasmap.v2");
        assertNotNull(classes);
        assertThat(classes.stream().map(Class::getName).collect(Collectors.toList()), hasItems("io.atlasmap.v2.Field", "io.atlasmap.v2.AtlasMapping", "io.atlasmap.v2.Action", "io.atlasmap.v2.Capitalize"));
    }

    @Test
    public void testGetUriDataType() {
        assertEquals("util", AtlasUtil.getUriDataType("atlas:java:util?param1=value1&param2=value2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindClassesForPackageIllegalArgumentException() {
        AtlasUtil.findClassesForPackage("io.atlasmapv2");
    }

    @Test
    public void testFind() {
        File file = Paths.get("src" + File.separator + "main" + File.separator + "java" + File.separator + "io" + File.separator + "atlasmap" + File.separator + "core").toFile();
        assertEquals(0, AtlasUtil.find(file, ".").size());
    }

    @Test
    public void testLoadPropertiesFromURL() throws Exception {
        URL url = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "AtlasUtilTest.properties").toUri().toURL();

        Properties properties = AtlasUtil.loadPropertiesFromURL(url);

        assertNotNull(properties);
        assertEquals("value1", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
    }

    @Test(expected = NullPointerException.class)
    public void testLoadPropertiesFromURLNullPointerException() throws Exception {
        AtlasUtil.loadPropertiesFromURL(null);
    }

    @Test(expected = MalformedURLException.class)
    public void testLoadPropertiesFromURLMalformedURLException() throws Exception {
        AtlasUtil.loadPropertiesFromURL(new URL("invalid URL"));
    }

    @Test
    public void testMatchUriModule() {
        assertFalse(AtlasUtil.matchUriModule(null, null));
        assertFalse(AtlasUtil.matchUriModule(null, "atlas:java"));
        assertFalse(AtlasUtil.matchUriModule("atlas:java", null));

        assertFalse(AtlasUtil.matchUriModule("", ""));
        assertFalse(AtlasUtil.matchUriModule("atlas:java", ""));
        assertFalse(AtlasUtil.matchUriModule("", "atlas:java"));

        assertTrue(AtlasUtil.matchUriModule("atlas:java", "atlas:java"));
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateUriIllegalStateException() {
        AtlasUtil.validateUri("java:atlas");
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateUriIllegalStateExceptionMultipleQuestionMark() {
        AtlasUtil.validateUri("atlas:?java?");
    }

    @Test
    public void testValidateUriSingleQuestionMark() {
        AtlasUtil.validateUri("atlas:java?");
    }

    @Test
    public void testValidateUri() {
        AtlasUtil.validateUri("atlas:java");
    }

    @Test
    public void testGetUriPartsAsArray() {
        assertNull(AtlasUtil.getUriPartsAsArray(null));
        assertEquals(2, AtlasUtil.getUriPartsAsArray("atlas:?java").size());
        assertEquals(2, AtlasUtil.getUriPartsAsArray("atlas:?").size());
        assertEquals(2, AtlasUtil.getUriPartsAsArray("atlas:").size());
    }

    @Test
    public void testGetUriScheme() {
        assertNull(AtlasUtil.getUriScheme(null));
    }

    @Test
    public void testGetUriParameters() {
        assertNull(AtlasUtil.getUriParameters(null));
        assertEquals(0, AtlasUtil.getUriParameters("").size());
        assertEquals(0, AtlasUtil.getUriParameters("atlas:").size());
        assertEquals(0, AtlasUtil.getUriParameters("atlas:?").size());
        assertEquals(2, AtlasUtil.getUriParameters("atlas:?param1=value1&param2=value2").size());
        assertEquals(1, AtlasUtil.getUriParameters("atlas:?param1=value1&param2=").size());
        assertEquals(1, AtlasUtil.getUriParameters("atlas:?param1=&param2=value2").size());
        assertEquals(1, AtlasUtil.getUriParameters("atlas:?=&param2=value2").size());
        assertEquals(0, AtlasUtil.getUriParameters("atlas:?=").size());
        assertEquals(0, AtlasUtil.getUriParameters("atlas:?&").size());
        assertEquals(0, AtlasUtil.getUriParameters("atlas:?p").size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUriParametersIllegalArgumentException() {
        AtlasUtil.getUriParameters("atlas:?%%XX");
    }

    @Test
    public void testGetUriParameterValue() {
        assertNull(AtlasUtil.getUriParameterValue("atlas:?java", "java"));
        assertEquals("value1", AtlasUtil.getUriParameterValue("atlas:?param1=value1&param2=value2", "param1"));
        assertEquals("value1", AtlasUtil.getUriParameterValue("atlas:?param1=value1&param2=", "param1"));
        assertEquals("value2", AtlasUtil.getUriParameterValue("atlas:?param1=&param2=value2", "param2"));
        assertNull(AtlasUtil.getUriParameterValue("atlas:?", "java"));
        assertNull(AtlasUtil.getUriParameterValue("atlas:?param", "java"));
        assertNull(AtlasUtil.getUriParameterValue("atlas:?&", "java"));
        assertNull(AtlasUtil.getUriParameterValue("atlas:?=", "java"));
        assertNull(AtlasUtil.getUriParameterValue("atlas:? ", "java"));
    }

    @Test
    public void testFindClassesFromJar() throws Exception {
        URL jarFile = new File("target" + File.separator + "test-dependencies" + File.separator + "atlas-model.jar").toURI().toURL();
        String urlString = "jar:file:" + jarFile.getPath() + "!/";
        assertFalse(AtlasUtil.findClassesFromJar(new URL(urlString)).isEmpty());
    }

    @Test
    public void testFindClassesFromJarFileNotFoundIOException() throws Exception {
        String urlString = "jar:file:" + File.separator + "target" + File.separator + "test-dependencies" + File.separator + "atlas-model.jar!/";
        assertEquals(0, AtlasUtil.findClassesFromJar(new URL(urlString)).size());
    }

    @Test(expected = ClassCastException.class)
    public void testFindClassesFromJarClassCastException() throws Exception {
        URL url = Paths.get("target" + File.separator + "test-dependencies" + File.separator + "atlas-model.jar").toUri().toURL();
        AtlasUtil.findClassesFromJar(url);
    }

    @Test
    public void testToAuditStatus() {
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.ERROR));
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.WARN));
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.INFO));
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.ALL));
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.NONE));
    }
}
