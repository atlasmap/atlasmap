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
package io.atlasmap.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;

public class AtlasServiceTest {

    private AtlasService service = null;
    private ObjectMapper mapper = null;

    @Before
    public void setUp() throws Exception {
        service = new AtlasService();
        mapper = Json.mapper();
    }

    @After
    public void tearDown() {
        service = null;
        mapper = null;
    }

    @Test
    public void testListMappings() throws Exception {
        Response resp = service.listMappings(
                generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/mappings"), null);
        StringMap sMap = Json.mapper().readValue((byte[])resp.getEntity(), StringMap.class);
        System.out.println("Found " + sMap.getStringMapEntry().size() + " objects");
        for (StringMapEntry s : sMap.getStringMapEntry()) {
            System.out.println("\t n: " + s.getName() + " v: " + s.getValue());
        }
    }

    @Test
    public void testGetMapping() {
        Response resp = service.getMappingRequest("JSON", "junit3");
        assertEquals(byte[].class, resp.getEntity().getClass());
        resp = service.getMappingRequest("XML", "junit3");
        assertEquals(java.lang.String.class, resp.getEntity().getClass());
    }

    @Test
    public void testFilenameMatch() {
        String fileName = "atlasmapping-foo.xml";
        assertTrue(fileName.matches("atlasmapping-[a-zA-Z0-9]+.xml"));
    }

    @Test
    public void testActionDeserialization() throws Exception {
        File file = new File("src/test/resources/atlasmapping-actions.json");
        AtlasMapping mapping = mapper.readValue(file, AtlasMapping.class);

        Mappings mappings = mapping.getMappings();
        for (BaseMapping baseMapping : mappings.getMapping()) {
            if (MappingType.MAP.equals(baseMapping.getMappingType())) {
                List<Field> fields = ((Mapping) baseMapping).getOutputField();
                for (Field f : fields) {
                    if (f.getActions() != null && f.getActions().getActions() != null
                            && !f.getActions().getActions().isEmpty()) {
                        System.out.println("Found actions: " + f.getActions().getActions().size());
                    }
                }
            }
        }
    }

    @Test
    public void testJarUpload() throws Exception {
        new File("target/tmp").mkdirs();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int answer = compiler.run(System.in, System.out, System.err,
                "-d", "target/tmp",
                "src/test/resources/upload/io/atlasmap/service/my/MyFieldActions.java");
        assertEquals(0, answer);
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream("target/tmp/my.jar"));
        jarOut.putNextEntry(new JarEntry("io/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("io/atlasmap/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("io/atlasmap/service/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("io/atlasmap/service/my/"));
        jarOut.closeEntry();
        JarEntry classEntry = new JarEntry("io/atlasmap/service/my/MyFieldActions.class");
        jarOut.putNextEntry(classEntry);
        byte[] buffer = new byte[1024];
        BufferedInputStream in = new BufferedInputStream(new FileInputStream("target/tmp/io/atlasmap/service/my/MyFieldActions.class"));
        int count = -1;
        while ((count = in.read(buffer)) != -1) {
            jarOut.write(buffer, 0, count);
        }
        in.close();
        jarOut.closeEntry();

        jarOut.putNextEntry(new JarEntry("META-INF/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("META-INF/services/"));
        jarOut.closeEntry();
        JarEntry svcEntry = new JarEntry("META-INF/services/io.atlasmap.api.AtlasFieldAction");
        jarOut.putNextEntry(svcEntry);
        in = new BufferedInputStream(new FileInputStream("src/test/resources/upload/META-INF/services/io.atlasmap.api.AtlasFieldAction"));
        while ((count = in.read(buffer)) != -1) {
            jarOut.write(buffer, 0, count);
        }
        in.close();
        jarOut.closeEntry();
        jarOut.close();
        FileInputStream jarIn = new FileInputStream("target/tmp/my.jar");
        MultipartInput requestIn = mock(MultipartInput.class);
        List<InputPart> parts = new LinkedList<>();
        InputPart jarPart = mock(InputPart.class);
        parts.add(jarPart);
        when(requestIn.getParts()).thenReturn(parts);
        when(jarPart.getBody(InputStream.class, null)).thenReturn(jarIn);
        when(jarPart.getMediaType()).thenReturn(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        Response resUL = service.uploadLibrary(requestIn);
        assertEquals(200, resUL.getStatus());
        Response resFA = service.listFieldActions(null);
        assertEquals(200, resFA.getStatus());
        String responseJson = new String((byte[])resFA.getEntity());
        assertTrue(responseJson, responseJson.contains("MyCustomFieldAction"));
    }

    protected UriInfo generateTestUriInfo(String baseUri, String absoluteUri) throws Exception {
        return new TestUriInfo(new URI(baseUri), new URI(absoluteUri));
    }

}
