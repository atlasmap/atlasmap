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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.Mapping;
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

    private AtlasService atlasService;
    private DocumentService documentService;
    private MappingService mappingService;
    private ObjectMapper mapper = null;

    @BeforeEach
    public void setUp() throws Exception {
        File workspaceFolderWithSpace = new File(TEMP_DIR);
        System.setProperty(AtlasService.ATLASMAP_WORKSPACE, workspaceFolderWithSpace.getAbsolutePath());
        atlasService = new AtlasService();
        documentService = new DocumentService(atlasService);
        mappingService = new MappingService(atlasService);
        mapper = Json.mapper();
    }

    @AfterEach
    public void tearDown() {
        atlasService.deleteLibraries();
        atlasService.deleteAllMappingProjects();
        atlasService = null;
        mapper = null;
        AtlasUtil.deleteDirectory(new File(TEST_JAR_DIR));
        AtlasUtil.deleteDirectory(new File(TEMP_DIR));
    }

    @Test
    public void testVersion() throws Exception {
        Response resp = atlasService.version();
        String body = resp.getEntity().toString();
        assertNotNull(body, body);
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
        Util.createJarFile(TEST_JAR_DIR, TEST_JAR_PATH, false, false);
        FileInputStream jarIn = new FileInputStream(TEST_JAR_PATH);
        Response resUL = atlasService.uploadLibrary(jarIn);
        assertEquals(200, resUL.getStatus());
        Response resFA = atlasService.listFieldActions(null);
        assertEquals(200, resFA.getStatus());
        String responseJson = new String((byte[])resFA.getEntity());
        assertTrue(responseJson.contains("myCustomFieldAction"));
        Response resMB = atlasService.listMappingBuilderClasses(null);
        assertEquals(200, resMB.getStatus());
        ArrayNode builders = (ArrayNode) new ObjectMapper().readTree((byte[])resMB.getEntity()).get("ArrayList");
        assertEquals(1, builders.size());
        assertEquals("io.atlasmap.service.my.MyCustomMappingBuilder", builders.get(0).asText());

        BufferedInputStream in = new BufferedInputStream(new FileInputStream("src/test/resources/mappings/atlasmapping-custom-action.json"));
        Response resVD = mappingService.validateMappingRequest(in, 0, null);
        assertEquals(200, resVD.getStatus());
    }

    @Test
    public void testJarUploadNoModelLoader() throws Exception {
        assumeFalse(isWindowsJDK8());

        Util.createJarFile(TEST_JAR_DIR, TEST_JAR_PATH, true, false);
        FileInputStream jarIn = new FileInputStream(TEST_JAR_PATH);
        WebApplicationException e = assertThrows(WebApplicationException.class, () -> {
            atlasService.uploadLibrary(jarIn);
        });
        assertTrue(e.getMessage().contains("META-INF/services"), e.getMessage());
    }

    @Test
    public void testJarUploadNoProcessorLoader() throws Exception {
        assumeFalse(isWindowsJDK8());

        Util.createJarFile(TEST_JAR_DIR, TEST_JAR_PATH, false, true);
        FileInputStream jarIn = new FileInputStream(TEST_JAR_PATH);
        Response resUL = atlasService.uploadLibrary(jarIn);
        assertEquals(200, resUL.getStatus());
        Response resFA = atlasService.listFieldActions(null);
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
        Response resMR = mappingService.processMappingRequest(new ByteArrayInputStream(mapper.writeValueAsBytes(request)), 0, null);
        assertEquals(200, resMR.getStatus());
        ProcessMappingResponse pmr = Json.mapper().readValue((byte[])resMR.getEntity(), ProcessMappingResponse.class);
        assertEquals(1, pmr.getAudits().getAudit().size(), Util.printAudit(pmr.getAudits()));
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
    public void testListMappingDefinitionNames() throws Exception {
        Response resp = atlasService.listMappingDefinitionNames(
                Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project"), null);
        StringMap sMap = Json.mapper().readValue((byte[])resp.getEntity(), StringMap.class);
        LOG.info("Found " + sMap.getStringMapEntry().size() + " objects");
        for (StringMapEntry s : sMap.getStringMapEntry()) {
            LOG.info("\t n: " + s.getName() + " v: " + s.getValue());
        }
    }

    @Test
    public void testADMUpload() throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("json-schema-source-to-xml-schema-target.adm");
        Response res = atlasService.importADMArchiveRequest(in, 0,
            Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project/0/adm"));
        assertEquals(200, res.getStatus());
        res = mappingService.getMappingRequest(0);
        assertEquals(200, res.getStatus());
        AtlasMapping mappings = mapper.readValue((byte[])res.getEntity(), AtlasMapping.class);
        assertEquals(4, mappings.getMappings().getMapping().size());
    }

    @Test
    public void testDeleteAll() throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("json-schema-source-to-xml-schema-target.adm");
        Response res = atlasService.importADMArchiveRequest(in, 0,
            Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project/0/adm"));
        assertEquals(200, res.getStatus());
        res = documentService.getDocumentCatalogRequest(0);
        assertEquals(200, res.getStatus());
        res = atlasService.deleteAll();
        assertEquals(200, res.getStatus());
        res = documentService.getDocumentCatalogRequest(0);
        assertEquals(204, res.getStatus());  // Document catalog file was not found
    }

    @Test
    public void testDeleteMappingProjectById() throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("json-schema-source-to-xml-schema-target.adm");
        Response res = atlasService.importADMArchiveRequest(in, 0,
            Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project/0/adm"));
        assertEquals(200, res.getStatus());
        ADMArchiveHandler admHandler = atlasService.getADMArchiveHandler(0);
        assertNotNull(admHandler);
        assertNotNull(admHandler.getMappingDefinition());
        res = atlasService.deleteMappingProjectById(0);
        assertEquals(200, res.getStatus());
        admHandler = atlasService.getADMArchiveHandler(0);
        if (admHandler != null) {
            assertNull(admHandler.getMappingDefinition());
        }
    }
}
