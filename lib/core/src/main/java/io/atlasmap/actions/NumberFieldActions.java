/**
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
package io.atlasmap.actions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.AbsoluteValue;
import io.atlasmap.v2.Add;
import io.atlasmap.v2.AreaUnitType;
import io.atlasmap.v2.Average;
import io.atlasmap.v2.Ceiling;
import io.atlasmap.v2.ConvertAreaUnit;
import io.atlasmap.v2.ConvertDistanceUnit;
import io.atlasmap.v2.ConvertMassUnit;
import io.atlasmap.v2.ConvertVolumeUnit;
import io.atlasmap.v2.DistanceUnitType;
import io.atlasmap.v2.Divide;
import io.atlasmap.v2.Floor;
import io.atlasmap.v2.MassUnitType;
import io.atlasmap.v2.Maximum;
import io.atlasmap.v2.Minimum;
import io.atlasmap.v2.Multiply;
import io.atlasmap.v2.Round;
import io.atlasmap.v2.Subtract;
import io.atlasmap.v2.VolumeUnitType;

public class NumberFieldActions implements AtlasFieldAction {
    private static final Logger LOG = LoggerFactory.getLogger(NumberFieldActions.class);

    // 1D
    private static final double KILO_GRAMS_IN_A_POUND = 0.45359237;
    private static final double YARDS_IN_A_MILE = 1760.0;
    private static final double FEET_IN_A_YARD = 3.0;
    private static final double INCHES_IN_A_FOOT = 12.0;
    private static final double METERS_IN_A_INCH = 0.0254;
    // 2D
    private static final double SQUARE_FEET_IN_A_SQUARE_METER = Math.pow(1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT,
            2.0);
    private static final double SQUARE_METERS_IN_A_SQUARE_MILE = Math
            .pow(YARDS_IN_A_MILE * FEET_IN_A_YARD * INCHES_IN_A_FOOT * METERS_IN_A_INCH, 2.0);
    private static final double SQUARE_FEET_IN_A_SQUARE_MILE = Math.pow(YARDS_IN_A_MILE * FEET_IN_A_YARD, 2.0);
    // 3D
    private static final double LITERS_IN_A_CUBIC_METER = 1000.0;
    private static final double CUBIC_FEET_IN_A_CUBIC_METER = Math.pow(1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT, 3.0);
    private static final double GALLONS_US_FLUID_IN_A_CUBIC_METER = 264.17205236;

    private static Map<MassUnitType, Map<MassUnitType, Double>> massConvertionTable;
    static {
        Map<MassUnitType, Map<MassUnitType, Double>> rootTable = new EnumMap<>(MassUnitType.class);
        Map<MassUnitType, Double> kgRates = new EnumMap<>(MassUnitType.class);
        kgRates.put(MassUnitType.KILOGRAM_KG, 1.0);
        kgRates.put(MassUnitType.POUND_LB, 1.0 / KILO_GRAMS_IN_A_POUND);
        rootTable.put(MassUnitType.KILOGRAM_KG, Collections.unmodifiableMap(kgRates));
        Map<MassUnitType, Double> lbsRates = new EnumMap<>(MassUnitType.class);
        lbsRates.put(MassUnitType.KILOGRAM_KG, KILO_GRAMS_IN_A_POUND);
        lbsRates.put(MassUnitType.POUND_LB, 1.0);
        rootTable.put(MassUnitType.POUND_LB, Collections.unmodifiableMap(lbsRates));
        massConvertionTable = Collections.unmodifiableMap(rootTable);
    }

    private static Map<DistanceUnitType, Map<DistanceUnitType, Double>> distanceConvertionTable;
    static {
        Map<DistanceUnitType, Map<DistanceUnitType, Double>> rootTable = new EnumMap<>(DistanceUnitType.class);
        Map<DistanceUnitType, Double> mRates = new EnumMap<>(DistanceUnitType.class);
        mRates.put(DistanceUnitType.METER_M, 1.0);
        mRates.put(DistanceUnitType.FOOT_FT, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT);
        mRates.put(DistanceUnitType.YARD_YD, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT / FEET_IN_A_YARD);
        mRates.put(DistanceUnitType.MILE_MI, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT / FEET_IN_A_YARD / YARDS_IN_A_MILE);
        mRates.put(DistanceUnitType.INCH_IN, 1.0 / METERS_IN_A_INCH);
        rootTable.put(DistanceUnitType.METER_M, Collections.unmodifiableMap(mRates));
        Map<DistanceUnitType, Double> ftRates = new EnumMap<>(DistanceUnitType.class);
        ftRates.put(DistanceUnitType.METER_M, INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        ftRates.put(DistanceUnitType.FOOT_FT, 1.0);
        ftRates.put(DistanceUnitType.YARD_YD, 1.0 / FEET_IN_A_YARD);
        ftRates.put(DistanceUnitType.MILE_MI, 1.0 / FEET_IN_A_YARD / YARDS_IN_A_MILE);
        ftRates.put(DistanceUnitType.INCH_IN, INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.FOOT_FT, Collections.unmodifiableMap(ftRates));
        Map<DistanceUnitType, Double> ydRates = new EnumMap<>(DistanceUnitType.class);
        ydRates.put(DistanceUnitType.METER_M, FEET_IN_A_YARD * INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        ydRates.put(DistanceUnitType.FOOT_FT, FEET_IN_A_YARD);
        ydRates.put(DistanceUnitType.YARD_YD, 1.0);
        ydRates.put(DistanceUnitType.MILE_MI, 1.0 / YARDS_IN_A_MILE);
        ydRates.put(DistanceUnitType.INCH_IN, FEET_IN_A_YARD * INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.YARD_YD, Collections.unmodifiableMap(ydRates));
        Map<DistanceUnitType, Double> miRates = new EnumMap<>(DistanceUnitType.class);
        miRates.put(DistanceUnitType.METER_M, YARDS_IN_A_MILE * FEET_IN_A_YARD * INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        miRates.put(DistanceUnitType.FOOT_FT, YARDS_IN_A_MILE * FEET_IN_A_YARD);
        miRates.put(DistanceUnitType.YARD_YD, YARDS_IN_A_MILE);
        miRates.put(DistanceUnitType.MILE_MI, 1.0);
        miRates.put(DistanceUnitType.INCH_IN, YARDS_IN_A_MILE * FEET_IN_A_YARD * INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.MILE_MI, Collections.unmodifiableMap(miRates));
        Map<DistanceUnitType, Double> inRates = new EnumMap<>(DistanceUnitType.class);
        inRates.put(DistanceUnitType.METER_M, METERS_IN_A_INCH);
        inRates.put(DistanceUnitType.FOOT_FT, 1.0 / INCHES_IN_A_FOOT);
        inRates.put(DistanceUnitType.YARD_YD, 1.0 / INCHES_IN_A_FOOT / FEET_IN_A_YARD);
        inRates.put(DistanceUnitType.MILE_MI, 1.0 / INCHES_IN_A_FOOT / FEET_IN_A_YARD / YARDS_IN_A_MILE);
        inRates.put(DistanceUnitType.INCH_IN, 1.0);
        rootTable.put(DistanceUnitType.INCH_IN, Collections.unmodifiableMap(inRates));
        distanceConvertionTable = Collections.unmodifiableMap(rootTable);
    }

    private static Map<AreaUnitType, Map<AreaUnitType, Double>> areaConvertionTable;
    static {
        Map<AreaUnitType, Map<AreaUnitType, Double>> rootTable = new EnumMap<>(AreaUnitType.class);
        Map<AreaUnitType, Double> m2Rates = new EnumMap<>(AreaUnitType.class);
        m2Rates.put(AreaUnitType.SQUARE_METER, 1.0);
        m2Rates.put(AreaUnitType.SQUARE_FOOT, SQUARE_FEET_IN_A_SQUARE_METER);
        m2Rates.put(AreaUnitType.SQUARE_MILE, 1.0 / SQUARE_METERS_IN_A_SQUARE_MILE);
        rootTable.put(AreaUnitType.SQUARE_METER, Collections.unmodifiableMap(m2Rates));
        Map<AreaUnitType, Double> ft2Rates = new EnumMap<>(AreaUnitType.class);
        ft2Rates.put(AreaUnitType.SQUARE_METER, 1.0 / SQUARE_FEET_IN_A_SQUARE_METER);
        ft2Rates.put(AreaUnitType.SQUARE_FOOT, 1.0);
        ft2Rates.put(AreaUnitType.SQUARE_MILE, 1.0 / SQUARE_FEET_IN_A_SQUARE_MILE);
        rootTable.put(AreaUnitType.SQUARE_FOOT, Collections.unmodifiableMap(ft2Rates));
        Map<AreaUnitType, Double> mi2Rates = new EnumMap<>(AreaUnitType.class);
        mi2Rates.put(AreaUnitType.SQUARE_METER, SQUARE_METERS_IN_A_SQUARE_MILE);
        mi2Rates.put(AreaUnitType.SQUARE_FOOT, SQUARE_FEET_IN_A_SQUARE_MILE);
        mi2Rates.put(AreaUnitType.SQUARE_MILE, 1.0);
        rootTable.put(AreaUnitType.SQUARE_MILE, Collections.unmodifiableMap(mi2Rates));
        areaConvertionTable = Collections.unmodifiableMap(rootTable);
    }

    private static Map<VolumeUnitType, Map<VolumeUnitType, Double>> volumeConvertionTable;
    static {
        Map<VolumeUnitType, Map<VolumeUnitType, Double>> rootTable = new EnumMap<>(VolumeUnitType.class);
        Map<VolumeUnitType, Double> m3Rates = new EnumMap<>(VolumeUnitType.class);
        m3Rates.put(VolumeUnitType.CUBIC_METER, 1.0);
        m3Rates.put(VolumeUnitType.LITER, LITERS_IN_A_CUBIC_METER);
        m3Rates.put(VolumeUnitType.CUBIC_FOOT, CUBIC_FEET_IN_A_CUBIC_METER);
        m3Rates.put(VolumeUnitType.GALLON_US_FLUID, GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.CUBIC_METER, Collections.unmodifiableMap(m3Rates));
        Map<VolumeUnitType, Double> literRates = new EnumMap<>(VolumeUnitType.class);
        literRates.put(VolumeUnitType.CUBIC_METER, 1.0 / LITERS_IN_A_CUBIC_METER);
        literRates.put(VolumeUnitType.LITER, 1.0);
        literRates.put(VolumeUnitType.CUBIC_FOOT, 1.0 / LITERS_IN_A_CUBIC_METER * CUBIC_FEET_IN_A_CUBIC_METER);
        literRates.put(VolumeUnitType.GALLON_US_FLUID,
                1.0 / LITERS_IN_A_CUBIC_METER * GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.LITER, Collections.unmodifiableMap(literRates));
        Map<VolumeUnitType, Double> cftRates = new EnumMap<>(VolumeUnitType.class);
        cftRates.put(VolumeUnitType.CUBIC_METER, 1.0 / CUBIC_FEET_IN_A_CUBIC_METER);
        cftRates.put(VolumeUnitType.LITER, 1.0 / CUBIC_FEET_IN_A_CUBIC_METER * LITERS_IN_A_CUBIC_METER);
        cftRates.put(VolumeUnitType.CUBIC_FOOT, 1.0);
        cftRates.put(VolumeUnitType.GALLON_US_FLUID,
                1.0 / CUBIC_FEET_IN_A_CUBIC_METER * GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.CUBIC_FOOT, Collections.unmodifiableMap(cftRates));
        Map<VolumeUnitType, Double> galUsFluidRates = new EnumMap<>(VolumeUnitType.class);
        galUsFluidRates.put(VolumeUnitType.CUBIC_METER, 1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.LITER, 1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER * LITERS_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.CUBIC_FOOT,
                1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER * CUBIC_FEET_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.GALLON_US_FLUID, 1.0);
        rootTable.put(VolumeUnitType.GALLON_US_FLUID, Collections.unmodifiableMap(galUsFluidRates));
        volumeConvertionTable = Collections.unmodifiableMap(rootTable);
    }

    @AtlasActionProcessor
    public static Number absoluteValue(AbsoluteValue action, Number input) {
        if (input == null) {
            return 0;
        }
        if (input instanceof BigDecimal) {
            return ((BigDecimal) input).abs();
        }
        if (requiresDoubleResult(input)) {
            return Math.abs(input.doubleValue());
        }
        return Math.abs(input.longValue());
    }

    @AtlasActionProcessor
    public static Number add(Add action, List<Number> inputs) {
        if (inputs == null) {
            return 0;
        }

        Number sum = 0L;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (sum instanceof BigDecimal) {
                    sum = ((BigDecimal) sum).add(BigDecimal.valueOf(((Number) entry).doubleValue()));
                } else if (entry instanceof BigDecimal) {
                    sum = BigDecimal.valueOf(sum.doubleValue()).add((BigDecimal) entry);
                } else if (requiresDoubleResult(sum) || requiresDoubleResult(entry)) {
                    sum = sum.doubleValue() + ((Number) entry).doubleValue();
                } else {
                    sum = sum.longValue() + ((Number) entry).longValue();
                }
            } else {
                warnIgnoringValue("Add", entry);
            }
        }

        return sum;
    }

    @AtlasActionProcessor
    public static Number average(Average action, List<Number> inputs) {
        if (inputs == null) {
            return 0;
        }
        return add(null, inputs).doubleValue() / inputs.size();
    }

    @AtlasActionProcessor
    public static Number ceiling(Ceiling action, Number input) {
        return input == null ? 0L : (long)Math.ceil(input.doubleValue());
    }

    @AtlasActionProcessor
    public static Number convertMassUnit(ConvertMassUnit convertMassUnit, Number input) {
        if (input == null) {
            return 0;
        }

        if (convertMassUnit == null || convertMassUnit.getFromUnit() == null
                || convertMassUnit.getToUnit() == null) {
            throw new IllegalArgumentException("ConvertMassUnit must be specified  with fromUnit and toUnit");
        }

        MassUnitType fromUnit = convertMassUnit.getFromUnit();
        MassUnitType toUnit = convertMassUnit.getToUnit();
        double rate = massConvertionTable.get(fromUnit).get(toUnit);
        return doMultiply(input, rate);
    }

    @AtlasActionProcessor
    public static Number convertDistanceUnit(ConvertDistanceUnit convertDistanceUnit, Number input) {
        if (input == null) {
            return 0;
        }

        if (convertDistanceUnit == null || convertDistanceUnit.getFromUnit() == null
                || convertDistanceUnit.getToUnit() == null) {
            throw new IllegalArgumentException("ConvertDistanceUnit must be specified  with fromUnit and toUnit");
        }

        DistanceUnitType fromUnit = convertDistanceUnit.getFromUnit();
        DistanceUnitType toUnit = convertDistanceUnit.getToUnit();
        double rate = distanceConvertionTable.get(fromUnit).get(toUnit);
        return doMultiply(input, rate);
    }

    @AtlasActionProcessor
    public static Number convertAreaUnit(ConvertAreaUnit convertAreaUnit, Number input) {
        if (input == null) {
            return 0;
        }

        if (convertAreaUnit == null || convertAreaUnit.getFromUnit() == null
                || convertAreaUnit.getToUnit() == null) {
            throw new IllegalArgumentException("ConvertAreaUnit must be specified  with fromUnit and toUnit");
        }

        AreaUnitType fromUnit = convertAreaUnit.getFromUnit();
        AreaUnitType toUnit = convertAreaUnit.getToUnit();
        double rate = areaConvertionTable.get(fromUnit).get(toUnit);
        return doMultiply(input, rate);
    }

    @AtlasActionProcessor
    public static Number convertVolumeUnit(ConvertVolumeUnit convertVolumeUnit, Number input) {
        if (input == null) {
            return 0;
        }

        if (convertVolumeUnit == null || convertVolumeUnit.getFromUnit() == null
                || convertVolumeUnit.getToUnit() == null) {
            throw new IllegalArgumentException("ConvertVolumeUnit must be specified  with fromUnit and toUnit");
        }

        VolumeUnitType fromUnit = convertVolumeUnit.getFromUnit();
        VolumeUnitType toUnit = convertVolumeUnit.getToUnit();
        double rate = volumeConvertionTable.get(fromUnit).get(toUnit);
        return doMultiply(input, rate);
    }

    @AtlasActionProcessor
    public static Number divide(Divide divide, List<Number> inputs) {
        if (inputs == null) {
            return 0;
        }

        Number quotient = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (quotient == null) {
                    quotient = (Number) entry;
                } else if (quotient instanceof BigDecimal) {
                    quotient = ((BigDecimal) quotient).divide(BigDecimal.valueOf(((Number) entry).doubleValue()));
                } else if (entry instanceof BigDecimal) {
                    quotient = BigDecimal.valueOf(quotient.doubleValue()).divide((BigDecimal) entry);
                } else {
                    quotient = quotient.doubleValue() / ((Number) entry).doubleValue();
                }
            } else {
                warnIgnoringValue("Divide", entry);
            }
        }

        return quotient;
    }

    @AtlasActionProcessor
    public static Number floor(Floor floor, Number input) {
        return input == null ? 0L : (long)Math.floor(input.doubleValue());
    }

    @AtlasActionProcessor
    public static Number maximum(Maximum maximum, List<Number> inputs) {
        if (inputs == null) {
            return 0;
        }

        Number max = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (max instanceof BigDecimal && entry instanceof BigDecimal) {
                    max = ((BigDecimal) entry).max((BigDecimal)max);
                } else if (max == null || ((Number) entry).doubleValue() > max.doubleValue()) {
                    max = (Number) entry;
                }
            } else {
                warnIgnoringValue("Maximum", entry);
            }
        }

        return max;
    }

    @AtlasActionProcessor
    public static Number minimum(Minimum minimum, List<Number> inputs) {
        if (inputs == null) {
            return 0;
        }

        Number min = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (min instanceof BigDecimal && entry instanceof BigDecimal) {
                    min = ((BigDecimal) entry).min((BigDecimal)min);
                } else if (min == null || ((Number) entry).doubleValue() < min.doubleValue()) {
                    min = (Number) entry;
                }
            } else {
                warnIgnoringValue("Minimum", entry);
            }
        }

        return min;
    }

    @AtlasActionProcessor
    public static Number multiply(Multiply multiply, List<Number> inputs) {
        if (inputs == null) {
            return 0;
        }

        Number product = 1L;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (product instanceof BigDecimal) {
                    product = ((BigDecimal) product).multiply(BigDecimal.valueOf(((Number) entry).doubleValue()));
                } else if (entry instanceof BigDecimal) {
                    product = BigDecimal.valueOf(product.doubleValue()).multiply((BigDecimal) entry);
                } else if (requiresDoubleResult(product) || requiresDoubleResult(entry)) {
                    product = product.doubleValue() * ((Number) entry).doubleValue();
                } else {
                    product = product.longValue() * ((Number) entry).longValue();
                }
            } else {
                warnIgnoringValue("Multiply", entry);
            }
        }

        return product;
    }

    @AtlasActionProcessor
    public static Number round(Round action, Number input) {
        return input == null ? 0L : Math.round(input.doubleValue());
    }

    @AtlasActionProcessor
    public static Number subtract(Subtract subtract, List<Number> inputs) {
        if (inputs == null) {
            return 0;
        }

        Number difference = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (difference == null) {
                    difference = (Number) entry;
                } else if (difference instanceof BigDecimal) {
                    difference = ((BigDecimal) difference).subtract(BigDecimal.valueOf(((Number) entry).doubleValue()));
                } else if (entry instanceof BigDecimal) {
                    difference = BigDecimal.valueOf(difference.doubleValue()).subtract((BigDecimal) entry);
                } else if (requiresDoubleResult(difference) || requiresDoubleResult(entry)) {
                    difference = difference.doubleValue() - ((Number) entry).doubleValue();
                } else {
                    difference = difference.longValue() - ((Number) entry).longValue();
                }
            } else {
                warnIgnoringValue("Subtract", entry);
            }
        }

        return difference;
    }

    private static Number doMultiply(Number input, double rate) {
        if (input instanceof BigDecimal) {
            return ((BigDecimal) input).multiply(BigDecimal.valueOf(rate));
        }
        return (input.doubleValue() * rate);
    }

    private static boolean requiresDoubleResult(Object object) {
        return object instanceof Double || object instanceof Float;
    }

    /**
     * @TODO Add audit via @AtlasSession instead - https://github.com/atlasmap/atlasmap/issues/1269
     * @param value value
     */
    private static void warnIgnoringValue(String action, Object value) {
        LOG.warn("The source collection/arry/map must only contain numbers for '{}' transformation - ignoring '{}'",
            action, value != null ? value : "null");
    }
}
