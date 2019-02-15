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
package io.atlasmap.v2;

import java.math.BigDecimal;

import io.atlasmap.api.AtlasFieldAction;

/**
 *
 */
public class BaseConvertUnit extends Action implements AtlasFieldAction {

    private static final long serialVersionUID = 1L;

    // 1D
    protected static final double KILO_GRAMS_IN_A_POUND = 0.45359237;
    protected static final double YARDS_IN_A_MILE = 1760.0;
    protected static final double FEET_IN_A_YARD = 3.0;
    protected static final double INCHES_IN_A_FOOT = 12.0;
    protected static final double METERS_IN_A_INCH = 0.0254;

    protected static Number multiply(Number input, double rate) {
        if (input instanceof BigDecimal) {
            return ((BigDecimal) input).multiply(BigDecimal.valueOf(rate));
        }
        return (input.doubleValue() * rate);
    }
}
