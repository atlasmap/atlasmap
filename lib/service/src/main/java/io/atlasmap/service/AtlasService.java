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
package io.atlasmap.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import io.atlasmap.v2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.core.DefaultAtlasFieldActionService;
import io.atlasmap.service.AtlasLibraryLoader.AtlasLibraryLoaderListener;
import io.atlasmap.v2.ActionDetails;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.ProcessMappingRequest;
import io.atlasmap.v2.ProcessMappingResponse;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;
import io.atlasmap.v2.Validations;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api
@Path("/")
public class AtlasService {

    static final String MAPPING_NAME_PREFIX = "UI.";
    static final String ATLASMAP_ADM_PATH = "atlasmap.adm.path";
    static final String ATLASMAP_WORKSPACE = "atlasmap.workspace";
    private static final Logger LOG = LoggerFactory.getLogger(AtlasService.class);

    private final DefaultAtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    private final AtlasContext defaultContext;

    private String baseFolder = "";
    private String mappingFolder = "";
    private String libFolder = "";
    private AtlasLibraryLoader libraryLoader;

    public AtlasService() throws AtlasException {
        this.defaultContext = atlasContextFactory.createContext(new AtlasMapping());

        String atlasmapWorkspace = System.getProperty(ATLASMAP_WORKSPACE);
        if (atlasmapWorkspace != null && atlasmapWorkspace.length() > 0) {
            baseFolder = atlasmapWorkspace;
        }
        else {
            baseFolder = "target";
        }

        mappingFolder = baseFolder + File.separator + "mappings";
        libFolder = baseFolder + File.separator + "lib";

        this.libraryLoader = new AtlasLibraryLoader(libFolder);

        // Add atlas-core in case it runs on modular class loader
        this.libraryLoader.addAlternativeLoader(DefaultAtlasFieldActionService.class.getClassLoader());
        this.libraryLoader.addListener(new AtlasLibraryLoaderListener() {
            @Override
            public void onUpdate(AtlasLibraryLoader loader) {
                ((DefaultAtlasFieldActionService)atlasContextFactory.getFieldActionService()).init(libraryLoader);
            }
        });

        String atlasmapAdmPath = System.getProperty(ATLASMAP_ADM_PATH);
        if (atlasmapAdmPath != null && atlasmapAdmPath.length() > 0) {
            this.libraryLoader.clearLibaries();
            ADMArchiveHandler admHandler = new ADMArchiveHandler(this.libraryLoader);
            java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(0));
            admHandler.setPersistDirectory(mappingDirPath);
            admHandler.setIgnoreLibrary(false);
            admHandler.setLibraryDirectory(Paths.get(libFolder));
            admHandler.load(Paths.get(atlasmapAdmPath));
            admHandler.persist();
            this.libraryLoader.reload();
            return;
        }

        ((DefaultAtlasFieldActionService)atlasContextFactory.getFieldActionService()).init(libraryLoader);
    }

    @GET
    @Path("/fieldActions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List FieldActions", notes = "Retrieves a list of available field action")
    @ApiResponses(@ApiResponse(code = 200, response = ActionDetails.class, message = "Return a list of field action detail"))
    public Response listFieldActions(@Context UriInfo uriInfo) {
        ActionDetails details = new ActionDetails();

        if (atlasContextFactory == null || atlasContextFactory.getFieldActionService() == null) {
            return Response.ok().entity(toJson(details)).build();
        }

        details.getActionDetail().addAll(atlasContextFactory.getFieldActionService().listActionDetails());
        byte[] serialized = toJson(details);
        if (LOG.isDebugEnabled()) {
            LOG.debug(new String(serialized));
        }
        return Response.ok().entity(serialized).build();
    }

    @Deprecated
    @GET
    @Path("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List Mappings", notes = "Retrieves a list of mapping file name saved with specified mappingDefinitionId")
    @ApiResponses(@ApiResponse(code = 200, response = StringMap.class, message = "Return a list of a pair of mapping file name and content"))
    public Response listMappingsOld(@Context UriInfo uriInfo, @QueryParam("filter") final String filter)
    {
        return listMappings(uriInfo, filter, 0);
    }

    @GET
    @Path("/mappings/{mappingDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List Mappings", notes = "Retrieves a list of mapping file name saved with specified mappingDefinitionId")
    @ApiResponses(@ApiResponse(code = 200, response = StringMap.class, message = "Return a list of a pair of mapping file name and content"))
    public Response listMappings(@Context UriInfo uriInfo, @QueryParam("filter") final String filter,
                                 @ApiParam("Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
        StringMap sMap = new StringMap();
        LOG.debug("listMappings with filter '{}'", filter);
        java.nio.file.Path mappingFolderPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
        if (!mappingFolderPath.toFile().exists() || !mappingFolderPath.toFile().isDirectory()) {
            return Response.ok().entity(toJson(sMap)).build();
        }

        ADMArchiveHandler handler = new ADMArchiveHandler(this.libraryLoader);
        handler.setIgnoreLibrary(true);
        try {
            handler.load(mappingFolderPath);
            AtlasMapping map = handler.getMappingDefinition();
            StringMapEntry mapEntry = new StringMapEntry();
            mapEntry.setName(map.getName());
            UriBuilder builder = uriInfo.getBaseUriBuilder().path("v2").path("atlas").path("mapping")
                .path(map.getName());
            mapEntry.setValue(builder.build().toString());
            sMap.getStringMapEntry().add(mapEntry);

            byte[] serialized = toJson(sMap);
            if (LOG.isDebugEnabled()) {
                LOG.debug(new String(serialized));
            }
            return Response.ok().entity(serialized).build();
        } catch (AtlasException e) {
            LOG.error("Error processing the AtlasMap catalog file " + mappingFolderPath + "\n" +
                e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Deprecated
    @DELETE
    @Path("/mapping")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove Mapping", notes = "Remove a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Specified mapping file was removed successfully"),
        @ApiResponse(code = 204, message = "Mapping file was not found")})
    public Response removeMappingRequestOld() {
        return removeMappingRequest(0);
    }

    @DELETE
    @Path("/mapping/{mappingDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove Mapping", notes = "Remove a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Specified mapping file was removed successfully"),
        @ApiResponse(code = 204, message = "Mapping file was not found")})
    public Response removeMappingRequest(@ApiParam("Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {

        java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
        File mappingDirFile = mappingDirPath.toFile();

        if (mappingDirFile == null || !mappingDirFile.exists()) {
            return Response.noContent().build();
        }

        if (!mappingDirFile.isDirectory()) {
            LOG.warn("Removing invalid file '{}' in a persistent directory", mappingDirFile.getAbsolutePath());
        } else {
            AtlasUtil.deleteDirectory(mappingDirFile);
        }

        return Response.ok().build();
    }

    @Deprecated
    @DELETE
    @Path("/mapping/RESET")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove Mapping by ID", notes = "Remove mapping file and catalogs related to specified ID")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Mapping file and Catalogs were removed successfully"),
        @ApiResponse(code = 204, message = "Unable to remove mapping file and Catalogs for the specified ID")})
    public Response resetMappingByIdOld()
    {
        return resetMappingById(0);
    }

    @DELETE
    @Path("/mapping/RESET/{mappingDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove Mapping by ID", notes = "Remove mapping file and catalogs related to specified ID")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Mapping file and Catalogs were removed successfully"),
        @ApiResponse(code = 204, message = "Unable to remove mapping file and Catalogs for the specified ID")})
    public Response resetMappingById(@ApiParam("Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
        LOG.debug("resetMappingById {} ", mappingDefinitionId);

        java.nio.file.Path mappingFolderPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
        File mappingFolderFile = mappingFolderPath.toFile();

        if (mappingFolderFile == null || !mappingFolderFile.exists()) {
            return Response.ok().build();
        }

        if (!mappingFolderFile.isDirectory()) {
            LOG.warn("{} is not a directory - removing anyway", mappingFolderFile.getAbsolutePath());
        }
        AtlasUtil.deleteDirectory(mappingFolderFile);
        return Response.ok().build();
    }

    @DELETE
    @Path("/mapping/RESET/ALL")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove All Mappings", notes = "Remove all mapping files and catalogs saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, message = "All mapping files were removed successfully"),
        @ApiResponse(code = 204, message = "Unable to remove all mapping files")})
    public Response resetAllMappings() {
        LOG.debug("resetAllMappings");

        java.nio.file.Path mappingFolderPath = Paths.get(mappingFolder);
        File mappingFolderPathFile = mappingFolderPath.toFile();

        if (mappingFolderPathFile == null || !mappingFolderPathFile.exists()) {
            return Response.ok().build();
        }

        AtlasUtil.deleteDirectoryContents(mappingFolderPathFile);
        return Response.ok().build();
    }

    @DELETE
    @Path("/mapping/resetLibs")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove All User-Defined JAR libraries", notes = "Remove all user-defined JAR files saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, message = "All user-defined JAR files were removed successfully"),
        @ApiResponse(code = 204, message = "Unable to remove all user-defined JAR files")})
    public Response resetUserLibs() {
        LOG.debug("resetUserLibs");
        this.libraryLoader.clearLibaries();
        return Response.ok().build();
    }

    @Deprecated
    @GET
    @Path("/mapping/{mappingFormat}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Get Mapping", notes = "Retrieve a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, response = AtlasMapping.class, message = "Return a mapping file content"),
        @ApiResponse(code = 204, message = "Mapping file was not found"),
        @ApiResponse(code = 500, message = "Mapping file access error")})
    public Response getMappingRequestOld(
      @ApiParam("Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat)
    {
        return getMappingRequest(mappingFormat, 0);
    }

    @GET
    @Path("/mapping/{mappingFormat}/{mappingDefinitionId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Get Mapping", notes = "Retrieve a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, response = AtlasMapping.class, message = "Return a mapping file content"),
        @ApiResponse(code = 204, message = "Mapping file was not found"),
        @ApiResponse(code = 500, message = "Mapping file access error")})
    public Response getMappingRequest(
      @ApiParam("Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
      @ApiParam("Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
        LOG.debug("getMappingRequest: {} '{}'", mappingFormat, mappingDefinitionId);
        java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
        File mappingDirFile = mappingDirPath.toFile();

        if (mappingDirFile == null || !mappingDirFile.exists()) {
            LOG.debug("getMappingRequest: {} '{}' not found", mappingFormat, mappingDefinitionId);
            return Response.noContent().build();
        }

        LOG.debug("getMappingRequest: {} '{}'", mappingFormat, mappingDirPath);
        ADMArchiveHandler admHandler = new ADMArchiveHandler(this.libraryLoader);
        admHandler.setIgnoreLibrary(true);

        switch (mappingFormat) {
        case JSON:
            byte[] serialized = null;
            try {
                admHandler.load(mappingDirPath);
                serialized = admHandler.getMappingDefinitionBytes();
            } catch (Exception e) {
                LOG.error("Error retrieving mapping definition file in " + mappingDirPath, e);
                throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(new String(serialized));
            }
            return Response.ok().entity(serialized).build();
        case GZ:
            try {
                admHandler.load(mappingDirPath);
                return Response.ok().entity(admHandler.getGzippedADMDigetBytes()).build();
            } catch (Exception e) {
                LOG.error("Error getting compressed ADM digest file.\n" + e.getMessage(), e);
                throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
        case ZIP:
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                admHandler.load(mappingDirPath);
                admHandler.setIgnoreLibrary(false);
                admHandler.setLibraryDirectory(Paths.get(this.libFolder));
                admHandler.export(out);
                return Response.ok().entity(out.toByteArray()).build();
            } catch (Exception e) {
                LOG.error("Error getting ADM archive file.\n" + e.getMessage(), e);
                throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
        default:
            throw new WebApplicationException("Unrecognized mapping format: " + mappingFormat, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Deprecated
    @PUT
    @Path("/mapping/{mappingFormat}")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Mapping", notes = "Save a mapping file on the server")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses({
        @ApiResponse(code = 200, message = "Succeeded"),
        @ApiResponse(code = 500, message = "Mapping file save error")})
    public Response createMappingRequestOld(InputStream mapping,
    @ApiParam("Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
    @Context UriInfo uriInfo) {
        return createMappingRequest(mapping, mappingFormat, 0, uriInfo);
    }

    @PUT
    @Path("/mapping/{mappingFormat}/{mappingDefinitionId}")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Mapping", notes = "Save a mapping file on the server")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses({
        @ApiResponse(code = 200, message = "Succeeded"),
        @ApiResponse(code = 500, message = "Mapping file save error")})
    public Response createMappingRequest(InputStream mapping,
      @ApiParam("Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
      @ApiParam("Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
      @Context UriInfo uriInfo) {
        LOG.debug("createMappingRequest (save) with format '{}'", mappingFormat);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
        File mappingDirFile = mappingDirPath.toFile();
        if (!mappingDirFile.exists()) {
            mappingDirFile.mkdirs();
        }

        ADMArchiveHandler admHandler = new ADMArchiveHandler(this.libraryLoader);
        admHandler.setIgnoreLibrary(true);
        try {
            admHandler.load(mappingDirPath);
        } catch (Exception e) {
            LOG.error("Unexpected error while loading mapping directory.\n" + e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }

        switch (mappingFormat) {
        case JSON:
            try {
                admHandler.setMappingDefinition(mapping);
                admHandler.persist();
                builder.path(admHandler.getMappingDefinition().getName());
            } catch (AtlasException e) {
                LOG.error("Error saving Mapping Definition file.\n" + e.getMessage(), e);
                throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
            return Response.ok().location(builder.build()).build();
        case GZ:
            LOG.debug("  saveGzippedADMDigestRequest '{}' - ID: {}", admHandler.getGzippedADMDigestFileName(), mappingDefinitionId);
            try {
                admHandler.setGzippedADMDigest(mapping);
                admHandler.persist();
            } catch (AtlasException e) {
                LOG.error("Error saving gzipped ADM digest file.\n" + e.getMessage(), e);
                throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
            builder.path(admHandler.getGzippedADMDigestFileName());
            return Response.ok().location(builder.build()).build();
        case ZIP:
            LOG.debug("  importADMArchiveRequest - ID:'{}'", mappingDefinitionId);
            try {
                admHandler.setIgnoreLibrary(false);
                admHandler.setLibraryDirectory(Paths.get(libFolder));
                admHandler.load(mapping);
                admHandler.persist();
            } catch (Exception e) {
                LOG.error("Error importing ADM archive.\n" + e.getMessage(), e);
                throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
            builder.path("atlasmap-" + mappingDefinitionId + ".adm");
            return Response.ok().location(builder.build()).build();
        case XML:
            throw new WebApplicationException("XML mapping format is no longer supported. Please use JSON format instead.");
        default:
            throw new WebApplicationException("Unrecognized mapping format: " + mappingFormat, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Deprecated
    @POST
    @Path("/mapping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Mapping", notes = "Update existing mapping file on the server")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses(@ApiResponse(code = 200, message = "Succeeded"))
    public Response updateMappingRequestOld(
            InputStream mapping,
            @Context UriInfo uriInfo)
    {
        return updateMappingRequest(mapping, 0, uriInfo);
    }

    @POST
    @Path("/mapping/{mappingDefinitionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Mapping", notes = "Update existing mapping file on the server")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses(@ApiResponse(code = 200, message = "Succeeded"))
    public Response updateMappingRequest(
            InputStream mapping,
            @ApiParam("Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Context UriInfo uriInfo) {
        ADMArchiveHandler handler = new ADMArchiveHandler(this.libraryLoader);
        handler.setIgnoreLibrary(true);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        try {
            handler.setMappingDefinition(mapping);
            handler.persist();
            builder.path(handler.getMappingDefinition().getName());
        } catch (AtlasException e) {
            LOG.error("Error saving Mapping Definition file.\n" + e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok().location(builder.build()).build();
    }

    @Deprecated
    @PUT
    @Path("/mapping/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Validate Mapping", notes = "Validate mapping file")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses(@ApiResponse(code = 200, response = Validations.class, message = "Return a validation result"))
    public Response validateMappingRequest(InputStream mapping,
                                           @Context UriInfo uriInfo)
    {
        return validateMappingRequest(mapping, 0, uriInfo);
    }

    @PUT
    @Path("/mapping/validate/{mappingDefinitionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Validate Mapping", notes = "Validate mapping file")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses(@ApiResponse(code = 200, response = Validations.class, message = "Return a validation result"))
    public Response validateMappingRequest(InputStream mapping,
                                           @ApiParam("Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
                                           @Context UriInfo uriInfo) {
        try {
            AtlasMapping atlasMapping = fromJson(mapping, AtlasMapping.class);
            LOG.debug("Validate mappings: {}", atlasMapping.getName());
            return validateMapping(mappingDefinitionId, atlasMapping, uriInfo);
        } catch (AtlasException | IOException e) {
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/mapping/process")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Process Mapping", notes = "Process Mapping by feeding input data")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "request", value = "ProcessMappingRequest object", dataType = "io.atlasmap.v2.ProcessMappingRequest"))
    @ApiResponses({
        @ApiResponse(code = 200, response = ProcessMappingResponse.class, message = "Return a mapping result"),
        @ApiResponse(code = 204, message = "Skipped empty mapping execution") })
    public Response processMappingRequest(InputStream request, @Context UriInfo uriInfo) {
        ProcessMappingRequest pmr = fromJson(request, ProcessMappingRequest.class);
        if (pmr.getAtlasMapping() != null) {
            throw new WebApplicationException("Whole mapping execution is not yet supported");
        }
        Mapping mapping = pmr.getMapping();
        if (mapping == null) {
            return Response.noContent().build();
        }
        Audits audits = null;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Preview request: {}", new String(toJson(mapping)));
            }
            audits = defaultContext.processPreview(mapping);
        } catch (AtlasException e) {
            throw new WebApplicationException("Unable to process mapping preview", e);
        }
        ProcessMappingResponse response = new ProcessMappingResponse();
        response.setMapping(mapping);
        if (audits != null) {
            response.setAudits(audits);
        }
        byte[] serialized = toJson(response);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Preview outcome: {}", new String(serialized));
        }
        return Response.ok().entity(serialized).build();
    }

    @GET
    @Path("/ping")
    @ApiOperation(value = "Ping", notes = "Simple liveness check method used in liveness checks. Must not be protected via authetication.")
    @ApiResponses(@ApiResponse(code = 200, response = String.class, message = "Return 'pong'"))
    public Response ping() {
	LOG.debug("Ping...  responding with 'pong'.");
	return Response.ok().entity(toJson("pong")).build();
    }

    @PUT
    @Path("/library")
    @ApiOperation(value = "Upload Library", notes = "Upload a Java library archive file")
    @Consumes({MediaType.APPLICATION_OCTET_STREAM})
    @ApiResponses(@ApiResponse(
            code = 200, message = "Library upload successful."))
    public Response uploadLibrary(InputStream requestIn) {
        if (requestIn == null) {
            throw new WebApplicationException("No library file found in request body");
        }

        try {
            libraryLoader.addJarFromStream(requestIn);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            throw new WebApplicationException("Could not read file part: " + e.getMessage());
        }
        return Response.ok().build();
    }

    public AtlasLibraryLoader getLibraryLoader() {
        return this.libraryLoader;
    }

    protected Response validateMapping(Integer mappingDefinitionId, AtlasMapping mapping, UriInfo uriInfo) throws IOException, AtlasException {
        AtlasContext context = atlasContextFactory.createContext(mapping);
        AtlasSession session = context.createSession();
        context.processValidation(session);
        Validations validations = session.getValidations();

        if (session.getValidations() == null) {
            validations = new Validations();
        }

        return Response.ok().entity(toJson(validations)).build();
    }

    private byte[] toJson(Object value) {
        try {
            return Json.mapper().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> T fromJson(InputStream value, Class<T>clazz) {
        try {
            if (LOG.isDebugEnabled()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(value));
                StringBuffer buf = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(line);
                }
                LOG.debug(buf.toString());
                return Json.mapper().readValue(buf.toString(), clazz);
            }
            return Json.mapper().readValue(value, clazz);
        } catch (IOException e) {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }
    }

    private String getMappingSubDirectory(Integer mappingDefinitionId) {
        return this.mappingFolder + File.separator + mappingDefinitionId;
    }

}
