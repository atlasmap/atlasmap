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

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.ActionDetails;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;
import io.atlasmap.v2.Validations;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationPath("/")
@Path("v2/atlas")
public class AtlasService extends Application {

    private static final Logger logger = LoggerFactory.getLogger(AtlasService.class);
    final Application javaServiceApp;
    final DefaultAtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    private String baseFolder = "target/mappings";

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN = "*";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS = "Content-Type";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String DEFAULT_ACCESS_CONTROL_ALLOW_METHODS = "GET,PUT,POST,PATCH,DELETE,OPTIONS,HEAD";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_GPPPD = "GET,PUT,POST,PATCH,DELETE";

    public AtlasService() {
        javaServiceApp = new ResourceConfig().register(JacksonFeature.class);
    }

    protected Response standardCORSResponse() {
        return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                .header(ACCESS_CONTROL_ALLOW_METHODS, DEFAULT_ACCESS_CONTROL_ALLOW_METHODS).build();
    }

    @OPTIONS
    @Path("/mapping")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingOptions() {
        return standardCORSResponse();
    }

    @OPTIONS
    @Path("/mapping/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingValidateOptions() {
        return standardCORSResponse();
    }

    @OPTIONS
    @Path("/mapping/validate/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingValidateParameterizedOptions() {
        return standardCORSResponse();
    }

    @OPTIONS
    @Path("/fieldMapping/converterCheck")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFieldMappingConverterCheckOptions() {
        return standardCORSResponse();
    }

    @OPTIONS
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingParameterizedOptions() {
        return standardCORSResponse();
    }

    @OPTIONS
    @Path("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingsOptions() {
        return standardCORSResponse();
    }

    @OPTIONS
    @Path("/fieldActions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFieldActionsOptions() {
        return standardCORSResponse();
    }

    @GET
    @Path("/fieldActions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFieldActions(@Context UriInfo uriInfo) {
        ActionDetails details = new ActionDetails();

        if (atlasContextFactory == null || atlasContextFactory.getFieldActionService() == null) {
            return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                    .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                    .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).entity(details).build();
        }

        details.getActionDetail().addAll(atlasContextFactory.getFieldActionService().listActionDetails());
        return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).entity(details).build();
    }

    @GET
    @Path("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMappings(@Context UriInfo uriInfo, final @QueryParam("filter") String filter) {
        StringMap sMap = new StringMap();

        java.nio.file.Path mappingFolder = Paths.get(baseFolder);
        File[] mappings = mappingFolder.toFile().listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (filter != null && name != null && !name.toLowerCase().contains(filter.toLowerCase())) {
                    return false;
                }
                return (name != null ? name.matches("atlasmapping-[a-zA-Z0-9\\.\\-]+.xml") : false);
            }
        });

        if (mappings == null) {
            return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                    .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                    .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).entity(sMap).build();
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
            throw new WebApplicationException(e.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).entity(sMap).build();
    }

    @DELETE
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeMappingRequest(@PathParam("mappingId") String mappingId) {

        java.nio.file.Path mappingFilePath = Paths
                .get(baseFolder + File.separator + generateMappingFileName(mappingId));
        File mappingFile = mappingFilePath.toFile();

        if (!mappingFile.exists()) {
            return Response.noContent().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                    .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                    .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).build();
        }

        if (mappingFile != null && !mappingFile.delete()) {
            logger.warn("Unable to delete mapping file " + mappingFile.toString());
        }

        return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).build();
    }

    @GET
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingRequest(@PathParam("mappingId") String mappingId) {

        java.nio.file.Path mappingFilePath = Paths
                .get(baseFolder + File.separator + generateMappingFileName(mappingId));
        File mappingFile = mappingFilePath.toFile();

        if (!mappingFile.exists()) {
            return Response.noContent().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                    .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                    .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).build();
        }

        AtlasMapping atlasMapping = null;
        try {
            atlasMapping = getMappingFromFile(mappingFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Error retrieving mapping " + e.getMessage(), e);
        }

        return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).entity(atlasMapping).build();
    }

    @PUT
    @Path("/mapping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMappingRequest(AtlasMapping mapping, @Context UriInfo uriInfo) {
        return saveMapping(mapping, uriInfo);
    }

    @POST
    @Path("/mapping/{mappingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMappingRequest(AtlasMapping mapping, @Context UriInfo uriInfo) {
        return saveMapping(mapping, uriInfo);
    }

    @PUT
    @Path("/mapping/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateMappingRequest(AtlasMapping mapping, @Context UriInfo uriInfo) {
        try {
            return validateMapping(mapping, uriInfo);
        } catch (AtlasException | IOException e) {
            throw new WebApplicationException(e.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/fieldMapping/converterCheck")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processConverterCheckRequest(Mapping mapping, @Context UriInfo uriInfo) {
        return converterCheck(mapping, uriInfo);
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
            logger.warn("Failed to deleting temporary file: "
                    + (temporaryMappingFile != null ? temporaryMappingFile.toString() : null));
        }

        return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).entity(validations).build();
    }

    protected Response converterCheck(Mapping mapping, UriInfo uriInfo) {

        if (mapping == null) {
            throw new WebApplicationException("Mapping must be specified", Status.BAD_REQUEST);
        }

        AtlasConversionService conversionService = atlasContextFactory.getConversionService();

        Validations validations = new Validations();
        List<Field> inputFields = new ArrayList<Field>();
        List<Field> outputFields = new ArrayList<Field>();

        for (Field f : mapping.getInputField()) {
            inputFields.add(f);
        }

        for (Field f : mapping.getOutputField()) {
            inputFields.add(f);
        }

        // if(mapping.get)
        // throw new AtlasException("Unsupported mapping type: " +
        // mapping.getClass().getName());
        // }

        if (inputFields == null || inputFields.isEmpty() || outputFields == null || outputFields.isEmpty()) {
            throw new WebApplicationException(
                    "Must have one of inputField(s) and outputField(s) in order to check for available converter",
                    Status.BAD_REQUEST);
        }

        FieldType inputType = null;
        FieldType outputType = null;

        for (Field inputField : inputFields) {
            if (inputField instanceof JavaField) {
                inputType = ((JavaField) inputField).getFieldType();
            }

            for (Field outputField : outputFields) {
                if (outputField instanceof JavaField) {
                    outputType = ((JavaField) outputField).getFieldType();
                }

                Optional<AtlasConverter> optionalConverter = conversionService.findMatchingConverter(inputType,
                        outputType);
                if (optionalConverter.isPresent()) {
                    AtlasConverter converter = optionalConverter.get();
                    // TODO: return "ok"
                } else {
                    // TODO: return "Converter needed"
                }

            }
        }

        return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).entity(validations).build();
    }

    protected Response saveMapping(AtlasMapping mapping, UriInfo uriInfo) {
        try {
            saveMappingToFile(mapping);
        } catch (Exception e) {
            logger.error("Error saving mapping " + mapping.getName() + " to file: " + e.getMessage(), e);
        }

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(mapping.getName());

        return Response.ok().header(ACCESS_CONTROL_ALLOW_ORIGIN, DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN)
                .header(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS)
                .header(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS_GPPPD).location(builder.build())
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

    protected void saveMappingToFile(AtlasMapping atlasMapping) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext
                .newInstance("io.atlasmap.v2:io.atlasmap.java.v2:io.atlasmap.xml.v2:io.atlasmap.json.v2");
        Marshaller marshaller = null;

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.marshal(atlasMapping, createMappingFile(atlasMapping.getName()));
    }

    private File createMappingFile(String mappingName) {
        File dir = new File(baseFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = baseFolder + File.separator + generateMappingFileName(mappingName);
        logger.debug("Creating mapping file '{}'", fileName);
        return new File(fileName);
    }

    protected String generateMappingFileName(String mappingName) {
        return String.format("atlasmapping-%s.xml", mappingName);
    }
}