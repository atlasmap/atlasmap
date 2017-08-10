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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
import io.atlasmap.v2.NumberType;
import io.atlasmap.v2.SumUp;
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

    private static Map<MassUnitType, Map<MassUnitType, Double>> massConvertionTable;
    static {
        Map<MassUnitType, Map<MassUnitType, Double>> rootTable = new HashMap<>();
        Map<MassUnitType, Double> kgRates = new HashMap<>();
        kgRates.put(MassUnitType.KILO_GRAM, 1.0);
        kgRates.put(MassUnitType.POUND, 1.0 / KILO_GRAMS_IN_A_POUND);
        rootTable.put(MassUnitType.KILO_GRAM, Collections.unmodifiableMap(kgRates));
        Map<MassUnitType, Double> lbsRates = new HashMap<>();
        lbsRates.put(MassUnitType.KILO_GRAM, KILO_GRAMS_IN_A_POUND);
        lbsRates.put(MassUnitType.POUND, 1.0);
        rootTable.put(MassUnitType.POUND, Collections.unmodifiableMap(lbsRates));
        massConvertionTable = Collections.unmodifiableMap(rootTable);
    }

    private static Map<DistanceUnitType, Map<DistanceUnitType, Double>> distanceConvertionTable;
    static {
        Map<DistanceUnitType, Map<DistanceUnitType, Double>> rootTable = new HashMap<>();
        Map<DistanceUnitType, Double> mRates = new HashMap<>();
        mRates.put(DistanceUnitType.METER, 1.0);
        mRates.put(DistanceUnitType.FOOT, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT);
        mRates.put(DistanceUnitType.YARD, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT / FEET_IN_A_YARD);
        mRates.put(DistanceUnitType.MILE, 1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT / FEET_IN_A_YARD / YARDS_IN_A_MILE);
        mRates.put(DistanceUnitType.INCH, 1.0 / METERS_IN_A_INCH);
        rootTable.put(DistanceUnitType.METER, Collections.unmodifiableMap(mRates));
        Map<DistanceUnitType, Double> ftRates = new HashMap<>();
        ftRates.put(DistanceUnitType.METER, INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        ftRates.put(DistanceUnitType.FOOT, 1.0);
        ftRates.put(DistanceUnitType.YARD, 1.0 / FEET_IN_A_YARD);
        ftRates.put(DistanceUnitType.MILE, 1.0 / FEET_IN_A_YARD / YARDS_IN_A_MILE);
        ftRates.put(DistanceUnitType.INCH, INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.FOOT, Collections.unmodifiableMap(ftRates));
        Map<DistanceUnitType, Double> ydRates = new HashMap<>();
        ydRates.put(DistanceUnitType.METER, FEET_IN_A_YARD * INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        ydRates.put(DistanceUnitType.FOOT, FEET_IN_A_YARD);
        ydRates.put(DistanceUnitType.YARD, 1.0);
        ydRates.put(DistanceUnitType.MILE, 1.0 / YARDS_IN_A_MILE);
        ydRates.put(DistanceUnitType.INCH, FEET_IN_A_YARD * INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.YARD, Collections.unmodifiableMap(ydRates));
        Map<DistanceUnitType, Double> miRates = new HashMap<>();
        miRates.put(DistanceUnitType.METER, YARDS_IN_A_MILE * FEET_IN_A_YARD * INCHES_IN_A_FOOT * METERS_IN_A_INCH);
        miRates.put(DistanceUnitType.FOOT, YARDS_IN_A_MILE * FEET_IN_A_YARD);
        miRates.put(DistanceUnitType.YARD, YARDS_IN_A_MILE);
        miRates.put(DistanceUnitType.MILE, 1.0);
        miRates.put(DistanceUnitType.INCH, YARDS_IN_A_MILE * FEET_IN_A_YARD * INCHES_IN_A_FOOT);
        rootTable.put(DistanceUnitType.MILE, Collections.unmodifiableMap(miRates));
        Map<DistanceUnitType, Double> inRates = new HashMap<>();
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
        Map<AreaUnitType, Map<AreaUnitType, Double>> rootTable = new HashMap<>();
        Map<AreaUnitType, Double> m2Rates = new HashMap<>();
        m2Rates.put(AreaUnitType.SQUARE_METER, 1.0);
        m2Rates.put(AreaUnitType.SQUARE_FOOT, SQUARE_FEET_IN_A_SQUARE_METER);
        m2Rates.put(AreaUnitType.SQUARE_MILE, 1.0 / SQUARE_METERS_IN_A_SQUARE_MILE);
        rootTable.put(AreaUnitType.SQUARE_METER, Collections.unmodifiableMap(m2Rates));
        Map<AreaUnitType, Double> ft2Rates = new HashMap<>();
        ft2Rates.put(AreaUnitType.SQUARE_METER, 1.0 / SQUARE_FEET_IN_A_SQUARE_METER);
        ft2Rates.put(AreaUnitType.SQUARE_FOOT, 1.0);
        ft2Rates.put(AreaUnitType.SQUARE_MILE, 1.0 / SQUARE_FEET_IN_A_SQUARE_MILE);
        rootTable.put(AreaUnitType.SQUARE_FOOT, Collections.unmodifiableMap(ft2Rates));
        Map<AreaUnitType, Double> mi2Rates = new HashMap<>();
        mi2Rates.put(AreaUnitType.SQUARE_METER, SQUARE_METERS_IN_A_SQUARE_MILE);
        mi2Rates.put(AreaUnitType.SQUARE_FOOT, SQUARE_FEET_IN_A_SQUARE_MILE);
        mi2Rates.put(AreaUnitType.SQUARE_MILE, 1.0);
        rootTable.put(AreaUnitType.SQUARE_MILE, Collections.unmodifiableMap(mi2Rates));
        areaConvertionTable = Collections.unmodifiableMap(rootTable);
    }

    private static Map<VolumeUnitType, Map<VolumeUnitType, Double>> volumeConvertionTable;
    static {
        Map<VolumeUnitType, Map<VolumeUnitType, Double>> rootTable = new HashMap<>();
        Map<VolumeUnitType, Double> m3Rates = new HashMap<>();
        m3Rates.put(VolumeUnitType.CUBIC_METER, 1.0);
        m3Rates.put(VolumeUnitType.LITTER, LITTERS_IN_A_CUBIC_METER);
        m3Rates.put(VolumeUnitType.CUBIC_FOOT, CUBIC_FEET_IN_A_CUBIC_METER);
        m3Rates.put(VolumeUnitType.GALLON_US_FLUID, GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.CUBIC_METER, Collections.unmodifiableMap(m3Rates));
        Map<VolumeUnitType, Double> litterRates = new HashMap<>();
        litterRates.put(VolumeUnitType.CUBIC_METER, 1.0 / LITTERS_IN_A_CUBIC_METER);
        litterRates.put(VolumeUnitType.LITTER, 1.0);
        litterRates.put(VolumeUnitType.CUBIC_FOOT, 1.0 / LITTERS_IN_A_CUBIC_METER * CUBIC_FEET_IN_A_CUBIC_METER);
        litterRates.put(VolumeUnitType.GALLON_US_FLUID,
                1.0 / LITTERS_IN_A_CUBIC_METER * GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.LITTER, Collections.unmodifiableMap(litterRates));
        Map<VolumeUnitType, Double> cftRates = new HashMap<>();
        cftRates.put(VolumeUnitType.CUBIC_METER, 1.0 / CUBIC_FEET_IN_A_CUBIC_METER);
        cftRates.put(VolumeUnitType.LITTER, 1.0 / CUBIC_FEET_IN_A_CUBIC_METER * LITTERS_IN_A_CUBIC_METER);
        cftRates.put(VolumeUnitType.CUBIC_FOOT, 1.0);
        cftRates.put(VolumeUnitType.GALLON_US_FLUID,
                1.0 / CUBIC_FEET_IN_A_CUBIC_METER * GALLONS_US_FLUID_IN_A_CUBIC_METER);
        rootTable.put(VolumeUnitType.CUBIC_FOOT, Collections.unmodifiableMap(cftRates));
        Map<VolumeUnitType, Double> galUsFluidRates = new HashMap<>();
        galUsFluidRates.put(VolumeUnitType.CUBIC_METER, 1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.LITTER, 1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER * LITTERS_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.CUBIC_FOOT,
                1.0 / GALLONS_US_FLUID_IN_A_CUBIC_METER * CUBIC_FEET_IN_A_CUBIC_METER);
        galUsFluidRates.put(VolumeUnitType.GALLON_US_FLUID, 1.0);
        rootTable.put(VolumeUnitType.GALLON_US_FLUID, Collections.unmodifiableMap(galUsFluidRates));
        volumeConvertionTable = Collections.unmodifiableMap(rootTable);
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

    private static Number doMultiply(Number input, double rate) {
        if (input instanceof BigDecimal) {
            return ((BigDecimal) input).multiply(new BigDecimal(rate));
        } else if (input instanceof Double) {
            return (double) ((Double) input * rate);
        } else if (input instanceof Float) {
            return (float) ((Float) input * rate);
        } else if (input instanceof Long) {
            return (long) ((Long) input * rate);
        } else if (input instanceof AtomicLong) {
            return (long) (((AtomicLong) input).get() * rate);
        } else if (input instanceof Integer) {
            return (int) ((Integer) input * rate);
        } else if (input instanceof AtomicInteger) {
            return (int) (((AtomicInteger) input).get() * rate);
        } else if (input instanceof Byte) {
            return (byte) ((Byte) input * rate);
        } else {
            return Double.parseDouble(input.toString()) * rate;
        }
    }

    @AtlasFieldActionInfo(name = "SumUp", sourceType = FieldType.COMPLEX, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Number sumUp(Action action, Object input) {
        if (input == null) {
            return 0;
        }

        if (action == null || !(action instanceof SumUp)) {
            throw new IllegalArgumentException("SumUp must be specfied as an action");
        }

        SumUp sumup = (SumUp) action;
        NumberType numberType = sumup.getNumberType();

        Iterable<?> inputs;
        if (input instanceof Iterable) {
            inputs = (Iterable<?>) input;
        } else if (input instanceof Map) {
            inputs = ((Map<?, ?>) input).values();
        } else if (input instanceof Number[]) {
            inputs = Arrays.asList((Number[]) input);
        } else if (input instanceof double[]) {
            double[] din = (double[]) input;
            List<Double> dinList = new ArrayList<>(din.length);
            for (double e : din) {
                dinList.add(e);
            }
            if (numberType == null) {
                numberType = NumberType.DOUBLE;
            }
            inputs = dinList;
        } else if (input instanceof float[]) {
            float[] fin = (float[]) input;
            List<Float> finList = new ArrayList<>(fin.length);
            for (float e : fin) {
                finList.add(e);
            }
            if (numberType == null) {
                numberType = NumberType.FLOAT;
            }
            inputs = finList;
        } else if (input instanceof long[]) {
            long[] lin = (long[]) input;
            List<Long> linList = new ArrayList<>(lin.length);
            for (long e : lin) {
                linList.add(e);
            }
            if (numberType == null) {
                numberType = NumberType.LONG;
            }
            inputs = linList;
        } else if (input instanceof int[]) {
            int[] iin = (int[]) input;
            List<Integer> iinList = new ArrayList<>(iin.length);
            for (int e : iin) {
                iinList.add(e);
            }
            if (numberType == null) {
                numberType = NumberType.INTEGER;
            }
            inputs = iinList;
        } else if (input instanceof byte[]) {
            byte[] bin = (byte[]) input;
            List<Byte> binList = new ArrayList<>(bin.length);
            for (byte e : bin) {
                binList.add(e);
            }
            if (numberType == null)
                numberType = NumberType.BYTE;
            inputs = binList;
        } else {
            throw new IllegalArgumentException(
                    "Illegal input[" + input + "] it must be a Collection, Map or array of numbers");
        }

        // if number type is not yet detected, check the first element
        if (numberType == null) {
            Object e = inputs.iterator().next();
            if (e instanceof BigDecimal) {
                numberType = NumberType.DECIMAL;
            } else if (e instanceof Float) {
                numberType = NumberType.FLOAT;
            } else if (e instanceof Long) {
                numberType = NumberType.LONG;
            } else if (e instanceof Integer) {
                numberType = NumberType.INTEGER;
            } else if (e instanceof Byte) {
                numberType = NumberType.BYTE;
            } else {
                numberType = NumberType.DOUBLE; // Double by default
            }
        }

        switch (numberType) {

        case BYTE:
            byte b = 0x0;
            for (Object entry : inputs) {
                if (entry instanceof Number) {
                    b += ((Number) entry).byteValue();
                } else {
                    b += Byte.parseByte(entry.toString());
                }
            }
            return b;

        case DECIMAL:
            BigDecimal bd = new BigDecimal(0);
            for (Object entry : inputs) {
                if (entry instanceof BigDecimal) {
                    bd.add((BigDecimal) entry);
                } else if (entry instanceof Number) {
                    double dentry = ((Number) entry).doubleValue();
                    bd.add(new BigDecimal(dentry));
                } else {
                    bd.add(new BigDecimal(entry.toString()));
                }
            }
            return bd;

        case DOUBLE:
            double answer = 0.0d;
            for (Object entry : inputs) {
                if (entry instanceof Number) {
                    answer += ((Number) entry).doubleValue();
                } else {
                    answer += Double.parseDouble(entry.toString());
                }
            }
            return answer;

        case FLOAT:
            float f = 0.0f;
            for (Object entry : inputs) {
                if (entry instanceof Number) {
                    f += ((Number) entry).floatValue();
                } else {
                    f += Float.parseFloat(entry.toString());
                }
            }
            return f;

        case INTEGER:
            int i = 0;
            for (Object entry : inputs) {
                if (entry instanceof Number) {
                    i += ((Number) entry).intValue();
                } else {
                    i += Integer.parseInt(entry.toString());
                }
            }
            return i;

        case LONG:
            long l = 0L;
            for (Object entry : inputs) {
                if (entry instanceof Number) {
                    l += ((Number) entry).longValue();
                } else {
                    l += Long.parseLong(entry.toString());
                }
            }
            return l;

        case SHORT:
            short s = 0;
            for (Object entry : inputs) {
                if (entry instanceof Number) {
                    s += ((Number) entry).shortValue();
                } else {
                    s += Short.parseShort(entry.toString());
                }
            }
            return s;

        default:
            throw new IllegalArgumentException("Unsupported number type: " + numberType);
        }

    }

}
