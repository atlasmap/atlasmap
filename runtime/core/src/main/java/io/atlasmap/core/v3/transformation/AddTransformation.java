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
        addParameter(new BaseParameter(this, OPERAND_PARAMETER + 1, Role.INPUT, false, false,
                                       "A numberic source field, property, or constant to add"));
        addParameter(new BaseParameter(this, OPERAND_PARAMETER + 2, Role.INPUT, false, true,
                                       "A numberic source field, property, or constant to add"));
        sumParameter = addParameter(new BaseParameter(this, SUM_PARAMETER, Role.OUTPUT, false, false,
                                                      "A target field or property to which to add the sum of the operands"));
    }

    /**
     * @see BaseTransformation#execute()
     */
    @Override
    protected void execute() {
        Double sum = 0.0;
        boolean sumIsDecimal = false;
        for (Parameter parameter : parameters()) {
            if (parameter.name().startsWith(OPERAND_PARAMETER)) {
                Object value = parameter.value();
                if (value == null) {
                    continue;
                }
                if (value instanceof Number) {
                    sum += ((Number)value).doubleValue();
                    if (value instanceof Float || value instanceof Double) {
                        sumIsDecimal = true;
                    }
                } else {
                    Number convertedNumber = 0;
                    if (value instanceof Boolean) {
                        if (Boolean.TRUE.equals(value)) {
                            convertedNumber = 1;
                        }
                    } else if (value instanceof Character) {
                        convertedNumber = Integer.valueOf((Character)value);
                    } else {
                        String operand = value.toString().trim().toLowerCase();
                        if (!operand.isEmpty()) {
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
                                convertedNumber = Byte.parseByte(operand, radix);
                            } catch (NumberFormatException notByte) {
                                try {
                                    convertedNumber = Short.parseShort(operand, radix);
                                } catch (NumberFormatException notShort) {
                                    try {
                                        convertedNumber = Integer.parseInt(operand, radix);
                                    } catch (NumberFormatException notInteger) {
                                        try {
                                            convertedNumber = Long.parseLong(operand, radix);
                                        } catch (NumberFormatException notLong) {
                                            try {
                                                convertedNumber = Float.parseFloat(operand);
                                                sumIsDecimal = true;
                                            } catch (NumberFormatException notFloat) {
                                                try {
                                                    convertedNumber = Double.parseDouble(operand);
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
                    addMessage(Status.WARNING, parameter, "The %s %s was automatically converted to the %s %s",
                               value.getClass().getSimpleName().toLowerCase(), value instanceof String ? "'" + value + "'": value,
                               convertedNumber.getClass().getSimpleName().toLowerCase(), convertedNumber);
                    sum += convertedNumber.doubleValue();
                }
            }
        }
        if (sumIsDecimal) {
            if (sum.doubleValue() == sum.floatValue()) {
                sumParameter.setOutputValue(sum.floatValue());
            } else {
                sumParameter.setOutputValue(sum);
            }
        } else {
            if (sum.doubleValue() == sum.byteValue()) {
                sumParameter.setOutputValue(sum.byteValue());
            } else if (sum.doubleValue() == sum.shortValue()) {
                sumParameter.setOutputValue(sum.shortValue());
            } else if (sum.doubleValue() == sum.intValue()) {
                sumParameter.setOutputValue(sum.intValue());
            } else {
                sumParameter.setOutputValue(sum.longValue());
            }
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
