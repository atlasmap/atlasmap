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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
import io.atlasmap.v2.ADMDigest;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.DataSourceKey;
import io.atlasmap.v2.DataSourceMetadata;
import io.atlasmap.v2.Json;

/**
 * <div>
 * The API for handling ADM archive. It encapsulates ADM archive structure
 * and format and isolate file/stream I/O from other part.
 * ADM archive is a zipped archive file or its exploded directory which contains
 * <ul>
 * <li>Mapping Definition file (atlasmapping-UI.n.json)</li>
 * <li>Gzipped digest file which contains all non-Java document metadata
 *  and mapping definition in a single JSON file (adm-catalog-files-n.gz)</li>
 * <li>Java libraries (jar files in lib/ directory)</li>
 * </ul>
 * </div>
 * {@link #load(Path)} {@link #export(OutputStream)}
 *
 * <div>
 * This handler follows lazy loading strategy as much as
 * possible, i.e. defer to serialize/deserialize until it is really required.
 * Also note that at this moment Java library directory is not managed by this class.
 * Only when it imports/exports ADM archive file, library jars are extracted/bundled
 * if {@link #isIgnoreLibrary} is set to {@code false}.
 * </div>
 *
 * <div>
 * TODO <a href="https://github.com/atlasmap/atlasmap/issues/1476">
 * https://github.com/atlasmap/atlasmap/issues/1476</a>
 * A gzipped digest file have to be splitted into individual schemas and a catalog file.
 * </div>
 */
public class ADMArchiveHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ADMArchiveHandler.class);
    private static final String MAPPING_DEFINITION_FILTER = "atlasmapping";
    private static final String MAPPING_DEFINITION_TEMPLATE = "atlasmapping-UI.%s.json";
    private static final String GZIPPED_ADM_DIGEST_FILTER = "adm-catalog-files";
    private static final String GZIPPED_ADM_DIGEST_TEMPLATE = "adm-catalog-files-%s.gz";

    private byte[] buffer = new byte[2048];
    private byte[] gzippedAdmDigestBytes = null;
    private byte[] mappingDefinitionBytes = null;
    private ObjectMapper jsonMapper;
    private ObjectMapper jsonMapperForDigest;

    private AtlasMapping mappingDefinition = null;
    private String mappingDefinitionId = "0";
    private Map<DataSourceKey, DataSourceMetadata> dataSourceMetadata;
    private boolean ignoreLibrary = false;
    private Path persistDirectory;
    private Path libraryDirectory;

    public ADMArchiveHandler() {
        this(ADMArchiveHandler.class.getClassLoader());
    }

    public ADMArchiveHandler(ClassLoader loader) {
        this.jsonMapper = Json.withClassLoader(loader);
        this.jsonMapperForDigest = this.jsonMapper.copy();
        this.jsonMapperForDigest.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
    }

    /**
     * Load an ADM archive file, an exploded directory or mapping definition JSON file.
     * @param path {@code java.nio.file.Path} of the ADM archive file or an exploded directory
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
                this.mappingDefinitionBytes = readIntoByteArray(fin);
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
                this.mappingDefinitionBytes = readIntoByteArray(in);
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

            if (this.getMappingDefinitionBytes() != null) {
                String mappingFileName = getMappingDefinitionFileName();
                LOG.debug("  Creating mapping definition file '{}'", mappingFileName);
                catEntry = new ZipEntry(mappingFileName);
                zipOut.putNextEntry(catEntry);
                zipOut.write(getMappingDefinitionBytes(), 0, getMappingDefinitionBytes().length);
                zipOut.closeEntry();
            }

            if (getGzippedADMDigestBytes() != null) {
                LOG.debug("  Creating gzipped ADM digest file '{}'", getGzippedADMDigestFileName());
                catEntry = new ZipEntry(getGzippedADMDigestFileName());
                zipOut.putNextEntry(catEntry);
                zipOut.write(getGzippedADMDigestBytes(), 0, getGzippedADMDigestBytes().length);
                zipOut.closeEntry();

                zipOut.putNextEntry(new ZipEntry("lib/"));
                zipOut.closeEntry();
            }

            if (!isIgnoreLibrary() && libraryDirectory != null && libraryDirectory.toFile().isDirectory()) {
                for (File jarFile : libraryDirectory.toFile().listFiles()) {
                    LOG.debug("  Creating jar file entry '{}'", "lib/" + jarFile.getName());
                    ZipEntry libEntry = new ZipEntry("lib/" + jarFile.getName());
                    zipOut.putNextEntry(libEntry);
                    redirectStream(new FileInputStream(jarFile), zipOut);
                    zipOut.closeEntry();
                }
            }
        } catch (Exception e) {
            throw new AtlasException("Error exporting ADM archive file", e);
        }
    }

    /**
     * Persist ADM archive into a directory.
     * @throws AtlasException If it fails to persist
     */
    public void persist() throws AtlasException {
        if (this.persistDirectory == null) {
            throw new AtlasException("Persist Directory must be set");
        }

        Path mdPath = this.persistDirectory.resolve(getMappingDefinitionFileName());
        if (getMappingDefinitionBytes() != null) {
            try {
                this.mappingDefinition = jsonMapper.readValue(getMappingDefinitionBytes(), AtlasMapping.class);
            } catch (Exception e) {
                LOG.warn("Invalid serialized mapping definition content detected, discarding");
                if (LOG.isDebugEnabled()) {
                    String str = String.format("Mapping Definition: [%s]: ",
                        getMappingDefinitionBytes() != null ? new String(getMappingDefinitionBytes()) : "");
                    LOG.warn(str, e);
                }
                this.mappingDefinitionBytes = null;
                this.mappingDefinition = null;
            }
        }
        if (this.mappingDefinition  != null) {
            try {
                jsonMapper.writeValue(mdPath.toFile(), this.mappingDefinition);
            } catch (Exception e) {
                LOG.warn("Failed to persist mapping definition", e);
            }
        }

        if (getGzippedADMDigestBytes() != null) {
            Path digestPath = this.persistDirectory.resolve(getGzippedADMDigestFileName());
            try (FileOutputStream out = new FileOutputStream(digestPath.toFile())) {
                out.write(getGzippedADMDigestBytes());
            } catch (Exception e) {
                LOG.warn("Failed to persist gzipped ADM digest file");
            }
        }
    }

    public AtlasMapping getMappingDefinition() {
        if (this.mappingDefinition == null && this.mappingDefinitionBytes != null) {
            try {
                this.mappingDefinition = jsonMapper.readValue(this.mappingDefinitionBytes,
                    AtlasMapping.class);
            } catch (Exception e) {
                LOG.warn("Invalid serialized mapping definition content detected, discarding");
                this.mappingDefinitionBytes = null;
                if (LOG.isDebugEnabled()) {
                    LOG.warn("", e);
                }
            }
        }
        return this.mappingDefinition;
    }

    public void setMappingDefinition(AtlasMapping mapping) {
        this.mappingDefinitionBytes = null;
        this.mappingDefinition = mapping;
    }

    public void setMappingDefinitionBytes(InputStream is) throws AtlasException {
        try {
            this.mappingDefinition = null;
            this.mappingDefinitionBytes = readIntoByteArray(is);
            if (LOG.isDebugEnabled()) {
                LOG.debug(this.jsonMapper.writeValueAsString(getMappingDefinition()));
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    public byte[] getMappingDefinitionBytes() throws AtlasException {
        try {
            if (this.mappingDefinitionBytes == null && this.mappingDefinition != null) {
                this.mappingDefinitionBytes = jsonMapper.writeValueAsBytes(this.mappingDefinition);
            }
            return this.mappingDefinitionBytes;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    public void setGzippedADMDigest(InputStream is) throws AtlasException {
        try {
            this.gzippedAdmDigestBytes = readIntoByteArray(is);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    public byte[] getGzippedADMDigestBytes() {
        return this.gzippedAdmDigestBytes;
    }

    public DataSourceMetadata getDataSourceMetadata(boolean isSource, String documentId) throws AtlasException {
        return getDataSourceMetadata(new DataSourceKey(isSource, documentId));
    }

    public DataSourceMetadata getDataSourceMetadata(DataSourceKey key) throws AtlasException {
        if (getDataSourceMetadataMap() == null) {
            return null;
        }
        return getDataSourceMetadataMap().get(key);
    }

    public Map<DataSourceKey, DataSourceMetadata> getDataSourceMetadataMap() throws AtlasException {
        if (this.dataSourceMetadata == null) {
            if (this.gzippedAdmDigestBytes == null) {
                return null;
            }
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(this.gzippedAdmDigestBytes))) {
                ADMDigest digest = jsonMapperForDigest.readValue(in, ADMDigest.class);
                this.dataSourceMetadata = new HashMap<>();
                for (int i=0; i<digest.getExportMeta().length; i++) {
                    DataSourceMetadata meta = digest.getExportMeta()[i];
                    String spec = digest.getExportBlockData()[i].getValue();
                    if (meta.getId() == null) {
                        meta.setId(meta.getName());
                    }
                    meta.setSpecification(spec != null ? spec.getBytes() : null);
                    this.dataSourceMetadata.put(new DataSourceKey(meta.getIsSource(), meta.getId()), meta);
                }
            } catch (Exception e) {
                throw new AtlasException(e);
            }
        }
        return Collections.unmodifiableMap(this.dataSourceMetadata);
    }

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

    public void clear() {
        this.mappingDefinitionBytes = null;
        this.mappingDefinition = null;
        this.gzippedAdmDigestBytes = null;
        this.dataSourceMetadata = null;
    }

    public void setIgnoreLibrary(boolean ignoreLib) {
        this.ignoreLibrary = ignoreLib;
    }

    public boolean isIgnoreLibrary() {
        return this.ignoreLibrary;
    }

    public void setPersistDirectory(Path dir) throws AtlasException {
        ensureDirectory(dir);
        this.persistDirectory = dir;
    }

    public void setLibraryDirectory(Path dir) throws AtlasException {
        ensureDirectory(dir);
        this.libraryDirectory = dir;
    }

    public void setMappingDefinitionId(String id) {
        this.mappingDefinitionId = id;
    }

    public String getGzippedADMDigestFileName() {
        return String.format(GZIPPED_ADM_DIGEST_TEMPLATE, this.mappingDefinitionId);
    }

    public String getMappingDefinitionFileName() {
        return String.format(MAPPING_DEFINITION_TEMPLATE, this.mappingDefinitionId);
    }

    private void loadExploded(File dir) throws AtlasException {
        setPersistDirectory(dir.toPath());
        this.mappingDefinitionId = dir.getName();
        File mappingDefinitionFile = dir.toPath().resolve(getMappingDefinitionFileName()).toFile();
        if (mappingDefinitionFile.exists() && mappingDefinitionFile.isFile()) {
            try (InputStream mappingdefis = new FileInputStream(mappingDefinitionFile)) {
                this.mappingDefinitionBytes = readIntoByteArray(mappingdefis);
            } catch (Exception e) {
                throw new AtlasException("Failed to read mapping definition file", e);
            }
        }

        File digestFile = dir.toPath().resolve(getGzippedADMDigestFileName()).toFile();
        if (digestFile.exists() && digestFile.isFile()) {
            try (InputStream digestis = new FileInputStream(digestFile)) {
                this.gzippedAdmDigestBytes = readIntoByteArray(digestis);
            } catch (Exception e) {
                throw new AtlasException("Failed to read digest file", e);
            }
        }
    }

    private void loadADMFile(File file) throws AtlasException {
        try {
            loadADMStream(new FileInputStream(file));
        } catch (AtlasException ae) {
            throw ae;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private void loadADMStream(InputStream in) throws AtlasException {
        String catEntryName;
        ZipEntry catEntry;
        ZipInputStream zipIn = null;
        try {
            zipIn = new ZipInputStream(in);
            boolean mappingDefinitionFound = false;
            while ((catEntry = zipIn.getNextEntry()) != null) {
                catEntryName = catEntry.getName();
                LOG.debug("  Extracting ADM file entry '{}'", catEntryName);
                if (catEntryName.contains(GZIPPED_ADM_DIGEST_FILTER)) {
                    this.gzippedAdmDigestBytes = readIntoByteArray(zipIn);
                } else if (!isIgnoreLibrary() && catEntryName.contains(".jar")) {
                    if (this.libraryDirectory == null) {
                        throw new AtlasException("Library directory is not specified");
                    }
                    int separatorPos = catEntryName.replaceAll("\\\\", "/").lastIndexOf("/");
                    String name = separatorPos == -1 ? catEntryName : catEntryName.substring(separatorPos + 1);
                    Path libPath = this.libraryDirectory.resolve(name);
                    try (FileOutputStream fos = new FileOutputStream(libPath.toFile())) {
                        redirectStream(zipIn, fos);
                    } catch (Exception e) {
                        LOG.warn(String.format("Failed to save a jar file '%s', ignoring...", name), e);
                    }
                } else if (catEntryName.contains(MAPPING_DEFINITION_FILTER)) {
                    if (mappingDefinitionFound) {
                        throw new AtlasException("Multiple mapping definition files are found in a same .adm archive");
                    }
                    this.mappingDefinitionBytes = readIntoByteArray(zipIn);
                    mappingDefinitionFound = true;
                } else {
                    LOG.debug("Ignoring file '{}' in .adm archive", catEntryName);
                }
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        } finally {
            try {
                zipIn.close();
            } catch (Exception e) {}
        }
    }

    private void redirectStream(InputStream in, OutputStream out) throws Exception {
        int len = 0;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    private byte[] readIntoByteArray(InputStream in) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            redirectStream(in, baos);
            return baos.toByteArray();
        }
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
