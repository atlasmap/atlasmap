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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
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

import io.atlasmap.v2.Json;

/**
 * The base Mojo of AtlasMap maven plugin.
 * @see GenerateFieldActionsMojo
 * @see GenerateInspectionsMojo
 */
public abstract class AbstractAtlasMapMojo extends AbstractMojo {

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
     * Resolves the classpath.
     * @param artifacts artifacts
     * @return resolved
     * @throws MojoFailureException unexpected error
     */
    protected List<URL> resolveClasspath(List<String> artifacts) throws MojoFailureException {
        final List<URL> urls = new ArrayList<>();

        try {
            for (String gav : artifacts) {
                Artifact artifact = new DefaultArtifact(gav);

                getLog().debug("Resolving dependencies for artifact: " + artifact);

                CollectRequest collectRequest = new CollectRequest();
                collectRequest.setRoot(new Dependency(artifact, ""));
                collectRequest.setRepositories(getRemoteRepos());

                DependencyRequest dependencyRequest = new DependencyRequest();
                dependencyRequest.setCollectRequest(collectRequest);
                DependencyResult dependencyResult = getSystem().resolveDependencies(getRepoSession(), dependencyRequest);

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

    /**
     * Writes to the JSON file.
     * @param name file name
     * @param object object to write
     * @throws MojoExecutionException failure
     */
    protected void writeToJsonFile(String name, Object object) throws MojoExecutionException {
        try {
            ObjectMapper objectMapper = Json.mapper();
            File target = outputFile;
            if (target == null) {
                target = new File(outputDir, name + ".json");
            }
	    target.getParentFile().mkdirs();
            objectMapper.writeValue(target, object);
            getLog().info("Created: " + target);
        } catch (JsonProcessingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Gets the repository system.
     * @return repository system
     */
    public RepositorySystem getSystem() {
        return system;
    }

    /**
     * Sets the repository system.
     * @param system repository system
     */
    public void setSystem(RepositorySystem system) {
        this.system = system;
    }

    /**
     * Gets the remote repositories.
     * @return remote repos
     */
    public List<RemoteRepository> getRemoteRepos() {
        return remoteRepos;
    }

    /**
     * Sets the remote reopsitories.
     * @param remoteRepos remote repos
     */
    public void setRemoteRepos(List<RemoteRepository> remoteRepos) {
        this.remoteRepos = remoteRepos;
    }

    /**
     * Get the repository system session.
     * @return repo session
     */
    public RepositorySystemSession getRepoSession() {
        return repoSession;
    }

    /**
     * Sets the repository system session.
     * @param repoSession repo session
     */
    public void setRepoSession(RepositorySystemSession repoSession) {
        this.repoSession = repoSession;
    }

    /**
     * Gets the output directory.
     * @return output directory
     */
    public File getOutputDir() {
        return outputDir;
    }

    /**
     * Sets the output directory.
     * @param outputDir output directory
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Gets the output file.
     * @return output file
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Sets the output file.
     * @param outputFile output file
     */
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

}
