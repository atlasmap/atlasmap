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
package io.atlasmap.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import io.atlasmap.expression.Expression;
import io.atlasmap.expression.FunctionResolver;
import io.atlasmap.expression.parser.ParseException;
import io.atlasmap.spi.ActionProcessor;
import io.atlasmap.spi.FunctionFactory;
import io.atlasmap.v2.ActionParameter;
import io.atlasmap.v2.ActionParameters;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

public class DefaultAtlasFunctionResolver implements FunctionResolver {

    private static DefaultAtlasFunctionResolver instance;

    private HashMap<String, FunctionFactory> functions = new HashMap<>();
    private DefaultAtlasFieldActionService fieldActionService;

    public static DefaultAtlasFunctionResolver getInstance() {
        if (instance == null) {
            instance = new DefaultAtlasFunctionResolver();
            instance.init();
        }
        return instance;
    }

    private void init() {
        functions = new HashMap<>();
        ServiceLoader<FunctionFactory> implementations = ServiceLoader.load(FunctionFactory.class, FunctionFactory.class.getClassLoader());
        for (FunctionFactory f : implementations) {
            functions.put(f.getName().toUpperCase(), f);
        }

        fieldActionService = DefaultAtlasFieldActionService.getInstance();
        fieldActionService.init();
    }

    @Override
    public Expression resolve(final String name, List<Expression> args) throws ParseException {
        String functionName = name.toUpperCase();
        FunctionFactory f = functions.get(functionName);
        if (f != null) {
            return f.create(args);
        } else {
            //lookup action
            return (ctx) -> {
                List<Field> arguments = new ArrayList<>();
                for (Expression arg: args) {
                    arguments.add(arg.evaluate(ctx));
                }

                Object valueForTypeEvaluation = null;
                if (arguments.isEmpty()) {
                    return null;
                } else {
                    valueForTypeEvaluation = arguments.get(arguments.size() - 1);
                }

                ActionProcessor actionProcessor = fieldActionService.findActionProcessor(name, valueForTypeEvaluation);
                if (actionProcessor != null) {
                    Map<String, Object> actionParameters = new HashMap<>();
                    ActionParameters actionDetailParameters = actionProcessor.getActionDetail().getParameters();
                    if (actionDetailParameters != null && actionDetailParameters.getParameter() != null) {
                        for (ActionParameter parameter : actionDetailParameters.getParameter()) {
                            if (!arguments.isEmpty()) {
                                Object parameterValue = arguments.remove(0).getValue();
                                actionParameters.put(parameter.getName(), parameterValue);
                            } else {
                                throw new IllegalArgumentException(String.format("The transformation '%s' expects more parameters. The parameter '%s' is missing", name, parameter.getName()));
                            }
                        }
                    }
                    if (arguments.isEmpty()) {
                        throw new IllegalArgumentException(String.format("The transformation '%s' expects more arguments", name));
                    }

                    FieldGroup fields = new FieldGroup();
                    fields.getField().addAll(arguments);
                    return fieldActionService.buildAndProcessAction(actionProcessor, actionParameters, fields);
                } else {
                    throw new IllegalArgumentException(String.format("The expression function or transformation '%s' was not found", name));
                }
            };
        }


    }
}
