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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
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

    private static final Logger LOG = LoggerFactory.getLogger(AtlasService.class);

    private final DefaultAtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    private final AtlasContext defaultContext;

    private String atlasmapCatalogName = "atlasmap-catalog.adm";
    private String baseFolder = "target";
    private String mappingFolder = baseFolder + File.separator + "mappings";
    private String libFolder = baseFolder + File.separator + "lib";
    private AtlasLibraryLoader libraryLoader;

    public AtlasService() throws AtlasException {
        this.defaultContext = atlasContextFactory.createContext(new AtlasMapping());
        this.libraryLoader = new AtlasLibraryLoader(libFolder);
        // Add atlas-core in case it runs on modular class loader
        this.libraryLoader.addAlternativeLoader(DefaultAtlasFieldActionService.class.getClassLoader());
        this.libraryLoader.addListener(new AtlasLibraryLoaderListener() {
            @Override
            public void onUpdate(AtlasLibraryLoader loader) {
                ((DefaultAtlasFieldActionService)atlasContextFactory.getFieldActionService()).init(libraryLoader);
            }
        });
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
        return Response.ok().entity(toJson(details)).build();
    }

    @GET
    @Path("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List Mappings", notes = "Retrieves a list of mapping file name saved on the server")
    @ApiResponses(@ApiResponse(code = 200, response = StringMap.class, message = "Return a list of a pair of mapping file name and content"))
    public Response listMappings(@Context UriInfo uriInfo, @QueryParam("filter") final String filter) {
        StringMap sMap = new StringMap();
        LOG.debug("listMappings with filter '{}'", filter);
        java.nio.file.Path mappingFolderPath = Paths.get(mappingFolder);
        File[] mappings = mappingFolderPath.toFile().listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (filter != null && name != null && !name.toLowerCase().contains(filter.toLowerCase())) {
                    return false;
                }
                return (name != null ? name.matches("atlasmapping-[a-zA-Z0-9\\.\\-]+.xml") : false);
            }
        });

        if (mappings == null) {
            return Response.ok().entity(toJson(sMap)).build();
        }

        try {
            for (File mapping : mappings) {
                AtlasMapping map = getMappingFromFile(mapping.getAbsolutePath());
                StringMapEntry mapEntry = new StringMapEntry();
                mapEntry.setName(map.getName());
                UriBuilder builder = uriInfo.getBaseUriBuilder().path("v2").path("atlas").path("mapping")
                        .path(map.getName());
                mapEntry.setValue(builder.build().toString());
                sMap.getStringMapEntry().add(mapEntry);
            }
        } catch (JAXBException e) {
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok().entity(toJson(sMap)).build();
    }

    @DELETE
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove Mapping", notes = "Remove a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Specified mapping file was removed successfully"),
        @ApiResponse(code = 204, message = "Mapping file was not found")})
    public Response removeMappingRequest(@ApiParam("Mapping ID") @PathParam("mappingId") String mappingId) {

        java.nio.file.Path mappingFilePath = Paths
                .get(mappingFolder + File.separator + generateMappingFileName(mappingId));
        File mappingFile = mappingFilePath.toFile();

        if (!mappingFile.exists()) {
            return Response.noContent().build();
        }

        if (mappingFile != null && !mappingFile.delete()) {
            LOG.warn("Unable to delete mapping file " + mappingFile.toString());
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("/mapping/RESET")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove All Mappings", notes = "Remove all mapping files saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, message = "All mapping files were removed successfully"),
        @ApiResponse(code = 204, message = "Unable to remove all mapping files")})
    public Response resetMappings() {
        LOG.debug("resetMappings");

        java.nio.file.Path mappingFolderPath = Paths.get(mappingFolder);
        File[] mappings = mappingFolderPath.toFile().listFiles();

        if (mappings == null) {
            return Response.ok().build();
        }

        try {
            for (File mappingFile : mappings) {
                if (mappingFile.exists()) {
                    if (!mappingFile.delete()) {
                        LOG.warn("Unable to delete mapping file " + mappingFile.toString());
                    }
                }
            }
        } catch (Exception e) {
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/mapping/{mappingFormat}/{mappingId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Get Mapping", notes = "Retrieve a mapping file saved on the server")
    @ApiResponses({
        @ApiResponse(code = 200, response = AtlasMapping.class, message = "Return a mapping file content"),
        @ApiResponse(code = 204, message = "Mapping file was not found")})
    public Response getMappingRequest(
      @ApiParam("Mapping Format") @PathParam("mappingFormat") String mappingFormat,
      @ApiParam("Mapping ID") @PathParam("mappingId") String mappingId) {
        File mappingFile = getMappingFile(mappingFormat, mappingId);

        if (mappingFile == null) {
            return Response.noContent().build();
        }
        String mappingFilePath = mappingFile.getAbsolutePath();
        LOG.debug("getMappingRequest: {} '{}'", mappingFormat, mappingFilePath);
        switch (mappingFormat) {
        case "XML":
            String data = null;
            try {
                data = new String(Files.readAllBytes(mappingFile.toPath()));
            } catch (Exception e) {
                LOG.error("Error retrieving XML mapping " + mappingFilePath, e);
            }
            return Response.ok().entity(data).build();
        case "JSON":
            AtlasMapping atlasMapping = null;
            try {
                atlasMapping = getMappingFromFile(mappingFilePath);
            } catch (Exception e) {
                LOG.error("Error retrieving JSON mapping " + mappingFilePath, e);
            }
            return Response.ok().entity(toJson(atlasMapping)).build();
        case "GZ":
            byte[] binData = null;
            try {
                FileInputStream inputStream = new FileInputStream(mappingFile);
                int length = inputStream.available();
                binData = new byte[length];
                int bytesRead = inputStream.read(binData);
                inputStream.close();
            } catch (Exception e) {
                LOG.error("Error getting compressed mapping documents.\n" + e.getMessage(), e);
            }
            return Response.ok().entity(binData).build();
        default:
            return Response.noContent().build();
        }
    }

    @PUT
    @Path("/mapping/{mappingFormat}")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Mapping", notes = "Save a mapping file on the server")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses(@ApiResponse(code = 200, message = "Succeeded"))
    public Response createMappingRequest(InputStream mapping,
      @ApiParam("Mapping Format") @PathParam("mappingFormat") String mappingFormat,
      @Context UriInfo uriInfo) {
        LOG.debug("createMappingRequest with format '{}'", mappingFormat);
        switch (mappingFormat) {
        case "XML":
           return saveMapping(fromXml(mapping, AtlasMapping.class), uriInfo);
        case "JSON":
           return saveMapping(fromJson(mapping, AtlasMapping.class), uriInfo);
        case "GZ":
            LOG.debug("saveCompressedMappingRequest '{}'", atlasmapCatalogName);
            try {
                createBinaryMappingFile(atlasmapCatalogName, mapping);
            } catch (Exception e) {
                LOG.error("Error saving compressed mapping documents.\n" + e.getMessage(), e);
            }
            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(atlasmapCatalogName);
            return Response.ok().location(builder.build()).build();
        default:
            return Response.noContent().build();
        }
    }

    @POST
    @Path("/mapping/{mappingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Mapping", notes = "Update existing mapping file on the server")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses(@ApiResponse(code = 200, message = "Succeeded"))
    public Response updateMappingRequest(
            InputStream mapping,
            @ApiParam("Mapping ID") @PathParam("mappingId") String mappingId,
            @Context UriInfo uriInfo) {
        return saveMapping(fromJson(mapping, AtlasMapping.class), uriInfo);
    }

    @PUT
    @Path("/mapping/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Validate Mapping", notes = "Validate mapping file")
    @ApiImplicitParams(@ApiImplicitParam(
            name = "mapping", value = "Mapping file content", dataType = "io.atlasmap.v2.AtlasMapping"))
    @ApiResponses(@ApiResponse(code = 200, response = Validations.class, message = "Return a validation result"))
    public Response validateMappingRequest(InputStream mapping, @Context UriInfo uriInfo) {
        try {
            return validateMapping(fromJson(mapping, AtlasMapping.class), uriInfo);
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
            audits = defaultContext.processPreview(mapping);
        } catch (AtlasException e) {
            throw new WebApplicationException("Unable to process mapping preview", e);
        }
        ProcessMappingResponse response = new ProcessMappingResponse();
        response.setMapping(mapping);
        if (audits != null) {
            response.setAudits(audits);
        }
        return Response.ok().entity(toJson(response)).build();
    }

    @GET
    @Path("/ping")
    @ApiOperation(value = "Ping", notes = "Simple liveness check method used in liveness checks. Must not be protected via authetication.")
    @ApiResponses(@ApiResponse(code = 200, response = String.class, message = "Return 'pong'"))
    public String ping() {
        return "pong";
    }

    @POST
    @Path("/library")
    @ApiOperation(value = "Upload Library", notes = "Upload library jar files")
    @Consumes({ "multipart/mixed" })
    @ApiResponses(@ApiResponse(
            code = 200, message = "Succeeded to upload library"))
    public Response uploadLibrary(MultipartInput requestIn) {
        if (requestIn == null || requestIn.getParts() == null || requestIn.getParts().isEmpty()) {
            throw new WebApplicationException("No library file is found in request body");
        }

        for (InputPart part : requestIn.getParts()) {
            MediaType type = part.getMediaType();
            if (!MediaType.APPLICATION_OCTET_STREAM_TYPE.equals(type)) {
                throw new WebApplicationException(String.format("Unsupported media type ''", type.getType()));
            }

            try {
                libraryLoader.addJarFromStream(part.getBody(InputStream.class, null));
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.error("", e);
                }
                throw new WebApplicationException("Could not read file part: " + e.getMessage());
            }
        }
        return Response.ok().build();
    }

    public AtlasLibraryLoader getLibraryLoader() {
        return this.libraryLoader;
    }

    protected Response validateMapping(AtlasMapping mapping, UriInfo uriInfo) throws IOException, AtlasException {

        File temporaryMappingFile = File.createTempFile("atlas-mapping", "xml");
        temporaryMappingFile.deleteOnExit();
        atlasContextFactory.getMappingService().saveMappingAsFile(mapping, temporaryMappingFile);

        AtlasContext context = atlasContextFactory.createContext(temporaryMappingFile.toURI());
        AtlasSession session = context.createSession();
        context.processValidation(session);
        Validations validations = session.getValidations();

        if (session.getValidations() == null) {
            validations = new Validations();
        }

        if (temporaryMappingFile.exists() && !temporaryMappingFile.delete()) {
            LOG.warn("Failed to deleting temporary file: "
                    + (temporaryMappingFile != null ? temporaryMappingFile.toString() : null));
        }

        return Response.ok().entity(toJson(validations)).build();
    }

    protected Response saveMapping(AtlasMapping mapping, UriInfo uriInfo) {
        try {
            saveMappingToFile(mapping);
        } catch (Exception e) {
            LOG.error("Error saving mapping " + mapping.getName() + " to file: " + e.getMessage(), e);
        }

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(mapping.getName());

        return Response.ok().location(builder.build())
                .build();
    }

    public AtlasMapping getMappingFromFile(String fileName) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext
                .newInstance("io.atlasmap.v2:io.atlasmap.java.v2:io.atlasmap.xml.v2:io.atlasmap.json.v2");
        Marshaller marshaller = null;
        Unmarshaller unmarshaller = null;

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        unmarshaller = jaxbContext.createUnmarshaller();

        StreamSource fileSource = new StreamSource(new File(fileName));
        JAXBElement<AtlasMapping> mappingElem = unmarshaller.unmarshal(fileSource, AtlasMapping.class);
        if (mappingElem != null) {
            return mappingElem.getValue();
        }
        return null;
    }

    public AtlasMapping getMappingFromStream(InputStream streamSource) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext
                .newInstance("io.atlasmap.v2:io.atlasmap.java.v2:io.atlasmap.xml.v2:io.atlasmap.json.v2");
        Marshaller marshaller = null;
        Unmarshaller unmarshaller = null;

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        unmarshaller = jaxbContext.createUnmarshaller();

        JAXBElement<AtlasMapping> mappingElem = (JAXBElement<AtlasMapping>) unmarshaller.unmarshal(streamSource);
        if (mappingElem != null) {
            return mappingElem.getValue();
        }
        return null;
    }

    protected void saveMappingToFile(AtlasMapping atlasMapping) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext
                .newInstance("io.atlasmap.v2:io.atlasmap.java.v2:io.atlasmap.xml.v2:io.atlasmap.json.v2");
        Marshaller marshaller = null;

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.marshal(atlasMapping, createMappingFile(atlasMapping.getName()));
    }

    private File createMappingFile(String mappingName) {
        File dir = new File(mappingFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = mappingFolder + File.separator + generateMappingFileName(mappingName);
        LOG.debug("Creating mapping file '{}'", fileName);
        return new File(fileName);
    }

    private void createBinaryMappingFile(String fileName, InputStream mapping) throws IOException {
        File dir = new File(mappingFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = mappingFolder + File.separator + fileName;
        LOG.debug("Creating binary mapping file '{}'", filePath);
        FileOutputStream outputStream = new FileOutputStream(filePath);
        int length = 0;
        byte[] data = null;
        int bytesRead = 0;
        do {
            length = mapping.available();
            if (length <= 0) {
                break;
            }
            data = new byte[length];
            if ((bytesRead = mapping.read(data)) == -1) {
                break;
            }
            outputStream.write(data);
        } while (true);
        outputStream.close();
        mapping.close();
    }

    private File getMappingFile(String mappingFormat, String mappingId) {
        File mappingFile = null;
        java.nio.file.Path mappingFilePath = null;

        if (mappingFormat.equals("JSON") || mappingFormat.equals("XML")) {
            mappingFilePath = Paths.get(mappingFolder + File.separator + generateMappingFileName(mappingId));
        }
        else {
            mappingFilePath = Paths.get(mappingFolder + File.separator + atlasmapCatalogName);
        }

        mappingFile = mappingFilePath.toFile();
        if (!mappingFile.exists()) {
            return null;
        }
        return mappingFile;
    }

    protected String generateMappingFileName(String mappingName) {
        return String.format("atlasmapping-%s.xml", mappingName);
    }

    protected byte[] toJson(Object value) {
        try {
            return Json.mapper().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected <T> T fromJson(InputStream value, Class<T>clazz) {
        try {
            return Json.mapper().readValue(value, clazz);
        } catch (IOException e) {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }
    }

    protected <T> T fromXml(InputStream value, Class<T>clazz) {
        AtlasMapping atlasMapping = null;
        try {
            atlasMapping = getMappingFromStream(value);
        } catch (Exception e) {
            throw new WebApplicationException("Error retrieving mapping " + e.getMessage(), e);
        }
        return (T) atlasMapping;
    }
}
