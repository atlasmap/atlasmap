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
package io.atlasmap.json.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.atlasmap.json.inspect.JsonDocumentInspectionService;
import io.atlasmap.json.v2.InspectionType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;
import io.atlasmap.v2.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

// http://localhost:8585/v2/atlas/xml/inspection

@ApplicationPath("/")
@Path("v2/atlas/json")
public class JsonService extends Application {

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

    // example request: http://localhost:8181/rest/myresource?from=jason%20baker
    @GET
    @Path("/simple")
    @Produces(MediaType.TEXT_PLAIN)
    public String simpleHelloWorld(@QueryParam("from") String from) {
        return "Got it! " + from;
    }

    @GET
    @Path("/inspect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClass(@QueryParam("uri") String uri, @QueryParam("type") String type) throws Exception {
        long startTime = System.currentTimeMillis();
        long endTime;
        JsonDocument d = null;

        try {

            if (type == null) {
                throw new Exception("uri and type parameters must be specified");
            } else {
                InspectionType inspectType = InspectionType.valueOf(type);
                JsonDocumentInspectionService s = new JsonDocumentInspectionService();

                switch (inspectType) {
                case INSTANCE:
                    // d = s.inspectXmlDocument(new File(uri)); break;
                case SCHEMA:
                    // d = s.inspectSchema(new File(uri)); break;
                default:
                    throw new Exception("Unknown type specified: " + type);
                }
            }
        } catch (Exception e) {
            LOG.error("Error inspecting xml: " + e.getMessage(), e);
        } finally {
            endTime = System.currentTimeMillis() - startTime;
        }

        return Response.ok().entity(toJson(d)).build();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/inspect")
    public Response inspectClass(InputStream requestIn) throws Exception {
        JsonInspectionRequest request = fromJson(requestIn, JsonInspectionRequest.class);
        long startTime = System.currentTimeMillis();

        JsonInspectionResponse response = new JsonInspectionResponse();
        JsonDocument d = null;

        try {

            if (request.getType() == null || request.getJsonData() == null) {
                response.setErrorMessage(
                        "Json data and Instance or Schema inspection type must be specified in request");
            } else {
                JsonDocumentInspectionService s = new JsonDocumentInspectionService();

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
