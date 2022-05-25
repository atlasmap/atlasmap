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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.junit.jupiter.api.Test;
import org.apache.maven.it.Verifier;

public class GenerateFieldActionsMojoTest {

    @Test
    public void test() throws Exception {
        Path projectPath = Paths.get(".");
        String projectPathStr = projectPath.toAbsolutePath().toString();
        Path settingsPath = projectPath.resolve("src").resolve("test").resolve("resources").resolve("settings.xml");
        String settingsPathStr = settingsPath.toAbsolutePath().toString();
        Verifier verifier = new Verifier(projectPathStr, settingsPathStr, true);
        verifier.executeGoal("atlasmap:generate-field-actions");
        JsonNode node = new ObjectMapper().readTree(new FileInputStream("target/generated-sources/atlasmap/atlasmap-field-action.json"));
        ArrayNode details = (ArrayNode) node.get("ActionDetails").get("actionDetail");
        for (JsonNode entry : details) {
            if ("io.atlasmap.maven.test.PaulsFieldActionsModel".equals(entry.get("name").asText())) {
                return;
            };
        }
        fail("The custom field action io.atlasmap.maven.test.PaulsFieldActionsModel was not found");
    }
}
