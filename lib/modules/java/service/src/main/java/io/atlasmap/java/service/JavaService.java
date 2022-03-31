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

import static io.atlasmap.java.v2.JavaConstants.OPTION_CLASSPATH;
import static io.atlasmap.java.v2.JavaConstants.OPTION_CLASS_NAME;
import static io.atlasmap.java.v2.JavaConstants.OPTION_COLLECTION_CLASS_NAME;
import static io.atlasmap.java.v2.JavaConstants.OPTION_COLLECTION_TYPE;
import static io.atlasmap.java.v2.JavaConstants.OPTION_DISABLE_PRIVATE_ONLY_FIELDS;
import static io.atlasmap.java.v2.JavaConstants.OPTION_DISABLE_PROTECTED_ONLY_FIELDS;
import static io.atlasmap.java.v2.JavaConstants.OPTION_DISABLE_PUBLIC_GETTER_SETTER_FIELDS;
import static io.atlasmap.java.v2.JavaConstants.OPTION_DISABLE_PUBLIC_ONLY_FIELDS;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.ClassInspectionRequest;
import io.atlasmap.java.v2.ClassInspectionResponse;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.Field;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    /**
     * A constructor.
     * @param atlasService AtlasService
     * @param documentService DocumentService
     */
    public JavaService(AtlasService atlasService, DocumentService documentService) {
        super(atlasService, documentService);
        getDocumentService().registerModuleService(DocumentType.JAVA, this);
    }

    /**
     * Imports a Java Class as a Document with specified fully qualified class name and return a Document object.
     * @param requestIn request
     * @param mappingDefinitionId Mapping Definition ID
     * @param dataSourceType DataSourceType
     * @param documentId Document ID
     * @param uriInfo URI info
     * @return {@link ClassInspectionResponse}
     */
    @POST
    @Path("/project/{mappingDefinitionId}/document/{dataSourceType}/{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Import Java Document", description ="Import a Java Class as a Document with specified fully qualified class name and return a Document object")
    @RequestBody(description = "ClassInspectionRequest object", content = @Content(schema = @Schema(implementation = ClassInspectionRequest.class)))
    @ApiResponses(@ApiResponse(
            responseCode = "200", content = @Content(schema = @Schema(implementation = ClassInspectionResponse.class)), description = "Return a Document object represented by JavaClass"))
    public Response importJavaDocument(
            InputStream requestIn,
            @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
            @Parameter(description = "Document ID") @PathParam("documentId") String documentId,
            @Context UriInfo uriInfo
        ) {
        long startTime = System.currentTimeMillis();

        ClassInspectionRequest request = fromJson(requestIn, ClassInspectionRequest.class);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Import Java Class request: {}", new String(toJson(request)));
        }
        ClassInspectionResponse response = new ClassInspectionResponse();
        DocumentMetadata metadata = createDocumentMetadataFrom(request, dataSourceType, documentId);
        try {
            storeDocumentMetadata(mappingDefinitionId, metadata.getDataSourceType(), metadata.getId(), metadata);
            performDocumentInspection(mappingDefinitionId, metadata, null);
            File f = getAtlasService()
                        .getADMArchiveHandler(mappingDefinitionId)
                        .getDocumentInspectionResultFile(new DocumentKey(dataSourceType, documentId));
            response.setJavaClass(fromJson(new FileInputStream(f), JavaClass.class));
        } catch (Exception e) {
            String msg = String.format("Error importing Java Class %s - %s: %s",
                metadata.getInspectionParameters().get(OPTION_CLASS_NAME), e.getClass().getName(), e.getMessage());
            LOG.error(msg, e);
            throw new WebApplicationException(msg, e);
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }
        byte[] serialized = toJson(response);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Import Java Class response: {}", new String(serialized));
        }
        return Response.ok().entity(serialized).build();
    }

    private DocumentMetadata createDocumentMetadataFrom(ClassInspectionRequest request, DataSourceType dsType, String documentId) {
        DocumentMetadata meta = super.createDocumentMetadataFrom(request, dsType, documentId);
        if (request.getClasspath() != null) {
            meta.getInspectionParameters().put(OPTION_CLASSPATH, request.getClasspath());
        }
        if (request.getClassName() != null) {
            meta.getInspectionParameters().put(OPTION_CLASS_NAME, request.getClassName());
        }
        if (request.getCollectionClassName() != null) {
            meta.getInspectionParameters().put(OPTION_COLLECTION_CLASS_NAME, request.getCollectionClassName());
        }
        if (request.getCollectionType() != null) {
            meta.getInspectionParameters().put(OPTION_COLLECTION_TYPE, request.getCollectionType().value());
        } else {
            meta.getInspectionParameters().put(OPTION_COLLECTION_TYPE, CollectionType.NONE.value());
        }
        if (request.isDisablePrivateOnlyFields() != null) {
            meta.getInspectionParameters().put(OPTION_DISABLE_PRIVATE_ONLY_FIELDS, Boolean.toString(request.isDisablePrivateOnlyFields()));
        }
        if (request.isDisableProtectedOnlyFields() != null) {
            meta.getInspectionParameters().put(OPTION_DISABLE_PROTECTED_ONLY_FIELDS, Boolean.toString(request.isDisableProtectedOnlyFields()));
        }
        if (request.isDisablePublicGetterSetterFields() != null) {
            meta.getInspectionParameters().put(OPTION_DISABLE_PUBLIC_GETTER_SETTER_FIELDS, Boolean.toString(request.isDisablePublicGetterSetterFields()));
        }
        if (request.isDisablePublicOnlyFields() != null) {
            meta.getInspectionParameters().put(OPTION_DISABLE_PUBLIC_ONLY_FIELDS, Boolean.toString(request.isDisablePublicOnlyFields()));
        }
        return meta;
    }

    @Override
    public void performDocumentInspection(Integer mappingDefinitionId, DocumentMetadata meta, File spec) throws AtlasException {
        ClassInspectionService classInspectionService = configureInspectionService(meta);

        try {
            JavaClass c = null;
            String className = meta.getInspectionParameters().get(OPTION_CLASS_NAME);
            if (AtlasUtil.isEmpty(className)) {
                throw new AtlasException("Class name must be specified to import Java Document");
            }
            String classPath = meta.getInspectionParameters().get(OPTION_CLASSPATH);
            CollectionType collectionType = null;
            if (meta.getInspectionParameters().containsKey(OPTION_COLLECTION_TYPE)) {
                collectionType = CollectionType.fromValue(meta.getInspectionParameters().get(OPTION_COLLECTION_TYPE));
            }
            String collectionClassName = meta.getInspectionParameters().get(OPTION_COLLECTION_CLASS_NAME);
            if (AtlasUtil.isEmpty(classPath)) {
                c = classInspectionService.inspectClass(
                        getAtlasService().getLibraryLoader(),
                        className,
                        collectionType,
                        collectionClassName);
            } else {
                c = classInspectionService.inspectClass(className, collectionType, collectionClassName, classPath);
            }
            storeInspectionResult(mappingDefinitionId, meta.getDataSourceType(), meta.getId(), c);
        } catch (Throwable e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Maps inspection parameters from the request to the inspection service.
     * @param meta DocumentMetadata
     */
    protected ClassInspectionService configureInspectionService(DocumentMetadata meta) {
        ClassInspectionService answer = new ClassInspectionService();
        answer.setConversionService(DefaultAtlasConversionService.getInstance());
        if (meta.getFieldNameExclusions() != null && meta.getFieldNameExclusions().getString() != null
                && !meta.getFieldNameExclusions().getString().isEmpty()) {
            answer.getFieldExclusions().addAll(meta.getFieldNameExclusions().getString());
        }

        if (meta.getInspectionParameters().containsKey(OPTION_DISABLE_PRIVATE_ONLY_FIELDS)) {
            answer.setDisablePrivateOnlyFields(Boolean.parseBoolean(meta.getInspectionParameters().get(OPTION_DISABLE_PRIVATE_ONLY_FIELDS)));
        }

        if (meta.getInspectionParameters().containsKey(OPTION_DISABLE_PROTECTED_ONLY_FIELDS)) {
            answer.setDisableProtectedOnlyFields(Boolean.parseBoolean(meta.getInspectionParameters().get(OPTION_DISABLE_PROTECTED_ONLY_FIELDS)));
        }

        if (meta.getInspectionParameters().containsKey(OPTION_DISABLE_PUBLIC_ONLY_FIELDS)) {
            answer.setDisablePublicOnlyFields(Boolean.parseBoolean(meta.getInspectionParameters().get(OPTION_DISABLE_PUBLIC_ONLY_FIELDS)));
        }

        if (meta.getInspectionParameters().containsKey(OPTION_DISABLE_PUBLIC_GETTER_SETTER_FIELDS)) {
            answer.setDisablePublicGetterSetterFields(Boolean.parseBoolean(meta.getInspectionParameters().get(OPTION_DISABLE_PUBLIC_GETTER_SETTER_FIELDS)));
        }
        return answer;
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

    @Override
    protected Logger getLogger() {
        return JavaService.LOG;
    }

}
