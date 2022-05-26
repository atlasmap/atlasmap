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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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

import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasMappingBuilder;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.core.DefaultAtlasFieldActionService;
import io.atlasmap.service.AtlasLibraryLoader.AtlasLibraryLoaderListener;
import io.atlasmap.v2.ActionDetails;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * {@link AtlasService}, {@link MappingService} and {@link DocumentService} provide core backend REST services which
 * is not specific to the individual data formats.
 * {@link MappingService} handles mappings, {@link DocumentService} handles Documents (data sources in other words), and
 * {@link AtlasService} handles the rest such as field action, library, ADM Digest file and ADM archive file.
 */
@Path("/")
public class AtlasService extends BaseAtlasService {
    /** Mapping name prefix. */
    static final String MAPPING_NAME_PREFIX = "UI.";
    /** The property name for the ADM Archive file to preload. */
    static final String ATLASMAP_ADM_PATH = "atlasmap.adm.path";
    /** The property name for the AtlasMap design time service backend working directory. */
    static final String ATLASMAP_WORKSPACE = "atlasmap.workspace";
    private static final Logger LOG = LoggerFactory.getLogger(AtlasService.class);

    private final DefaultAtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();

    private String baseFolder = "";
    private String mappingFolder = "";
    private String libFolder = "";
    private Map<Integer, SoftReference<ADMArchiveHandler>> admHandlerMap = new ConcurrentHashMap<>();

    /**
     * A constructor.
     * @throws AtlasException unexpected error
     */
    public AtlasService() throws AtlasException {
        String atlasmapWorkspace = System.getProperty(ATLASMAP_WORKSPACE);
        LOG.debug("AtlasMap backend Working directory: {}", atlasmapWorkspace);
        if (atlasmapWorkspace != null && atlasmapWorkspace.length() > 0) {
            baseFolder = atlasmapWorkspace;
        } else {
            baseFolder = "target";
        }

        mappingFolder = baseFolder + File.separator + "mappings";
        libFolder = baseFolder + File.separator + "lib";

        setLibraryLoader(new AtlasLibraryLoader(libFolder));

        // Add atlas-core in case it runs on modular class loader
        getLibraryLoader().addAlternativeLoader(DefaultAtlasFieldActionService.class.getClassLoader());
        getLibraryLoader().addListener(new AtlasLibraryLoaderListener() {
            @Override
            public void onUpdate(AtlasLibraryLoader loader) {
                synchronized (atlasContextFactory) {
                    ((DefaultAtlasContextFactory) atlasContextFactory).destroy();
                    ((DefaultAtlasContextFactory) atlasContextFactory).init(getLibraryLoader());
                }
            }
        });

        String atlasmapAdmPath = System.getProperty(ATLASMAP_ADM_PATH);
        if (atlasmapAdmPath != null && atlasmapAdmPath.length() > 0) {
            LOG.debug("Loading initial ADM file: {}", atlasmapAdmPath);
            getLibraryLoader().clearLibraries();
            ADMArchiveHandler admHandler = new ADMArchiveHandler(getLibraryLoader());
            java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(0));
            admHandler.setPersistDirectory(mappingDirPath);
            admHandler.setIgnoreLibrary(false);
            admHandler.setLibraryDirectory(Paths.get(libFolder));
            admHandler.load(Paths.get(atlasmapAdmPath));
            getLibraryLoader().reload();
            admHandler.persist();
        }

        synchronized (atlasContextFactory) {
            ((DefaultAtlasContextFactory) atlasContextFactory).destroy();
            ((DefaultAtlasContextFactory) atlasContextFactory).init(getLibraryLoader());
        }
    }

    /** Global operations */

    /**
     * Retrieves a list of available field action.
     * @param uriInfo URI info
     * @return {@link ActionDetails} serialized to JSON
     */
    @GET
    @Path("/fieldAction")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List FieldActions", description = "Retrieves a list of available field action")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActionDetails.class)), description = "Return a list of field action detail"))
    public Response listFieldActions(@Context UriInfo uriInfo) {
        ActionDetails details = new ActionDetails();

        if (atlasContextFactory == null || atlasContextFactory.getFieldActionService() == null) {
            return Response.ok().entity(toJson(details)).build();
        }

        details.getActionDetail().addAll(atlasContextFactory.getFieldActionService().listActionDetails());
        byte[] serialized = toJson(details);
        if (LOG.isTraceEnabled()) {
            LOG.trace(new String(serialized));
        }
        return Response.ok().entity(serialized).build();
    }

    /**
     * Retrieves a list of mapping definition names from all existing mapping projects.
     * @param uriInfo URI info
     * @param filter filter
     * @return A list of mapping definition name in {@link StringMap}
     */
    @GET
    @Path("/project")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Mapping Definition names", description = "Retrieves a list of mapping definition names")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StringMap.class)) , description = "Return a list of mapping definition names"))
    public Response listMappingDefinitionNames(@Context UriInfo uriInfo, @QueryParam("filter") final String filter) {
        StringMap sMap = new StringMap();
        LOG.debug("listMappingDefinitionNames - filter is not supported, ignoring '{}'", filter);

        java.nio.file.Path mappingFolderPath = Paths.get(mappingFolder);
        File mappingFolderPathFile = mappingFolderPath.toFile();
        if (mappingFolderPathFile.exists()) {
            for (String id : mappingFolderPathFile.list()) {
                Integer mappingDefinitionId;
                try {
                    mappingDefinitionId = Integer.parseInt(id);
                } catch (Exception e) {
                    continue;
                }
                ADMArchiveHandler handler = getADMArchiveHandler(mappingDefinitionId);
                AtlasMapping map = handler.getMappingDefinition();
                if (map == null) {
                    continue;
                }
                StringMapEntry mapEntry = new StringMapEntry();
                mapEntry.setName(map.getName());
                UriBuilder builder = uriInfo.getBaseUriBuilder().path("v2").path("atlas").path("project")
                        .path(id).path("mapping");
                mapEntry.setValue(builder.build().toString());
                sMap.getStringMapEntry().add(mapEntry);
            }
        }
        byte[] serialized = toJson(sMap);
        if (LOG.isDebugEnabled()) {
            LOG.debug(new String(serialized));
        }
        return Response.ok().entity(serialized).build();
    }

    /**
     * Delete all mapping projects including Mapping Definitions and Documents saved on the server.
     * @return empty response
     */
    @DELETE
    @Path("/project")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete All Mapping projects", description = "Delete all mapping projects including Mapping Definitions and Documents saved on the server")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All mapping projects were deleted successfully"),
            @ApiResponse(responseCode = "204", description = "Unable to delete all mapping projects") })
    public Response deleteAllMappingProjects() {
        LOG.debug("deleteAllMappingProjects");

        java.nio.file.Path mappingFolderPath = Paths.get(mappingFolder);
        File mappingFolderPathFile = mappingFolderPath.toFile();

        if (mappingFolderPathFile == null || !mappingFolderPathFile.exists()) {
            return Response.ok().build();
        }

        AtlasUtil.deleteDirectoryContents(mappingFolderPathFile);
        this.admHandlerMap.clear();
        return Response.ok().build();
    }

    /**
     * Retrieves AtlasMap core library version.
     * @return version
     */
    @GET
    @Path("/version")
    @Operation(summary = "Version", description = "Retrieves AtlasMap core library version.")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "string")), description = "Return 'pong'"))
    public Response version() {
        String version = this.atlasContextFactory.getProperties()
                .get(AtlasContextFactory.PROPERTY_ATLASMAP_CORE_VERSION);
        LOG.debug("Answering AtlasMap version: {}", version);
        return Response.ok().entity(toJson(version)).build();
    }

    /**
     * Uploads a Java library archive file (jar).
     * @param requestIn request
     * @return empty response
     */
    @PUT
    @Path("/library")
    @Operation(summary = "Upload Library", description = "Upload a Java library archive file")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM })
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Library upload successful."))
    public Response uploadLibrary(InputStream requestIn) {
        if (requestIn == null) {
            throw new WebApplicationException("No library file found in request body");
        }

        try {
            getLibraryLoader().addJarFromStream(requestIn);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            StringBuilder buf = new StringBuilder();
            buf.append("Failed to import a jar file. This error occurs when:\n")
                    .append(("\t1. The jar file is not compatible with the JVM which AtlasMap backend server is running on\n"))
                    .append("\t2. The jar file is broken\n")
                    .append("\t3. There is a missing file under META-INF/services, i.e. Java service declaration for custom transformation, custom transformation model, custom mapping builder, etc\n");
            throw new WebApplicationException(buf.toString(), e);
        }
        return Response.ok().build();
    }

    /**
     * Removes all user-defined JAR files saved on the server.
     * @return empty response
     */
    @DELETE
    @Path("/library")
    @Operation(summary = "Remove All User-Defined JAR libraries", description = "Remove all user-defined JAR files saved on the server")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All user-defined JAR files were removed successfully"),
            @ApiResponse(responseCode = "204", description = "Unable to remove all user-defined JAR files") })
    public Response deleteLibraries() {
        LOG.debug("deleteLibraries");
        getLibraryLoader().clearLibraries();
        return Response.ok().build();
    }

    /**
     * Retrieves a list of available Java library class names from uploaded JARs.
     * @param uriInfo URI info
     * @return class names
     */
    @GET
    @Path("/library/class")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Library Classes", description = "Retrieves a list of available Java library class names from uploaded JARs.")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "ArrayList<String>")), description = "Return a list of loadable class names"))
    public Response listLibraryClasses(@Context UriInfo uriInfo) {
        ArrayList<String> classNames;
        try {
            classNames = getLibraryLoader().getLibraryClassNames();
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

    /**
     * List mapping builder classes which defines custom mapping logic.
     * @param uriInfo URI info
     * @return class names
     */
    @GET
    @Path("/library/class/mappingBuilder")
    @Operation(summary = "List mapping builder classes", description = "List mapping builder classes which defines custom mapping logic")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "ArrayList<String>")), description = "Return a list of loadable class names"))
    public Response listMappingBuilderClasses(@Context UriInfo uriInfo) {
        ArrayList<String> classNames;
        try {
            classNames = getLibraryLoader().getSubTypesOf(AtlasMappingBuilder.class, false);
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

    /**
     * Delete all user-defined library JAR files and mapping projects including Mapping Definitions and Documents saved on the server.
     * @return empty response
     */
    @DELETE
    @Path("/all")
    @Operation(summary = "Delete all", description = "Delete all user-defined library JAR files and mapping projects")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All user-defined libarary JARs and mapping projects were deleted successfully"),
        @ApiResponse(responseCode = "204", description = "Unable to delete all user-defined JAR files and mapping projects") })
    public Response deleteAll() {
        LOG.debug("deleteAll");
        getLibraryLoader().clearLibraries();
        deleteAllMappingProjects();
        return Response.ok().build();
    }

    /**
     * Simple liveness check method used in liveness checks. Must not be protected via authetication.
     * @return pong
     */
    @GET
    @Path("/ping")
    @Operation(summary = "Ping", description = "Simple liveness check method used in liveness checks. Must not be protected via authetication.")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "string")), description = "Return 'pong'"))
    public Response ping() {
        LOG.debug("Ping...  responding with 'pong'.");
        return Response.ok().entity(toJson("pong")).build();
    }

    /** Per project operations */

    /**
     * Removes the mapping project including a Mapping Definition and Documents related to specified ID.
     * @param mappingDefinitionId mapping definition ID
     * @return empty response
     */
    @DELETE
    @Path("/project/{mappingDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete Mapping Project by ID", description = "Delete the mapping project including a Mapping Definition and Documents related to specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping project was removed successfully"),
            @ApiResponse(responseCode = "204", description = "Unable to remove a mapping project for the specified ID") })
    public Response deleteMappingProjectById(
            @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
        LOG.debug("deleteMappingProjectById {} ", mappingDefinitionId);

        java.nio.file.Path mappingFolderPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
        File mappingFolderFile = mappingFolderPath.toFile();

        if (mappingFolderFile == null || !mappingFolderFile.exists()) {
            return Response.ok().build();
        }

        if (!mappingFolderFile.isDirectory()) {
            LOG.warn("{} is not a directory - removing anyway", mappingFolderFile.getAbsolutePath());
        }
        AtlasUtil.deleteDirectory(mappingFolderFile);
        admHandlerMap.remove(mappingDefinitionId);
        return Response.ok().build();
    }

    /**
    * Retrieve an ADM file saved on the server.
    * @param mappingDefinitionId mapping definition ID
    * @return file
    */
    @GET
    @Path("/project/{mappingDefinitionId}/adm")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @Operation(summary = "Get Mapping", description = "Retrieve a mapping file saved on the server")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "binary"), mediaType = MediaType.APPLICATION_OCTET_STREAM), description = "Return an ADM file content"),
            @ApiResponse(responseCode = "204", description = "ADM file was not found"),
            @ApiResponse(responseCode = "500", description = "ADM file access error") })
    public Response getADMRequest(
            @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
        LOG.debug("getMappingRequest: {}", mappingDefinitionId);
        ADMArchiveHandler admHandler = getADMArchiveHandler(mappingDefinitionId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            admHandler.setIgnoreLibrary(false);
            admHandler.setLibraryDirectory(Paths.get(this.libFolder));
            admHandler.export(out);
            return Response.ok().entity(out.toByteArray()).build();
        } catch (Exception e) {
            LOG.error("Error getting ADM archive file.\n" + e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Saves an ADM archive file on the server.
     * @param mapping request payload
     * @param mappingDefinitionId mapping definition ID
     * @param uriInfo URI info
     * @return empty response
     */
    @PUT
    @Path("/project/{mappingDefinitionId}/adm")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Import ADM archive", description = "Import an ADM archive file on the server")
    @RequestBody(description = "ADM archive file content", content = @Content(schema = @Schema(type = "binary"), mediaType = MediaType.APPLICATION_OCTET_STREAM))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Succeeded"),
            @ApiResponse(responseCode = "500", description = "ADM archive file import error") })
    public Response importADMArchiveRequest(InputStream mapping,
            @Parameter(description = "Mapping definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Context UriInfo uriInfo) {
        LOG.debug("importADMArchiveRequest with definition ID '{}'", mappingDefinitionId);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        ADMArchiveHandler admHandler = getADMArchiveHandler(mappingDefinitionId);

        LOG.debug("  importADMArchiveRequest - ID:'{}'", mappingDefinitionId);
        try {
            admHandler.setIgnoreLibrary(false);
            admHandler.setLibraryDirectory(Paths.get(libFolder));
            admHandler.load(mapping);
            admHandler.persist();
            LOG.debug("  importADMArchiveRequest complete - ID:'{}'", mappingDefinitionId);
        } catch (Exception e) {
            LOG.error("Error importing ADM archive.\n" + e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                mapping.close();
            } catch (IOException e) {}
        }
        builder.path("atlasmap-" + mappingDefinitionId + ".adm");
        return Response.ok().location(builder.build()).build();
    }

    String getMappingSubDirectory(Integer mappingDefinitionId) {
        return this.mappingFolder + File.separator + mappingDefinitionId;
    }

    public ADMArchiveHandler getADMArchiveHandler(Integer mappingDefinitionId) {
        SoftReference<ADMArchiveHandler> handlerRef = this.admHandlerMap.get(mappingDefinitionId);
        if (handlerRef != null && handlerRef.get() != null) {
            return handlerRef.get();
        }
        java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
        File mappingDirFile = mappingDirPath.toFile();
        if (!mappingDirFile.exists()) {
            mappingDirFile.mkdirs();
        }

        ADMArchiveHandler admHandler = new ADMArchiveHandler(getLibraryLoader());
        admHandler.setIgnoreLibrary(true);
        try {
            admHandler.load(mappingDirPath);
        } catch (Exception e) {
            LOG.error("Unexpected error while loading mapping directory.\n" + e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
        this.admHandlerMap.put(mappingDefinitionId, new SoftReference<>(admHandler));
        return admHandler;
    }

    DefaultAtlasContextFactory getContextFactory() {
        return this.atlasContextFactory;
    }
}
