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
package io.atlasmap.xml.service;

import java.io.File;
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

import io.atlasmap.v2.Json;
import io.atlasmap.xml.inspect.XmlInspectionService;
import io.atlasmap.xml.v2.InspectionType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlInspectionRequest;
import io.atlasmap.xml.v2.XmlInspectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/xml/")
public class XmlService {

    private static final Logger LOG = LoggerFactory.getLogger(XmlService.class);

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
    @Operation(summary = "Simple", description = "Simple hello service")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "Return a response"))
    public String simpleHelloWorld(@QueryParam("from") String from) {
        return "Got it! " + from;
    }


    @GET
    @Path("/inspect")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Inspect XML via URI", description = "Inspect a XML schema or instance located at specified URI and return a Document object")
    @RequestBody(description = "Inspection type, one of `instance` or `Schema`", content = @Content(schema = @Schema(implementation = InspectionType.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = XmlDocument.class)), description = "Return a Document object represented by XmlDocument"))
    public Response getClass(@QueryParam("uri") String uri, @QueryParam("type") String type) {
        XmlDocument d = null;

        try {

            if (type == null) {
                throw new Exception("uri and type parameters must be specified");
            }
            InspectionType inspectType = InspectionType.valueOf(type);
            XmlInspectionService s = new XmlInspectionService();

            switch (inspectType) {
            case INSTANCE:
                d = s.inspectXmlDocument(new File(uri));
                break;
            case SCHEMA:
                d = s.inspectSchema(new File(uri));
                break;
            default:
                throw new Exception("Unknown type specified: " + type);
            }
        } catch (Exception e) {
            LOG.error("Error inspecting xml: " + e.getMessage(), e);
        }

        return Response.ok().entity(toJson(d)).build();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/inspect")
    @Operation(summary = "Inspect XML", description = "Inspect a XML schema or instance and return a Document object")
    @RequestBody(description = "XmlInspectionRequest object", content = @Content(schema = @Schema(implementation = XmlInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200",  content = @Content(schema = @Schema(implementation = XmlInspectionResponse.class)), description = "Return a Document object represented by XmlDocument"))
    public Response inspectClass(InputStream request) {
        return inspectClass(fromJson(request, XmlInspectionRequest.class));
    }

    public Response inspectClass(XmlInspectionRequest request) {
        long startTime = System.currentTimeMillis();

        XmlInspectionResponse response = new XmlInspectionResponse();
        XmlDocument d = null;

        try {

            if (request.getType() == null) {
                response.setErrorMessage("Instance or Schema type must be specified in request");
            } else {
                XmlInspectionService s = new XmlInspectionService();

                switch (request.getType()) {
                case INSTANCE:
                    d = s.inspectXmlDocument(request.getXmlData());
                    break;
                case SCHEMA:
                    d = s.inspectSchema(request.getXmlData());
                    break;
                default:
                    response.setErrorMessage("Unsupported inspection type: " + request.getType());
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error("Error inspecting xml: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        response.setXmlDocument(d);
        return Response.ok().entity(toJson(response)).build();
    }
}
