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
package io.atlasmap.java.v2;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import io.atlasmap.v2.Actions;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Camelize;
import io.atlasmap.v2.Capitalize;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.Lowercase;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.Properties;
import io.atlasmap.v2.Property;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SeparateByDash;
import io.atlasmap.v2.SeparateByUnderscore;
import io.atlasmap.v2.StringLength;
import io.atlasmap.v2.StringList;
import io.atlasmap.v2.Trim;
import io.atlasmap.v2.TrimLeft;
import io.atlasmap.v2.TrimRight;
import io.atlasmap.v2.Uppercase;

public abstract class BaseMarshallerTest {

	public boolean deleteTestFolders = true;

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() throws Exception {
		Files.createDirectories(Paths.get("target/junit/" + testName.getMethodName()));
	}

	@After
	public void tearDown() throws Exception {
		if (deleteTestFolders) {
			Path directory = Paths.get("target/junit/" + testName.getMethodName());
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						throw exc;
					}
				}
			});
		}
	}

	protected AtlasMapping generateAtlasMapping() {
		AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
		atlasMapping.setName("junit");

		JavaField inputField = new JavaField();
		inputField.setName("foo");
		inputField.setValue("bar");

		JavaField outputField = new JavaField();
		outputField.setName("woot");
		outputField.setValue("blerg");

		Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
		fm.getInputField().add(inputField);
		fm.getOutputField().add(outputField);

		atlasMapping.getMappings().getMapping().add(fm);
		return atlasMapping;
	}
	
	protected AtlasMapping generateActionMapping() {
		AtlasMapping mapping = generateAtlasMapping();
		JavaField outputField = (JavaField)((Mapping)mapping.getMappings().getMapping().get(0)).getOutputField().get(0);
		
		Actions actions = new Actions();
		actions.getActions().add(new Camelize());
		actions.getActions().add(new Capitalize());
		actions.getActions().add(new Lowercase());
		actions.getActions().add(new SeparateByDash());
		actions.getActions().add(new SeparateByUnderscore());
		actions.getActions().add(new StringLength());
		actions.getActions().add(new Trim());
		actions.getActions().add(new TrimLeft());
		actions.getActions().add(new TrimRight());
		actions.getActions().add(new Uppercase());
		outputField.setActions(actions);
		return mapping;
	}
	
	protected AtlasMapping generatePropertyReferenceMapping() {
		AtlasMapping mapping = generateAtlasMapping();

		PropertyField inputField = new PropertyField();
		inputField.setName("foo");		

		Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
		fm.getInputField().add(inputField);
		
		Property p = new Property();
		p.setName("foo");
		p.setValue("bar");
		mapping.setProperties(new Properties());
		mapping.getProperties().getProperty().add(p);
		return mapping;
	}
	
	protected AtlasMapping generateConstantMapping() {
		AtlasMapping mapping = generateAtlasMapping();

		ConstantField inputField = new ConstantField();
		inputField.setValue("foo");

		Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
		fm.getInputField().add(inputField);

		return mapping;
	}	
	
	protected AtlasMapping generateCollectionMapping() {
		AtlasMapping innerMapping1 = generateAtlasMapping();
		AtlasMapping innerMapping2 = generateAtlasMapping();	
		
		Collection cMapping = (Collection) AtlasModelFactory.createMapping(MappingType.COLLECTION);
		cMapping.setMappings(new Mappings());
		
		cMapping.getMappings().getMapping().addAll(innerMapping1.getMappings().getMapping());
		cMapping.getMappings().getMapping().addAll(innerMapping2.getMappings().getMapping());
		cMapping.setCollectionType(CollectionType.LIST);
		
		AtlasMapping mapping = generateAtlasMapping();
		mapping.getMappings().getMapping().clear();
		mapping.getMappings().getMapping().add(cMapping);		
		return mapping;
	}
	
	protected AtlasMapping generateCombineMapping() {		
		JavaField inputJavaField = new JavaField();
		inputJavaField.setName("foo");
		inputJavaField.setValue("bar");
		inputJavaField.setIndex(4);
		
		JavaField inputJavaFieldB = new JavaField();
		inputJavaFieldB.setName("foo3");
		inputJavaFieldB.setValue("bar3");
		inputJavaFieldB.setIndex(3);

		JavaField outputJavaFieldA = new JavaField();
		outputJavaFieldA.setName("woot");
		outputJavaFieldA.setValue("blerg");		
		
		Mapping fm = (Mapping) AtlasModelFactory.createMapping(MappingType.COMBINE);
		fm.setStrategy("SPACE");
		
		fm.getInputField().add(inputJavaField);
		fm.getInputField().add(inputJavaFieldB);
		fm.getOutputField().add(outputJavaFieldA);
		
		AtlasMapping mapping = generateAtlasMapping();
		mapping.getMappings().getMapping().clear();
		mapping.getMappings().getMapping().add(fm);		
		return mapping;		
	}
	
	protected AtlasMapping generateMultiSourceMapping() {
		AtlasMapping mapping = generateSeparateAtlasMapping();
		
		DataSource sourceA = new DataSource();
		sourceA.setUri("someSourceURI:A");
		sourceA.setId("sourceA");
		mapping.getDataSource().add(sourceA);
		
		DataSource targetA = new DataSource();
		targetA.setUri("someTargetURI:A");
		targetA.setId("targetA");
		mapping.getDataSource().add(targetA);
		
		DataSource targetB = new DataSource();
		targetB.setUri("someTargetURI:B");
		targetB.setId("targetB");
		mapping.getDataSource().add(targetB);
		
		Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
		fm.getInputField().get(0).setDocId("sourceA");
		fm.getOutputField().get(0).setDocId("targetA");
		fm.getOutputField().get(1).setDocId("targetB");

		return mapping;
	}
	
	protected AtlasMapping generateSeparateAtlasMapping() {	
		JavaField inputJavaField = new JavaField();
		inputJavaField.setName("foo");
		inputJavaField.setValue("bar");
		
		JavaField outputJavaFieldA = new JavaField();
		outputJavaFieldA.setIndex(4);
		outputJavaFieldA.setName("woot");
		outputJavaFieldA.setValue("blerg");		
		
		JavaField outputJavaFieldB = new JavaField();
		outputJavaFieldA.setIndex(5);
		outputJavaFieldA.setName("woot2");
		outputJavaFieldA.setValue("blerg2");
		
		Mapping fm = (Mapping) AtlasModelFactory.createMapping(MappingType.SEPARATE);
		
		fm.getInputField().add(inputJavaField);
		fm.getOutputField().add(outputJavaFieldA);
		fm.getOutputField().add(outputJavaFieldB);
		
		AtlasMapping mapping = generateAtlasMapping();
		mapping.getMappings().getMapping().clear();
		mapping.getMappings().getMapping().add(fm);		
		return mapping;			
	}
	
	public MavenClasspathRequest generateMavenClasspathRequest() {
		MavenClasspathRequest mavenClasspathRequest = new MavenClasspathRequest();
		mavenClasspathRequest.setExecuteTimeout(30000L);
		mavenClasspathRequest.setPomXmlData(generatePomXmlAsString());
		return mavenClasspathRequest;
	}
		
	public ClassInspectionRequest generateClassInspectionRequest() {
		ClassInspectionRequest classInspectionRequest = new ClassInspectionRequest();
		classInspectionRequest.setClasspath("/Users/mattrpav/.m2/repository/org/twitter4j/twitter4j-core/4.0.5/twitter4j-core-4.0.5.jar");
		classInspectionRequest.setClassName("twitter4j.StatusJSONImpl");
		classInspectionRequest.setFieldNameBlacklist(new StringList());
		classInspectionRequest.getFieldNameBlacklist().getString().add("createdAt");
		return classInspectionRequest;
	}
	
	public String generatePomXmlAsString() {
		return new String(
				 "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
				+ "	<modelVersion>4.0.0</modelVersion>"
				+ "	<groupId>foo.bar</groupId>"
				+ "	<artifactId>test.model</artifactId>"
				+ "	<version>1.10.0</version>"
				+ "	<packaging>jar</packaging>"
				+ "	<name>Test :: Model</name>"
				+ "	<dependencies>"
				+ "		<dependency>"
				+ "			<groupId>com.fasterxml.jackson.core</groupId>"
				+ "			<artifactId>jackson-annotations</artifactId>"
				+ "			<version>2.8.5</version>"
				+ "		</dependency>"
				+ "		<dependency>"
				+ "			<groupId>com.fasterxml.jackson.core</groupId>"
				+ "			<artifactId>jackson-databind</artifactId>"
				+ "			<version>2.8.5</version>"
				+ "		</dependency>"
				+ "		<dependency>"
				+ "			<groupId>com.fasterxml.jackson.core</groupId>"
				+ "			<artifactId>jackson-core</artifactId>"
				+ "			<version>2.8.5</version>"
				+ "		</dependency>"
				+ "	</dependencies>"
				+ "</project>");
	}
}
