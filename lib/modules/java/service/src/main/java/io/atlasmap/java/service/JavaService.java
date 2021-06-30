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
package io.atlasmap.java.service;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.ClassInspectionRequest;
import io.atlasmap.java.v2.ClassInspectionResponse;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.service.AtlasService;
import io.atlasmap.v2.Json;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/java/")
public class JavaService {

    private static final Logger LOG = LoggerFactory.getLogger(JavaService.class);

    @Context
    private ResourceContext resourceContext;

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
    @Operation(summary = "Simple", description ="Simple hello service")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "Return a response"))
    public String simpleHelloWorld(@Parameter(description = "From") @QueryParam("from") String from) {
        return "Got it! " + from;
    }

    @POST
    @Path("/class")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Inspect Class", description ="Inspect a Java Class with specified fully qualified class name and return a Document object")
    @RequestBody(description = "ClassInspectionRequest object", content = @Content(schema = @Schema(implementation = ClassInspectionRequest.class)))
    @ApiResponses(@ApiResponse(
            responseCode = "200", content = @Content(schema = @Schema(implementation = ClassInspectionResponse.class)), description = "Return a Document object represented by JavaClass"))
    public Response inspectClass(InputStream requestIn) {
        ClassInspectionRequest request = fromJson(requestIn, ClassInspectionRequest.class);
        ClassInspectionResponse response = new ClassInspectionResponse();
        ClassInspectionService classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());

        configureInspectionService(classInspectionService, request);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Class inspection request: {}", new String(toJson(request)));
        }

        long startTime = System.currentTimeMillis();
        try {
            JavaClass c = null;
            if (request.getClasspath() == null || request.getClasspath().isEmpty()) {
                AtlasService atlasService = resourceContext.getResource(AtlasService.class);
                c = classInspectionService.inspectClass(
                        atlasService.getLibraryLoader(),
                        request.getClassName(),
                        request.getCollectionType(),
                        request.getCollectionClassName());
            } else {
                c = classInspectionService.inspectClass(request.getClassName(), request.getCollectionType(), request.getClasspath());
            }
            response.setJavaClass(c);
        } catch (Throwable e) {
            String msg = String.format("Error inspecting class %s - %s: %s",
                    request.getClassName(), e.getClass().getName(), e.getMessage());
            LOG.error(msg, e);
            response.setErrorMessage(msg);
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Class inspection response: {}", new String(toJson(response)));
        }
        return Response.ok().entity(toJson(response)).build();
    }

    protected void configureInspectionService(ClassInspectionService classInspectionService,
            ClassInspectionRequest request) {
        if (request.getFieldNameExclusions() != null && request.getFieldNameExclusions().getString() != null
                && !request.getFieldNameExclusions().getString().isEmpty()) {
            classInspectionService.getFieldExclusions().addAll(request.getFieldNameExclusions().getString());
        }

        if (request.isDisablePrivateOnlyFields() != null) {
            classInspectionService.setDisablePrivateOnlyFields(request.isDisablePrivateOnlyFields());
        }

        if (request.isDisableProtectedOnlyFields() != null) {
            classInspectionService.setDisableProtectedOnlyFields(request.isDisableProtectedOnlyFields());
        }

        if (request.isDisablePublicOnlyFields() != null) {
            classInspectionService.setDisablePublicOnlyFields(request.isDisablePublicOnlyFields());
        }

        if (request.isDisablePublicGetterSetterFields() != null) {
            classInspectionService.setDisablePublicGetterSetterFields(request.isDisablePublicGetterSetterFields());
        }

    }
}
