/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.json.inspect.JsonDocumentInspectionService;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.v2.Json;
import io.atlasmap.xml.inspect.XmlInspectionService;
import io.atlasmap.xml.v2.XmlDocument;

@Mojo(name = "generate-inspections")
public class GenerateInspectionsMojo extends AbstractMojo {

    @Component
    private RepositorySystem system;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    /**
     * The directory where inspections get generated to.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/atlasmap")
    private File outputDir;

    /**
     * The file name to generate.
     */
    @Parameter()
    private File outputFile;

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

    public static class Inspection {
        private List<String> artifacts;
        private String className;
        private List<String> classNames;
        private String fileName;

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
    }

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
     *             </inspection>
     *         </inspections>
     *     </configuration>
     *
     * </code>
     */
    @Parameter()
    private List<Inspection> inspections;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (outputDir != null) {
            outputDir.mkdirs();
        }
        if (this.artifacts != null && this.className != null) {
            generateJavaInspection(this.artifacts, Arrays.asList(className));
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
                generateJavaInspection(inspection.artifacts, classNames);
            }
        }
    }

    private void generateJavaInspection(List<String> artifacts, Collection<String> classNames)
            throws MojoFailureException, MojoExecutionException {

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
                c = classInspectionService.inspectClass(loader, clazz);
            } catch (ClassNotFoundException | IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            writeToFile(className, c);
        }
    }

    private void writeToFile(String name, Object object) throws MojoExecutionException {
        try {
            ObjectMapper objectMapper = Json.mapper();
            File target = outputFile;
            if (target == null) {
                target = new File(outputDir, "atlasmap-inspection-" + name + ".json");
            }
            objectMapper.writeValue(target, object);
            getLog().info("Created: " + target);
        } catch (JsonProcessingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private List<URL> resolveClasspath(List<String> artifacts) throws MojoFailureException {
        final List<URL> urls = new ArrayList<>();

        try {
            for (String gav : artifacts) {
                Artifact artifact = new DefaultArtifact(gav);

                getLog().debug("Resolving dependencies for artifact: " + artifact);

                CollectRequest collectRequest = new CollectRequest();
                collectRequest.setRoot(new Dependency(artifact, ""));
                collectRequest.setRepositories(remoteRepos);

                DependencyRequest dependencyRequest = new DependencyRequest();
                dependencyRequest.setCollectRequest(collectRequest);
                DependencyResult dependencyResult = system.resolveDependencies(repoSession, dependencyRequest);

                PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
                dependencyResult.getRoot().accept(nlg);

                Iterator<DependencyNode> it = nlg.getNodes().iterator();
                while (it.hasNext()) {
                    DependencyNode node = it.next();
                    if (node.getDependency() != null) {
                        Artifact x = node.getDependency().getArtifact();
                        if (x.getFile() != null) {
                            getLog().debug("Found dependency: " + x + " for artifact: " + artifact);
                            urls.add(x.getFile().toURI().toURL());
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (DependencyResolutionException e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        return urls;
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
                    generateJsonSchemaInspection(child.getAbsolutePath());
                } else if (child.getName().toLowerCase().endsWith(".xsd")) {
                    generateXmlSchemaInspection(child.getAbsolutePath());
                } else {
                    getLog().warn(String.format("Ignoring unsupported file type '%s'", child.getAbsolutePath()));
                    continue;
                }
            }
            return;
        }

        if (file.getName().toLowerCase().endsWith(".json")) {
            generateJsonSchemaInspection(file.getAbsolutePath());
        } else if (file.getName().toLowerCase().endsWith(".xsd")) {
            generateXmlSchemaInspection(file.getAbsolutePath());
        } else {
            throw new MojoFailureException(String.format("Inspection type '%s' is not supported", inspection.getClass().getName()));
        }
    }

    private void generateJsonSchemaInspection(String fileName) throws MojoFailureException {
        try {
            Path path = Paths.get(fileName);
            String schema = new String(Files.readAllBytes(path));
            JsonDocument d = new JsonDocumentInspectionService().inspectJsonSchema(schema);
            String name = path.getFileName().toString();
            String outputName = name.substring(0, name.length() - 5);
            writeToFile(outputName, d);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void generateXmlSchemaInspection(String fileName) throws MojoFailureException {
        try {
            File f = new File(fileName);
            XmlDocument d = new XmlInspectionService().inspectSchema(f);
            String outputName = f.getName().substring(0, f.getName().length() - 4);
            writeToFile(outputName, d);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    public RepositorySystem getSystem() {
        return system;
    }

    public void setSystem(RepositorySystem system) {
        this.system = system;
    }

    public List<RemoteRepository> getRemoteRepos() {
        return remoteRepos;
    }

    public void setRemoteRepos(List<RemoteRepository> remoteRepos) {
        this.remoteRepos = remoteRepos;
    }

    public RepositorySystemSession getRepoSession() {
        return repoSession;
    }

    public void setRepoSession(RepositorySystemSession repoSession) {
        this.repoSession = repoSession;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
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

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
