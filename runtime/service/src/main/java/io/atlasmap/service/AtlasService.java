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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.ActionDetails;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;
import io.atlasmap.v2.Validations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
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
import java.io.InputStream;
import java.nio.file.Paths;

@ApplicationPath("/")
@Path("v2/atlas")
public class AtlasService extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasService.class);

    private final DefaultAtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    private String baseFolder = "target/mappings";

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


    @GET
    @Path("/fieldActions")
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response listMappings(@Context UriInfo uriInfo, @QueryParam("filter") final String filter) {
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
    public Response removeMappingRequest(@PathParam("mappingId") String mappingId) {

        java.nio.file.Path mappingFilePath = Paths
                .get(baseFolder + File.separator + generateMappingFileName(mappingId));
        File mappingFile = mappingFilePath.toFile();

        if (!mappingFile.exists()) {
            return Response.noContent().build();
        }

        if (mappingFile != null && !mappingFile.delete()) {
            LOG.warn("Unable to delete mapping file " + mappingFile.toString());
        }

        return Response.ok().build();
    }

    @GET
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingRequest(@PathParam("mappingId") String mappingId) {

        java.nio.file.Path mappingFilePath = Paths
                .get(baseFolder + File.separator + generateMappingFileName(mappingId));
        File mappingFile = mappingFilePath.toFile();

        if (!mappingFile.exists()) {
            return Response.noContent().build();
        }

        AtlasMapping atlasMapping = null;
        try {
            atlasMapping = getMappingFromFile(mappingFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Error retrieving mapping " + e.getMessage(), e);
        }

        return Response.ok().entity(toJson(atlasMapping)).build();
    }

    @PUT
    @Path("/mapping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMappingRequest(InputStream mapping, @Context UriInfo uriInfo) {
        return saveMapping(fromJson(mapping, AtlasMapping.class), uriInfo);
    }

    @POST
    @Path("/mapping/{mappingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMappingRequest(InputStream mapping, @Context UriInfo uriInfo) {
        return saveMapping(fromJson(mapping, AtlasMapping.class), uriInfo);
    }

    @PUT
    @Path("/mapping/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateMappingRequest(InputStream mapping, @Context UriInfo uriInfo) {
        try {
            return validateMapping(fromJson(mapping, AtlasMapping.class), uriInfo);
        } catch (AtlasException | IOException e) {
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Simple liveness check method used in liveness checks. Must not be protected via authetication.
     *
     * @return literally "pong"
     */
    @GET
    @Path("/ping")
    public String ping() {
        return "pong";
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
        LOG.debug("Creating mapping file '{}'", fileName);
        return new File(fileName);
    }

    protected String generateMappingFileName(String mappingName) {
        return String.format("atlasmapping-%s.xml", mappingName);
    }
}
