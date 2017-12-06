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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.AreaUnitType;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.ConvertAreaUnit;
import io.atlasmap.v2.ConvertDistanceUnit;
import io.atlasmap.v2.ConvertMassUnit;
import io.atlasmap.v2.ConvertVolumeUnit;
import io.atlasmap.v2.DistanceUnitType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.MassUnitType;
import io.atlasmap.v2.VolumeUnitType;

public class NumberFieldActions implements AtlasFieldAction {
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
    private static final double LITTERS_IN_A_CUBIC_METER = 1000.0;
    private static final double CUBIC_FEET_IN_A_CUBIC_METER = Math.pow(1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT, 3.0);
    private static final double GALLONS_US_FLUID_IN_A_CUBIC_METER = 264.17205236;

    private static final String COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG = "The source collection/arry/map must only contain numbers";

    private static Map<MassUnitType, Map<MassUnitType, Double>> massConvertionTable;
    static {
        Map<MassUnitType, Map<MassUnitType, Double>> rootTable = new EnumMap<>(MassUnitType.class);
        Map<MassUnitType, Double> kgRates = new EnumMap<>(MassUnitType.class);
        kgRates.put(MassUnitType.KILO_GRAM, 1.0);
        kgRates.put(MassUnitType.POUND, 1.0 / KILO_GRAMS_IN_A_POUND);
        rootTable.put(MassUnitType.KILO_GRAM, Collections.unmodifiableMap(kgRates));
        Map<MassUnitType, Double> lbsRates = new EnumMap<>(MassUnitType.class);
        lbsRates.put(MassUnitType.KILO_GRAM, KILO_GRAMS_IN_A_POUND);
        lbsRates.put(MassUnitType.POUND, 1.0);
        rootTable.put(MassUnitType.POUND, Collections.unmodifiableMap(lbsRates));
        massConvertionTable = Collections.unmodifiableMap(rootTable);
    }

    private static Map<DistanceUnitType, Map<DistanceUnitType, Double>> distanceConvertionTable;
    static {
        Map<DistanceUnitType, Map<DistanceUnitType, Double>> rootTable = new EnumMap<>(DistanceUnitType.class);
        Map<DistanceUnitType, Double> mRates = new EnumMap<>(DistanceUnitType.class);
        mRates.put(DistanceUnitType.METER, 1.0);
        mRates.put(DistanceUnitType.FOOT, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT);
        mRates.put(DistanceUnitType.YARD, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT / FEET_IN_A_YARD);
        mRates.put(DistanceUnitType.MILE, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT / FEET_IN_A_YARD / YARDS_IN_A_MILE);
        mRates.put(DistanceUnitType.INCH, 1.0 / METERS_IN_A_INCH);
        rootTable.put(DistanceUnitType.METER, Collections.unmodifiableMap(mRates));
        Map<DistanceUnitType, Double> ftRates = new EnumMap<>(DistanceUnitType.class);
        ftRates.put(DistanceUnitType.METER, INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        ftRates.put(DistanceUnitType.FOOT, 1.0);
        ftRates.put(DistanceUnitType.YARD, 1.0 / FEET_IN_A_YARD);
        ftRates.put(DistanceUnitType.MILE, 1.0 / FEET_IN_A_YARD / YARDS_IN_A_MILE);
        ftRates.put(DistanceUnitType.INCH, INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.FOOT, Collections.unmodifiableMap(ftRates));
        Map<DistanceUnitType, Double> ydRates = new EnumMap<>(DistanceUnitType.class);
        ydRates.put(DistanceUnitType.METER, FEET_IN_A_YARD * INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        ydRates.put(DistanceUnitType.FOOT, FEET_IN_A_YARD);
        ydRates.put(DistanceUnitType.YARD, 1.0);
        ydRates.put(DistanceUnitType.MILE, 1.0 / YARDS_IN_A_MILE);
        ydRates.put(DistanceUnitType.INCH, FEET_IN_A_YARD * INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.YARD, Collections.unmodifiableMap(ydRates));
        Map<DistanceUnitType, Double> miRates = new EnumMap<>(DistanceUnitType.class);
        miRates.put(DistanceUnitType.METER, YARDS_IN_A_MILE * FEET_IN_A_YARD * INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        miRates.put(DistanceUnitType.FOOT, YARDS_IN_A_MILE * FEET_IN_A_YARD);
        miRates.put(DistanceUnitType.YARD, YARDS_IN_A_MILE);
        miRates.put(DistanceUnitType.MILE, 1.0);
        miRates.put(DistanceUnitType.INCH, YARDS_IN_A_MILE * FEET_IN_A_YARD * INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.MILE, Collections.unmodifiableMap(miRates));
        Map<DistanceUnitType, Double> inRates = new EnumMap<>(DistanceUnitType.class);
        inRates.put(DistanceUnitType.METER, METERS_IN_A_INCH);
        inRates.put(DistanceUnitType.FOOT, 1.0 / INCHES_IN_A_FOOT);
        inRates.put(DistanceUnitType.YARD, 1.0 / INCHES_IN_A_FOOT / FEET_IN_A_YARD);
        inRates.put(DistanceUnitType.MILE, 1.0 / INCHES_IN_A_FOOT / FEET_IN_A_YARD / YARDS_IN_A_MILE);
        inRates.put(DistanceUnitType.INCH, 1.0);
        rootTable.put(DistanceUnitType.INCH, Collections.unmodifiableMap(inRates));
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
        m3Rates.put(VolumeUnitType.LITTER, LITTERS_IN_A_CUBIC_METER);
        m3Rates.put(VolumeUnitType.CUBIC_FOOT, CUBIC_FEET_IN_A_CUBIC_METER);
        m3Rates.put(VolumeUnitType.GALLON_US_FLUID, GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.CUBIC_METER, Collections.unmodifiableMap(m3Rates));
        Map<VolumeUnitType, Double> litterRates = new EnumMap<>(VolumeUnitType.class);
        litterRates.put(VolumeUnitType.CUBIC_METER, 1.0 / LITTERS_IN_A_CUBIC_METER);
        litterRates.put(VolumeUnitType.LITTER, 1.0);
        litterRates.put(VolumeUnitType.CUBIC_FOOT, 1.0 / LITTERS_IN_A_CUBIC_METER * CUBIC_FEET_IN_A_CUBIC_METER);
        litterRates.put(VolumeUnitType.GALLON_US_FLUID,
                1.0 / LITTERS_IN_A_CUBIC_METER * GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.LITTER, Collections.unmodifiableMap(litterRates));
        Map<VolumeUnitType, Double> cftRates = new EnumMap<>(VolumeUnitType.class);
        cftRates.put(VolumeUnitType.CUBIC_METER, 1.0 / CUBIC_FEET_IN_A_CUBIC_METER);
        cftRates.put(VolumeUnitType.LITTER, 1.0 / CUBIC_FEET_IN_A_CUBIC_METER * LITTERS_IN_A_CUBIC_METER);
        cftRates.put(VolumeUnitType.CUBIC_FOOT, 1.0);
        cftRates.put(VolumeUnitType.GALLON_US_FLUID,
                1.0 / CUBIC_FEET_IN_A_CUBIC_METER * GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.CUBIC_FOOT, Collections.unmodifiableMap(cftRates));
        Map<VolumeUnitType, Double> galUsFluidRates = new EnumMap<>(VolumeUnitType.class);
        galUsFluidRates.put(VolumeUnitType.CUBIC_METER, 1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.LITTER, 1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER * LITTERS_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.CUBIC_FOOT,
                1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER * CUBIC_FEET_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.GALLON_US_FLUID, 1.0);
        rootTable.put(VolumeUnitType.GALLON_US_FLUID, Collections.unmodifiableMap(galUsFluidRates));
        volumeConvertionTable = Collections.unmodifiableMap(rootTable);
    }

    @AtlasFieldActionInfo(name = "AbsoluteValue", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Number absoluteValue(Action action, Number input) {
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

    @AtlasFieldActionInfo(name = "Add", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Number add(Action action, Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = collection(input);

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
                throw new IllegalArgumentException(COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return sum;
    }

    @AtlasFieldActionInfo(name = "Average", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Number average(Action action, Object input) {
        if (input == null) {
            return 0;
        }
        Collection<?> inputs = collection(input);
        return add(null, input).doubleValue() / inputs.size();
    }

    @AtlasFieldActionInfo(name = "Ceiling", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Number ceiling(Action action, Number input) {
        return input == null ? 0L : (long)Math.ceil(input.doubleValue());
    }

    @AtlasFieldActionInfo(name = "ConvertMassUnit", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Number convertMassUnit(Action action, Number input) {
        if (input == null) {
            return 0;
        }

        if (action == null || !(action instanceof ConvertMassUnit) || ((ConvertMassUnit) action).getFromUnit() == null
                || ((ConvertMassUnit) action).getToUnit() == null) {
            throw new IllegalArgumentException("ConvertMassUnit must be specfied  with fromUnit and toUnit");
        }

        MassUnitType fromUnit = ((ConvertMassUnit) action).getFromUnit();
        MassUnitType toUnit = ((ConvertMassUnit) action).getToUnit();
        double rate = massConvertionTable.get(fromUnit).get(toUnit);
        return doMultiply(input, rate);
    }

    @AtlasFieldActionInfo(name = "ConvertDistanceUnit", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Number convertDistanceUnit(Action action, Number input) {
        if (input == null) {
            return 0;
        }

        if (action == null || !(action instanceof ConvertDistanceUnit)
                || ((ConvertDistanceUnit) action).getFromUnit() == null
                || ((ConvertDistanceUnit) action).getToUnit() == null) {
            throw new IllegalArgumentException("ConvertDistanceUnit must be specfied  with fromUnit and toUnit");
        }

        DistanceUnitType fromUnit = ((ConvertDistanceUnit) action).getFromUnit();
        DistanceUnitType toUnit = ((ConvertDistanceUnit) action).getToUnit();
        double rate = distanceConvertionTable.get(fromUnit).get(toUnit);
        return doMultiply(input, rate);
    }

    @AtlasFieldActionInfo(name = "ConvertAreaUnit", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Number convertAreaUnit(Action action, Number input) {
        if (input == null) {
            return 0;
        }

        if (action == null || !(action instanceof ConvertAreaUnit) || ((ConvertAreaUnit) action).getFromUnit() == null
                || ((ConvertAreaUnit) action).getToUnit() == null) {
            throw new IllegalArgumentException("ConvertAreaUnit must be specfied  with fromUnit and toUnit");
        }

        AreaUnitType fromUnit = ((ConvertAreaUnit) action).getFromUnit();
        AreaUnitType toUnit = ((ConvertAreaUnit) action).getToUnit();
        double rate = areaConvertionTable.get(fromUnit).get(toUnit);
        return doMultiply(input, rate);
    }

    @AtlasFieldActionInfo(name = "ConvertVolumeUnit", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Number convertVolumeUnit(Action action, Number input) {
        if (input == null) {
            return 0;
        }

        if (action == null || !(action instanceof ConvertVolumeUnit)
                || ((ConvertVolumeUnit) action).getFromUnit() == null
                || ((ConvertVolumeUnit) action).getToUnit() == null) {
            throw new IllegalArgumentException("ConvertVolumeUnit must be specfied  with fromUnit and toUnit");
        }

        VolumeUnitType fromUnit = ((ConvertVolumeUnit) action).getFromUnit();
        VolumeUnitType toUnit = ((ConvertVolumeUnit) action).getToUnit();
        double rate = volumeConvertionTable.get(fromUnit).get(toUnit);
        return doMultiply(input, rate);
    }

    @AtlasFieldActionInfo(name = "Divide", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Number divide(Action action, Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = collection(input);

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
                throw new IllegalArgumentException(COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return quotient;
    }

    @AtlasFieldActionInfo(name = "Floor", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Number floor(Action action, Number input) {
        return input == null ? 0L : (long)Math.floor(input.doubleValue());
    }

    @AtlasFieldActionInfo(name = "Maximum", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Number maximum(Action action, Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = collection(input);

        Number max = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (max instanceof BigDecimal && entry instanceof BigDecimal) {
                    max = ((BigDecimal) entry).max((BigDecimal)max);
                } else if (max == null || ((Number) entry).doubleValue() > max.doubleValue()) {
                    max = (Number) entry;
                }
            } else {
                throw new IllegalArgumentException(COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return max;
    }

    @AtlasFieldActionInfo(name = "Minimum", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Number minimum(Action action, Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = collection(input);

        Number min = null;
        for (Object entry : inputs) {
            if (entry instanceof Number) {
                if (min instanceof BigDecimal && entry instanceof BigDecimal) {
                    min = ((BigDecimal) entry).min((BigDecimal)min);
                } else if (min == null || ((Number) entry).doubleValue() < min.doubleValue()) {
                    min = (Number) entry;
                }
            } else {
                throw new IllegalArgumentException(COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return min;
    }

    @AtlasFieldActionInfo(name = "Multiply", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Number multiply(Action action, Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = collection(input);

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
                throw new IllegalArgumentException(COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return product;
    }

    @AtlasFieldActionInfo(name = "Round", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Number round(Action action, Number input) {
        return input == null ? 0L : Math.round(input.doubleValue());
    }

    @AtlasFieldActionInfo(name = "Subtract", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Number subtract(Action action, Object input) {
        if (input == null) {
            return 0;
        }

        Collection<?> inputs = collection(input);

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
                throw new IllegalArgumentException(COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG);
            }
        }

        return difference;
    }

    private static Collection<?> collection(Object input) {
        if (input instanceof Collection) {
            return (Collection<?>) input;
        } else if (input instanceof Map) {
            return ((Map<?, ?>) input).values();
        } else if (input instanceof Number[]) {
            return Arrays.asList((Object[]) input);
        } else if (input instanceof double[]) {
            double[] din = (double[]) input;
            List<Double> dinList = new ArrayList<>(din.length);
            for (double e : din) {
                dinList.add(e);
            }
            return dinList;
        } else if (input instanceof float[]) {
            float[] fin = (float[]) input;
            List<Float> finList = new ArrayList<>(fin.length);
            for (float e : fin) {
                finList.add(e);
            }
            return finList;
        } else if (input instanceof long[]) {
            long[] lin = (long[]) input;
            List<Long> linList = new ArrayList<>(lin.length);
            for (long e : lin) {
                linList.add(e);
            }
            return linList;
        } else if (input instanceof int[]) {
            int[] iin = (int[]) input;
            List<Integer> iinList = new ArrayList<>(iin.length);
            for (int e : iin) {
                iinList.add(e);
            }
            return iinList;
        } else if (input instanceof byte[]) {
            byte[] bin = (byte[]) input;
            List<Byte> binList = new ArrayList<>(bin.length);
            for (byte e : bin) {
                binList.add(e);
            }
            return binList;
        } else {
            throw new IllegalArgumentException(
                    "Illegal input[" + input + "]. Input must be a Collection, Map or array of numbers");
        }
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
}
