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

import java.io.InputStream;
import java.util.List;

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

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.ClassInspectionRequest;
import io.atlasmap.java.v2.ClassInspectionResponse;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.Field;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Java Service provides Java inspection service which generate an AtlasMap Document object from Java class.
 */
@Path("/java/")
public class JavaService extends ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(JavaService.class);

    @Context
    private ResourceContext resourceContext;

    /**
     * Inspects a Java Class with specified fully qualified class name and return a Document object.
     * @param requestIn request
     * @return {@link ClassInspectionResponse}
     */
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

    /**
     * Maps inspection parameters from the request to the inspection service.
     * @param classInspectionService inspection service
     * @param request request
     */
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
