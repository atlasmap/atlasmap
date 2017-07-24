package io.atlasmap.runtime;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.atlasmap.java.service.JavaService;
import io.atlasmap.java.v2.MavenClasspathRequest;
import io.atlasmap.java.v2.MavenClasspathResponse;
import io.atlasmap.service.AtlasJsonProvider;
import io.atlasmap.service.AtlasService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Bean
    public JavaService javaService() {
        return new JavaServiceEmptyClasspath();
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

    public  static class JavaServiceEmptyClasspath extends JavaService {
        public JavaServiceEmptyClasspath() {
            super();
        }

        /**
         * Stub out mavenclasspath processing for now.
         *
         * @param request
         * @return
         * @throws Exception
         */
        @POST
        @Consumes({MediaType.APPLICATION_JSON})
        @Produces({MediaType.APPLICATION_JSON})
        @Path("/mavenclasspath")
        public Response generateClasspath(MavenClasspathRequest request) throws Exception {
            MavenClasspathResponse response = new MavenClasspathResponse();
            response.setExecutionTime(0L);
            response.setClasspath("");
            return Response.ok()
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Headers", "Content-Type")
                           .header("Access-Control-Allow-Methods", "GET,PUT,POST,PATCH,DELETE")
                           .entity(response)
                           .build();
        }
    }
}
