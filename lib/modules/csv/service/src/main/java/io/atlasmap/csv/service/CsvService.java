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
package io.atlasmap.csv.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.csv.core.CsvConfig;
import io.atlasmap.csv.core.CsvFieldReader;
import io.atlasmap.csv.v2.CsvInspectionRequest;
import io.atlasmap.csv.v2.CsvInspectionResponse;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.Document;
import io.atlasmap.v2.Field;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * CSV Service provides CSV inspection service which generate an AtlasMap Document object from the CSV.
 */
@Path("/csv/")
public class CsvService extends ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvService.class);

    @Context
    private ResourceContext resourceContext;

    /**
     * Inspect a CSV instance and return a Document object.
     * @param requestIn request
     * @return {@link CsvInspectionResponse}
     * @throws IOException unexpected error
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/inspect")
    @Operation(summary = "Inspect CSV", description = "Inspect a CSV instance and return a Document object")
    @RequestBody(description = "JsonInspectionRequest object",  content = @Content(schema = @Schema(implementation = CsvInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CsvInspectionResponse.class)),
        description = "Return a Document object"))
    public Response inspect(InputStream requestIn) throws IOException  {
        long startTime = System.currentTimeMillis();

        CsvInspectionRequest request = fromJson(requestIn, CsvInspectionRequest.class);
        Map<String,String> options = request.getOptions();
        CsvInspectionResponse response = new CsvInspectionResponse();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Options: {}", options);
            }
            CsvConfig csvConfig = CsvConfig.newConfig(options);
            CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
            csvFieldReader.setDocument(new ByteArrayInputStream(request.getCsvData().getBytes()));

            Document document = csvFieldReader.readSchema();
            response.setCsvDocument(document);
        } catch (Exception e) {
            LOG.error("Error inspecting CSV: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(("Response: {}" + new ObjectMapper().writeValueAsString(response)));
        }
        return Response.ok().entity(toJson(response)).build();
    }

    @Override
    public Field getField(String path, boolean recursive) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Field> searchFields(String keywords) {
        // TODO Auto-generated method stub
        return null;
    }
}
