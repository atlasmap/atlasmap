package io.atlasmap.java.inspect;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.java.inspect.MavenClasspathHelper;

@Ignore
public class MavenClasspathHelperTest {

	private MavenClasspathHelper mavenClasspathHelper = null;
	
	@Before
	public void setUp() {
		mavenClasspathHelper = new MavenClasspathHelper();
	}
	
	@After
	public void tearDown() {
		mavenClasspathHelper = null;
	}
	
	@Test
	public void testMavenClasspath() throws Exception {		
		Path testPom = Paths.get("src/test/resources/pom-classpath-test.xml");
        String pomData = new String(Files.readAllBytes(testPom));
		String classpath = mavenClasspathHelper.generateClasspathFromPom(pomData);
		assertNotNull(classpath);
		assertTrue(classpath.contains("jackson-annotations"));
		assertTrue(classpath.contains("jackson-databind"));
		assertTrue(classpath.contains("jackson-core"));
	}
	
	@Test(expected=IOException.class)
	public void testMavenClasspathTimeout() throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir") + File.separator + "src/test/resources");
				
		List<String> cmd = new LinkedList<String>();
		cmd.add(workingDirectory.toString() + File.separator + "test-timeout.sh");

		mavenClasspathHelper.executeMavenProcess(workingDirectory.toString(), cmd);
		fail("Expected IOException to indicate process timeout exceeded");
	}
	
	@Test
	public void testManageWorkingFolder() throws Exception {
		Path tmpFolder = mavenClasspathHelper.createWorkingDirectory();
		mavenClasspathHelper.deleteWorkingDirectory(tmpFolder);
		Integer count = mavenClasspathHelper.cleanupTempFolders();
		assertNotNull(count);
		assertEquals(new Integer(0), count);
	}
	
	@Test
	public void testCleanupWorkingFolder() throws Exception {
		mavenClasspathHelper.cleanupTempFolders();
		
		mavenClasspathHelper.createWorkingDirectory();
		mavenClasspathHelper.createWorkingDirectory();
		mavenClasspathHelper.createWorkingDirectory();
		mavenClasspathHelper.createWorkingDirectory();
		Integer count = mavenClasspathHelper.cleanupTempFolders();
		assertNotNull(count);
		assertEquals(new Integer(4), count);
	}



}
