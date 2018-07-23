/**
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.atlasmap.core.v3;

import java.io.File;

import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.api.v3.Session;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.VerifyArgument;

// TODO GUI

// TODO validation warnings for narrowing or other type auto-conversion by handler
// TODO verify exactly one transformation to field(s) is to target field(s), not property
// TODO verify all results used
// TODO verify target fields appear only once in all output parameters
// TODO clear previous target field & transformation dependencies before changing string value
// TODO combine/split
// TODO mark validation warnings as ignored

// TODO global properties
// TODO property refs in other mappings
// TODO Separate runtime and modeler into their own projects
// TODO i18n
// TODO java handler fields as getter/setter, name in method, etc.
// TODO undo/redo
// TODO new mapping for additional transformations with target field(s)?
// TODO chain transformations via variables
// TODO target field as source (implies dependency management)
// TODO JSON, XML handlers
// TODO unions (SML choice)
// TODO if transformation
// TODO test old customer giant XML
// TODO GUI auto combine/separate on multiple selection
public class Atlas {

    private final Context context;

    public Atlas(File mappingFile) throws AtlasException {
        VerifyArgument.isNotNull("mappingFile", mappingFile);
        context = new Context(mappingFile);
    }

    public File mappingFile() {
        return context.mappingFile;
    }

    public MappingDocument mappingDocument() {
        return context.mappingDocument;
    }

    public Session createSession() {
        return new SessionImpl(context);
    }
}
