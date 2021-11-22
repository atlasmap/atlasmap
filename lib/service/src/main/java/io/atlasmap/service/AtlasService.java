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
package io.atlasmap.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasMappingBuilder;
import io.atlasmap.api.AtlasPreviewContext;
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
import io.atlasmap.v2.MappingFileType;
import io.atlasmap.v2.ProcessMappingRequest;
import io.atlasmap.v2.ProcessMappingResponse;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;
import io.atlasmap.v2.Validations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/")
public class AtlasService {

    static final String MAPPING_NAME_PREFIX = "UI.";
    static final String ATLASMAP_ADM_PATH = "atlasmap.adm.path";
    static final String ATLASMAP_WORKSPACE = "atlasmap.workspace";
    private static final Logger LOG = LoggerFactory.getLogger(AtlasService.class);

    private final DefaultAtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    private final AtlasPreviewContext previewContext;

    private String baseFolder = "";
    private String mappingFolder = "";
    private String libFolder = "";
    private AtlasLibraryLoader libraryLoader;

    public AtlasService() throws AtlasException {
        String atlasmapWorkspace = System.getProperty(ATLASMAP_WORKSPACE);
        LOG.debug("AtlasMap backend Working directory: {}", atlasmapWorkspace);
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
                synchronized (atlasContextFactory) {
                    ((DefaultAtlasContextFactory)atlasContextFactory).destroy();
                    ((DefaultAtlasContextFactory)atlasContextFactory).init(libraryLoader);
                }
            }
        });

        String atlasmapAdmPath = System.getProperty(ATLASMAP_ADM_PATH);
        if (atlasmapAdmPath != null && atlasmapAdmPath.length() > 0) {
            LOG.debug("Loading initial ADM file: {}", atlasmapAdmPath);
            this.libraryLoader.clearLibraries();
            ADMArchiveHandler admHandler = new ADMArchiveHandler(this.libraryLoader);
            java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(0));
            admHandler.setPersistDirectory(mappingDirPath);
            admHandler.setIgnoreLibrary(false);
            admHandler.setLibraryDirectory(Paths.get(libFolder));
            admHandler.load(Paths.get(atlasmapAdmPath));
            this.libraryLoader.reload();
            admHandler.persist();
        }

        synchronized (atlasContextFactory) {
            ((DefaultAtlasContextFactory)atlasContextFactory).destroy();
            ((DefaultAtlasContextFactory)atlasContextFactory).init(libraryLoader);
        }
        this.previewContext = atlasContextFactory.createPreviewContext();
    }

    @GET
    @Path("/fieldActions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List FieldActions", description = "Retrieves a list of available field action")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActionDetails.class)) , description = "Return a list of field action detail"))
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
    @Operation(summary = "List Mappings", description = "Retrieves a list of mapping file name saved with specified mappingDefinitionId")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StringMap.class)) , description = "Return a list of a pair of mapping file name and content"))
    public Response listMappingsOld(@Context UriInfo uriInfo, @QueryParam("filter") final String filter)
    {
        return listMappings(uriInfo, filter, 0);
    }

    @GET
    @Path("/mappings/{mappingDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Mappings", description = "Retrieves a list of mapping file name saved with specified mappingDefinitionId")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StringMap.class)) , description = "Return a list of a pair of mapping file name and content"))
    public Response listMappings(@Context UriInfo uriInfo, @QueryParam("filter") final String filter,
                                 @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
        StringMap sMap = new StringMap();
        LOG.debug("listMappings with filter '{}'", filter);

        ADMArchiveHandler handler = loadExplodedMappingDirectory(mappingDefinitionId);
        AtlasMapping map = handler.getMappingDefinition();
        if (map == null) {
            return Response.ok().entity(toJson(sMap)).build();
        }
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
    }

    @Deprecated
    @DELETE
    @Path("/mapping")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove Mapping", description = "Remove a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Specified mapping file was removed successfully"),
        @ApiResponse(responseCode = "204", description = "Mapping file was not found")})
    public Response removeMappingRequestOld() {
        return removeMappingRequest(0);
    }

    @DELETE
    @Path("/mapping/{mappingDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove Mapping", description = "Remove a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Specified mapping file was removed successfully"),
        @ApiResponse(responseCode = "204", description = "Mapping file was not found")})
    public Response removeMappingRequest(@Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {

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
    @Operation(summary = "Remove Mapping by ID", description = "Remove mapping file and catalogs related to specified ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapping file and Catalogs were removed successfully"),
        @ApiResponse(responseCode = "204", description = "Unable to remove mapping file and Catalogs for the specified ID")})
    public Response resetMappingByIdOld()
    {
        return resetMappingById(0);
    }

    @DELETE
    @Path("/mapping/RESET/{mappingDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove Mapping by ID", description = "Remove mapping file and catalogs related to specified ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapping file and Catalogs were removed successfully"),
        @ApiResponse(responseCode = "204", description = "Unable to remove mapping file and Catalogs for the specified ID")})
    public Response resetMappingById(@Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
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
    @Operation(summary =  "Remove All Mappings", description = "Remove all mapping files and catalogs saved on the server")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All mapping files were removed successfully"),
        @ApiResponse(responseCode = "204", description = "Unable to remove all mapping files")})
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
    @Operation(summary = "Remove All User-Defined JAR libraries", description = "Remove all user-defined JAR files saved on the server")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All user-defined JAR files were removed successfully"),
        @ApiResponse(responseCode = "204", description = "Unable to remove all user-defined JAR files")})
    public Response resetUserLibs() {
        LOG.debug("resetUserLibs");
        this.libraryLoader.clearLibraries();
        return Response.ok().build();
    }

    @Deprecated
    @GET
    @Path("/mapping/{mappingFormat}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @Operation(summary = "Get Mapping", description = "Retrieve a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation =  AtlasMapping.class)), description = "Return a mapping file content"),
        @ApiResponse(responseCode = "204", description = "Mapping file was not found"),
        @ApiResponse(responseCode = "500", description = "Mapping file access error")})
    public Response getMappingRequestOld(
      @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat)
    {
        return getMappingRequest(mappingFormat, 0);
    }

    @GET
    @Path("/mapping/{mappingFormat}/{mappingDefinitionId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @Operation(summary = "Get Mapping", description = "Retrieve a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AtlasMapping.class)), description = "Return a mapping file content"),
        @ApiResponse(responseCode = "204", description = "Mapping file was not found"),
        @ApiResponse(responseCode = "500", description = "Mapping file access error")})
    public Response getMappingRequest(
      @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
      @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
        LOG.debug("getMappingRequest: {} '{}'", mappingFormat, mappingDefinitionId);
        ADMArchiveHandler admHandler = loadExplodedMappingDirectory(mappingDefinitionId);

        switch (mappingFormat) {
        case JSON:
            byte[] serialized = null;
            try {
                serialized = admHandler.getMappingDefinitionBytes();
            } catch (Exception e) {
                LOG.error("Error retrieving mapping definition file for ID:" + mappingDefinitionId, e);
                throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
            if (LOG.isDebugEnabled() && serialized != null) {
                LOG.debug(new String(serialized));
            }
            if (serialized == null) {
                LOG.debug("Mapping definition not found for ID:{}", mappingDefinitionId);
                return Response.noContent().build();
            }
            return Response.ok().entity(serialized).build();
        case GZ:
            try {
                if (admHandler.getGzippedADMDigestBytes() == null) {
                    LOG.debug("ADM Digest file not found for ID:{}", mappingDefinitionId);
                    return Response.noContent().build();
                }
                return Response.ok().entity(admHandler.getGzippedADMDigestBytes()).build();
            } catch (Exception e) {
                LOG.error("Error getting compressed ADM digest file.\n" + e.getMessage(), e);
                throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
        case ZIP:
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
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
    @Operation(summary = "Create Mapping", description = "Save a mapping file on the server")
    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Succeeded"),
        @ApiResponse(responseCode = "500", description = "Mapping file save error")})
    public Response createMappingRequestOld(InputStream mapping,
    @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
    @Context UriInfo uriInfo) {
        return createMappingRequest(mapping, mappingFormat, 0, uriInfo);
    }

    @PUT
    @Path("/mapping/{mappingFormat}/{mappingDefinitionId}")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create Mapping", description = "Save a mapping file on the server")
    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Succeeded"),
        @ApiResponse(responseCode = "500", description = "Mapping file save error")})
    public Response createMappingRequest(InputStream mapping,
      @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
      @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
      @Context UriInfo uriInfo) {
        LOG.debug("createMappingRequest (save) with format '{}'", mappingFormat);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        ADMArchiveHandler admHandler = loadExplodedMappingDirectory(mappingDefinitionId);

        switch (mappingFormat) {
        case JSON:
            try {
                admHandler.setMappingDefinitionBytes(mapping);
                admHandler.persist();
                if (admHandler.getMappingDefinition() != null) {
                    builder.path(admHandler.getMappingDefinition().getName());
                }
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
                this.libraryLoader.reload();
                admHandler.persist();
                LOG.debug("  importADMArchiveRequest complete - ID:'{}'", mappingDefinitionId);
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
    @Operation(summary = "Update Mapping", description = "Update existing mapping file on the server")
    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Succeeded"))
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
    @Operation(summary = "Update Mapping", description = "Update existing mapping file on the server")
    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Succeeded"))
    public Response updateMappingRequest(
            InputStream mapping,
            @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Context UriInfo uriInfo) {
        ADMArchiveHandler handler = loadExplodedMappingDirectory(mappingDefinitionId);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        try {
            handler.setMappingDefinitionBytes(mapping);
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
    @Operation(summary = "Validate Mapping", description = "Validate mapping file")
    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Validations.class)), description = "Return a validation result"))
    public Response validateMappingRequestOld(InputStream mapping,
                                           @Context UriInfo uriInfo)
    {
        return validateMappingRequest(mapping, 0, uriInfo);
    }

    @PUT
    @Path("/mapping/validate/{mappingDefinitionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Validate Mapping", description = "Validate mapping file")
    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation =  Validations.class)), description = "Return a validation result"))
    public Response validateMappingRequest(InputStream mapping,
                                           @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
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
    @Operation(summary = "Process Mapping", description = "Process Mapping by feeding input data")
    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProcessMappingResponse.class)), description = "Return a mapping result"),
        @ApiResponse(responseCode = "204", description = "Skipped empty mapping execution") })
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
            audits = previewContext.processPreview(mapping);
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
    @Operation(summary = "Ping", description = "Simple liveness check method used in liveness checks. Must not be protected via authetication.")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "string")), description = "Return 'pong'"))
    public Response ping() {
        LOG.debug("Ping...  responding with 'pong'.");
        return Response.ok().entity(toJson("pong")).build();
    }

    @GET
    @Path("/version")
    @Operation(summary = "Version", description = "Retrieves AtlasMap core library version.")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "string")), description = "Return 'pong'"))
    public Response version() {
        String version = this.atlasContextFactory.getProperties().get(AtlasContextFactory.PROPERTY_ATLASMAP_CORE_VERSION);
        LOG.debug("Answering AtlasMap version: {}", version);
        return Response.ok().entity(toJson(version)).build();
    }

    @GET
    @Path("/library/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Library Classes",
        description = "Retrieves a list of available Java library class names from uploaded JARs.")
    @ApiResponses(@ApiResponse(
        responseCode = "200", content = @Content(schema = @Schema(type = "ArrayList<String>")),
        description = "Return a list of loadable class names"))
    public Response listLibraryClasses(@Context UriInfo uriInfo) {
        ArrayList<String> classNames;
        try {
            classNames = libraryLoader.getLibraryClassNames();
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Library class retrieval error.", e);
            }
            throw new WebApplicationException("Error retrieving class names from uploaded JARs.");
        }
        byte[] serialized = toJson(classNames);
        if (LOG.isDebugEnabled()) {
            LOG.debug(new String(serialized));
        }
        return Response.ok().entity(serialized).build();
    }

    @PUT
    @Path("/library")
    @Operation(summary = "Upload Library", description = "Upload a Java library archive file")
    @Consumes({MediaType.APPLICATION_OCTET_STREAM})
    @ApiResponses(@ApiResponse(
            responseCode = "200", description = "Library upload successful."))
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
            StringBuilder buf = new StringBuilder();
            buf.append("Failed to import a jar file. This error occurs when:\n")
                .append(("\t1. The jar file is not compatible with the JVM AtlasMap backend server is running on\n"))
                .append("\t2. The jar file is broken\n")
                .append("\t3. There is a missing file under META-INF/services, i.e. Java service declaration for custom transformation, custom transformation model, custom mapping builder, etc\n");
            throw new WebApplicationException(buf.toString(), e);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/mappingBuilders")
    @Operation(summary = "List mapping builder classes",
        description = "List mapping builder classes which defines custom mapping logic")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(@ApiResponse(
            responseCode = "200", content = @Content(schema = @Schema(type = "ArrayList<String>")),
            description = "Return a list of loadable class names"))
    public Response listMappingBuilderClasses(@Context UriInfo uriInfo) {
        ArrayList<String> classNames;
        try {
            classNames = libraryLoader.getSubTypesOf(AtlasMappingBuilder.class, false);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Library class retrieval error.", e);
            }
            throw new WebApplicationException("Error retrieving class names from uploaded JARs.");
        }
        byte[] serialized = toJson(classNames);
        if (LOG.isDebugEnabled()) {
            LOG.debug(new String(serialized));
        }
        return Response.ok().entity(serialized).build();
    }

    
    public AtlasLibraryLoader getLibraryLoader() {
        return this.libraryLoader;
    }

    protected Response validateMapping(Integer mappingDefinitionId, AtlasMapping mapping, UriInfo uriInfo) throws IOException, AtlasException {
        AtlasSession session;
        synchronized (atlasContextFactory) {
            AtlasContext context = atlasContextFactory.createContext(mapping);
            session = context.createSession();
            context.processValidation(session);
        }

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
                return Json.withClassLoader(this.libraryLoader).readValue(buf.toString(), clazz);
            }
            return Json.withClassLoader(this.libraryLoader).readValue(value, clazz);
        } catch (IOException e) {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }
    }

    private String getMappingSubDirectory(Integer mappingDefinitionId) {
        return this.mappingFolder + File.separator + mappingDefinitionId;
    }

    private ADMArchiveHandler loadExplodedMappingDirectory(Integer mappingDefinitionId) {
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
        return admHandler;
    }

}
