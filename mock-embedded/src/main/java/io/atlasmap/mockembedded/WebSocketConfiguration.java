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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServerEndpointRegistration;

import io.atlasmap.service.lsp.AtlasExpressionLSPServerEndpointConfig;
import io.atlasmap.service.lsp.AtlasExpressionLSPService;

/**
 * Websocket configuration.
 */
@Configuration
public class WebSocketConfiguration {

    /**
     * Creates {@link ServerEndpointRegistration} for AtlasMap conditional mapping expression LSP server.
     * @return registration
     */
    @Bean
    public ServerEndpointRegistration expressionLSPEndpoint() {
        return new ServerEndpointRegistration(
            AtlasExpressionLSPServerEndpointConfig.WEBSOCKET_SERVER_PATH, AtlasExpressionLSPService.class);
    }

    /**
     * Creates {@link ServerEndpointExporter} for AtlasMap conditional mapping expression LSP server.
     * @return exporter
     */
    @Bean
    public ServerEndpointExporter endpointExporter() {
        return new ServerEndpointExporter();
    }
}
