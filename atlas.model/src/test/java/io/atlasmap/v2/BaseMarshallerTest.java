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
package io.atlasmap.v2;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import io.atlasmap.v2.AtlasModelFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.*;

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
	
	protected LookupTable generateLookupTable() {
		LookupTable lookupTable = AtlasModelFactory.createLookupTable();
		lookupTable.setName("junit-lookuptable");
		lookupTable.setDescription("Sample lookup table entry for reference");
		
		LookupEntry entry1 = new LookupEntry();
		entry1.setSourceType(FieldType.STRING);
		entry1.setSourceValue("foo");
		entry1.setTargetType(FieldType.STRING);
		entry1.setTargetValue("bar");
		
		LookupEntry entry2 = new LookupEntry();
		entry2.setSourceType(FieldType.STRING);
		entry2.setSourceValue("blah");
		entry2.setTargetType(FieldType.STRING);
		entry2.setTargetValue("blur");
		
		lookupTable.getLookupEntryList().getLookupEntry().add(entry1);
		lookupTable.getLookupEntryList().getLookupEntry().add(entry2);
		return lookupTable;
	}

	protected AtlasMapping generateAtlasMapping() {
		AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();
		mapping.setName("junit");
		mapping.setFieldMappings(new FieldMappings());

		MappedField inputField = new MappedField();
		MockField inputMockField = new MockField();
		inputMockField.setName("foo");
		inputMockField.setValue("bar");
		inputField.setField(inputMockField);

		MappedField outputField = new MappedField();
		MockField outputMockField = new MockField();
		outputMockField.setName("woot");
		outputMockField.setValue("blerg");
		outputField.setField(outputMockField);

		MapFieldMapping fm = new MapFieldMapping();
		fm.setInputField(inputField);
		fm.setOutputField(outputField);

		mapping.getFieldMappings().getFieldMapping().add(fm);
		return mapping;
	}

	protected void validateAtlasMapping(AtlasMapping mapping) {
		assertNotNull(mapping);
		assertNotNull(mapping.getName());
		assertEquals("junit", mapping.getName());
		assertNotNull(mapping.getFieldMappings());
		assertEquals(new Integer(1), new Integer(mapping.getFieldMappings().getFieldMapping().size()));
		assertNotNull(mapping.getProperties());

		MapFieldMapping fm = (MapFieldMapping)mapping.getFieldMappings().getFieldMapping().get(0);
		assertNotNull(fm);
		assertNull(fm.getAlias());

		MappedField m1 = fm.getInputField();
		assertNotNull(m1);
		assertNull(m1.getFieldActions());
		//assertTrue(m1.getFieldActions().getFieldAction().isEmpty());
		assertNotNull(m1.getField());
		Field f1 = m1.getField();		
		assertTrue(f1 instanceof MockField);
		assertEquals("foo", ((MockField) f1).getName());
		assertEquals("bar", f1.getValue());
		assertNull(((MockField) f1).getType());

		MappedField m2 = fm.getOutputField();
		assertNotNull(m2);
		assertNull(m2.getFieldActions());
		//assertTrue(m2.getFieldActions().getFieldAction().isEmpty());
		assertNotNull(m2.getField());
		Field f2 = m2.getField();		
		assertTrue(f2 instanceof MockField);
		assertEquals("woot", ((MockField) f2).getName());
		assertEquals("blerg", f2.getValue());
		assertNull(((MockField) f2).getType());
		
	}
}
