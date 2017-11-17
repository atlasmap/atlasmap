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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

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

        List<String> javaUris = Arrays.asList(uriJavaNover, uriJavaNoverWParm, uriJavaNoverWParms,
                uriJavaVer1, uriJavaVer2, uriJavaVer2WParm, uriJavaVer2WParams);
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
        assertThat(classes.stream().map(Class::getName).collect(Collectors.toList()), hasItems("io.atlasmap.v2.Field",
                "io.atlasmap.v2.AtlasMapping", "io.atlasmap.v2.Action", "io.atlasmap.v2.Capitalize"));
    }
}
