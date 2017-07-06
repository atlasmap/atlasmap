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
import io.atlasmap.api.AtlasContextFactory;
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

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationPath("/")
@Path("v2/atlas")
public class AtlasService extends Application {		
	
	final Application javaServiceApp;
	final DefaultAtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();
	private String baseFolder = "target/mappings";
	
	public AtlasService() {
		javaServiceApp = new ResourceConfig().register(JacksonFeature.class);
	}

	protected Response standardCORSResponse() {
	    return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE,OPTIONS,HEAD")
                .build();
	}
	
    @OPTIONS
    @Path("/mapping")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingOptions() throws Exception {
    	return standardCORSResponse();
    }
    
    @OPTIONS
    @Path("/mapping/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingValidateOptions() throws Exception {
        return standardCORSResponse();
    }
    
    @OPTIONS
    @Path("/mapping/validate/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingValidateParameterizedOptions() throws Exception {
        return standardCORSResponse();
    }
    
    @OPTIONS
    @Path("/fieldMapping/converterCheck")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFieldMappingConverterCheckOptions() throws Exception {
        return standardCORSResponse();
    }
     
    @OPTIONS
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingParameterizedOptions() throws Exception {
        return standardCORSResponse();
    }

    @OPTIONS
    @Path("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappingsOptions() throws Exception {
        return standardCORSResponse();
    }
    
    @OPTIONS
    @Path("/fieldActions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFieldActionsOptions() throws Exception {
        return standardCORSResponse();
    }
	
    @GET
    @Path("/fieldActions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFieldActions(@Context UriInfo uriInfo) throws Exception {
        ActionDetails details = new ActionDetails();
        
        if(atlasContextFactory == null || atlasContextFactory.getFieldActionService() == null) {
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Content-Type")
                    .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
                    .entity(details)
                    .build();
        }
        
        details.getActionDetail().addAll(atlasContextFactory.getFieldActionService().listActionDetails());
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
                .entity(details)
                .build();
    }

    
    @GET
    @Path("/mappings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMappings(@Context UriInfo uriInfo, final @QueryParam("filter") String filter) throws Exception {
    	StringMap sMap = new StringMap();
    	
    	java.nio.file.Path mappingFolder = Paths.get(baseFolder);
    	File[] mappings = mappingFolder.toFile().listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (filter != null && name != null && !name.toLowerCase().contains(filter.toLowerCase())) {
					return false;
				}
                return name.matches("atlasmapping-[a-zA-Z0-9\\.\\-]+.xml");
            }
		});
    	
    	if(mappings == null) {
    		return Response.ok()
        			.header("Access-Control-Allow-Origin", "*")
        			.header("Access-Control-Allow-Headers", "Content-Type")
        			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
        			.entity(sMap)
        			.build();
    	}
    	
    	for(File mapping : mappings) {
    		AtlasMapping map = getMappingFromFile(mapping.getAbsolutePath());
    		StringMapEntry mapEntry = new StringMapEntry();
    		mapEntry.setName(map.getName());
    		
        	UriBuilder builder = uriInfo.getBaseUriBuilder()
        			.path("v2").path("atlas").path("mapping").path(map.getName());
    		mapEntry.setValue(builder.build().toString());
    		sMap.getStringMapEntry().add(mapEntry);
    	}
    	    	
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
    			.entity(sMap)
    			.build();
    }      
    
    @DELETE
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response removeMappingRequest(@PathParam("mappingId") String mappingId) throws Exception {
    	
    	java.nio.file.Path mappingFilePath = Paths.get(baseFolder + File.separator + generateMappingFileName(mappingId));
    	File mappingFile = mappingFilePath.toFile();
    	
    	if(!mappingFile.exists()) {
    		return Response.noContent()
        			.header("Access-Control-Allow-Origin", "*")
        			.header("Access-Control-Allow-Headers", "Content-Type")
        			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
        			.build();
    	}
    	
    	mappingFile.delete();
    	    	
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
    			.build();
    }
    
    @GET
    @Path("/mapping/{mappingId}")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response getMappingRequest(@PathParam("mappingId") String mappingId) throws Exception {
    	
    	java.nio.file.Path mappingFilePath = Paths.get(baseFolder + File.separator + generateMappingFileName(mappingId));
    	File mappingFile = mappingFilePath.toFile();
    	
    	if(!mappingFile.exists()) {
    		return Response.noContent()
        			.header("Access-Control-Allow-Origin", "*")
        			.header("Access-Control-Allow-Headers", "Content-Type")
        			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
        			.build();
    	}
    	
    	AtlasMapping atlasMapping = null;
    	try {
    		atlasMapping = getMappingFromFile(mappingFile.getAbsolutePath());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return Response.ok()
    			.header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Headers", "Content-Type")
    			.header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
    			.entity(atlasMapping)
    			.build();
    }
    
    @PUT
    @Path("/mapping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMappingRequest(AtlasMapping mapping, @Context UriInfo uriInfo) throws Exception {
    	return saveMapping(mapping, uriInfo);
    }
    
    @POST
    @Path("/mapping/{mappingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMappingRequest(AtlasMapping mapping, @Context UriInfo uriInfo) throws Exception {
    	return saveMapping(mapping, uriInfo);
    }
    	    
    @PUT
    @Path("/mapping/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateMappingRequest(AtlasMapping mapping, @Context UriInfo uriInfo) throws Exception {
        return validateMapping(mapping, uriInfo);
    }
    
    @PUT
    @Path("/fieldMapping/converterCheck")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processConverterCheckRequest(Mapping mapping, @Context UriInfo uriInfo) throws Exception {
        return converterCheck(mapping, uriInfo);
    }
    
    protected Response validateMapping(AtlasMapping mapping, UriInfo uriInfo) throws Exception {
        
        File temporaryMappingFile = File.createTempFile("atlas-mapping", "xml");
        temporaryMappingFile.deleteOnExit();
        atlasContextFactory.getMappingService().saveMappingAsFile(mapping, temporaryMappingFile);
        
        AtlasContext context = atlasContextFactory.createContext(temporaryMappingFile.toURI());
        AtlasSession session = context.createSession();
        context.processValidation(session);
        Validations validations = session.getValidations();
        
        if(session.getValidations() == null) {
            validations = new Validations();
        }

        if(temporaryMappingFile.exists()) {
            temporaryMappingFile.delete();
        }
        
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
                .entity(validations)
                .build();
    }
    
    protected Response converterCheck(Mapping mapping, UriInfo uriInfo) throws Exception {
        
        if(mapping == null) {
            throw new AtlasException("Mapping must be specified");
        }
        
        AtlasConversionService conversionService = atlasContextFactory.getConversionService();

        Validations validations = new Validations();
        List<Field> inputFields = new ArrayList<Field>();
        List<Field> outputFields = new ArrayList<Field>();
        
    
        for(Field f : mapping.getInputField()) {
            inputFields.add(f);
        }
        
        for(Field f : mapping.getOutputField()) {
            inputFields.add(f);
        }
        
        //if(mapping.get)
//            throw new AtlasException("Unsupported mapping type: " + mapping.getClass().getName());
        //}
        
        if(inputFields == null || inputFields.size() < 1 || outputFields == null || outputFields.size() < 1) {
            throw new AtlasException("Must have one of inputField(s) and outputField(s) in order to check for available converter");
        }
        

        FieldType inputType = null;
        FieldType outputType = null;
        
        for(Field inputField : inputFields) {
            if(inputField instanceof JavaField) {
                inputType = ((JavaField)inputField).getFieldType();
            }
            
            for(Field outputField : outputFields) {
                if(outputField instanceof JavaField) {
                    outputType = ((JavaField)outputField).getFieldType();
                }
                
                Optional<AtlasConverter> optionalConverter = conversionService.findMatchingConverter(inputType, outputType);
                if(optionalConverter.isPresent()) {
                    AtlasConverter converter = optionalConverter.get();
                    // TODO: return "ok"
                } else {
                    // TODO: return "Converter needed"
                }
                
            }
        }

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
                .entity(validations)
                .build();
    }
    
    protected Response saveMapping(AtlasMapping mapping, UriInfo uriInfo) throws Exception {
        try {
            saveMappingToFile(mapping);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(mapping.getName());

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
                .location(builder.build())
                .build();
    }
    
    public AtlasMapping getMappingFromFile(String fileName) throws Exception {
    	JAXBContext jaxbContext = JAXBContext.newInstance("io.atlasmap.v2:io.atlasmap.java.v2:io.atlasmap.xml.v2:io.atlasmap.json.v2");
    	Marshaller marshaller = null;
    	Unmarshaller unmarshaller = null;

		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		unmarshaller = jaxbContext.createUnmarshaller();
		
		StreamSource fileSource = new StreamSource(new File(fileName));
		JAXBElement<AtlasMapping> mappingElem = unmarshaller.unmarshal(fileSource, AtlasMapping.class);
		if(mappingElem != null) {
			return mappingElem.getValue();
		}
		return null;	
    }
    
    protected void saveMappingToFile(AtlasMapping atlasMapping) throws Exception {
    	JAXBContext jaxbContext = JAXBContext.newInstance("io.atlasmap.v2:io.atlasmap.java.v2:io.atlasmap.xml.v2:io.atlasmap.json.v2");
    	Marshaller marshaller = null;

		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		marshaller.marshal(atlasMapping, new File(baseFolder + File.separator + generateMappingFileName(atlasMapping.getName())));
    }
    
    protected String generateMappingFileName(String mappingName) {
    	return String.format("atlasmapping-%s.xml", mappingName);
    }
}