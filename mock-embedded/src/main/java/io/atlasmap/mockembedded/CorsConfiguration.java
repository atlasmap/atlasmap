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
package io.atlasmap.mockembedded;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS configuration.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("cors")
public class CorsConfiguration {

    private List<String> allowedOrigins = Arrays.asList(org.springframework.web.cors.CorsConfiguration.ALL);

    /**
     * Gets the {@link CorsFilter}.
     * @return CorsFliter filter
     */
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(request -> {

            // in case you want to allow access to some resource From any origin:
//            String pathInfo = request.getPathInfo();
//            if (pathInfo != null &&
//                (pathInfo.endsWith("/swagger.json") ||
//                 pathInfo.endsWith("/swagger.yaml"))) {
//                return new org.springframework.web.cors.CorsConfiguration().applyPermitDefaultValues();
//            }

            org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
            config.setAllowedOrigins(allowedOrigins);
            config.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
            config.applyPermitDefaultValues();
            return config;
        });
    }

    /**
     * Gets allowedOrigins.
     * @return allowedOrigins
     */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * Sets allowedOrigins.
     * @param allowedOrigins allowedOrigins
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

}
