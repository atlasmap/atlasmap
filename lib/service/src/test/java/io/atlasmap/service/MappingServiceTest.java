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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.core.AtlasUtil;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.ProcessMappingRequest;
import io.atlasmap.v2.ProcessMappingResponse;

public class MappingServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasServiceTest.class);
    private static final String TEMP_DIR = "target/with space2";
    private static final String TEST_JAR_DIR = "target/tmp2";
    private static final String TEST_JAR_PATH = TEST_JAR_DIR + "/my.jar";

    private AtlasService atlasService;
    private MappingService mappingService;
    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() throws Exception {
        File workspaceFolderWithSpace = new File(TEMP_DIR);
        System.setProperty(AtlasService.ATLASMAP_WORKSPACE, workspaceFolderWithSpace.getAbsolutePath());
        this.atlasService = new AtlasService();
        this.mappingService = new MappingService(atlasService);
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
    public void testGetMapping() {
        Response resp = mappingService.getMappingRequest(3);
        assertEquals(204, resp.getStatus());
        assertNull(resp.getEntity());
    }

    @Test
    public void testProcessMapping() throws Exception {
        Response res = mappingService.processMappingRequest(this.getClass().getClassLoader().getResourceAsStream("mappings/process-mapping-request.json"),
                0, Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project/0/mapping/process"));
        ProcessMappingResponse resp = Json.mapper().readValue((byte[])res.getEntity(), ProcessMappingResponse.class);
        assertEquals(0, resp.getAudits().getAudit().size(), Util.printAudit(resp.getAudits()));
        FieldGroup group = (FieldGroup) resp.getMapping().getOutputField().get(0);
        assertEquals("/addressList<>/city", group.getPath());
        Field f = group.getField().get(0);
        assertEquals("/addressList<0>/city", f.getPath());
        assertEquals("testZzz", f.getValue());
    }

    @Test
    public void testProcessMapping2977() throws Exception {
        Response res = mappingService.processMappingRequest(this.getClass().getClassLoader().getResourceAsStream("mappings/process-mapping-request-2977.json"),
                0, Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project/0/mapping/process"));
        ProcessMappingResponse resp = Json.mapper().readValue((byte[])res.getEntity(), ProcessMappingResponse.class);
        assertEquals(0, resp.getAudits().getAudit().size(), Util.printAudit(resp.getAudits()));
        Field field = resp.getMapping().getOutputField().get(0);
        assertEquals("/ns:XmlOE/ns:Address/ns:addressLine1", field.getPath());
        assertEquals("Boston", field.getValue());
    }

    @Test
    public void testProcessMapping3064() throws Exception {
        Response res = mappingService.processMappingRequest(this.getClass().getClassLoader().getResourceAsStream("mappings/process-mapping-request-3064.json"),
                0, Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project/0/mapping/process"));
        ProcessMappingResponse resp = Json.mapper().readValue((byte[])res.getEntity(), ProcessMappingResponse.class);
        assertEquals(0, resp.getAudits().getAudit().size(), Util.printAudit(resp.getAudits()));
        Field field = resp.getMapping().getInputField().get(0);
        assertEquals("/primitives/stringPrimitive", field.getPath());
    }

    @Test
    public void testProcessMappingChangeMappingIndex3935() throws Exception {
        Response res = mappingService.processMappingRequest(this.getClass().getClassLoader().getResourceAsStream("mappings/process-mapping-request-3935.json"),
                0, Util.generateTestUriInfo("http://localhost:8686/v2/atlas",
                "http://localhost:8686/v2/atlas/project/0/mapping/mapping.xxxxxx/field/TARGET/0/index"));
        ProcessMappingResponse resp = Json.mapper().readValue((byte[])res.getEntity(), ProcessMappingResponse.class);
        Field field = resp.getMapping().getOutputField().get(0);
        assertEquals(0, field.getIndex());
    }

    @Test
    public void testProcessMappingCustomAction() throws Exception {
        Util.createJarFile(TEST_JAR_DIR, TEST_JAR_PATH, false, false);
        FileInputStream jarIn = new FileInputStream(TEST_JAR_PATH);
        Response resUL = atlasService.uploadLibrary(jarIn);
        assertEquals(200, resUL.getStatus());
        Response resFA = atlasService.listFieldActions(null);
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
        Response resMR = mappingService.processMappingRequest(new ByteArrayInputStream(mapper.writeValueAsBytes(request)), 0, null);
        assertEquals(200, resMR.getStatus());
        ProcessMappingResponse pmr = Json.mapper().readValue((byte[])resMR.getEntity(), ProcessMappingResponse.class);
        assertEquals(0, pmr.getAudits().getAudit().size(), Util.printAudit(pmr.getAudits()));
        assertEquals("param foo", pmr.getMapping().getOutputField().get(0).getValue());
    }

}
