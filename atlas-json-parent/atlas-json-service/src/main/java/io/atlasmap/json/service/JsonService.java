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

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.json.inspect.JsonDocumentInspectionService;
import io.atlasmap.json.v2.InspectionType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// http://localhost:8585/v2/atlas/xml/inspection

@ApplicationPath("/")
@Path("v2/atlas/json")
public class JsonService extends Application {

    final Application jsonServiceApp;

    private static final Logger logger = LoggerFactory.getLogger(JsonService.class);

    public JsonService() {
        jsonServiceApp = new ResourceConfig().register(JacksonFeature.class);
    }

    // example request: http://localhost:8181/rest/myresource?from=jason%20baker
    @GET
    @Path("/simple")
    @Produces(MediaType.TEXT_PLAIN)
    public String simpleHelloWorld(@QueryParam("from") String from) {
        return "Got it! " + from;
    }

    @OPTIONS
    @Path("/inspect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testJsonOptions() throws Exception {
        return Response.ok().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE").build();
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
            logger.error("Error inspecting xml: " + e.getMessage(), e);
        } finally {
            endTime = System.currentTimeMillis() - startTime;
        }

        return Response.ok().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE").entity(d).build();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/inspect")
    public Response inspectClass(JsonInspectionRequest request) throws Exception {
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
            logger.error("Error inspecting json: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        response.setJsonDocument(d);
        return Response.ok().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE").entity(response).build();
    }

    protected boolean validJsonData(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return false;
        }

        if (!jsonData.startsWith("{") && !jsonData.startsWith("[")) {
            return false;
        }

        return true;
    }

    protected String cleanJsonData(String jsonData) {
        return jsonData.trim();
    }
}
