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

// moved to standalone as a spring controller

//package io.atlasmap.service;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
//import jakarta.ws.rs.Consumes;
//import jakarta.ws.rs.DELETE;
//import jakarta.ws.rs.GET;
//import jakarta.ws.rs.POST;
//import jakarta.ws.rs.PUT;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.PathParam;
//import jakarta.ws.rs.Produces;
//import jakarta.ws.rs.QueryParam;
//import jakarta.ws.rs.core.Context;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import jakarta.ws.rs.core.UriBuilder;
//import jakarta.ws.rs.core.UriInfo;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//
//import io.atlasmap.api.AtlasContext;
//import io.atlasmap.api.AtlasContextFactory;
//import io.atlasmap.api.AtlasException;
//import io.atlasmap.api.AtlasMappingBuilder;
//import io.atlasmap.api.AtlasPreviewContext;
//import io.atlasmap.api.AtlasSession;
//import io.atlasmap.core.ADMArchiveHandler;
//import io.atlasmap.core.AtlasUtil;
//import io.atlasmap.core.DefaultAtlasContextFactory;
//import io.atlasmap.core.DefaultAtlasFieldActionService;
//import io.atlasmap.service.exception.AtlasServiceException;
//import io.atlasmap.v2.ActionDetails;
//import io.atlasmap.v2.AtlasMapping;
//import io.atlasmap.v2.Audits;
//import io.atlasmap.v2.Json;
//import io.atlasmap.v2.Mapping;
//import io.atlasmap.v2.MappingFileType;
//import io.atlasmap.v2.ProcessMappingRequest;
//import io.atlasmap.v2.ProcessMappingResponse;
//import io.atlasmap.v2.StringMap;
//import io.atlasmap.v2.StringMapEntry;
//import io.atlasmap.v2.Validations;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//
//@Path("/")
//public class AtlasService {
//    static final String ATLASMAP_ADM_PATH = "atlasmap.adm.path";
//    static final String ATLASMAP_WORKSPACE = "atlasmap.workspace";
//    private static final Logger LOG = LoggerFactory.getLogger(AtlasService.class);
//    private final DefaultAtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();
//    private final AtlasPreviewContext previewContext;
//    private final AtlasLibraryLoader libraryLoader;
//    private String baseFolder = "";
//    private String mappingFolder = "";
//    private String libFolder = "";
//    public AtlasService() throws AtlasException {
//        String atlasmapWorkspace = System.getProperty(ATLASMAP_WORKSPACE);
//        LOG.info("AtlasMap backend Working directory: {}", atlasmapWorkspace);
//        if (atlasmapWorkspace != null && atlasmapWorkspace.length() > 0) baseFolder = atlasmapWorkspace;
//        else baseFolder = "target";
//        mappingFolder = baseFolder + File.separator + "mappings";
//        libFolder = baseFolder + File.separator + "lib";
//        this.libraryLoader = new AtlasLibraryLoader(libFolder);
//        // Add atlas-core in case it runs on modular class loader
//        this.libraryLoader.addAlternativeLoader(DefaultAtlasFieldActionService.class.getClassLoader());
//        this.libraryLoader.addListener(loader -> {
//            synchronized (atlasContextFactory) {
//                atlasContextFactory.destroy();
//                atlasContextFactory.init(libraryLoader);
//            }
//        });
//        String atlasmapAdmPath = System.getProperty(ATLASMAP_ADM_PATH);
//        if (atlasmapAdmPath != null && atlasmapAdmPath.length() > 0) {
//            LOG.info("Loading initial ADM file: {}", atlasmapAdmPath);
//            this.libraryLoader.clearLibraries();
//            ADMArchiveHandler admHandler = new ADMArchiveHandler(this.libraryLoader);
//            java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(0));
//            admHandler.setPersistDirectory(mappingDirPath);
//            admHandler.setIgnoreLibrary(false);
//            admHandler.setLibraryDirectory(Paths.get(libFolder));
//            admHandler.load(Paths.get(atlasmapAdmPath));
//            this.libraryLoader.reload();
//            admHandler.persist();
//        }
//        synchronized (atlasContextFactory) {
//            atlasContextFactory.destroy();
//            atlasContextFactory.init(libraryLoader);
//        }
//        this.previewContext = atlasContextFactory.createPreviewContext();
//    }
//
//    @GET
//    @Path("/fieldActions")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "List FieldActions", description = "Retrieves a list of available field action")
//    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ActionDetails.class)) , description = "Return a list of field action detail"))
//    public Response listFieldActions(@Context UriInfo uriInfo) {
//        ActionDetails details = new ActionDetails();
//
//        if (atlasContextFactory == null || atlasContextFactory.getFieldActionService() == null) {
//            return Response.ok().entity(toJson(details)).build();
//        }
//
//        details.getActionDetail().addAll(atlasContextFactory.getFieldActionService().listActionDetails());
//        byte[] serialized = toJson(details);
//        if (LOG.isDebugEnabled()) {
//            LOG.info(new String(serialized));
//        }
//        return Response.ok().entity(serialized).build();
//    }
//
//    @Deprecated
//    @GET
//    @Path("/mappings")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "List Mappings", description = "Retrieves a list of mapping file name saved with specified mappingDefinitionId")
//    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StringMap.class)) , description = "Return a list of a pair of mapping file name and content"))
//    public Response listMappingsOld(@Context UriInfo uriInfo, @QueryParam("filter") final String filter)
//    {
//        return listMappings(uriInfo, filter, 0);
//    }
//
//    @GET
//    @Path("/mappings/{mappingDefinitionId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "List Mappings", description = "Retrieves a list of mapping file name saved with specified mappingDefinitionId")
//    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StringMap.class)) , description = "Return a list of a pair of mapping file name and content"))
//    public Response listMappings(@Context UriInfo uriInfo, @QueryParam("filter") final String filter,
//                                 @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
//        StringMap sMap = new StringMap();
//        LOG.info("listMappings with filter '{}'", filter);
//        ADMArchiveHandler handler = loadExplodedMappingDirectory(mappingDefinitionId);
//        AtlasMapping map = handler.getMappingDefinition();
//        if (map == null) return Response.ok().entity(toJson(sMap)).build();
//        StringMapEntry mapEntry = new StringMapEntry();
//        mapEntry.setName(map.getName());
//        UriBuilder builder = uriInfo.getBaseUriBuilder().path("v2").path("atlas").path("mapping").path(map.getName());
//        mapEntry.setValue(builder.build().toString());
//        sMap.getStringMapEntry().add(mapEntry);
//        byte[] serialized = toJson(sMap);
//        if (LOG.isDebugEnabled()) LOG.info(new String(serialized));
//        return Response.ok().entity(serialized).build();
//    }
//
//    @GET
//    @Path("/mappings/{userId}/{mappingDefinitionId}/{mapperType}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "List Mappings", description = "Retrieves a list of mapping file name saved with specified mappingDefinitionId")
//    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = StringMap.class)) , description = "Return a list of a pair of mapping file name and content"))
//    public Response listMappings(@Context UriInfo uriInfo, @QueryParam("filter") final String filter,@Parameter(description = "User ID") @PathParam("userId") String userId,
//                                 @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") String mappingDefinitionId,
//                                 @Parameter(description = "Mapper type") @PathParam("mapperType") String mapperType) {
//        StringMap sMap = new StringMap();
//        LOG.info("listMappings with filter '{}'", filter);
//
//        ADMArchiveHandler handler = loadExplodedMappingDirectory(userId,mappingDefinitionId,mapperType);
//        AtlasMapping map = handler.getMappingDefinition();
//        if (map == null) {
//            return Response.ok().entity(toJson(sMap)).build();
//        }
//        StringMapEntry mapEntry = new StringMapEntry();
//        mapEntry.setName(map.getName());
//        UriBuilder builder = uriInfo.getBaseUriBuilder().path("v2").path("atlas").path("mapping")
//            .path(map.getName());
//        mapEntry.setValue(builder.build().toString());
//        sMap.getStringMapEntry().add(mapEntry);
//
//        byte[] serialized = toJson(sMap);
//        if (LOG.isDebugEnabled()) {
//            LOG.info(new String(serialized));
//        }
//        return Response.ok().entity(serialized).build();
//    }
//    public File fetchAdmForId(String id,String mapperType) throws Exception {
//        if (id == null || id.isEmpty()) {
//            throw new Exception("Id not present");
//        }
//        String filepath = getAdmMappingFilePath(id,mapperType);
//        LOG.info("Adm Filepath for MappingId with mapperType= "+mapperType+" is "+ filepath.toString());
//        File admFile = new File(filepath);
//        if (admFile != null && admFile.exists() && admFile.isFile()) {
//            return admFile;
//        } else {
//            LOG.error("Adm file not found or is not a valid file for ID: " + id);
//            throw new RuntimeException("Adm file not found or is not a valid file for ID: " + id);
//        }
//    }
//    public File fetchAdmForId(String id) throws Exception {
//        if (id == null || id.isEmpty()) {
//            throw new Exception("Id not present");
//        }
//        String folderpath=getServerMappingFolderPath(id);
//        java.nio.file.Path source=Paths.get(folderpath);
//        LOG.info("Adm Folderpath for MappingId with"+ folderpath.toString());
//        File admFile = File.createTempFile("zippedFolder", ".zip");
//        admFile.deleteOnExit();
//
//        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(admFile))) {
//            Files.walk(source)
//                .filter(path -> !Files.isDirectory(path))
//                .forEach(path -> {
//                    ZipEntry zipEntry = new ZipEntry(source.relativize(path).toString().replace("\\", "/"));
//                    try {
//                        zos.putNextEntry(zipEntry);
//                        Files.copy(path, zos);
//                        zos.closeEntry();
//                    } catch (IOException e) {
//                        LOG.error("Failed to export Adms with Id="+id+"   "+e.getMessage());
//                    }
//                });
//        }
//
//        if (admFile != null && admFile.exists() && admFile.isFile()) {
//            return admFile;
//        } else {
//            LOG.error("Adm file not found or is not a valid file for ID: " + id);
//            throw new RuntimeException("Adm file not found or is not a valid file for ID: " + id);
//        }
//    }
//
//    @Deprecated
//    @DELETE
//    @Path("/mapping")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Remove Mapping", description = "Remove a mapping file saved on the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Specified mapping file was removed successfully"),
//        @ApiResponse(responseCode = "204", description = "Mapping file was not found")})
//    public Response removeMappingRequestOld() {
//        return removeMappingRequest(0);
//    }
//
//    @DELETE
//    @Path("/mapping/{mappingDefinitionId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Remove Mapping", description = "Remove a mapping file saved on the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Specified mapping file was removed successfully"),
//        @ApiResponse(responseCode = "204", description = "Mapping file was not found")})
//    public Response removeMappingRequest(@Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
//
//        java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
//        File mappingDirFile = mappingDirPath.toFile();
//
//        if (mappingDirFile == null || !mappingDirFile.exists()) {
//            return Response.noContent().build();
//        }
//
//        if (!mappingDirFile.isDirectory()) {
//            LOG.warn("Removing invalid file '{}' in a persistent directory", mappingDirFile.getAbsolutePath());
//        } else {
//            AtlasUtil.deleteDirectory(mappingDirFile);
//        }
//
//        return Response.ok().build();
//    }
//
//    @DELETE
//    @Path("/mapping/{userId}/{mappingDefinitionId}/{mapperType}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Remove Mapping", description = "Remove a mapping file saved on the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Specified mapping file was removed successfully"),
//        @ApiResponse(responseCode = "204", description = "Mapping file was not found")})
//    public Response removeMappingRequestWithUserId(@Parameter(description = "User ID") @PathParam("userId") String userId,@Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") String mappingDefinitionId, @Parameter(description = "Mapper Type") @PathParam("mapperType") String mapperType) {
//
//        java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(userId,mappingDefinitionId,mapperType));
//        File mappingDirFile = mappingDirPath.toFile();
//
//        if (mappingDirFile == null || !mappingDirFile.exists()) {
//            return Response.noContent().build();
//        }
//
//        if (!mappingDirFile.isDirectory()) {
//            LOG.warn("Removing invalid file '{}' in a persistent directory", mappingDirFile.getAbsolutePath());
//        } else {
//            AtlasUtil.deleteDirectory(mappingDirFile);
//        }
//
//        return Response.ok().build();
//    }
//
//    @Deprecated
//    @DELETE
//    @Path("/mapping/RESET")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Remove Mapping by ID", description = "Remove mapping file and catalogs related to specified ID")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Mapping file and Catalogs were removed successfully"),
//        @ApiResponse(responseCode = "204", description = "Unable to remove mapping file and Catalogs for the specified ID")})
//    public Response resetMappingByIdOld()
//    {
//        return resetMappingById(0);
//    }
//
//    @DELETE
//    @Path("/mapping/RESET/{mappingDefinitionId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Remove Mapping by ID", description = "Remove mapping file and catalogs related to specified ID")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Mapping file and Catalogs were removed successfully"),
//        @ApiResponse(responseCode = "204", description = "Unable to remove mapping file and Catalogs for the specified ID")})
//    public Response resetMappingById(@Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
//        LOG.info("resetMappingById {} ", mappingDefinitionId);
//
//        java.nio.file.Path mappingFolderPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
//        File mappingFolderFile = mappingFolderPath.toFile();
//
//        if (mappingFolderFile == null || !mappingFolderFile.exists()) {
//            return Response.ok().build();
//        }
//
//        if (!mappingFolderFile.isDirectory()) {
//            LOG.warn("{} is not a directory - removing anyway", mappingFolderFile.getAbsolutePath());
//        }
//        AtlasUtil.deleteDirectory(mappingFolderFile);
//        return Response.ok().build();
//    }
//    @DELETE
//    @Path("/mapping/RESET/{userId}/{mappingDefinitionId}/{mapperType}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Remove Mapping by ID", description = "Remove mapping file and catalogs related to specified ID")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Mapping file and Catalogs were removed successfully"),
//        @ApiResponse(responseCode = "204", description = "Unable to remove mapping file and Catalogs for the specified ID")})
//    public Response resetMappingByIdWithUserId(@Parameter(description = "User ID") @PathParam("userId") String userId,@Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") String mappingDefinitionId, @Parameter(description = "Mapper Type") @PathParam("mapperType") String mapperType) {
//        LOG.info("resetMappingById {} ", mappingDefinitionId);
//
//        java.nio.file.Path mappingFolderPath = Paths.get(getMappingSubDirectory(userId,mappingDefinitionId,mapperType));
//        File mappingFolderFile = mappingFolderPath.toFile();
//
//        if (mappingFolderFile == null || !mappingFolderFile.exists()) {
//            return Response.ok().build();
//        }
//
//        if (!mappingFolderFile.isDirectory()) {
//            LOG.warn("{} is not a directory - removing anyway", mappingFolderFile.getAbsolutePath());
//        }
//        if(userId.endsWith("-temp")){
//            try{
//                LOG.info("Deleting mappings with temp user-id");
//                String path = getMappingSubDirectory(userId,mappingDefinitionId,mapperType);
//                String suffix = File.separator + mappingDefinitionId;
//                path = path.substring(0,path.length() - suffix.length());
//                mappingFolderPath = Paths.get(path);
//                mappingFolderFile = mappingFolderPath.toFile();
//
//            }catch (Exception e){
//                LOG.error("Error accessing mapping-id folder");
//                throw new RuntimeException("Error accessing mapping-id folder");
//            }
//            LOG.info("Path to mappingfolder file is: "+mappingFolderFile.getAbsolutePath());
//        }
//        AtlasUtil.deleteDirectory(mappingFolderFile);
//        return Response.ok().build();
//    }
//
//    @DELETE
//    @Path("/mapping/RESET/ALL")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary =  "Remove All Mappings", description = "Remove all mapping files and catalogs saved on the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "All mapping files were removed successfully"),
//        @ApiResponse(responseCode = "204", description = "Unable to remove all mapping files")})
//    public Response resetAllMappings() {
//        LOG.info("resetAllMappings");
//
//        java.nio.file.Path mappingFolderPath = Paths.get(mappingFolder);
//        File mappingFolderPathFile = mappingFolderPath.toFile();
//
//        if (mappingFolderPathFile == null || !mappingFolderPathFile.exists()) {
//            return Response.ok().build();
//        }
//
//        AtlasUtil.deleteDirectoryContents(mappingFolderPathFile);
//        return Response.ok().build();
//    }
//
//    @DELETE
//    @Path("/mapping/resetLibs")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Remove All User-Defined JAR libraries", description = "Remove all user-defined JAR files saved on the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "All user-defined JAR files were removed successfully"),
//        @ApiResponse(responseCode = "204", description = "Unable to remove all user-defined JAR files")})
//    public Response resetUserLibs() {
//        LOG.info("resetUserLibs");
//        this.libraryLoader.clearLibraries();
//        return Response.ok().build();
//    }
//
//    @Deprecated
//    @GET
//    @Path("/mapping/{mappingFormat}")
//    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
//    @Operation(summary = "Get Mapping", description = "Retrieve a mapping file saved on the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation =  AtlasMapping.class)), description = "Return a mapping file content"),
//        @ApiResponse(responseCode = "204", description = "Mapping file was not found"),
//        @ApiResponse(responseCode = "500", description = "Mapping file access error")})
//    public Response getMappingRequestOld(
//        @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat)
//    {
//        return getMappingRequest(mappingFormat, 0);
//    }
//
//    @GET
//    @Path("/mapping/{mappingFormat}/{mappingDefinitionId}")
//    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
//    @Operation(summary = "Get Mapping", description = "Retrieve a mapping file saved on the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AtlasMapping.class)), description = "Return a mapping file content"),
//        @ApiResponse(responseCode = "204", description = "Mapping file was not found"),
//        @ApiResponse(responseCode = "500", description = "Mapping file access error")})
//    public Response getMappingRequest(
//        @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
//        @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
//        LOG.info("getMappingRequest: {} '{}'", mappingFormat, mappingDefinitionId);
//        ADMArchiveHandler admHandler = loadExplodedMappingDirectory(mappingDefinitionId);
//
//        switch (mappingFormat) {
//            case JSON:
//                byte[] serialized = null;
//                try {
//                    serialized = admHandler.getMappingDefinitionBytes();
//                } catch (Exception e) {
//                    LOG.error("Error retrieving mapping definition file for ID:" + mappingDefinitionId, e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//                if (LOG.isDebugEnabled() && serialized != null) {
//                    LOG.info(new String(serialized));
//                }
//                if (serialized == null) {
//                    LOG.info("Mapping definition not found for ID:{}", mappingDefinitionId);
//                    return Response.noContent().build();
//                }
//                return Response.ok().entity(serialized).build();
//            case GZ:
//                try {
//                    if (admHandler.getGzippedADMDigestBytes() == null) {
//                        LOG.info("ADM Digest file not found for ID:{}", mappingDefinitionId);
//                        return Response.noContent().build();
//                    }
//                    return Response.ok().entity(admHandler.getGzippedADMDigestBytes()).build();
//                } catch (Exception e) {
//                    LOG.error("Error getting compressed ADM digest file.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//            case ZIP:
//                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//                    admHandler.setIgnoreLibrary(false);
//                    admHandler.setLibraryDirectory(Paths.get(this.libFolder));
//                    admHandler.export(out);
//                    return Response.ok().entity(out.toByteArray()).build();
//                } catch (Exception e) {
//                    LOG.error("Error getting ADM archive file.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//            default:
//                throw new AtlasServiceException("Unrecognized mapping format: " + mappingFormat);
//        }
//    }
//
//    @GET
//    @Path("/mapping/{mappingFormat}/{userId}/{mappingDefinitionId}/{mapperType}")
//    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
//    @Operation(summary = "Get Mapping", description = "Retrieve a mapping file saved on the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AtlasMapping.class)), description = "Return a mapping file content"),
//        @ApiResponse(responseCode = "204", description = "Mapping file was not found"),
//        @ApiResponse(responseCode = "500", description = "Mapping file access error")})
//    public Response getMappingRequestWithUserId(
//        @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
//        @Parameter(description = "User ID") @PathParam("userId") String userId,
//        @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") String mappingDefinitionId,
//        @Parameter(description = "Mapper Type") @PathParam("mapperType") String mapperType) {
//        LOG.info("getMappingRequest: {} '{}'", mappingFormat, mappingDefinitionId);
//        ADMArchiveHandler admHandler = loadExplodedMappingDirectory(userId,mappingDefinitionId,mapperType);
//
//        switch (mappingFormat) {
//            case JSON:
//                byte[] serialized = null;
//                try {
//                    serialized = admHandler.getMappingDefinitionBytes();
//                } catch (Exception e) {
//                    LOG.error("Error retrieving mapping definition file for ID:" + mappingDefinitionId, e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//                if (LOG.isDebugEnabled() && serialized != null) {
//                    LOG.info(new String(serialized));
//                }
//                if (serialized == null) {
//                    LOG.info("Mapping definition not found for ID:{}", mappingDefinitionId);
//                    return Response.noContent().build();
//                }
//                return Response.ok().entity(serialized).build();
//            case GZ:
//                try {
//                    if (admHandler.getGzippedADMDigestBytes() == null) {
//                        LOG.info("ADM Digest file not found for ID:{}", mappingDefinitionId);
//                        return Response.noContent().build();
//                    }
//                    return Response.ok().entity(admHandler.getGzippedADMDigestBytes()).build();
//                } catch (Exception e) {
//                    LOG.error("Error getting compressed ADM digest file.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//            case ZIP:
//                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//                    admHandler.setIgnoreLibrary(false);
//                    admHandler.setLibraryDirectory(Paths.get(this.libFolder));
//                    admHandler.export(out);
//                    return Response.ok().entity(out.toByteArray()).build();
//                } catch (Exception e) {
//                    LOG.error("Error getting ADM archive file.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//            default:
//                throw new AtlasServiceException("Unrecognized mapping format: " + mappingFormat  );
//        }
//    }
//
//    @Deprecated
//    @PUT
//    @Path("/mapping/{mappingFormat}")
//    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Create Mapping", description = "Save a mapping file on the server")
//    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Succeeded"),
//        @ApiResponse(responseCode = "500", description = "Mapping file save error")})
//    public Response createMappingRequestOld(InputStream mapping,
//                                            @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
//                                            @Context UriInfo uriInfo) {
//        return createMappingRequest(mapping, mappingFormat, 0, uriInfo);
//    }
//
//    @PUT
//    @Path("/mapping/{mappingFormat}/{mappingDefinitionId}")
//    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Create Mapping", description = "Save a mapping file on the server")
//    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Succeeded"),
//        @ApiResponse(responseCode = "500", description = "Mapping file save error")})
//    public Response createMappingRequest(InputStream mapping,
//                                         @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
//                                         @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
//                                         @Context UriInfo uriInfo) {
//        LOG.info("createMappingRequest (save) with format '{}'", mappingFormat);
//        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
//        ADMArchiveHandler admHandler = loadExplodedMappingDirectory(mappingDefinitionId);
//
//        switch (mappingFormat) {
//            case JSON:
//                try {
//                    admHandler.setMappingDefinitionBytes(mapping);
//                    admHandler.persist();
//                    if (admHandler.getMappingDefinition() != null) {
//                        builder.path(admHandler.getMappingDefinition().getName());
//                    }
//                } catch (AtlasException e) {
//                    LOG.error("Error saving Mapping Definition file.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//                return Response.ok().location(builder.build()).build();
//            case GZ:
//                LOG.info("  saveGzippedADMDigestRequest '{}' - ID: {}", admHandler.getGzippedADMDigestFileName(), mappingDefinitionId);
//                try {
//                    admHandler.setGzippedADMDigest(mapping);
//                    admHandler.persist();
//                } catch (AtlasException e) {
//                    LOG.error("Error saving gzipped ADM digest file.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//                builder.path(admHandler.getGzippedADMDigestFileName());
//                return Response.ok().location(builder.build()).build();
//            case ZIP:
//                LOG.info("  importADMArchiveRequest - ID:'{}'", mappingDefinitionId);
//                try {
//                    admHandler.setIgnoreLibrary(false);
//                    admHandler.setLibraryDirectory(Paths.get(libFolder));
//                    admHandler.load(mapping);
//                    this.libraryLoader.reload();
//                    admHandler.persist();
//                    LOG.info("  importADMArchiveRequest complete - ID:'{}'", mappingDefinitionId);
//                } catch (Exception e) {
//                    LOG.error("Error importing ADM archive.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//                builder.path("atlasmap-" + mappingDefinitionId + ".adm");
//                return Response.ok().location(builder.build()).build();
//            case XML:
//                throw new AtlasServiceException("XML mapping format is no longer supported. Please use JSON format instead.");
//            default:
//                throw new AtlasServiceException("Unrecognized mapping format: " + mappingFormat  );
//        }
//    }
//
//    @PUT
//    @Path("/mapping/{mappingFormat}/{userId}/{mappingDefinitionId}/{mapperType}")
//    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Create Mapping", description = "Save a mapping file on the server")
//    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Succeeded"),
//        @ApiResponse(responseCode = "500", description = "Mapping file save error")})
//    public Response createMappingRequestwithUserId(InputStream mapping,
//                                                   @Parameter(description = "Mapping Format") @PathParam("mappingFormat") MappingFileType mappingFormat,
//                                                   @Parameter(description = "User ID") @PathParam("userId") String userId,
//                                                   @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") String mappingDefinitionId,
//                                                   @Parameter(description = "Mapper Type") @PathParam("mapperType") String mapperType,
//                                                   @Context UriInfo uriInfo) {
//        LOG.info("createMappingRequest (save) with format '{}'", mappingFormat);
//        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
//        ADMArchiveHandler admHandler = loadExplodedMappingDirectory(userId,mappingDefinitionId,mapperType);
//        switch (mappingFormat) {
//            case JSON:
//                try {
//                    admHandler.setMappingDefinitionBytes(mapping);
//                    admHandler.persist();
//                    if (admHandler.getMappingDefinition() != null) {
//                        builder.path(admHandler.getMappingDefinition().getName());
//                    }
//                } catch (AtlasException e) {
//                    LOG.error("Error saving Mapping Definition file.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//                return Response.ok().location(builder.build()).build();
//            case GZ:
//                LOG.info("  saveGzippedADMDigestRequest '{}' - ID: {}", admHandler.getGzippedADMDigestFileName(), mappingDefinitionId);
//                try {
//                    admHandler.setGzippedADMDigest(mapping);
//                    admHandler.persist();
//                } catch (AtlasException e) {
//                    LOG.error("Error saving gzipped ADM digest file.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//                builder.path(admHandler.getGzippedADMDigestFileName());
//                return Response.ok().location(builder.build()).build();
//            case ZIP:
//                LOG.info("  importADMArchiveRequest - ID:'{}'", mappingDefinitionId);
//                try {
//                    admHandler.setIgnoreLibrary(false);
//                    admHandler.setLibraryDirectory(Paths.get(libFolder));
//                    admHandler.load(mapping);
//                    this.libraryLoader.reload();
//                    admHandler.persist();
//                    LOG.info("  importADMArchiveRequest complete - ID:'{}'", mappingDefinitionId);
//                } catch (Exception e) {
//                    LOG.error("Error importing ADM archive.\n" + e.getMessage(), e);
//                    throw new AtlasServiceException(e.getMessage());
//                }
//                builder.path("atlasmap-" + mappingDefinitionId + ".adm");
//                return Response.ok().location(builder.build()).build();
//            case XML:
//                throw new AtlasServiceException("XML mapping format is no longer supported. Please use JSON format instead.");
//            default:
//                throw new AtlasServiceException("Unrecognized mapping format: " + mappingFormat  );
//        }
//    }
//
//    @Deprecated
//    @POST
//    @Path("/mapping")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Update Mapping", description = "Update existing mapping file on the server")
//    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
//    @ApiResponses(@ApiResponse(responseCode = "200", description = "Succeeded"))
//    public Response updateMappingRequestOld(
//        InputStream mapping,
//        @Context UriInfo uriInfo)
//    {
//        return updateMappingRequest(mapping, 0, uriInfo);
//    }
//
//    @POST
//    @Path("/mapping/{mappingDefinitionId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Update Mapping", description = "Update existing mapping file on the server")
//    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
//    @ApiResponses(@ApiResponse(responseCode = "200", description = "Succeeded"))
//    public Response updateMappingRequest(
//        InputStream mapping,
//        @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
//        @Context UriInfo uriInfo) {
//        ADMArchiveHandler handler = loadExplodedMappingDirectory(mappingDefinitionId);
//        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
//        try {
//            handler.setMappingDefinitionBytes(mapping);
//            handler.persist();
//            builder.path(handler.getMappingDefinition().getName());
//        } catch (AtlasException e) {
//            LOG.error("Error saving Mapping Definition file.\n" + e.getMessage(), e);
//            throw new AtlasServiceException(e.getMessage());
//        }
//
//        return Response.ok().location(builder.build()).build();
//    }
//
//    @Deprecated
//    @PUT
//    @Path("/mapping/validate")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Validate Mapping", description = "Validate mapping file")
//    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
//    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Validations.class)), description = "Return a validation result"))
//    public Response validateMappingRequestOld(InputStream mapping,
//                                              @Context UriInfo uriInfo)
//    {
//        return validateMappingRequest(mapping, 0, uriInfo);
//    }
//
//    @PUT
//    @Path("/mapping/validate/{mappingDefinitionId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Validate Mapping", description = "Validate mapping file")
//    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
//    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation =  Validations.class)), description = "Return a validation result"))
//    public Response validateMappingRequest(InputStream mapping,
//                                           @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
//                                           @Context UriInfo uriInfo) {
//        try {
//            AtlasMapping atlasMapping = fromJson(mapping, AtlasMapping.class);
//            LOG.info("Validate mappings: {}", atlasMapping.getName());
//            return validateMapping(mappingDefinitionId, atlasMapping, uriInfo);
//        } catch (AtlasException | IOException e) {
//            throw new AtlasServiceException(e.getMessage());
//        }
//    }
//
//    @PUT
//    @Path("/mapping/process")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "Process Mapping", description = "Process Mapping by feeding input data")
//    @RequestBody(description = "Mapping file content", content = @Content(schema = @Schema(implementation = AtlasMapping.class)))
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProcessMappingResponse.class)), description = "Return a mapping result"),
//        @ApiResponse(responseCode = "204", description = "Skipped empty mapping execution") })
//    public Response processMappingRequest(InputStream request, @Context UriInfo uriInfo) {
//        ProcessMappingRequest pmr = fromJson(request, ProcessMappingRequest.class);
//        if (pmr.getAtlasMapping() != null) {
//            throw new AtlasServiceException("Whole mapping execution is not yet supported");
//        }
//        Mapping mapping = pmr.getMapping();
//        if (mapping == null) {
//            return Response.noContent().build();
//        }
//        Audits audits = null;
//        try {
//            if (LOG.isDebugEnabled()) {
//                LOG.info("Preview request: {}", new String(toJson(mapping)));
//            }
//            audits = previewContext.processPreview(mapping);
//        } catch (AtlasException e) {
//            throw new AtlasServiceException("Unable to process mapping preview");
//        }
//        ProcessMappingResponse response = new ProcessMappingResponse();
//        response.setMapping(mapping);
//        if (audits != null) {
//            response.setAudits(audits);
//        }
//        byte[] serialized = toJson(response);
//        if (LOG.isDebugEnabled()) {
//            LOG.info("Preview outcome: {}", new String(serialized));
//        }
//        return Response.ok().entity(serialized).build();
//    }
//
//    @GET
//    @Path("/ping")
//    @Operation(summary = "Ping", description = "Simple liveness check method used in liveness checks. Must not be protected via authetication.")
//    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "string")), description = "Return 'pong'"))
//    public Response ping() {
//        LOG.info("Ping...  responding with 'pong'.");
//        return Response.ok().entity(toJson("pong")).build();
//    }
//
//    @GET
//    @Path("/version")
//    @Operation(summary = "Version", description = "Retrieves AtlasMap core library version.")
//    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "string")), description = "Return 'pong'"))
//    public Response version() {
//        String version = this.atlasContextFactory.getProperties().get(AtlasContextFactory.PROPERTY_ATLASMAP_CORE_VERSION);
//        LOG.info("Answering AtlasMap version: {}", version);
//        return Response.ok().entity(toJson(version)).build();
//    }
//
//    @GET
//    @Path("/library/list")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Operation(summary = "List Library Classes",
//        description = "Retrieves a list of available Java library class names from uploaded JARs.")
//    @ApiResponses(@ApiResponse(
//        responseCode = "200", content = @Content(schema = @Schema(type = "ArrayList<String>")),
//        description = "Return a list of loadable class names"))
//    public Response listLibraryClasses(@Context UriInfo uriInfo) {
//        ArrayList<String> classNames;
//        try {
//            classNames = libraryLoader.getLibraryClassNames();
//        } catch (Exception e) {
//            if (LOG.isDebugEnabled()) {
//                LOG.error("Library class retrieval error.", e);
//            }
//            throw new AtlasServiceException("Error retrieving class names from uploaded JARs.");
//        }
//        byte[] serialized = toJson(classNames);
//        if (LOG.isDebugEnabled()) {
//            LOG.info(new String(serialized));
//        }
//        return Response.ok().entity(serialized).build();
//    }
//
//    @PUT
//    @Path("/library")
//    @Operation(summary = "Upload Library", description = "Upload a Java library archive file")
//    @Consumes({MediaType.APPLICATION_OCTET_STREAM})
//    @ApiResponses(@ApiResponse(
//        responseCode = "200", description = "Library upload successful."))
//    public Response uploadLibrary(InputStream requestIn) {
//        if (requestIn == null) {
//            throw new AtlasServiceException("No library file found in request body");
//        }
//
//        try {
//            libraryLoader.addJarFromStream(requestIn);
//        } catch (Exception e) {
//            if (LOG.isDebugEnabled()) {
//                LOG.error("", e);
//            }
//            StringBuilder buf = new StringBuilder();
//            buf.append("Failed to import a jar file. This error occurs when:\n")
//                .append(("\t1. The jar file is not compatible with the JVM AtlasMap backend server is running on\n"))
//                .append("\t2. The jar file is broken\n")
//                .append("\t3. There is a missing file under META-INF/services, i.e. Java service declaration for custom transformation, custom transformation model, custom mapping builder, etc\n");
//            throw new AtlasServiceException(buf.toString());
//        }
//        return Response.ok().build();
//    }
//
//    @GET
//    @Path("/mappingBuilders")
//    @Operation(summary = "List mapping builder classes",
//        description = "List mapping builder classes which defines custom mapping logic")
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiResponses(@ApiResponse(
//        responseCode = "200", content = @Content(schema = @Schema(type = "ArrayList<String>")),
//        description = "Return a list of loadable class names"))
//    public Response listMappingBuilderClasses(@Context UriInfo uriInfo) {
//        ArrayList<String> classNames;
//        try {
//            classNames = libraryLoader.getSubTypesOf(AtlasMappingBuilder.class, false);
//        } catch (Exception e) {
//            if (LOG.isDebugEnabled()) {
//                LOG.error("Library class retrieval error.", e);
//            }
//            throw new AtlasServiceException("Error retrieving class names from uploaded JARs.");
//        }
//        byte[] serialized = toJson(classNames);
//        if (LOG.isDebugEnabled()) {
//            LOG.info(new String(serialized));
//        }
//        return Response.ok().entity(serialized).build();
//    }
//
//
//    public AtlasLibraryLoader getLibraryLoader() {
//        return this.libraryLoader;
//    }
//
//    protected Response validateMapping(Integer mappingDefinitionId, AtlasMapping mapping, UriInfo uriInfo) throws IOException, AtlasException {
//        AtlasSession session;
//        synchronized (atlasContextFactory) {
//            AtlasContext context = atlasContextFactory.createContext(mapping);
//            session = context.createSession();
//            context.processValidation(session);
//        }
//
//        Validations validations = session.getValidations();
//        if (session.getValidations() == null) {
//            validations = new Validations();
//        }
//
//        return Response.ok().entity(toJson(validations)).build();
//    }
//
//    private byte[] toJson(Object value) {
//        try {
//            return Json.mapper().writeValueAsBytes(value);
//        } catch (JsonProcessingException e) {
//            throw new AtlasServiceException("Json processing exception");
//        }
//    }
//
//    private <T> T fromJson(InputStream value, Class<T>clazz) {
//        try {
//            if (LOG.isDebugEnabled()) {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(value));
//                StringBuffer buf = new StringBuffer();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    buf.append(line);
//                }
//                LOG.info(buf.toString());
//                return Json.withClassLoader(this.libraryLoader).readValue(buf.toString(), clazz);
//            }
//            return Json.withClassLoader(this.libraryLoader).readValue(value, clazz);
//        } catch (IOException e) {
//            throw new AtlasServiceException("Bad json request");
//        }
//    }
//
//    private String getMappingSubDirectory(Integer mappingDefinitionId) {
//        return this.mappingFolder + File.separator + mappingDefinitionId;
//    }
//    private String getMappingSubDirectory(String userId,String mappingDefinitionId,String mapperType) {
//        if(userId == null|| mappingDefinitionId==null){
//            LOG.error("MappingId or UserId is null(Not valid)");
//            throw new RuntimeException("MappingId or UserId is null(Not valid)");
//        }
////        if(isResp){
////            return this.mappingFolder +File.separator + userId + File.separator + mappingDefinitionId +File.separator + "response";
////        }
//        mapperType=mapperType.toLowerCase();
//        return this.mappingFolder +File.separator + userId + File.separator + mappingDefinitionId + File.separator + mapperType;
//    }
//
//    private ADMArchiveHandler loadExplodedMappingDirectory(Integer mappingDefinitionId) {
//        java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(mappingDefinitionId));
//        File mappingDirFile = mappingDirPath.toFile();
//        if (!mappingDirFile.exists()) {
//            mappingDirFile.mkdirs();
//        }
//
//        ADMArchiveHandler admHandler = new ADMArchiveHandler(this.libraryLoader);
//        admHandler.setIgnoreLibrary(true);
//        try {
//            admHandler.load(mappingDirPath);
//        } catch (Exception e) {
//            LOG.error("Unexpected error while loading mapping directory.\n" + e.getMessage(), e);
//            throw new AtlasServiceException(e.getMessage());
//        }
//        return admHandler;
//    }
//    private ADMArchiveHandler loadExplodedMappingDirectory(String userId,String mappingDefinitionId,String mapperType) {
//        java.nio.file.Path mappingDirPath = Paths.get(getMappingSubDirectory(userId,mappingDefinitionId,mapperType));
//        File mappingDirFile = mappingDirPath.toFile();
//        if (!mappingDirFile.exists()) {
//            mappingDirFile.mkdirs();
//        }
//
//        ADMArchiveHandler admHandler = new ADMArchiveHandler(this.libraryLoader);
//        admHandler.setIgnoreLibrary(true);
//        try {
//            admHandler.load(mappingDirPath);
//        } catch (Exception e) {
//            LOG.error("Unexpected error while loading mapping directory.\n" + e.getMessage(), e);
//            throw new RuntimeException("Unexpected error while loading mapping directory "+e.getMessage());
//        }
//        return admHandler;
//    }
//    private String getServerMappingSubDirectory(String mappingDefinitionId,String mapperType) {
//        if(mappingDefinitionId==null){
//            LOG.error("MappingId is null(Not valid)");
//            throw new RuntimeException("MappingId is null(Not valid)");
//        }
//        String baseFolder1 = "src" + File.separator + "main" + File.separator + "resources";
////        if(isResponse){
////            return  baseFolder1 + File.separator + "servermappings"+ File.separator + mappingDefinitionId+ File.separator + "response";
////        }
//        mapperType=mapperType.toLowerCase();
//        return  baseFolder1 + File.separator + "servermappings" + File.separator + mappingDefinitionId + File.separator + mapperType ;
//    }
//    private String getServerMappingFolderPath(String mappingDefinitionId) {
//        if(mappingDefinitionId==null){
//            LOG.error("MappingId is null(Not valid)");
//            throw new RuntimeException("MappingId is null(Not valid)");
//        }
//        String baseFolder1 = "src" + File.separator + "main" + File.separator + "resources";
//        return  baseFolder1 + File.separator + "servermappings" + File.separator + mappingDefinitionId;
//    }
//    private String getAdmMappingFilePath(String mappingDefinitionId,String mapperType){
//        String folderPath= getServerMappingSubDirectory(mappingDefinitionId,mapperType);
//        File folder = new File(folderPath);
//        if (!folder.isDirectory()) {
//            LOG.error("Error: '" + folderPath + "' is not a valid directory.");
//            return null;
//        }
//        File[] contents = folder.listFiles();
//        File foundFile = null;
//        int fileCount = 0;
//        if (contents != null) {
//            for (File item : contents) {
//                if (item.isFile()) {
//                    foundFile = item;
//                    fileCount++;
//                }
//            }
//        }
//        if (fileCount == 1 && foundFile != null) {
//            LOG.info("Found a single Adm: " + foundFile.getName());
//            return folderPath+File.separator+foundFile.getName();
//        } else if (fileCount == 0) {
//            LOG.error("No Adm found directly in the folder '" + folderPath + "'.");
//            return null;
//        } else {
//            LOG.error("Multiple Adm found in the folder '" + folderPath + "'. Cannot Adm a single file without knowing its name.");
//            return null;
//        }
//    }
//    public String fetchFilename(String id,String mapperType){
//        String folderPath= getServerMappingSubDirectory(id,mapperType);
//        File folder = new File(folderPath);
//        if (!folder.isDirectory()) {
//            LOG.error("Error: '" + folderPath + "' is not a valid directory.");
//            return null;
//        }
//        File[] contents = folder.listFiles();
//        File foundFile = null;
//        int fileCount = 0;
//        if (contents != null) {
//            for (File item : contents) {
//                if (item.isFile()) {
//                    foundFile = item;
//                    fileCount++;
//                }
//            }
//        }
//        if (fileCount == 1 && foundFile != null) {
//            LOG.info("Found a single Adm: " + foundFile.getName());
//            return foundFile.getName();
//        } else if (fileCount == 0) {
//            LOG.error("No Adm found directly in the folder: {}", folderPath);
//            return null;
//        } else {
//            LOG.error("Multiple Adm found in the folder: {}",folderPath + "Cannot Adm a single file without knowing its name.");
//            return null;
//        }
//    }
//    public String exportAdmToServer(String userId, String mappingId, String mapperType) {
//        String filePath= getServerMappingSubDirectory(mappingId,mapperType);
//        filePath=filePath+File.separator+mappingId+".adm";
//        File file = new File(filePath);
//        File parentDir = file.getParentFile();
//
//        if (parentDir != null && !parentDir.exists()) {
//            if (!parentDir.mkdirs()) {
//                LOG.error("Creation of parent directory failed: {}", parentDir.getAbsolutePath());
//                throw new RuntimeException("Creation of parent directory failed: "+parentDir.getAbsolutePath());
//            }
//        }
//        LOG.info("getMappingRequest: {} '{}'",mappingId);
//        ADMArchiveHandler admHandler = loadExplodedMappingDirectory(userId,mappingId,mapperType);
//        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//            admHandler.setIgnoreLibrary(false);
//            admHandler.setLibraryDirectory(Paths.get(this.libFolder));
//            admHandler.export(out);
//            try (FileOutputStream fos = new FileOutputStream(file)) {
//                fos.write(out.toByteArray());
//                LOG.info("File saved successfully at: " + file.getAbsolutePath());
//                resetMappingByIdWithUserId(userId,mappingId,mapperType);
//            } catch (IOException e) {
//                LOG.error("Error writing file: "+e.getMessage());
//                throw new RuntimeException("Error writing file: " + e.getMessage());
//            }
//        } catch (Exception e) {
//            LOG.error("Error getting ADM archive file.\n" + e.getMessage(), e);
//            throw new RuntimeException("Error getting ADM archive file: "+e.getMessage());
//        }
//        LOG.info("AdmFilePath: {}",filePath);
//        return filePath;
//    }
//    public void loadAdmFromServer(String userId,String mappingDefinitionId,String mapperType) {
//        String fpath = getMappingSubDirectory(userId,mappingDefinitionId,mapperType);
//        File mappingfilefolder = new File(fpath);
//        if(mappingfilefolder.exists()) LOG.info("MappingFileFolder already exists: {}",mappingfilefolder);
//        else {
//            String filePath= getAdmMappingFilePath(mappingDefinitionId,mapperType);
//            LOG.info("Adm folder found: {}",mappingfilefolder);
//            File file = new File(filePath);
//            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
//                ADMArchiveHandler admHandler = loadExplodedMappingDirectory(userId, mappingDefinitionId,mapperType);
//                LOG.info("ImportADMArchiveRequest - ID:'{}'", mappingDefinitionId);
//                try {
//                    admHandler.setIgnoreLibrary(false);
//                    admHandler.setLibraryDirectory(Paths.get(libFolder));
//                    admHandler.load(inputStream);
//                    this.libraryLoader.reload();
//                    admHandler.persist();
//                    LOG.info("ImportADMArchiveRequest complete - ID:'{}'", mappingDefinitionId);
//                } catch (Exception e) {
//                    LOG.error("Error importing ADM archive: {}", e.getMessage());
//                    throw new AtlasServiceException("Error importing ADM archive: " + e.getMessage());
//                }
//            } catch (Exception e){
//                LOG.error("Error reading ADM archive: {}", e.getMessage());
//                throw new AtlasServiceException("Error reading ADM archive: "+ e.getMessage());
//            }
//        }
//    }
//    @PUT
//    @Path("/loadmapping/{userId}/{mappingDefinitionId}/{mapperType}")
//    @Operation(summary = "Load Mapping", description = "Load a Mapping file from the server")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Succeeded"),
//        @ApiResponse(responseCode = "500", description = "Mapping file creation error")})
//    public Response loadmapping(@Parameter(description = "User ID") @PathParam("userId") String userId,
//                                @Parameter(description = "Mapping ID") @PathParam("mappingDefinitionId") String mappingDefinitionId,
//                                @Parameter(description = "Mapper Type") @PathParam("mapperType") String mapperType ) {
//        try {
//            loadAdmFromServer(userId, mappingDefinitionId, mapperType);
//            return Response.ok().build();
//        } catch (Exception e){
//            throw new AtlasServiceException(e.getMessage());
//        }
//    }
//    public void deleteAdmFromServer( String mappingDefinitionId, String mapperType){
//        String filePath= getAdmMappingFilePath(mappingDefinitionId,mapperType);
//        try {
//
//            String fileToDelete = new File(filePath).getParent();
//            File parentfolder = new File(fileToDelete);
//            try {
//                AtlasUtil.deleteDirectory(parentfolder);
//                LOG.info("AdmFile found at: {}", filePath);
//                LOG.info("File deleted successfully: {}", filePath);
//            }catch (Exception  e){
////                e.printStackTrace();
//                LOG.info("Failed to delete the file or file not present: {}", filePath);
//            }
//
//        } catch (NullPointerException e){
//            LOG.error("Files to be deleted not present, Maybe already deleted.");
//        } catch (Exception e) {
//            LOG.error("Security exception occurred while trying to delete the file: {}", e.getMessage());
//            throw new RuntimeException("Security exception occurred while trying to delete the file: "+e.getMessage());
//        }
//    }
//}
