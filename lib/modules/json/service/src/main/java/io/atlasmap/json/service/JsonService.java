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

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.atlasmap.json.inspect.JsonInspectionService;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;
import io.atlasmap.v2.Json;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/json/")
public class JsonService {

    private static final Logger LOG = LoggerFactory.getLogger(JsonService.class);

    protected byte[] toJson(Object value) {
        try {
            return Json.mapper().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected <T> T fromJson(InputStream value, Class<T>clazz) {
        try {
            return Json.mapper().readValue(value, clazz);
        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("/simple")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary ="Simple", description = "Simple hello service")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "Return a response"))
    public String simpleHelloWorld(@Parameter(description = "From") @QueryParam("from") String from) {
        return "Got it! " + from;
    }

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

            if (request.getType() == null || request.getJsonData() == null) {
                response.setErrorMessage(
                        "Json data and Instance or Schema inspection type must be specified in request");
            } else {
                JsonInspectionService s = new JsonInspectionService();

                String jsonData = cleanJsonData(request.getJsonData());
                if (!validJsonData(jsonData)) {
                    response.setErrorMessage("Invalid json payload specified");
                } else {
                    switch (request.getType()) {
                    case INSTANCE:
                        d = s.inspectJsonDocument(jsonData);
                        break;
                    case SCHEMA:
                        d = s.inspectJsonSchema(jsonData);
                        break;
                    default:
                        response.setErrorMessage("Unsupported inspection type: " + request.getType());
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

        response.setJsonDocument(d);
        return Response.ok().entity(toJson(response)).build();
    }

    protected boolean validJsonData(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return false;
        }

        return jsonData.startsWith("{") || jsonData.startsWith("[");
    }

    protected String cleanJsonData(String jsonData) {
        return jsonData.trim();
    }
}
