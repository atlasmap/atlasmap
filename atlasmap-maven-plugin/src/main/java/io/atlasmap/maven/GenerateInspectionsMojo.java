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
package io.atlasmap.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.json.inspect.JsonInspectionService;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.xml.inspect.XmlInspectionService;
import io.atlasmap.xml.v2.XmlDocument;

@Mojo(name = "generate-inspections")
public class GenerateInspectionsMojo extends AbstractAtlasMapMojo {

    public static final String DEFAULT_OUTPUT_FILE_PREFIX = "atlasmap-inspection";

    /**
     * A list of {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>} of
     * the artifacts to resolve.
     */
    @Parameter
    private List<String> artifacts;

    /**
     * The class name that should be inspected.
     */
    @Parameter(property = "className")
    private String className;

    /**
     * The collection type that should be inspected.
     */
    @Parameter(property = "collectionType")
    private String collectionType;

    /**
     * The class name that should be inspected.
     */
    @Parameter(property = "collectionClassName")
    private String collectionClassName;

    /**
     * Allows you to configure the plugin with: <code>
     *
     *     <configuration>
     *         <inspections>
     *             <inspection>
     *                 <artifacts>
     *                     <artifact>io.atlasmap:atlas-java-generateInspection-test:1.1</artifact>
     *                 </artifacts>
     *                 <className>org.some.JavaClass</className>
     *             </inspection>
     *             <inspection>
     *                 <artifacts>
     *                     <artifact>io.atlasmap:other:1.1</artifact>
     *                 </artifacts>
     *                 <classNames>
     *                     <className>org.some.JavaClass1</className>
     *                     <className>org.some.JavaClass2</className>
     *                 </classNames>
     *             </inspection>
     *             <inspection>
     *                 <fileName>src/test/resources</fileName>
     *                 <inspectionType>SCHEMA</inspectionType>
     *             </inspection>
     *         </inspections>
     *     </configuration>
     *
     * </code>
     */
    @Parameter()
    private List<Inspection> inspections;

    public enum InspectionType {
        SCHEMA, INSTANCE
    }

    public static class Inspection {
        private List<String> artifacts;
        private String className;
        private String collectionType;
        private String collectionClassName;
        private List<String> classNames;
        private String fileName;
        private InspectionType inspectionType;

        public String getClassName() {
            return className;
        }

        public List<String> getClassNames() {
            return classNames;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public void setClassNames(List<String> classNames) {
            this.classNames = classNames;
        }

        public String getCollectionType() {
            return collectionType;
        }

        public void setCollectionType(String collectionType) {
            this.collectionType = collectionType;
        }

        public String getCollectionClassName() {
            return collectionClassName;
        }

        public void setCollectionClassName(String collectionClassName) {
            this.collectionClassName = collectionClassName;
        }

        public List<String> getArtifacts() {
            return artifacts;
        }

        public void setArtifacts(List<String> artifacts) {
            this.artifacts = artifacts;
        }

        public String getFileName() {
            return this.fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public InspectionType getInspectionType() {
            return this.inspectionType;
        }

        public void setInspectionType(InspectionType inspectionType) {
            this.inspectionType = inspectionType;
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (getOutputDir() != null) {
            getOutputDir().mkdirs();
        }
        CollectionType collectionType = CollectionType.NONE;
        if (this.collectionType != null) {
            collectionType = CollectionType.valueOf(this.collectionType);
        }
        if (this.artifacts != null && this.className != null) {
            generateJavaInspection(this.artifacts, Arrays.asList(className), collectionType, collectionClassName);
        }
        if (inspections != null) {
            for (Inspection inspection : inspections) {
                ArrayList<String> classNames = new ArrayList<>();
                if (inspection.classNames != null) {
                    classNames.addAll(inspection.classNames);
                } else if (inspection.className != null) {
                    classNames.add(inspection.className);
                } else {
                    generateFileInspection(inspection);
                }
                collectionType = CollectionType.NONE;
                if (inspection.collectionType != null) {
                    collectionType = CollectionType.valueOf(inspection.collectionType);
                }
                generateJavaInspection(inspection.artifacts, classNames, collectionType, inspection.collectionClassName);
            }
        }
    }

    private void generateJavaInspection(List<String> artifacts, Collection<String> classNames,
            CollectionType collectionType, String collectionClassName) throws MojoFailureException, MojoExecutionException {

        List<URL> urls = artifacts == null ? Collections.emptyList() : resolveClasspath(artifacts);

        ClassLoader origTccl = Thread.currentThread().getContextClassLoader();
        for (String className : classNames) {
            Class<?> clazz = null;
            JavaClass c = null;
            // Not even this plugin will be available on this new URLClassLoader
            try (URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), origTccl)) {
                clazz = loader.loadClass(className);
                ClassInspectionService classInspectionService = new ClassInspectionService();
                classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
                c = classInspectionService.inspectClass(loader, clazz, collectionType, collectionClassName);
            } catch (ClassNotFoundException | IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            writeToJsonFile(DEFAULT_OUTPUT_FILE_PREFIX + "-" + className, c);
        }
    }

    private void generateFileInspection(Inspection inspection) throws MojoFailureException {
        File file = new File(inspection.fileName);
        if (!file.exists()) {
            getLog().warn(String.format("Ignoring '%s'", inspection.fileName));
            return;
        }

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (!child.exists() || child.isDirectory()) {
                    getLog().warn(String.format("Ignoring '%s'", child.getAbsolutePath()));
                    continue;
                }
                if (child.getName().toLowerCase().endsWith(".json")) {
                    if (inspection.inspectionType == InspectionType.INSTANCE) {
                        generateJsonInstanceInspection(child.getAbsolutePath());
                    } else {
                        // schema inspection by default
                        generateJsonSchemaInspection(child.getAbsolutePath());
                    }
                } else if (child.getName().toLowerCase().endsWith(".xsd")) {
                    generateXmlSchemaInspection(child.getAbsolutePath());
                } else if (child.getName().toLowerCase().endsWith(".xml")) {
                    if (inspection.inspectionType == InspectionType.SCHEMA) {
                        // In case it's SchemaSet - XML contains XSDs. InspectionType.SCHEMA have to be specified.
                        generateXmlSchemaInspection(child.getAbsolutePath());
                    } else {
                        generateXmlInstanceInspection(child.getAbsolutePath());
                    }
                } else {
                    getLog().warn(String.format("Ignoring unsupported file type '%s'", child.getAbsolutePath()));
                    continue;
                }
            }
            return;
        }

        if (file.getName().toLowerCase().endsWith(".json")) {
            if (inspection.inspectionType == InspectionType.INSTANCE) {
                generateJsonInstanceInspection(file.getAbsolutePath());
            } else {
                // schema inspection by default
                generateJsonSchemaInspection(file.getAbsolutePath());
            }
        } else if (file.getName().toLowerCase().endsWith(".xsd")) {
            generateXmlSchemaInspection(file.getAbsolutePath());
        } else if (file.getName().toLowerCase().endsWith(".xml")) {
            if (inspection.inspectionType == InspectionType.SCHEMA) {
                // In case it's SchemaSet - XML contains XSDs. InspectionType.SCHEMA have to be specified.
                generateXmlSchemaInspection(file.getAbsolutePath());
            } else {
                generateXmlInstanceInspection(file.getAbsolutePath());
            }
        } else {
            throw new MojoFailureException(String.format("Inspection type '%s' is not supported", inspection.getClass().getName()));
        }
    }

    private void generateJsonSchemaInspection(String fileName) throws MojoFailureException {
        try {
            Path path = Paths.get(fileName);
            String schema = new String(Files.readAllBytes(path));
            JsonDocument d = new JsonInspectionService().inspectJsonSchema(schema);
            String name = path.getFileName().toString();
            String outputName = name.substring(0, name.length() - 5);
            writeToJsonFile(DEFAULT_OUTPUT_FILE_PREFIX + "-" + outputName, d);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void generateJsonInstanceInspection(String fileName) throws MojoFailureException {
        try {
            Path path = Paths.get(fileName);
            String schema = new String(Files.readAllBytes(path));
            JsonDocument d = new JsonInspectionService().inspectJsonDocument(schema);
            String name = path.getFileName().toString();
            String outputName = name.substring(0, name.length() - 5);
            writeToJsonFile(DEFAULT_OUTPUT_FILE_PREFIX + "-" + outputName, d);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void generateXmlSchemaInspection(String fileName) throws MojoFailureException {
        try {
            File f = new File(fileName);
            XmlDocument d = new XmlInspectionService().inspectSchema(f);
            String outputName = f.getName().substring(0, f.getName().length() - 4);
            writeToJsonFile(DEFAULT_OUTPUT_FILE_PREFIX + "-" + outputName, d);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void generateXmlInstanceInspection(String fileName) throws MojoFailureException {
        try {
            File f = new File(fileName);
            XmlDocument d = new XmlInspectionService().inspectXmlDocument(f);
            String outputName = f.getName().substring(0, f.getName().length() - 4);
            writeToJsonFile(DEFAULT_OUTPUT_FILE_PREFIX + "-" + outputName, d);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    public List<String> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<String> artifacts) {
        this.artifacts = artifacts;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Inspection> getInspections() {
        return inspections;
    }

    public void setInspections(List<Inspection> inspections) {
        this.inspections = inspections;
    }

}
