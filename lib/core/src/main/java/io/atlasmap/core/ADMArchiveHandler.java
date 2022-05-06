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
package io.atlasmap.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.ReloadableClassLoader;
import io.atlasmap.v2.ADMDigest;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.DataSourceMetadata;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentCatalog;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.Json;

/**
 * <div>
 * The API for handling ADM archive. It encapsulates ADM archive structure
 * and format and isolate file/stream I/O and serialization/deserializatiopn from other part.
 * ADM archive is a zipped archive file or its exploded directory which contains
 * <ul>
 * <li>Mapping Definition file (atlasmapping-UI.n.json)</li>
 * <li>Document Catalog file (document-catalog.json</li>
 * <li>Document specification files (e.g. specification/SOURCE/some-json-doc-id/0)
 * <li>Document inspection result files (e.g. inspected/SOURCE/some-json-doc-id/inspected.json)
 * <li>Java libraries (jar files in lib/ directory)</li>
 * </ul>
 * In addition to above, older version of ADM archive has a gzipped digest file {@link ADMDigest}
 * which contains all document metadata and mapping definition in a single JSON file (adm-catalog-files-n.gz).
 * The digest file is deprecated and newer version of AtlasMap converts it into Document catalog
 * file and Document specifications automatically.
 * </div>
 * {@link #load(Path)} {@link #export(OutputStream)}
 *
 * <div>
 * Note that at this moment Java library directory is not managed by this class.
 * Only when it imports/exports ADM archive file, library jars are extracted/bundled
 * if {@link #isIgnoreLibrary} is set to {@code false}.
 * </div>
 *
 */
public class ADMArchiveHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ADMArchiveHandler.class);
    private static final String MAPPING_DEFINITION_FILTER = "atlasmapping";
    private static final String MAPPING_DEFINITION_TEMPLATE = "atlasmapping-UI.%s.json";
    private static final String GZIPPED_ADM_DIGEST_FILTER = "adm-catalog-files";
    private static final String DOCUMENT_CATALOG_FILTER = "document-catalog";
    private static final String DOCUMENT_CATALOG_NAME = "document-catalog.json";
    private static final String LIB_DIRECTORY = "lib";
    private static final String SPECIFICATION_DIRECTORY = "specification";
    private static final String INSPECTED_DIRECTORY = "inspected";
    private static final String INSPECTED_FILE = "inspected.json";

    private ObjectMapper jsonMapper;
    private ObjectMapper jsonMapperForDigest;

    private AtlasMapping mappingDefinition = null;
    private AtlasMappingHandler atlasMappingHandler = null;
    private String mappingDefinitionId = "0";
    private DocumentCatalog documentCatalog = new DocumentCatalog();
    private boolean ignoreLibrary = false;
    private ReloadableClassLoader libraryLoader;
    private Path persistDirectory;
    private Path libraryDirectory;
    
    /**
     * A constructor.
     */
    public ADMArchiveHandler() {
        this(ADMArchiveHandler.class.getClassLoader());
    }

    /**
     * A constructor.
     * @param loader class loader
     */
    public ADMArchiveHandler(ClassLoader loader) {
        if (loader instanceof ReloadableClassLoader) {
            this.libraryLoader = (ReloadableClassLoader) loader;
        }
        this.jsonMapper = Json.withClassLoader(loader);
        this.jsonMapperForDigest = this.jsonMapper.copy();
        this.jsonMapperForDigest.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
    }

    /**
     * Load an ADM archive file, an exploded directory or mapping definition JSON file.
     * @param path {@link java.nio.file.Path} of the ADM archive file or an exploded directory
     * @throws AtlasException If it fails to load
     */
    public void load(Path path) throws AtlasException {
        clear();
        File file = path.toFile();
        if (!file.exists() || (!file.isFile() && !file.isDirectory())) {
            throw new AtlasException(
                    String.format("'%s' doesn't exist or is not a regular file/directory", path.toString()));
        }

        if (file.isDirectory()) {
            loadExploded(file);
        } else if (file.getName().toLowerCase().endsWith(".adm")){
            loadADMFile(file);
        } else {
            try (FileInputStream fin = new FileInputStream(file)) {
                setMappingDefinitionFromStream(fin);
            } catch (Exception e) {
                throw new AtlasException(
                        String.format("Invalid mapping definition file: '%s'", path.toString()), e);
            }
        }
    }

    /**
     * Load an ADM archive from stream.
     * @param in InputStream to read an ADM Archive
     * @throws AtlasException If it fails to load
     */
    public void load(InputStream in) throws AtlasException {
        load(AtlasContextFactory.Format.ADM, in);
    }

    /**
     * Load an ADM archive or mapping definition from stream.
     * @param format {@code AtlasContextFactory.Format} to indicate stream format
     * @param in InputStream to read an ADM Archive
     * @throws AtlasException If it fails to load
     */
    public void load(AtlasContextFactory.Format format, InputStream in) throws AtlasException {
        if (format == AtlasContextFactory.Format.ADM) {
            loadADMStream(in);
        } else {
            try {
                setMappingDefinitionFromStream(in);
            } catch (Exception e) {
                throw new AtlasException("Invalid mapping definition from stream", e);
            }
        }
    }

    /**
     * Export into an ADM archive.
     * @param out OutputStream to write an ADM archive
     * @throws AtlasException If it fails to export
     */
    public void export(OutputStream out) throws AtlasException {
        LOG.debug("Creating ADM archive file for ID:'{}'", this.mappingDefinitionId);
        try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
            ZipEntry catEntry = null;

            if (getMappingDefinition() != null) {
                String mappingFileName = getMappingDefinitionFileName();
                LOG.debug("  Creating mapping definition file '{}'", mappingFileName);
                catEntry = new ZipEntry(mappingFileName);
                zipOut.putNextEntry(catEntry);
                jsonMapper.writeValue(zipOut, getMappingDefinition());
                zipOut.closeEntry();
            }

            if (getDocumentCatalog() != null) {
                LOG.debug("  Creating Document catalog JSON file '{}'", DOCUMENT_CATALOG_NAME);
                catEntry = new ZipEntry(DOCUMENT_CATALOG_NAME);
                zipOut.putNextEntry(catEntry);
                byte[] serialized = getSerializedDocumentCatalog();
                zipOut.write(serialized, 0, serialized.length);
                zipOut.closeEntry();
            }

            zipOut.putNextEntry(new ZipEntry(SPECIFICATION_DIRECTORY + "/"));
            zipOut.closeEntry();
            Path specDir = getPersistDirectory().resolve(SPECIFICATION_DIRECTORY);
            Path sourceSpecDir = specDir.resolve(DataSourceType.SOURCE.value());
            String entryPath = SPECIFICATION_DIRECTORY + "/" + DataSourceType.SOURCE.value();
            putDocumentResources(sourceSpecDir, entryPath, zipOut);
            Path targetSpecDir = specDir.resolve(DataSourceType.TARGET.value());
            entryPath = SPECIFICATION_DIRECTORY + "/" + DataSourceType.TARGET.value();
            putDocumentResources(targetSpecDir, entryPath, zipOut);
            zipOut.putNextEntry(new ZipEntry(INSPECTED_DIRECTORY+ "/"));
            zipOut.closeEntry();
            Path inspectedDir = getPersistDirectory().resolve(INSPECTED_DIRECTORY);
            Path sourceInspectedDir = inspectedDir.resolve(DataSourceType.SOURCE.value());
            entryPath = INSPECTED_DIRECTORY + "/" + DataSourceType.SOURCE.value();
            putDocumentResources(sourceInspectedDir, entryPath, zipOut);
            Path targetInspectedDir = inspectedDir.resolve(DataSourceType.TARGET.value());
            entryPath = INSPECTED_DIRECTORY + "/" + DataSourceType.TARGET.value();
            putDocumentResources(targetInspectedDir, entryPath, zipOut);

            if (!isIgnoreLibrary() && libraryDirectory != null && libraryDirectory.toFile().isDirectory()) {
                zipOut.putNextEntry(new ZipEntry(LIB_DIRECTORY + "/"));
                zipOut.closeEntry();
                for (File jarFile : libraryDirectory.toFile().listFiles()) {
                    String path = LIB_DIRECTORY + "/" + jarFile.getName();
                    LOG.debug("  Creating jar file entry '{}'", path);
                    ZipEntry libEntry = new ZipEntry(path);
                    zipOut.putNextEntry(libEntry);
                    try (FileInputStream fis = new FileInputStream(jarFile)) {
                        fis.transferTo(zipOut);
                    }
                    zipOut.closeEntry();
                }
            }
        } catch (Exception e) {
            throw new AtlasException("Error exporting ADM archive file", e);
        }
    }

    private void putDocumentResources(Path sourceParentPath, String targetParentPath, ZipOutputStream zipOut) throws Exception {
        if (!sourceParentPath.toFile().exists()) {
            return;
        }
        for (File dir : sourceParentPath.toFile().listFiles(f -> f.isDirectory())) {
            Path docDir = sourceParentPath.resolve(dir.getName());
            String docDirEntryPath = targetParentPath + "/" + dir.getName();
            zipOut.putNextEntry(new ZipEntry(docDirEntryPath + "/"));
            zipOut.closeEntry();
            for (File f : docDir.toFile().listFiles(f -> f.isFile())) {
                String fileEntryPath = docDirEntryPath + "/" + f.getName();
                LOG.debug("  Creating file entry '{}'", fileEntryPath);
                zipOut.putNextEntry(new ZipEntry(fileEntryPath));
                try (FileInputStream fis = new FileInputStream(f)) {
                    fis.transferTo(zipOut);
                }
                zipOut.closeEntry();
            }
        }
    }

    /**
     * Persist ADM archive into a directory.
     * @throws AtlasException If it fails to persist
     */
    public void persist() throws AtlasException {
        Path mdPath = getPersistDirectory().resolve(getMappingDefinitionFileName());
        if (getMappingDefinition()  != null) {
            try {
                jsonMapper.writeValue(mdPath.toFile(), getMappingDefinition());
            } catch (Exception e) {
                LOG.warn("Failed to persist mapping definition", e);
            }
        }

        Path catalogPath = getPersistDirectory().resolve(DOCUMENT_CATALOG_NAME);
        if (getDocumentCatalog() != null) {
            try {
                jsonMapper.writeValue(catalogPath.toFile(), getDocumentCatalog());
            } catch (Exception e) {
                LOG.warn("Failed to persist Document catalog file", e);
            }
        }
    }

    /**
     * Gets the {@link AtlasMapping} mapping definition.
     * @return mapping definition
     */
    public AtlasMapping getMappingDefinition() {
        return this.mappingDefinition;
    }

    /**
     * Gets the {@link AtlasMappingHandler}.
     * @return handler
     */
    public AtlasMappingHandler getAtlasMappingHandler() {
        return this.atlasMappingHandler;
    }

    /**
     * Gets the serialized {@link AtlasMapping} in byte array.
     * @return serialized
     */
    public byte[] getSerializedMappingDefinition() throws AtlasException {
        if (getMappingDefinition() == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            jsonMapper.writeValue(bos, getMappingDefinition());
            return bos.toByteArray();
        } catch (Exception e) {
            throw new AtlasException("Failed to serialize mapping definition", e);
        }
    }

    /**
     * Sets the {@link AtlasMapping} mapping definition.
     * @param mapping mapping definition
     */
    public void setMappingDefinition(AtlasMapping mapping) {
        this.mappingDefinition = mapping;
        this.atlasMappingHandler = new AtlasMappingHandler(mapping);
    }

    /**
     * Sets the serialized mapping definition JSON from InputStream.
     * @param is serialized mapping definition JSON
     * @throws AtlasException unexpected error
     */
    public void setMappingDefinitionFromStream(InputStream is) throws AtlasException {
        try {
            setMappingDefinition(jsonMapper.readValue(is, AtlasMapping.class));
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Looks up the DataSource metadata associated with the specified Document ID.
     * @param dstype DataSourceType to indicate SOURCE or TARGET
     * @param documentId Document ID
     * @return DataSource metadata
     * @throws AtlasException unexpected error
     */
    public DocumentMetadata getDocumentMetadata(DataSourceType dstype, String documentId) throws AtlasException {
        return getDocumentMetadata(new DocumentKey(dstype, documentId));
    }

    /**
     * Looks up the DataSource metadata associated with the specified DataSource key.
     * @param key DataSource key
     * @return DataSource metadata
     * @throws AtlasException unexpected error
     */
    public DocumentMetadata getDocumentMetadata(DocumentKey key) throws AtlasException {
        if (getDocumentCatalog() == null) {
            return null;
        }
        List<DocumentMetadata> docs = key.getDataSourceType() == DataSourceType.SOURCE
            ? getDocumentCatalog().getSources()
            : getDocumentCatalog().getTargets();
        Optional<DocumentMetadata> answer = docs.stream().filter(m -> key.getDocumentId().equals(m.getId())).findFirst();
        if (answer.isPresent()) {
            return answer.get();
        }
        return null;
    }

    /**
     * Sets the InspectionRequest object as a Document metadata.
     * @param documentKey DocumentKey
     * @param metadata DocumentMetadata
     */
    public void setDocumentMetadata(DocumentKey documentKey, DocumentMetadata metadata) throws AtlasException {
        if (getDocumentCatalog() == null) {
            setDocumentCatalog(new DocumentCatalog());
        }
        List<DocumentMetadata> docs = documentKey.getDataSourceType() == DataSourceType.SOURCE
            ? getDocumentCatalog().getSources()
            : getDocumentCatalog().getTargets();
        Optional<DocumentMetadata> meta = docs.stream().filter(m -> documentKey.getDocumentId().equals(m.getId())).findFirst();
        if (meta.isPresent()) {
            int index = docs.indexOf(meta.get());
            docs.set(index, metadata);
        } else {
            docs.add(metadata);
        }
    }

    /**
     * Sets the DocumentCatalog.
     * @param catalog catalog
     */
    public void setDocumentCatalog(DocumentCatalog catalog) {
        this.documentCatalog = catalog;
    }

    /**
     * Gets a map of Document metadata.
     * @return a map of Document metadata.
     * @throws AtlasException unexpected error
     */
    public DocumentCatalog getDocumentCatalog() throws AtlasException {
        return this.documentCatalog;
    }

    /**
     * Serializes the {@link DocumentCatalog} into JSON and return as a byte array.
     * @return serialized {@link DocumentCatalog}
     * @throws AtlasException unexpected error
     */
    public byte[] getSerializedDocumentCatalog() throws AtlasException {
        if (getDocumentCatalog() == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            jsonMapper.writeValue(bos, getDocumentCatalog());
            return bos.toByteArray();
        } catch (Exception e) {
            throw new AtlasException("Failed to serialize Document catalog", e);
        }
    }

    /**
     * Sets a map of Document metadata from InputStream.
     * @param is Document catalog InputStream
     * @throws AtlasException unexpected error
     */
    public void setDocumentCatalogFromStream(InputStream is) throws AtlasException {
        try {
            this.documentCatalog = jsonMapper.readValue(is, DocumentCatalog.class);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Clones a {@link AtlasMapping} mapping definition object.
     * @return mapping definition
     * @throws AtlasException unexpected error
     */
    public AtlasMapping cloneMappingDefinition() throws AtlasException {
        AtlasMapping atlasMapping = getMappingDefinition();
        if (atlasMapping == null) {
            return null;
        }
        try {
            byte[] bytes = this.jsonMapper.writeValueAsBytes(atlasMapping);
            return this.jsonMapper.readValue(bytes, AtlasMapping.class);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Clears all contents.
     */
    public void clear() throws AtlasException {
        this.mappingDefinition = null;
        this.atlasMappingHandler = null;
        this.documentCatalog = null;
        if (getPersistDirectory().resolve(SPECIFICATION_DIRECTORY).toFile().exists()) {
            for (File f : getPersistDirectory().resolve(SPECIFICATION_DIRECTORY).toFile().listFiles()) {
                AtlasUtil.deleteDirectory(f);
            }
            
        }
        if (getPersistDirectory().resolve(INSPECTED_DIRECTORY).toFile().exists()) {
            for (File f : getPersistDirectory().resolve(INSPECTED_DIRECTORY).toFile().listFiles()) {
                AtlasUtil.deleteDirectory(f);
            }
            
        }
        if (getPersistDirectory().resolve(getMappingDefinitionFileName()).toFile().exists()) {
            getPersistDirectory().resolve(getMappingDefinitionFileName()).toFile().delete();
        }
        if (getPersistDirectory().resolve(DOCUMENT_CATALOG_NAME).toFile().exists()) {
            getPersistDirectory().resolve(DOCUMENT_CATALOG_NAME).toFile().delete();
        }
    }

    /**
     * Sets if it ignores the library or not.
     * @param ignoreLib true to ignore library, or false
     */
    public void setIgnoreLibrary(boolean ignoreLib) {
        this.ignoreLibrary = ignoreLib;
    }

    /**
     * Gets if it ignores the library or not.
     * @return true if it ignores library, or false
     */
    public boolean isIgnoreLibrary() {
        return this.ignoreLibrary;
    }

    /**
     * Sets the persistent directory.
     * @param dir persistent directory path
     * @throws AtlasException unexpected error
     */
    public void setPersistDirectory(Path dir) throws AtlasException {
        ensureDirectory(dir);
        this.persistDirectory = dir;
    }

    /**
     * Gets the persistent directory.
     * @return persistent directory path
     * @throws AtlasException unexpected error
     */
    public Path getPersistDirectory() throws AtlasException {
        if (this.persistDirectory == null) {
            try {
                this.persistDirectory = Files.createTempDirectory("atlasmap");
                this.persistDirectory.toFile().deleteOnExit();
            } catch (IOException e) {
                throw new AtlasException("Failed to create a temporary directory to extract the ADM file", e);
            }
        }
        return this.persistDirectory;
    }

    /**
     * Sets the library directory.
     * @param dir library directory path
     * @throws AtlasException unexpected error
     */
    public void setLibraryDirectory(Path dir) throws AtlasException {
        ensureDirectory(dir);
        this.libraryDirectory = dir;
    }

    /**
     * Sets the mapping definition ID.
     * @param id mapping definition ID
     */
    public void setMappingDefinitionId(String id) {
        this.mappingDefinitionId = id;
    }

    /**
     * Gets the file name of the mapping definition JSON.
     * @return file name
     */
    public String getMappingDefinitionFileName() {
        return String.format(MAPPING_DEFINITION_TEMPLATE, this.mappingDefinitionId);
    }

    /**
     * Sets the Document specification.
     * @param documentKey DocumentKey
     * @param name file name
     * @param specification Document specification
     */
    public void setDocumentSpecification(DocumentKey documentKey, String name, InputStream specification) throws AtlasException {
        Path dsTypePath = getPersistDirectory().resolve(SPECIFICATION_DIRECTORY).resolve(documentKey.getDataSourceType().value());
        ensureDirectory(dsTypePath);
        Path docPath = dsTypePath.resolve(documentKey.getDocumentId());
        AtlasUtil.deleteDirectory(docPath.toFile());
        ensureDirectory(docPath);
        Path filePath = docPath.resolve(name);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile(), false)) {
            specification.transferTo(fos);
        } catch (Exception e) {
            LOG.warn(String.format("Failed to save a specification file '%s', ignoring...", filePath.toString()), e);
        }
    }

    /**
     * Sets the Document specification.
     * @param documentKey DocumentKey
     * @param specification Document specification
     */
    public void setDocumentSpecification(DocumentKey documentKey, InputStream specification) throws AtlasException {
        setDocumentSpecification(documentKey, "0", specification);
    }

    /**
     * Sets the Document specification.
     * @param dsType DataSourceType indicating SOURCE or TARGET
     * @param docId Document ID
     * @param in input stream
     * @throws AtlasException
     */
    public void setDocumentSpecificationFile(String dsType, String docId, InputStream in) throws AtlasException {
        setDocumentSpecification(new DocumentKey(DataSourceType.fromValue(dsType), docId), in);
    }

    /**
     * Gets the Document specification file handler.
     * @param docKey DocumentKey
     * @return file handler
     */
    public File getDocumentSpecificationFile(DocumentKey docKey) throws AtlasException {
        File specDir = getDocumentSpecificationDirectory(docKey);
        if (!specDir.exists() || !specDir.isDirectory() || specDir.list().length != 1) {
            return null;
        }
        return specDir.listFiles()[0];
    }
  
    private File getDocumentSpecificationDirectory(DocumentKey docKey) throws AtlasException {
        return getPersistDirectory()
            .resolve(SPECIFICATION_DIRECTORY)
            .resolve(docKey.getDataSourceType().value())
            .resolve(docKey.getDocumentId())
            .toFile();
    }

    /**
     * Sets the Document inspection result.
     * @param documentKey DocumentKey
     * @param resultObject Document inspection result object
     * @throws AtlasException unexpected error
     */
    public void setDocumentInspectionResult(DocumentKey documentKey, Serializable resultObject) throws AtlasException {
        Path docPath = getPersistDirectory()
            .resolve(INSPECTED_DIRECTORY)
            .resolve(documentKey.getDataSourceType().value())
            .resolve(documentKey.getDocumentId());
        AtlasUtil.deleteDirectory(docPath.toFile());
        ensureDirectory(docPath);
        Path filePath = docPath.resolve(INSPECTED_FILE);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile(), false)) {
            jsonMapper.writeValue(fos, resultObject);
        } catch (Exception e) {
            LOG.warn(String.format("Failed to save an inspection result JSON file '%s', ignoring...", filePath.toString()), e);
        }
    }

    /**
     * Store the serialized Document inspection result into a file.
     * @param dsType DataSourceType
     * @param docId Document ID
     * @param in serialized Document inspection result
     * @throws AtlasException unexpected error
     */
    public void setDocumentInspectionResultFile(String dsType, String docId, InputStream in) throws AtlasException {
        setDocumentInspectionResultFile(new DocumentKey(DataSourceType.fromValue(dsType), docId), in);
    }

    /**
     * Store the serialized Document inspection result into a file.
     * @param documentKey DocumentKey to identify the Document
     * @param in serialized Document inspection result
     * @throws AtlasException unexpected error
     */
    public void setDocumentInspectionResultFile(DocumentKey documentKey, InputStream in) throws AtlasException {
        Path docPath = getPersistDirectory()
            .resolve(INSPECTED_DIRECTORY)
            .resolve(documentKey.getDataSourceType().value())
            .resolve(documentKey.getDocumentId());
        ensureDirectory(docPath);
        Path filePath = docPath.resolve(INSPECTED_FILE);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile(), false)) {
            in.transferTo(fos);
        } catch (Exception e) {
            LOG.warn(String.format("Failed to save an inspection result JSON file '%s', ignoring...", filePath.toString()), e);
        }
    }

    /**
     * Gets the {@link File} which represents the persisted Document inspection result file.
     * @param documentKey DocumentKey to identify the Document
     * @return {@link File} for the persisted Document inspection result file
     * @throws AtlasException unexpected error
     */
    public File getDocumentInspectionResultFile(DocumentKey documentKey) throws AtlasException {
        File inspectedDir = getDocumentInspectionResultDirectory(documentKey);
        if (!inspectedDir.exists() || !inspectedDir.isDirectory() || inspectedDir.list().length != 1) {
            return null;
        }
        return inspectedDir.listFiles()[0];
    }

    private File getDocumentInspectionResultDirectory(DocumentKey documentKey) throws AtlasException {
        return getPersistDirectory()
            .resolve(INSPECTED_DIRECTORY)
            .resolve(documentKey.getDataSourceType().value())
            .resolve(documentKey.getDocumentId())
            .toFile();
    }

    /**
     * Deletes the Document specification, inspection result and metadata from the {@link DocumentCatalog}.
     * This also invokes {@link AtlasMappingHandler#removeDocumentReference(DocumentKey)} to remove
     * all the Document references from the Mapping Definition.
     * @param dsType SOURCE or TARGET
     * @param documentId Document ID of the Document to be deleted
     */
    public void deleteDocument(DataSourceType dsType, String documentId) throws AtlasException {
        DocumentCatalog catalog = getDocumentCatalog();
        List<DocumentMetadata> docs = dsType == DataSourceType.SOURCE ? catalog.getSources() : catalog.getTargets();
        Optional<DocumentMetadata> todelete = docs.stream().filter(m -> m.getId().equals(documentId)).findAny();
        if (!todelete.isPresent()) {
            return;
        }
        docs.remove(todelete.get());
        DocumentKey docKey = new DocumentKey(dsType, documentId);
        File specDir = getDocumentSpecificationDirectory(docKey);
        if (specDir != null && specDir.exists()) {
            AtlasUtil.deleteDirectory(specDir);
        }
        File inspectedDir = getDocumentInspectionResultDirectory(docKey);
        if (inspectedDir != null && specDir.exists()) {
            AtlasUtil.deleteDirectory(inspectedDir);
        }
        getAtlasMappingHandler().removeDocumentReference(docKey);
    }

    /**
     * Delete the specified field from the specified mapping.
     *
     * @param dsType
     * @param mappingId
     * @param fieldIndex
     * @throws AtlasException
     */
    public void deleteMappingField(DataSourceType dsType, String mappingId, Integer fieldIndex) throws AtlasException {
        AtlasMapping def = getMappingDefinition();
        getAtlasMappingHandler().removeMappingField(def, dsType, mappingId, fieldIndex);
        setMappingDefinition(def);
        persist();
    }

    /**
     * Loads ADM Archive from an exploded directory.
     * @param dir directory path.
     * @throws AtlasException unexpected error
     */
    private void loadExploded(File dir) throws AtlasException {
        setPersistDirectory(dir.toPath());
        this.mappingDefinitionId = dir.getName();
        File mappingDefinitionFile = dir.toPath().resolve(getMappingDefinitionFileName()).toFile();
        if (mappingDefinitionFile.exists() && mappingDefinitionFile.isFile()) {
            try (InputStream mappingdefis = new FileInputStream(mappingDefinitionFile)) {
                setMappingDefinitionFromStream(mappingdefis);
            } catch (Exception e) {
                throw new AtlasException("Failed to read mapping definition file", e);
            }
        }

        File catalogFile = dir.toPath().resolve(DOCUMENT_CATALOG_NAME).toFile();
        if (catalogFile.exists() && catalogFile.isFile()) {
            try (InputStream catalogis = new FileInputStream(catalogFile)) {
                setDocumentCatalogFromStream(catalogis);
            } catch (Exception e) {
                throw new AtlasException("Failed to read document catalog file", e);
            }
        }
    }

    private void loadADMFile(File file) throws AtlasException {
        try (FileInputStream fis = new FileInputStream(file)) {
            loadADMStream(fis);
        } catch (AtlasException ae) {
            throw ae;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private void loadADMStream(InputStream in) throws AtlasException {
        String catEntryName;
        ZipEntry catEntry;
        try (ZipInputStream zipIn = new ZipInputStream(in)) {
            boolean mappingDefinitionFound = false;
            boolean libraryFound = false;
            while ((catEntry = zipIn.getNextEntry()) != null) {
                if (catEntry.isDirectory()) {
                    continue;
                }
                catEntryName = cleanSeparator(catEntry.getName());
                LOG.debug("  Extracting ADM file entry '{}'", catEntryName);
                if (catEntryName.contains(GZIPPED_ADM_DIGEST_FILTER)) {
                    extractFromGzippedADMDigest(zipIn);
                } else if (catEntryName.contains(DOCUMENT_CATALOG_FILTER)) {
                    setDocumentCatalogFromStream(zipIn);
                } else if (catEntryName.startsWith(SPECIFICATION_DIRECTORY)) {
                    String[] segments = catEntryName.split("/");
                    if (segments.length != 4) {
                        continue;
                    }
                    setDocumentSpecificationFile(segments[1], segments[2], zipIn);
                } else if (catEntryName.startsWith(INSPECTED_DIRECTORY)) {
                    String[] segments = catEntryName.split("/");
                    if (segments.length != 4) {
                        continue;
                    }
                    setDocumentInspectionResultFile(segments[1], segments[2], zipIn);
                } else if (!isIgnoreLibrary() && catEntryName.contains(".jar")) {
                    if (this.libraryDirectory == null) {
                        throw new AtlasException("Library directory is not specified");
                    }
                    int separatorPos = catEntryName.lastIndexOf("/");
                    String name = separatorPos == -1 ? catEntryName : catEntryName.substring(separatorPos + 1);
                    Path libPath = this.libraryDirectory.resolve(name);
                    try (FileOutputStream fos = new FileOutputStream(libPath.toFile(), false)) {
                        zipIn.transferTo(fos);
                        libraryFound = true;
                    } catch (Exception e) {
                        LOG.warn(String.format("Failed to save a jar file '%s', ignoring...", name), e);
                    }
                } else if (catEntryName.contains(MAPPING_DEFINITION_FILTER)) {
                    if (mappingDefinitionFound) {
                        throw new AtlasException("Multiple mapping definition files are found in a same .adm archive");
                    }
                    ensureDirectory(getPersistDirectory());
                    Path mdPath = getPersistDirectory().resolve(getMappingDefinitionFileName());
                    zipIn.transferTo(new FileOutputStream(mdPath.toFile()));
                    mappingDefinitionFound = true;
                } else {
                    LOG.debug("Ignoring file '{}' in .adm archive", catEntryName);
                }
            }
            if (libraryFound && this.libraryLoader != null) {
                this.libraryLoader.reload();
            }
            // MappingDefinition potentially needs the custom Java classes to be loaded to deserialize
            if (mappingDefinitionFound) {
                Path mdPath = getPersistDirectory().resolve(getMappingDefinitionFileName());
                setMappingDefinitionFromStream(new FileInputStream(mdPath.toFile()));
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private String cleanSeparator(String path) {
        return path != null ? path.replaceAll("\\\\", "/") : null;
    }

    /**
     * Extracts Document metadata and specification from gzipped ADMDigest. It will be converted to
     * the DocumentCatalog and individual specification/inspected files.
     * @param in gzipped ADMDigest JSON file
     * @deprecated {@link ADMDigest} is kept for backward compatibility. It is recommended to convert the ADM
     * file with the newer version of AtlasMap UI to avoid auto conversion happens everytime the ADM file is loaded.
     */
    @Deprecated
    private void extractFromGzippedADMDigest(InputStream in) throws AtlasException {
        try {
            GZIPInputStream gzipped = new GZIPInputStream(in);
            ADMDigest digest = jsonMapperForDigest.readValue(gzipped, ADMDigest.class);
            setDocumentCatalog(new DocumentCatalog());
            for (int i=0; i<digest.getExportMeta().length; i++) {
                DataSourceMetadata meta = digest.getExportMeta()[i];
                String spec = digest.getExportBlockData()[i].getValue();
                if (meta.getId() == null) {
                    meta.setId(meta.getName());
                }
                DocumentMetadata docMeta = createDocumentMetadataFrom(meta);
                DocumentKey docKey = new DocumentKey(docMeta.getDataSourceType(), meta.getId());
                setDocumentMetadata(docKey, docMeta);
                if (spec != null) {
                    setDocumentSpecification(docKey, new ByteArrayInputStream(spec.getBytes()));
                }
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private DocumentMetadata createDocumentMetadataFrom(DataSourceMetadata meta) {
        DocumentMetadata answer = new DocumentMetadata();
        answer.setId(meta.getId());
        answer.setName(meta.getName());
        String docTypeStr = meta.getDocumentType();
        if (docTypeStr != null) {
            answer.setDocumentType(DocumentType.fromValue(docTypeStr.toUpperCase()));
        } else {
            // old adm file has DocumentType as DataSourceType
            docTypeStr = meta.getDataSourceType();
            if (docTypeStr != null) {
                try {
                    answer.setDocumentType(DocumentType.fromValue(docTypeStr.toUpperCase()));
                } catch (IllegalArgumentException e) {}
            }
        }
        answer.setInspectionType(meta.getInspectionType());
        answer.setDataSourceType(meta.getIsSource() ? DataSourceType.SOURCE : DataSourceType.TARGET);
        answer.setInspectionParameters(meta.getInspectionParameters());
        if (answer.getDocumentType() == DocumentType.JAVA) {
            if (answer.getInspectionParameters() == null) {
                answer.setInspectionParameters(new HashMap<>());
            }
            if (!answer.getInspectionParameters().containsKey("className")) {
                // old adm file has the FQCN of the Java Document only as a Document ID
                answer.getInspectionParameters().put("className", answer.getId());
            }
        }
        return answer;
    }

    private boolean ensureDirectory(Path dir) throws AtlasException {
        if (dir == null) {
            throw new AtlasException(String.format("Directory must not be Null"));
        }
        File dirf = dir.toFile();
        if (dirf.exists() && !dirf.isDirectory()) {
            throw new AtlasException(String.format("File '%s' is not a directory", dirf.getAbsolutePath()));
        } else if (!dirf.exists()) {
            dirf.mkdirs();
        }

        return true;
    }

}
