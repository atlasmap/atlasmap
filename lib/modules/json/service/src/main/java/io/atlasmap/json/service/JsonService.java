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
package io.atlasmap.json.service;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.core.AtlasUtil;
import io.atlasmap.json.inspect.JsonInspectionService;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.Field;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * The JSON Service provides JSON inspection service which generate an AtlasMap Document object from JSON instance or JSON schema.
 */
@Path("/json/")
public class JsonService extends ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(JsonService.class);

    /**
     * Inspect a JSON schema or instance and return a Document object.
     * @param requestIn request
     * @return {@link JsonInspectionResponse}
     */
    @POST
    @Path("/inspect")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary ="Inspect JSON", description = "Inspect a JSON schema or instance and return a Document object")
    @RequestBody(description = "JsonInspectionRequest object",  content = @Content(schema = @Schema(implementation = JsonInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = JsonInspectionResponse.class)), description = "Return a Document object represented by JsonDocument"))
    public Response inspect(InputStream requestIn) {
        JsonInspectionRequest request = fromJson(requestIn, JsonInspectionRequest.class);
        long startTime = System.currentTimeMillis();

        JsonInspectionResponse response = new JsonInspectionResponse();
        JsonDocument d = null;

        try {

            if (request.getInspectionType() == null || request.getJsonData() == null) {
                response.setErrorMessage(
                        "Json data and Instance or Schema inspection type must be specified in request");
            } else {
                JsonInspectionService s = new JsonInspectionService();

                String jsonData = cleanJsonData(request.getJsonData());
                if (!validJsonData(jsonData)) {
                    response.setErrorMessage("Invalid json payload specified");
                } else {
                    switch (request.getInspectionType()) {
                    case INSTANCE:
                        d = s.inspectJsonDocument(jsonData);
                        break;
                    case SCHEMA:
                        d = s.inspectJsonSchema(jsonData);
                        break;
                    default:
                        response.setErrorMessage("Unsupported inspection type: " + request.getInspectionType());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error inspecting json: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        AtlasUtil.excludeNotRequestedFields(d, request.getInspectPaths(), request.getSearchPhrase());

        response.setJsonDocument(d);
        return Response.ok().entity(toJson(response)).build();
    }

    /**
     * Gets if the JSON data is valid.
     * @param jsonData json data
     * @return true if valid, or false
     */
    protected boolean validJsonData(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return false;
        }

        return jsonData.startsWith("{") || jsonData.startsWith("[");
    }

    /**
     * Trims the String.
     * @param jsonData String
     * @return trimmed
     */
    protected String cleanJsonData(String jsonData) {
        return jsonData.trim();
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
