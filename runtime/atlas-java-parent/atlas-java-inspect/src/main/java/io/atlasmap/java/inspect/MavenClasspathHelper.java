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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenClasspathHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MavenClasspathHelper.class);

    private long processCheckInterval = 1000L;
    private long processMaxExecutionTime = 5000L;
    private String baseFolder = System.getProperty("java.io.tmpdir");
    public static final String WORKING_FOLDER_PREFIX = "atlas-mapping-mvn-";

    public String generateClasspathFromPom(String pom) throws Exception {

        if (pom == null || pom.isEmpty()) {
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generating classpath from pom:\n" + pom);
        }

        Path workingDirectory = createWorkingDirectory();
        Path pomFile = Paths.get(workingDirectory.toString(), "pom.xml");
        Files.write(pomFile, pom.getBytes());

        List<String> cmd = new LinkedList<String>();
        cmd.add("mvn");
        cmd.add("org.apache.maven.plugins:maven-dependency-plugin:3.0.0:build-classpath");
        cmd.add("-DincludeScope=runtime");

        String result = executeMavenProcess(workingDirectory.toString(), cmd);

        if (result != null) {
            result = parseClasspathFromMavenOutput(result);
        } else {
            LOG.error("MavenProcess returned unexpected result: " + result);
            throw new InspectionException("Unable to generate classpath from pom file");
        }

        try {
            deleteWorkingDirectory(workingDirectory);
        } catch (IOException ioe) {
            LOG.warn("Cleanup of working directory failed to complete: " + ioe.getMessage(), ioe);
        }

        return result;
    }

    protected String executeMavenProcess(String workingDirectory, List<String> cmd) throws IOException {
        String mavenOutputFilePath = workingDirectory + File.separator + "maven.output.txt";
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to execute process for command: " + cmd + "\n\tworkingDirectory: " + workingDirectory
                    + "\n\tmvn output log: " + mavenOutputFilePath);
        }

        File cwd = null;
        long startTime = System.currentTimeMillis();
        long totalTime = 0;
        Process process = null;
        StringBuilder outputMessage = new StringBuilder();

        if (workingDirectory == null || workingDirectory.isEmpty() || cmd == null || cmd.isEmpty()
                || cmd.get(0).isEmpty()) {
            LOG.error("Invalid workingDirectory: " + workingDirectory + " or command: " + cmd + " specified");
            throw new IllegalArgumentException("Working directory and command must be specified");
        }

        if (workingDirectory != null) {
            cwd = new File(workingDirectory);
            if (!cwd.exists()) {
                throw new IOException("Working Directory does not exist: " + workingDirectory);
            } else if (!cwd.isDirectory()) {
                throw new IOException("Working Directory is not a directory: " + workingDirectory);
            }
        }

        InputStream stdout = null;
        BufferedReader reader = null;

        long lastTimeDebugPrinted = 0;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File(workingDirectory));

            process = processBuilder.start();
            boolean running = true;
            int r = -1;

            if (process != null) {
                try {
                    stdout = process.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stdout));
                } catch (Exception e) {
                    LOG.error("Error reading output for command: " + cmd + " msg: " + e.getMessage(), e);
                }
            }

            Path mavenOutputFile = Paths.get(mavenOutputFilePath);

            while (running) {
                Thread.sleep(getProcessCheckInterval());
                totalTime = totalTime + getProcessCheckInterval();

                if (process != null) {
                    try {
                        StringBuilder currentUpdate = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            if (outputMessage.length() > 0) {
                                outputMessage.append(line).append("\n");
                                currentUpdate.append(line).append("\n");
                            } else {
                                outputMessage.append(line);
                            }
                        }
                        if (currentUpdate.length() > 0) {
                            try {
                                Files.write(mavenOutputFile, currentUpdate.toString().getBytes(),
                                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                            } catch (Exception e) {
                                LOG.error(
                                        "Error writing output for command: " + cmd + " to file: " + mavenOutputFilePath,
                                        e);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error reading output for command: " + cmd + " msg: " + e.getMessage(), e);
                    }
                }

                if (totalTime >= getProcessMaxExecutionTime()) {
                    process.destroy();
                    running = false;
                    throw new IOException("Command " + cmd + " killed due to exceeding max process time");
                }

                try {
                    r = process.exitValue();
                    running = false;
                    if (r != 0) {
                        outputMessage.append("Command returned non-zero exit code: " + r);
                    }
                } catch (IllegalThreadStateException itse) {
                    if (LOG.isDebugEnabled() && (System.currentTimeMillis() - lastTimeDebugPrinted > 10000)) {
                        String timeHMS = StringUtil.formatTimeHMS(System.currentTimeMillis() - startTime);
                        LOG.debug("Command still running: " + cmd + ", msg: " + itse.getMessage() + ", elapsed: "
                                + timeHMS);
                        lastTimeDebugPrinted = System.currentTimeMillis();
                    }
                }
            }

        } catch (IllegalArgumentException iae) {
            String errMsg = "Unable to execute command: " + cmd + " error: " + iae.getMessage();
            LOG.error(errMsg, iae);
            throw new IOException(errMsg, iae);
        } catch (IOException ioe) {
            String errMsg = "Unable to execute command: " + cmd + " error: " + ioe.getMessage();
            LOG.error(errMsg, ioe);
            throw new IOException(errMsg, ioe);
        } catch (InterruptedException intre) {
            String errMsg = "Command interrupted cmd: " + cmd + " error: " + intre.getMessage();
            LOG.error(errMsg, intre);
            throw new IOException(errMsg, intre);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    LOG.warn("Error closing BufferedReader for cmd: " + cmd, e);
                } finally {
                    reader = null;
                }
            }
            if (stdout != null) {
                try {
                    stdout.close();
                } catch (Exception e) {
                    LOG.warn("Error closing InputStream for cmd: " + cmd, e);
                } finally {
                    stdout = null;
                }
            }

            if (process != null) {
                try {
                    process.destroy();
                } catch (Throwable t) {
                    LOG.error(
                            "Error while attempting to destroy process for command: " + cmd + " msg: " + t.getMessage(),
                            t);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Exiting process for command: " + cmd + " exec time(ms): "
                        + (System.currentTimeMillis() - startTime) + " workingDirectory: " + workingDirectory);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Output for command: " + cmd + "\n" + outputMessage.toString());
        }

        return outputMessage.toString();
    }

    protected Path createWorkingDirectory() throws IOException {
        return Files.createTempDirectory(WORKING_FOLDER_PREFIX);
    }

    protected void deleteWorkingDirectory(Path tempDirectory) throws IOException {
        Files.deleteIfExists(Paths.get(tempDirectory.toString(), "pom.xml"));
        Files.deleteIfExists(Paths.get(tempDirectory.toString(), "maven.output.txt"));
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
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Deleted tempFolder: " + tmpFolder.getFileName());
                        }
                    }
                } catch (IOException ioe) {
                    LOG.warn("Error when attempting to delete tempFolder: " + tmpFolder.getFileName() + " msg: "
                            + ioe.getMessage(), ioe);
                }
            }
        }
        return count;
    }

    private String parseClasspathFromMavenOutput(String commandOutput) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(commandOutput));
        boolean useNextLine = true;
        String result = null;
        String line;
        while ((line = reader.readLine()) != null) {
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

    public String getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }

    protected String generateJavaCommand() {
        return System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    }
}
