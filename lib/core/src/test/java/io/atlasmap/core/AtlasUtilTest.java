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
package io.atlasmap.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.atlasmap.v2.ComplexType;
import io.atlasmap.v2.Document;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Fields;
import org.junit.jupiter.api.Test;

import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
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
    public void testGetUriDataType() {
        assertEquals("util", AtlasUtil.getUriDataType("atlas:java:util?param1=value1&param2=value2"));
    }

    @Test
    public void testLoadPropertiesFromURL() throws Exception {
        URL url = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "AtlasUtilTest.properties").toUri().toURL();

        Properties properties = AtlasUtil.loadPropertiesFromURL(url);

        assertNotNull(properties);
        assertEquals("value1", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
    }

    @Test
    public void testLoadPropertiesFromURLNullPointerException() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            AtlasUtil.loadPropertiesFromURL(null);
        });
    }

    @Test
    public void testLoadPropertiesFromURLMalformedURLException() throws Exception {
        assertThrows(MalformedURLException.class, () -> {
            AtlasUtil.loadPropertiesFromURL(new URL("invalid URL"));
        });
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

    @Test
    public void testValidateUriIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> {
            AtlasUtil.validateUri("java:atlas");
        });
    }

    @Test
    public void testValidateUriIllegalStateExceptionMultipleQuestionMark() {
        assertThrows(IllegalStateException.class, () -> {
            AtlasUtil.validateUri("atlas:?java?");
        });
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

    @Test
    public void testGetUriParametersIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            AtlasUtil.getUriParameters("atlas:?%%XX");
        });
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
    public void testToAuditStatus() {
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.ERROR));
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.WARN));
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.INFO));
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.ALL));
        assertNotNull(AtlasUtil.toAuditStatus(ValidationStatus.NONE));
    }

    @Test
    public void testExcludeNotRequestedFieldsShouldIncludeAllIfIncludePathsNull() {
        Document document = new Document();
        Fields fields = new Fields();
        fields.getField().add(newField("/A",
            newField("/A/A",
                newField("/A/A/A"),
                newField("/A/A/B")),
            newField("/A/B",
                newField("/A/B/A",
                    newField("/A/B/A/A")))
        ));
        document.setFields(fields);

        AtlasUtil.excludeNotRequestedFields(document, null);

        assertEquals("/A{/A/A{/A/A/A{}, /A/A/B{}, }, /A/B{/A/B/A{/A/B/A/A{}, }, }, }, ", fields.getField().get(0).toString());
    }

    @Test
    public void testExcludeNotRequestedFieldsShouldIncludeAllIfIncludePathsEmpty() {
        Document document = new Document();
        Fields fields = new Fields();
        fields.getField().add(newField("/A",
            newField("/A/A",
                newField("/A/A/A"),
                newField("/A/A/B")),
            newField("/A/B",
                newField("/A/B/A",
                    newField("/A/B/A/A")))
        ));
        document.setFields(fields);

        AtlasUtil.excludeNotRequestedFields(document, Collections.emptyList());

        assertEquals("/A{/A/A{/A/A/A{}, /A/A/B{}, }, /A/B{/A/B/A{/A/B/A/A{}, }, }, }, ", fields.getField().get(0).toString());
    }

    @Test
    public void testExcludeNotRequestedFieldsShouldIncludeRootLevel() {
        Document document = new Document();
        Fields fields = new Fields();
        fields.getField().add(newField("/A",
            newField("/A/A",
                newField("/A/A/A"),
                newField("/A/A/B")),
            newField("/A/B",
                newField("/A/B/A",
                    newField("/A/B/A/A")))
        ));
        document.setFields(fields);

        AtlasUtil.excludeNotRequestedFields(document, Arrays.asList("/"));

        assertEquals("/A{}, ", fields.getField().get(0).toString());
    }

    @Test
    public void testExcludeNotRequestedFieldsShouldIncludeOneLevel() {
        Document document = new Document();
        Fields fields = new Fields();
        fields.getField().add(newField("/A",
            newField("/A/A",
                newField("/A/A/A"),
                newField("/A/A/B")),
            newField("/A/B",
                newField("/A/B/A",
                    newField("/A/B/A/A")))
        ));
        document.setFields(fields);

        AtlasUtil.excludeNotRequestedFields(document, Arrays.asList("/A/"));

        assertEquals("/A{/A/A{}, /A/B{}, }, ", fields.getField().get(0).toString());
    }

    @Test
    public void testExcludeNotRequestedFieldsShouldIncludeTwoLevels() {
        Document document = new Document();
        Fields fields = new Fields();
        fields.getField().add(newField("/A",
            newField("/A/A",
                newField("/A/A/A"),
                newField("/A/A/B")),
            newField("/A/B",
                newField("/A/B/A",
                    newField("/A/B/A/A")))
        ));
        document.setFields(fields);

        AtlasUtil.excludeNotRequestedFields(document, Arrays.asList("/A/B"));

        assertEquals("/A{/A/A{}, /A/B{/A/B/A{}, }, }, ", fields.getField().get(0).toString());
    }

    @Test
    public void testExcludeNotRequestedFieldsShouldIncludeTwoDifferentLevels() {
        Document document = new Document();
        Fields fields = new Fields();
        fields.getField().add(newField("/A",
            newField("/A/A",
                newField("/A/A/A"),
                newField("/A/A/B")),
            newField("/A/B",
                newField("/A/B/A",
                    newField("/A/B/A/A")))
        ));
        document.setFields(fields);

        AtlasUtil.excludeNotRequestedFields(document, Arrays.asList("/A/A","/A/B"));

        assertEquals("/A{/A/A{/A/A/A{}, /A/A/B{}, }, /A/B{/A/B/A{}, }, }, ", fields.getField().get(0).toString());
    }

    @Test
    public void testExcludeNotRequestedFieldsShouldIncludeAllIntermediateLevels() {
        Document document = new Document();
        Fields fields = new Fields();
        fields.getField().add(newField("/A",
            newField("/A/A",
                newField("/A/A/A"),
                newField("/A/A/B")),
            newField("/A/B",
                newField("/A/B/A",
                    newField("/A/B/A/A")))
        ));
        document.setFields(fields);

        AtlasUtil.excludeNotRequestedFields(document, Arrays.asList("/A/A/B"));

        assertEquals("/A{/A/A{/A/A/A{}, /A/A/B{}, }, /A/B{}, }, ", fields.getField().get(0).toString());
    }

    private static class ComplexField extends Field implements ComplexType {

        @Override
        public List<? extends Field> getChildFields() {
            return null;
        }
    }

    private Field newField(String fieldPath, Field... children) {
        List<Field> childFields = new ArrayList<>();
        childFields.addAll(Arrays.asList(children));
        return new ComplexField() {

            @Override
            public String getPath() {
                return fieldPath;
            }

            @Override
            public List<? extends Field> getChildFields() {
                return childFields;
            }

            @Override
            public String toString() {
                StringBuilder text = new StringBuilder();
                text.append(getPath()).append("{");
                for (Field field: getChildFields()) {
                    text.append(field.toString());
                }
                text.append("}, ");
                return text.toString();
            }
        };
    }

    public void testValidationToString() {
        assertNotNull(AtlasUtil.validationToString(null));
        Validation info = new Validation();
        info.setScope(ValidationScope.MAPPING);
        info.setId("0001");
        info.setMessage("Information message");
        info.setStatus(ValidationStatus.INFO);
        assertNotNull(AtlasUtil.validationToString(info));
    }
}
