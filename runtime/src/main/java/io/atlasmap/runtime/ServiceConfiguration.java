package io.atlasmap.runtime;

import io.atlasmap.java.service.JavaService;
import io.atlasmap.service.AtlasJsonProvider;
import io.atlasmap.service.AtlasService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Bean
    public JavaService javaService() {
        return new JavaService();
    }

    @Bean
    public AtlasService atlasService() {
        return new AtlasService();
    }

    @Bean
    public AtlasJsonProvider atlasJsonProvider() {
        return new AtlasJsonProvider();
    }
}
