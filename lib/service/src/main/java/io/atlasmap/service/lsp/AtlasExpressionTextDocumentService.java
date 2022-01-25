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

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
 * The LSP4J {@link TextDocumentService} implementation for the AtlasMap conditional mapping expression.
 * @see AtlasExpressionLSPService
 * @see AtlasExpressionLanguageServer
 * @see AtlasExpressionWorkspaceService
 */
public class AtlasExpressionTextDocumentService implements TextDocumentService {

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // TODO Auto-generated method stub
        
    }
    
}
