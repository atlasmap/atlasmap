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
package io.atlasmap.core.v3.transformation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.atlasmap.api.v3.Message.Scope;
import io.atlasmap.api.v3.Message.Status;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.Parameter.Role;
import io.atlasmap.spi.v3.BaseParameter;
import io.atlasmap.spi.v3.BaseTransformation;
import io.atlasmap.spi.v3.util.I18n;

/**
 *
 */
public class AddTransformation extends BaseTransformation {

    public static final String NAME = I18n.localize("Add");
    public static final String OPERAND_PARAMETER = I18n.localize("Operand");
    public static final String SUM_PARAMETER = I18n.localize("Sum");

    private final transient BaseParameter sumParameter;

    public AddTransformation() {
        super(NAME, "Adds one or more numberic values");
        addParameter(new BaseParameter(this, OPERAND_PARAMETER, Role.INPUT, false, false,
                                       "A numberic source field, property, or constant to add"));
        addParameter(new BaseParameter(this, OPERAND_PARAMETER, Role.INPUT, false, true,
                                       "A numberic source field, property, or constant to add"));
        sumParameter = addParameter(new BaseParameter(this, SUM_PARAMETER, Role.OUTPUT, false, false,
                                                      "A target field or property to which to add the sum of the operands"));
    }

    /**
     * @see BaseTransformation#execute()
     */
    @Override
    protected void execute() {
        double sum = 0;
        int sumSize = 8;
        boolean sumIsDecimal = false;
        for (Parameter parameter : parameters()) {
            if (parameter.name().equals(OPERAND_PARAMETER)) {
                Object value = parameter.value();
                if (value == null) {
                    continue;
                }
                if (value instanceof Number) {
                    sum += ((Number)value).doubleValue();
                    if (value instanceof Short) {
                        sumSize = Math.max(sumSize, 16);
                    } else if (value instanceof Integer) {
                        sumSize = Math.max(sumSize, 32);
                    } else if (value instanceof Long) {
                        sumSize = Math.max(sumSize, 64);
                    } else if (value instanceof Float) {
                        sumSize = Math.max(sumSize, 32);
                        sumIsDecimal = true;
                    } else if (value instanceof Double) {
                        sumSize = Math.max(sumSize, 64);
                        sumIsDecimal = true;
                    }
                } else {
                    addMessage(Status.WARNING, Scope.PARAMETER, parameter,
                               "The %s %s was automatically converted to a number",
                               value.getClass().getSimpleName(), value);
                    if (value instanceof Boolean) {
                        if (Boolean.TRUE.equals(value)) {
                            sum += 1;
                        }
                    } else if (value instanceof Character) {
                        sumSize = 16;
                        sum += (char)value;
                    } else {
                        String operand = value.toString().trim().toLowerCase();
                        if (operand.isEmpty()) {
                            continue;
                        }
                        Matcher matcher = Pattern.compile(NUMBER_REGEX).matcher(operand);
                        if (matcher.find()) {
                            operand = matcher.group(1);
                        }
                        int radix = 10;
                        if (operand.startsWith("0x")) {
                            radix = 16;
                        } else if (operand.startsWith("0b")) {
                            radix = 2;
                        }
                        try {
                            sum += Byte.parseByte(operand, radix);
                        } catch (NumberFormatException notByte) {
                            try {
                                sum += Short.parseShort(operand, radix);
                                sumSize = Math.max(sumSize, 16);
                            } catch (NumberFormatException notShort) {
                                try {
                                    sum += Integer.parseInt(operand, radix);
                                    sumSize = Math.max(sumSize, 32);
                                } catch (NumberFormatException notInteger) {
                                    try {
                                        sum += Long.parseLong(operand, radix);
                                        sumSize = Math.max(sumSize, 64);
                                    } catch (NumberFormatException notLong) {
                                        try {
                                            sum += Float.parseFloat(operand);
                                            sumSize = 32;
                                            sumIsDecimal = true;
                                        } catch (NumberFormatException notFloat) {
                                            try {
                                                sum += Double.parseDouble(operand);
                                                sumSize = 64;
                                                sumIsDecimal = true;
                                            } catch (NumberFormatException ignored) {
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (sumSize == 8) {
            sumParameter.setOutputValue((byte)sum);
        } else if (sumSize == 16) {
            sumParameter.setOutputValue((short)sum);
        } else if (sumSize == 32) {
            if (sumIsDecimal) {
                sumParameter.setOutputValue((float)sum);
            } else {
                sumParameter.setOutputValue((int)sum);
            }
        } else if (sumIsDecimal) {
            sumParameter.setOutputValue(sum);
        } else {
            sumParameter.setOutputValue((long)sum);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(NAME);
        boolean firstOperand = true;
        for (Parameter parameter : parameters()) {
            if (OPERAND_PARAMETER.equals(parameter.name())) {
                if (firstOperand) {
                    firstOperand = false;
                    builder.append(' ');
                } else {
                    builder.append(" + ");
                }
            } else { // Must be sum parameter
                builder.append(" = ");
            }
            builder.append(parameter.stringValue());
        }
        return builder.toString();
    }
}
