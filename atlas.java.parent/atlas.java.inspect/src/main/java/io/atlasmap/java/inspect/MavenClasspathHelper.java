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
package io.atlasmap.java.inspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class MavenClasspathHelper {

	private static final Logger logger = LoggerFactory.getLogger(MavenClasspathHelper.class);

	private long processCheckInterval = 1000l;
	private long processMaxExecutionTime = 60000l;
	public static final String WORKING_FOLDER_PREFIX = "atlas-mapping-mvn-";

	public String generateClasspathFromPom(String pomText) throws IOException, InterruptedException {
		byte[] pom = pomText.getBytes("UTF-8");
		java.nio.file.Path workingDirectory = createWorkingDirectory();
		try {
			Files.write(workingDirectory.resolve("pom.xml"), pom);
			ArrayList<String> args = new ArrayList<>();
			args.add("mvn");
            args.add("--batch-mode");
			args.add("org.apache.maven.plugins:maven-dependency-plugin:3.0.0:build-classpath");
			args.add("-DincludeScope=runtime");
			// In case we ever want to configure were the local mvn repo lives:
			//if (localMavenRepoLocation != null) {
			//	args.add("-Dmaven.repo.local=" + localMavenRepoLocation);
			//}
			return executeMavenProcess(workingDirectory, args);
		} finally {
			try {
				deleteWorkingDirectory(workingDirectory);
			} catch (IOException ioe) {
				logger.warn("Cleanup of working directory failed to complete: " + ioe.getMessage(), ioe);
			}
		}
	}

	protected String executeMavenProcess(Path workingDirectory, List<String> args) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder().command(args)
				.redirectError(ProcessBuilder.Redirect.INHERIT)
				.directory(workingDirectory.toFile());
		// In case we ever want to configure the env:
		// Map<String, String> environment = builder.environment();
		// environment.put("MAVEN_OPTS", "-Xmx64M");
		Process mvn = builder.start();

		AtomicBoolean timedOut = new AtomicBoolean(false);
		Thread timeoutEnforcer = new Thread("Timeout Enforcer") {
			@Override
			synchronized public void run() {
				try {
					long start = System.currentTimeMillis();
					while (mvn.isAlive()) {
						long totalTime = System.currentTimeMillis() - start;
						if (totalTime >= getProcessMaxExecutionTime()) {
							mvn.destroy();
							timedOut.set(true);
						}
						// use wait so we can more quickly make this thread exit via a notify.
						this.wait(getProcessCheckInterval());
					}
				} catch (InterruptedException e) {
					logger.debug("Interrupted", e);
				}
			}
		};
		timeoutEnforcer.start();

		try {
			String result = parseClasspath(mvn.getInputStream());
			if (timedOut.get()) {
				throw new IOException("mvn killed due to exceeding max process time");
			}
			if (mvn.waitFor() != 0) {
				throw new IOException("Could not get the connector classpath, mvn exit value: " + mvn.exitValue());
			}
			return result;
		} finally {
			synchronized (timeoutEnforcer) {
				timeoutEnforcer.notify();
			}
			mvn.getInputStream().close();
			mvn.getOutputStream().close();
		}
	}

	private String parseClasspath(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		boolean useNextLine = true;
		String result = null;
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println("mvn: " + line);
			if (useNextLine) {
				useNextLine = false;
				result = line;
			}
			if (line.startsWith("[INFO] Dependencies classpath:")) {
				useNextLine = true;
			}
		}
		return result;
	}


	protected Path createWorkingDirectory() throws IOException {
		return Files.createTempDirectory(WORKING_FOLDER_PREFIX);
	}

	protected void deleteWorkingDirectory(Path tempDirectory) throws IOException {
		Files.deleteIfExists(Paths.get(tempDirectory.toString(), "pom.xml"));
		Files.deleteIfExists(tempDirectory);
	}

	public Integer cleanupTempFolders() throws IOException {
		Integer count = new Integer(0);
		try (Stream<Path> tmpFolders = Files.list(Paths.get(System.getProperty("java.io.tmpdir")))) {
			for (Path tmpFolder : tmpFolders.toArray(Path[]::new)) {
				try {
					if (tmpFolder.getFileName().toString().startsWith(WORKING_FOLDER_PREFIX)) {
						deleteWorkingDirectory(tmpFolder);
						count++;
						if (logger.isDebugEnabled()) {
							logger.debug("Deleted tempFolder: " + tmpFolder.getFileName());
						}
					}
				} catch (IOException ioe) {
					logger.warn("Error when attempting to delete tempFolder: " + tmpFolder.getFileName() + " msg: " + ioe.getMessage());
				}
			}
		}
		return count;
	}


	public long getProcessCheckInterval() {
		return processCheckInterval;
	}

	public void setProcessCheckInterval(long processCheckInterval) {
		this.processCheckInterval = processCheckInterval;
	}

	public long getProcessMaxExecutionTime() {
		return processMaxExecutionTime;
	}

	public void setProcessMaxExecutionTime(long processMaxExecutionTime) {
		this.processMaxExecutionTime = processMaxExecutionTime;
	}

}
