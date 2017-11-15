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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.atlasmap.java.service.JavaService;
import io.atlasmap.java.v2.MavenClasspathRequest;
import io.atlasmap.java.v2.MavenClasspathResponse;
import io.atlasmap.json.service.JsonService;
import io.atlasmap.service.AtlasJsonProvider;
import io.atlasmap.service.AtlasService;
import io.atlasmap.xml.service.XmlService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Bean
    public JavaService javaService() {
        return new JavaServiceEmptyClasspath();
    }

    @Bean
    public JsonService jsonService() {
        return new JsonService();
    }

    @Bean
    public XmlService xmlService() {
        return new XmlService();
    }

    @Bean
    public AtlasService atlasService() {
        return new AtlasService();
    }

    @Bean
    public AtlasJsonProvider atlasJsonProvider() {
        return new AtlasJsonProvider();
    }

    // =====================================================================

    public static class JavaServiceEmptyClasspath extends JavaService {

        /**
         * Stub out mavenclasspath processing for now.
         *
         * @param request
         * @return
         * @throws Exception
         */
        @Override
        @POST
        @Consumes({ MediaType.APPLICATION_JSON })
        @Produces({ MediaType.APPLICATION_JSON })
        @Path("/mavenclasspath")
        public Response generateClasspath(MavenClasspathRequest request) throws Exception {
            MavenClasspathResponse response = new MavenClasspathResponse();
            response.setExecutionTime(0L);
            response.setClasspath("");
            return Response.ok().header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Content-Type")
                    .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE").entity(response).build();
        }
    }
}
