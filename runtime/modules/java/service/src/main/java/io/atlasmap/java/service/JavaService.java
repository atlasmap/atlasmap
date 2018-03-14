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
package io.atlasmap.java.service;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.ApplicationPath;
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

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.inspect.MavenClasspathHelper;
import io.atlasmap.java.v2.ClassInspectionRequest;
import io.atlasmap.java.v2.ClassInspectionResponse;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.MavenClasspathRequest;
import io.atlasmap.java.v2.MavenClasspathResponse;
import io.atlasmap.v2.Json;

// http://localhost:8585/v2/atlas/java/class?className=java.lang.String

@ApplicationPath("/")
@Path("v2/atlas/java")
public class JavaService {

    private static final Logger LOG = LoggerFactory.getLogger(JavaService.class);

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

    // example from:
    // https://www.mkyong.com/webservices/jax-rs/json-example-with-jersey-jackson/
    @GET
    @Path("/class")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClass(@QueryParam("className") String className) {
        ClassInspectionService classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
        JavaClass c = classInspectionService.inspectClass(className);
        classInspectionService = null;
        return Response.ok().entity(toJson(c)).build();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/mavenclasspath")
    public Response generateClasspath(InputStream requestIn) {
        MavenClasspathRequest request = fromJson(requestIn, MavenClasspathRequest.class);
        MavenClasspathResponse response = new MavenClasspathResponse();
        MavenClasspathHelper mavenClasspathHelper = null;
        try {
            mavenClasspathHelper = new MavenClasspathHelper();
            if (request.getExecuteTimeout() != null) {
                mavenClasspathHelper.setProcessMaxExecutionTime(request.getExecuteTimeout());
            }

            long startTime = System.currentTimeMillis();
            String mavenResponse = mavenClasspathHelper.generateClasspathFromPom(request.getPomXmlData());
            response.setExecutionTime(System.currentTimeMillis() - startTime);
            response.setClasspath(mavenResponse);

        } catch (Exception e) {
            LOG.error("Error generating classpath from maven: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        }

        return Response.ok().entity(toJson(response)).build();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/class")
    public Response inspectClass(InputStream requestIn) {
        ClassInspectionRequest request = fromJson(requestIn, ClassInspectionRequest.class);
        ClassInspectionResponse response = new ClassInspectionResponse();
        ClassInspectionService classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());

        configureInspectionService(classInspectionService, request);

        long startTime = System.currentTimeMillis();
        try {
            JavaClass c = null;
            if (request.getClasspath() == null || request.getClasspath().isEmpty()) {
                c = classInspectionService.inspectClass(request.getClassName());
            } else {
                c = classInspectionService.inspectClass(request.getClassName(), request.getClasspath());
            }
            response.setJavaClass(c);
        } catch (Exception e) {
            LOG.error("Error inspecting class with classpath: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        return Response.ok().entity(toJson(response)).build();
    }

    protected void configureInspectionService(ClassInspectionService classInspectionService,
            ClassInspectionRequest request) {
        if (request.getFieldNameBlacklist() != null && request.getFieldNameBlacklist().getString() != null
                && !request.getFieldNameBlacklist().getString().isEmpty()) {
            classInspectionService.getFieldBlacklist().addAll(request.getFieldNameBlacklist().getString());
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
