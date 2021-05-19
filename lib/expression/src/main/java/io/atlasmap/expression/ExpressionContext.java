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
package io.atlasmap.expression;

import io.atlasmap.v2.Field;

/**
 * A Filterable is the object being evaluated by the filters. It provides
 * access to filtered properties.
 * 
 * @version $Revision: 1.4 $
 */
public interface ExpressionContext {

    /**
     * Extracts the named variable.
     *
     * @param name variable name
     * @return {@link Field} represents variable value
     * @throws ExpressionException If variable cannot be retrieved
     */
    Field getVariable(String name) throws ExpressionException;

}

