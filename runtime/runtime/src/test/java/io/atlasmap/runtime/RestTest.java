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
package io.atlasmap.runtime;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
    Application.class,
    ServiceConfiguration.class,
    CorsConfiguration.class,
})
public class RestTest {

    private static final Logger LOG = LoggerFactory.getLogger(RestTest.class);
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json");

    @LocalServerPort
    int port;

    @Test
    public void getFieldActions() throws IOException {
        Request request = new Request.Builder()
            .url("http://127.0.0.1:" + port + "/v2/atlas/fieldActions")
            .build();
        try (Response response = client.newCall(request).execute()) {
            assertThat(response.isSuccessful()).isTrue();
            System.out.println(response.body().string());
        }
    }

    @Test
    public void testJsonInspect() throws IOException {
        Request request = new Request.Builder()
            .url("http://127.0.0.1:" + port + "/v2/atlas/json/inspect")
            .post(RequestBody.create(APPLICATION_JSON, resource("atlasmap-json-inspection.json")))
            .build();
        try (Response response = client.newCall(request).execute()) {
            assertThat(response.isSuccessful()).isTrue();
            System.out.println(response.body().string());
        }

    }

    protected static String resource(String file) throws IOException {
        try (InputStream is = RestTest.class.getClassLoader().getResourceAsStream(file)) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            copy(is, os);
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    protected static void copy(InputStream is, ByteArrayOutputStream os) throws IOException {
        int c;
        while ((c = is.read()) >= 0) {
            os.write(c);
        }
    }

}
