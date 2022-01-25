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
package io.atlasmap.service.lsp;

import java.util.Collection;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.websocket.WebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AtlasMap Expression LSP Service provides Language Server functionality for the AtlasMap conditional mapping
 * expression language.
 * See https://microsoft.github.io/language-server-protocol/
 * It uses LSP4J to implement the LSP communication layer.
 * See https://projects.eclipse.org/projects/technology.lsp4j
 * This class is an implementation of the {@link javax.websocket.Endpoint} which plugs in to the LSP4J websocket binding.
 * @see AtlasExpressionLanguageServer
 * @see AtlasExpressionTextDocumentService
 * @see AtlasExpressionWorkspaceService
 */
public class AtlasExpressionLSPService extends WebSocketEndpoint<LanguageClient> {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasExpressionLSPService.class);

    @Override
    protected void configure(Launcher.Builder<LanguageClient> builder) {
        LOG.debug("AtlasMap Expression Language Server: Configuring");
        builder.setLocalService(new AtlasExpressionLanguageServer());
        builder.setRemoteInterface(LanguageClient.class);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        LOG.debug(("AtlasMap Expression Language Server: Establishing a connection"));
        session.setMaxTextMessageBufferSize(8192);
        session.setMaxBinaryMessageBufferSize(8192);;
        super.onOpen(session, config);
    }

    @Override
    protected void connect(Collection<Object> localServices, LanguageClient remoteProxy) {
        localServices.stream()
                .filter(LanguageClientAware.class::isInstance)
                .forEach(languageClientAware -> ((LanguageClientAware) languageClientAware).connect(remoteProxy));
    }

}
