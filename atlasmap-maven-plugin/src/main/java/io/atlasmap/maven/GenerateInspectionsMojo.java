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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.service.AtlasJsonProvider;
import io.atlasmap.java.v2.JavaClass;
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/atlasmap", readonly = true)
    private File outputDir;

    /**
     * The {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>} of the artifact to resolve.
     */
    @Parameter(property = "gav")
    private String gav;

    /**
     * The class name that should be inspected.
     */
    @Parameter(property = "className")
    private String className;

    public static class Inspection {
        private String gav;
        private String className;
        private List<String> classNames;

        public String getGav() {
            return gav;
        }

        public String getClassName() {
            return className;
        }

        public List<String> getClassNames() {
            return classNames;
        }
    }

    /**
     * Allows you to configure the plugin with:
     * <code>
     *
     *     <configuration>
     *         <inspections>
     *             <inspection>
     *                 <gav>io.atlasmap:atlas-java-generateInspection-test:1.1</gav>
     *                 <className>org.some.JavaClass</className>
     *             </inspection>
     *             <inspection>
     *                 <gav>io.atlasmap:other:1.1</gav>
     *                 <classNames>
     *                     <className>org.some.JavaClass1</className>
     *                     <className>org.some.JavaClass2</className>
     *                 </classNames>
     *             </inspection>
     *         </inspections>
     *     </configuration>
     *
     * </code>
     */
    @Parameter()
    private List<Inspection> inspections;

    public void execute() throws MojoExecutionException, MojoFailureException {
        outputDir.mkdirs();
        if( this.gav!=null && this.className !=null ) {
            generateInspection(this.gav, Arrays.asList(className));
        }
        if( inspections!=null ) {
            for (Inspection inspection : inspections) {
                ArrayList<String> classNames = new ArrayList<String>(inspection.classNames);
                if( inspection.className!=null ) {
                    classNames.add(inspection.className);
                }
                generateInspection(inspection.gav, classNames);
            }
        }
    }

    private void generateInspection(String gav, Collection<String> classNames) throws MojoFailureException, MojoExecutionException {
        URL[] urls = resolveClasspath(gav);

        for (String className : classNames) {
            Class<?> clazz = null;
            try {
                // Not even this plugin will be  available on this new URLClassLoader
                URLClassLoader loader = new URLClassLoader(urls, null);
                clazz = loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            ClassInspectionService classInspectionService = new ClassInspectionService();
            classInspectionService.setConversionService(DefaultAtlasConversionService.getRegistry());
            JavaClass c = classInspectionService.inspectClass(clazz);

            try {
                ObjectMapper objectMapper = AtlasJsonProvider.createObjectMapper();
                File target = new File(outputDir, "atlasmap-inpection-" + className + ".json");
                objectMapper.writeValue(target, c);
                getLog().info("Created: "+target);
            } catch (JsonProcessingException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private URL[] resolveClasspath(String mavenGav) throws MojoFailureException {
        try {
            Artifact artifact = new DefaultArtifact(mavenGav);

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(new Dependency(artifact, ""));
            collectRequest.setRepositories(remoteRepos);

            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setCollectRequest(collectRequest);
            DependencyResult dependencyResult = system.resolveDependencies(repoSession, dependencyRequest);

            PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
            dependencyResult.getRoot().accept( nlg );

            Iterator it = nlg.getNodes().iterator();
            ArrayList<URL> urls = new ArrayList<URL>();
            while(it.hasNext()) {
                DependencyNode node = (DependencyNode)it.next();
                if(node.getDependency() != null) {
                    Artifact x = node.getDependency().getArtifact();
                    if(x.getFile() != null) {
                        urls.add(x.getFile().toURI().toURL());
                    }
                }
            }
            return urls.toArray(new URL[urls.size()]);

        } catch (IllegalArgumentException e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (DependencyResolutionException e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
