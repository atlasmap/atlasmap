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
package io.atlasmap.service;

import static io.atlasmap.v2.MappingFileType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.atlasmap.core.AtlasUtil;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingFileType;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.ProcessMappingRequest;
import io.atlasmap.v2.ProcessMappingResponse;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;

public class AtlasServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasServiceTest.class);
    private static final String TEMP_DIR = "target/with space";
    private static final String TEST_JAR_DIR = "target/tmp";
    private static final String TEST_JAR_PATH = TEST_JAR_DIR + "/my.jar";

    private AtlasService service = null;
    private ObjectMapper mapper = null;

    @BeforeEach
    public void setUp() throws Exception {
        File workspaceFolderWithSpace = new File(TEMP_DIR);
        System.setProperty(AtlasService.ATLASMAP_WORKSPACE, workspaceFolderWithSpace.getAbsolutePath());
        service = new AtlasService();
        mapper = Json.mapper();
    }

    @AfterEach
    public void tearDown() {
        service.resetUserLibs();
        service.resetAllMappings();
        service = null;
        mapper = null;
        AtlasUtil.deleteDirectory(new File(TEST_JAR_DIR));
        AtlasUtil.deleteDirectory(new File(TEMP_DIR));
    }

    @Test
    public void testVersion() throws Exception {
        Response resp = service.version();
        String body = resp.getEntity().toString();
        assertNotNull(body, body);
    }

    @Test
    public void testListMappings() throws Exception {
        Response resp = service.listMappings(
                generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/mappings"), null, null);
        StringMap sMap = Json.mapper().readValue((byte[])resp.getEntity(), StringMap.class);
        LOG.info("Found " + sMap.getStringMapEntry().size() + " objects");
        for (StringMapEntry s : sMap.getStringMapEntry()) {
            LOG.info("\t n: " + s.getName() + " v: " + s.getValue());
        }
    }

    @Test
    public void testGetMapping() {
        Response resp = service.getMappingRequest(JSON, 3);
        assertEquals(204, resp.getStatus());
        assertNull(resp.getEntity());
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
                    if (f.getActions() != null && f.getActions() != null
                            && !f.getActions().isEmpty()) {
                        LOG.info("Found actions: " + f.getActions().size());
                    }
                }
            }
        }
    }

    @Test
    public void testJarUpload() throws Exception {
        createJarFile(false, false);
        FileInputStream jarIn = new FileInputStream(TEST_JAR_PATH);
        Response resUL = service.uploadLibrary(jarIn);
        assertEquals(200, resUL.getStatus());
        Response resFA = service.listFieldActions(null);
        assertEquals(200, resFA.getStatus());
        String responseJson = new String((byte[])resFA.getEntity());
        assertTrue(responseJson.contains("myCustomFieldAction"));
        Response resMB = service.listMappingBuilderClasses(null);
        assertEquals(200, resMB.getStatus());
        ArrayNode builders = (ArrayNode) new ObjectMapper().readTree((byte[])resMB.getEntity()).get("ArrayList");
        assertEquals(1, builders.size());
        assertEquals("io.atlasmap.service.my.MyCustomMappingBuilder", builders.get(0).asText());

        BufferedInputStream in = new BufferedInputStream(new FileInputStream("src/test/resources/mappings/atlasmapping-custom-action.json"));
        Response resVD = service.validateMappingRequest(in, 0, null);
        assertEquals(200, resVD.getStatus());
    }

    private void createJarFile(boolean skipModel, boolean skipProcessor) throws Exception {
        new File(TEST_JAR_DIR).mkdirs();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int answer = compiler.run(System.in, System.out, System.err,
                "-d", TEST_JAR_DIR,
                "src/test/resources/upload/io/atlasmap/service/my/MyCustomMappingBuilder.java",
                "src/test/resources/upload/io/atlasmap/service/my/MyFieldActions.java",
                "src/test/resources/upload/io/atlasmap/service/my/MyFieldActionsModel.java");
        assertEquals(0, answer);
        File jarFile = new File(TEST_JAR_PATH);
        if (jarFile.exists()) {
            jarFile.delete();
        }
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile));
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
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(TEST_JAR_DIR + "/io/atlasmap/service/my/MyFieldActions.class"));
        int count = -1;
        while ((count = in.read(buffer)) != -1) {
            jarOut.write(buffer, 0, count);
        }
        in.close();
        jarOut.closeEntry();
        classEntry = new JarEntry("io/atlasmap/service/my/MyFieldActionsModel.class");
        jarOut.putNextEntry(classEntry);
        in = new BufferedInputStream(new FileInputStream("target/tmp/io/atlasmap/service/my/MyFieldActionsModel.class"));
        count = -1;
        while ((count = in.read(buffer)) != -1) {
            jarOut.write(buffer, 0, count);
        }
        in.close();
        jarOut.closeEntry();
        classEntry = new JarEntry("io/atlasmap/service/my/MyCustomMappingBuilder.class");
        jarOut.putNextEntry(classEntry);
        in = new BufferedInputStream(new FileInputStream(TEST_JAR_DIR + "/io/atlasmap/service/my/MyCustomMappingBuilder.class"));
        count = -1;
        while ((count = in.read(buffer)) != -1) {
            jarOut.write(buffer, 0, count);
        }
        in.close();
        jarOut.closeEntry();

        jarOut.putNextEntry(new JarEntry("META-INF/"));
        jarOut.closeEntry();
        jarOut.putNextEntry(new JarEntry("META-INF/services/"));
        jarOut.closeEntry();
        if (!skipProcessor) {
            JarEntry svcEntry = new JarEntry("META-INF/services/io.atlasmap.spi.AtlasFieldAction");
            jarOut.putNextEntry(svcEntry);
            in = new BufferedInputStream(new FileInputStream("src/test/resources/upload/META-INF/services/io.atlasmap.spi.AtlasFieldAction"));
            while ((count = in.read(buffer)) != -1) {
                jarOut.write(buffer, 0, count);
            }
            in.close();
            jarOut.closeEntry();
        }
        if (!skipModel) {
            JarEntry svcEntry = new JarEntry("META-INF/services/io.atlasmap.v2.Action");
            jarOut.putNextEntry(svcEntry);
            in = new BufferedInputStream(new FileInputStream("src/test/resources/upload/META-INF/services/io.atlasmap.v2.Action"));
            while ((count = in.read(buffer)) != -1) {
                jarOut.write(buffer, 0, count);
            }
            in.close();
            jarOut.closeEntry();
        }
        jarOut.close();
    }

    @Test
    public void testJarUploadNoModelLoader() throws Exception {
        assumeFalse(isWindowsJDK8());

        createJarFile(true, false);
        FileInputStream jarIn = new FileInputStream(TEST_JAR_PATH);
        WebApplicationException e = assertThrows(WebApplicationException.class, () -> {
            service.uploadLibrary(jarIn);
        });
        assertTrue(e.getMessage().contains("META-INF/services"), e.getMessage());
    }

    @Test
    public void testJarUploadNoProcessorLoader() throws Exception {
        assumeFalse(isWindowsJDK8());

        createJarFile(false, true);
        FileInputStream jarIn = new FileInputStream(TEST_JAR_PATH);
        Response resUL = service.uploadLibrary(jarIn);
        assertEquals(200, resUL.getStatus());
        Response resFA = service.listFieldActions(null);
        assertEquals(200, resFA.getStatus());
        String responseJson = new String((byte[])resFA.getEntity());
        assertFalse(responseJson.contains("myCustomFieldAction"));

        BufferedInputStream in = new BufferedInputStream(new FileInputStream("src/test/resources/mappings/atlasmapping-custom-action.json"));
        AtlasMapping am = mapper.readValue(in, AtlasMapping.class);
        Mapping m = (Mapping) am.getMappings().getMapping().get(0);
        Field f = m.getInputField().get(0);
        f.setValue("foo");
        Action action = f.getActions().get(0);
        Method method = action.getClass().getDeclaredMethod("setParam", new Class[] {String.class});
        method.invoke(action, "param");
        ProcessMappingRequest request = new ProcessMappingRequest();
        request.setMapping(m);
        Response resMR = service.processMappingRequest(new ByteArrayInputStream(mapper.writeValueAsBytes(request)), null);
        assertEquals(200, resMR.getStatus());
        ProcessMappingResponse pmr = Json.mapper().readValue((byte[])resMR.getEntity(), ProcessMappingResponse.class);
        assertEquals(1, pmr.getAudits().getAudit().size(), printAudit(pmr.getAudits()));
        Audit audit = pmr.getAudits().getAudit().get(0);
        assertEquals(AuditStatus.WARN, audit.getStatus());
        assertTrue(audit.getMessage().contains("Couldn't find metadata for a FieldAction 'MyFieldActionsModel'"));
        assertEquals("foo", pmr.getMapping().getOutputField().get(0).getValue());
    }

    private boolean isWindowsJDK8() {
        return System.getProperty("os.name").toLowerCase().contains("win")
            && Double.parseDouble(System.getProperty("java.specification.version")) < 9;
    }

    @Test
    public void testADMUpload() throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("json-schema-source-to-xml-schema-target.adm");
        Response res = service.createMappingRequest(in, MappingFileType.ZIP, 0,
            generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/mapping/ZIP/0"));
        assertEquals(200, res.getStatus());
        res = service.getMappingRequest(MappingFileType.JSON, 0);
        assertEquals(200, res.getStatus());
        AtlasMapping mappings = mapper.readValue((byte[])res.getEntity(), AtlasMapping.class);
        assertEquals(4, mappings.getMappings().getMapping().size());
    }

    @Test
    public void testProcessMapping() throws Exception {
        Response res = service.processMappingRequest(this.getClass().getClassLoader().getResourceAsStream("mappings/process-mapping-request.json"),
                generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/mapping/process"));
        ProcessMappingResponse resp = Json.mapper().readValue((byte[])res.getEntity(), ProcessMappingResponse.class);
        assertEquals(0, resp.getAudits().getAudit().size(), printAudit(resp.getAudits()));
        FieldGroup group = (FieldGroup) resp.getMapping().getOutputField().get(0);
        assertEquals("/addressList<>/city", group.getPath());
        Field f = group.getField().get(0);
        assertEquals("/addressList<0>/city", f.getPath());
        assertEquals("testZzz", f.getValue());
    }

    @Test
    public void testProcessMapping2977() throws Exception {
        Response res = service.processMappingRequest(this.getClass().getClassLoader().getResourceAsStream("mappings/process-mapping-request-2977.json"),
                generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/mapping/process"));
        ProcessMappingResponse resp = Json.mapper().readValue((byte[])res.getEntity(), ProcessMappingResponse.class);
        assertEquals(0, resp.getAudits().getAudit().size(), printAudit(resp.getAudits()));
        Field field = resp.getMapping().getOutputField().get(0);
        assertEquals("/ns:XmlOE/ns:Address/ns:addressLine1", field.getPath());
        assertEquals("Boston", field.getValue());
    }

    @Test
    public void testProcessMapping3064() throws Exception {
        Response res = service.processMappingRequest(this.getClass().getClassLoader().getResourceAsStream("mappings/process-mapping-request-3064.json"),
                generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/mapping/process"));
        ProcessMappingResponse resp = Json.mapper().readValue((byte[])res.getEntity(), ProcessMappingResponse.class);
        assertEquals(0, resp.getAudits().getAudit().size(), printAudit(resp.getAudits()));
        Field field = resp.getMapping().getInputField().get(0);
        assertEquals("/primitives/stringPrimitive", field.getPath());
    }

    @Test
    public void testProcessMappingCustomAction() throws Exception {
        createJarFile(false, false);
        FileInputStream jarIn = new FileInputStream(TEST_JAR_PATH);
        Response resUL = service.uploadLibrary(jarIn);
        assertEquals(200, resUL.getStatus());
        Response resFA = service.listFieldActions(null);
        assertEquals(200, resFA.getStatus());
        BufferedInputStream in = new BufferedInputStream(new FileInputStream("src/test/resources/mappings/atlasmapping-custom-action.json"));
        AtlasMapping am = mapper.readValue(in, AtlasMapping.class);
        Mapping m = (Mapping) am.getMappings().getMapping().get(0);
        Field f = m.getInputField().get(0);
        f.setValue("foo");
        Action action = f.getActions().get(0);
        Method method = action.getClass().getDeclaredMethod("setParam", new Class[] {String.class});
        method.invoke(action, "param");
        ProcessMappingRequest request = new ProcessMappingRequest();
        request.setMapping(m);
        Response resMR = service.processMappingRequest(new ByteArrayInputStream(mapper.writeValueAsBytes(request)), null);
        assertEquals(200, resMR.getStatus());
        ProcessMappingResponse pmr = Json.mapper().readValue((byte[])resMR.getEntity(), ProcessMappingResponse.class);
        assertEquals(0, pmr.getAudits().getAudit().size(), printAudit(pmr.getAudits()));
        assertEquals("param foo", pmr.getMapping().getOutputField().get(0).getValue());
    }

    protected UriInfo generateTestUriInfo(String baseUri, String absoluteUri) throws Exception {
        return new TestUriInfo(new URI(baseUri), new URI(absoluteUri));
    }

    protected String printAudit(Audits audits) {
        StringBuilder buf = new StringBuilder("Audits: ");
        for (Audit a : audits.getAudit()) {
            buf.append('[');
            buf.append(a.getStatus());
            buf.append(", message=");
            buf.append(a.getMessage());
            buf.append("], ");
        }
        return buf.toString();
    }

}
