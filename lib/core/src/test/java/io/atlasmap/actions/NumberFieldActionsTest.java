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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

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

public class NumberFieldActionsTest {

    @Test
    public void testAbsoluteValue() {
        assertEquals(0, NumberFieldActions.absoluteValue(new AbsoluteValue(), null));
        assertEquals(BigDecimal.valueOf(1), NumberFieldActions.absoluteValue(new AbsoluteValue(), BigDecimal.valueOf(-1)));
        assertEquals(1.0, NumberFieldActions.absoluteValue(new AbsoluteValue(), -1.0));
        assertEquals(1.0, NumberFieldActions.absoluteValue(new AbsoluteValue(), -1F));
        assertEquals(1L, NumberFieldActions.absoluteValue(new AbsoluteValue(), new AtomicLong(-1L)));
        assertEquals(1L, NumberFieldActions.absoluteValue(new AbsoluteValue(), -1L));
        assertEquals(1L, NumberFieldActions.absoluteValue(new AbsoluteValue(), new AtomicInteger(-1)));
        assertEquals(1L, NumberFieldActions.absoluteValue(new AbsoluteValue(), -1));
        assertEquals(1L, NumberFieldActions.absoluteValue(new AbsoluteValue(), (byte) -1));
    }

    @Test
    public void testAdd() {
        assertEquals(BigDecimal.valueOf(10.0), NumberFieldActions.add(new Add(), Arrays.asList(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) })));
        assertEquals(10.0, NumberFieldActions.add(new Add(), Arrays.asList(1.0, 2.0, 3.0, 4.0)));
        assertEquals(10L, NumberFieldActions.add(new Add(), Arrays.asList(1, 2, 3, 4)));
        assertEquals(0, NumberFieldActions.add(new Add(), null));
    }

    @Test
    public void testAverage() {
        assertEquals(2.5, NumberFieldActions.average(new Average(), Arrays.asList(1.0, 2.0, 3.0, 4.0)));
        assertEquals(2.5, NumberFieldActions.average(new Average(), Arrays.asList(1, 2, 3, 4)));
        assertEquals(0, NumberFieldActions.average(new Average(), null));
    }

    @Test
    public void testCeiling() {
        assertEquals(0L, NumberFieldActions.ceiling(new Ceiling(), null));
        assertEquals(2L, NumberFieldActions.ceiling(new Ceiling(), BigDecimal.valueOf(1.1)));
        assertEquals(2L, NumberFieldActions.ceiling(new Ceiling(), 1.1));
        assertEquals(2L, NumberFieldActions.ceiling(new Ceiling(), 1.1F));
        assertEquals(2L, NumberFieldActions.ceiling(new Ceiling(), new AtomicLong(2L)));
        assertEquals(2L, NumberFieldActions.ceiling(new Ceiling(), 2L));
        assertEquals(2L, NumberFieldActions.ceiling(new Ceiling(), new AtomicInteger(2)));
        assertEquals(2L, NumberFieldActions.ceiling(new Ceiling(), 2));
        assertEquals(2L, NumberFieldActions.ceiling(new Ceiling(), (byte) 2));
    }

    @Test
    public void testConvertAreaUnit() {
        ConvertAreaUnit action = new ConvertAreaUnit();
        action.setFromUnit(AreaUnitType.SQUARE_METER);
        action.setToUnit(AreaUnitType.SQUARE_METER);
        assertEquals(1.0, NumberFieldActions.convertAreaUnit(action, 1.0));
        action.setToUnit(AreaUnitType.SQUARE_FOOT);
        assertEquals(21.527820833419447, NumberFieldActions.convertAreaUnit(action, 2.0));
        action.setToUnit(AreaUnitType.SQUARE_MILE);
        assertEquals(1.1583064756273378, NumberFieldActions.convertAreaUnit(action, 3000000.0));

        action.setFromUnit(AreaUnitType.SQUARE_FOOT);
        action.setToUnit(AreaUnitType.SQUARE_METER);
        assertEquals(3.7161215999999997, NumberFieldActions.convertAreaUnit(action, 40.0));
        action.setToUnit(AreaUnitType.SQUARE_FOOT);
        assertEquals(5.0, NumberFieldActions.convertAreaUnit(action, 5.0));
        action.setToUnit(AreaUnitType.SQUARE_MILE);
        assertEquals(2.1522038567493116, NumberFieldActions.convertAreaUnit(action, 60000000.0));

        action.setFromUnit(AreaUnitType.SQUARE_MILE);
        action.setToUnit(AreaUnitType.SQUARE_METER);
        assertEquals(18129916.772352, NumberFieldActions.convertAreaUnit(action, 7.0));
        action.setToUnit(AreaUnitType.SQUARE_FOOT);
        assertEquals(223027200.0, NumberFieldActions.convertAreaUnit(action, 8.0));
        action.setToUnit(AreaUnitType.SQUARE_MILE);
        assertEquals(9.0, NumberFieldActions.convertAreaUnit(action, 9.0));
        assertNotNull(NumberFieldActions.convertAreaUnit(action, new BigDecimal("9")));
    }

    @Test
    public void testConvertDistanceUnit() {
        ConvertDistanceUnit action = new ConvertDistanceUnit();
        action.setFromUnit(DistanceUnitType.METER_M);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(1.0, NumberFieldActions.convertDistanceUnit(action, 1.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(6.561679790026247, NumberFieldActions.convertDistanceUnit(action, 2.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(3.2808398950131235, NumberFieldActions.convertDistanceUnit(action, 3.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(2.4854847689493362, NumberFieldActions.convertDistanceUnit(action, 4000.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(196.8503937007874, NumberFieldActions.convertDistanceUnit(action, 5.0));

        action.setFromUnit(DistanceUnitType.FOOT_FT);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(1.8287999999999998, NumberFieldActions.convertDistanceUnit(action, 6.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(7.0, NumberFieldActions.convertDistanceUnit(action, 7.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(27.0, NumberFieldActions.convertDistanceUnit(action, 81.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(1.7045454545454546, NumberFieldActions.convertDistanceUnit(action, 9000.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(12.0, NumberFieldActions.convertDistanceUnit(action, 1.0));

        action.setFromUnit(DistanceUnitType.YARD_YD);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(22.86, NumberFieldActions.convertDistanceUnit(action, 25.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(9.0, NumberFieldActions.convertDistanceUnit(action, 3.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(4.0, NumberFieldActions.convertDistanceUnit(action, 4.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(2.840909090909091, NumberFieldActions.convertDistanceUnit(action, 5000.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(216.0, NumberFieldActions.convertDistanceUnit(action, 6.0));

        action.setFromUnit(DistanceUnitType.MILE_MI);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(11265.408, NumberFieldActions.convertDistanceUnit(action, 7.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(42240.0, NumberFieldActions.convertDistanceUnit(action, 8.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(15840.0, NumberFieldActions.convertDistanceUnit(action, 9.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(1.0, NumberFieldActions.convertDistanceUnit(action, 1.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(126720.0, NumberFieldActions.convertDistanceUnit(action, 2.0));

        action.setFromUnit(DistanceUnitType.INCH_IN);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(7.62, NumberFieldActions.convertDistanceUnit(action, 300.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(3.5, NumberFieldActions.convertDistanceUnit(action, 42.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(1.5, NumberFieldActions.convertDistanceUnit(action, 54.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(9.469696969696969, NumberFieldActions.convertDistanceUnit(action, 600000.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(6.0, NumberFieldActions.convertDistanceUnit(action, 6.0));
    }

    @Test
    public void testConvertMassUnit() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setFromUnit(MassUnitType.KILOGRAM_KG);
        action.setToUnit(MassUnitType.POUND_LB);
        assertEquals(11, NumberFieldActions.convertMassUnit(action, 5).intValue());
        action.setFromUnit(MassUnitType.POUND_LB);
        action.setToUnit(MassUnitType.KILOGRAM_KG);
        assertEquals(4.5359235f, NumberFieldActions.convertMassUnit(action, 10.0f).floatValue(), 0);
        assertEquals(0, NumberFieldActions.convertMassUnit(action, null).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoFromNorToSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        assertEquals(11, NumberFieldActions.convertMassUnit(action, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoFromSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setToUnit(MassUnitType.POUND_LB);
        assertEquals(11, NumberFieldActions.convertMassUnit(action, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoToSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setFromUnit(MassUnitType.KILOGRAM_KG);
        assertEquals(11, NumberFieldActions.convertMassUnit(action, 5));
    }

    @Test
    public void testConvertVolumeUnit() {
        ConvertVolumeUnit action = new ConvertVolumeUnit();
        action.setFromUnit(VolumeUnitType.CUBIC_METER);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(1.0, NumberFieldActions.convertVolumeUnit(action, 1.0));
        action.setToUnit(VolumeUnitType.LITER);
        assertEquals(2000.0, NumberFieldActions.convertVolumeUnit(action, 2.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(105.94400016446578, NumberFieldActions.convertVolumeUnit(action, 3.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(1056.68820944, NumberFieldActions.convertVolumeUnit(action, 4.0));

        action.setFromUnit(VolumeUnitType.LITER);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(5.0, NumberFieldActions.convertVolumeUnit(action, 5000.0));
        action.setToUnit(VolumeUnitType.LITER);
        assertEquals(6.0, NumberFieldActions.convertVolumeUnit(action, 6.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(2.4720266705042016, NumberFieldActions.convertVolumeUnit(action, 70.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(2.11337641888, NumberFieldActions.convertVolumeUnit(action, 8.0));

        action.setFromUnit(VolumeUnitType.CUBIC_FOOT);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(2.54851619328, NumberFieldActions.convertVolumeUnit(action, 90.0));
        action.setToUnit(VolumeUnitType.LITER);
        assertEquals(28.316846591999997, NumberFieldActions.convertVolumeUnit(action, 1.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(2.0, NumberFieldActions.convertVolumeUnit(action, 2.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(22.441558441715735, NumberFieldActions.convertVolumeUnit(action, 3.0));

        action.setFromUnit(VolumeUnitType.GALLON_US_FLUID);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(1.5141647135893872, NumberFieldActions.convertVolumeUnit(action, 400.0));
        action.setToUnit(VolumeUnitType.LITER);
        assertEquals(18.92705891986734, NumberFieldActions.convertVolumeUnit(action, 5.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(8.020833333277116, NumberFieldActions.convertVolumeUnit(action, 60.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(7.0, NumberFieldActions.convertVolumeUnit(action, 7.0));
    }

    @Test
    public void testDivide() {
        assertEquals(BigDecimal.valueOf(2), NumberFieldActions.divide(new Divide(), Arrays.asList(new BigDecimal[] { BigDecimal.valueOf(4), BigDecimal.valueOf(2) })));
        assertEquals(2.0, NumberFieldActions.divide(new Divide(), Arrays.asList(4.0, 2.0)));
        assertEquals(2.0, NumberFieldActions.divide(new Divide(), Arrays.asList(4, 2)));
        assertEquals(0, NumberFieldActions.divide(new Divide(), null));
    }

    @Test
    public void testFloor() {
        assertEquals(0L, NumberFieldActions.floor(new Floor(), null));
        assertEquals(1L, NumberFieldActions.floor(new Floor(), BigDecimal.valueOf(1.5)));
        assertEquals(1L, NumberFieldActions.floor(new Floor(), 1.5));
        assertEquals(1L, NumberFieldActions.floor(new Floor(), 1.5F));
        assertEquals(2L, NumberFieldActions.floor(new Floor(), new AtomicLong(2L)));
        assertEquals(2L, NumberFieldActions.floor(new Floor(), 2L));
        assertEquals(2L, NumberFieldActions.floor(new Floor(), new AtomicInteger(2)));
        assertEquals(2L, NumberFieldActions.floor(new Floor(), 2));
        assertEquals(2L, NumberFieldActions.floor(new Floor(), (byte) 2));
    }

    @Test
    public void testMaximum() {
        assertEquals(BigDecimal.valueOf(4), NumberFieldActions.maximum(new Maximum(), Arrays.asList(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) })));
        assertEquals(4.0, NumberFieldActions.maximum(new Maximum(), Arrays.asList(1.0, 2.0, 3.0, 4.0)));
        assertEquals(4, NumberFieldActions.maximum(new Maximum(), Arrays.asList(1, 2, 3, 4)));
        assertEquals(BigDecimal.valueOf(4), NumberFieldActions.maximum(new Maximum(), Arrays.asList((byte) 1, 2, 3.0, BigDecimal.valueOf(4))));
        assertEquals(0, NumberFieldActions.maximum(new Maximum(), null));
    }

    @Test
    public void testMinimum() {
        assertEquals(BigDecimal.valueOf(1), NumberFieldActions.minimum(new Minimum(), Arrays.asList(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) })));
        assertEquals(1.0, NumberFieldActions.minimum(new Minimum(), Arrays.asList(1.0, 2.0, 3.0, 4.0)));
        assertEquals(1, NumberFieldActions.minimum(new Minimum(), Arrays.asList(1, 2, 3, 4)));
        assertEquals((byte) 1, NumberFieldActions.minimum(new Minimum(), Arrays.asList((byte) 1, 2, 3.0, BigDecimal.valueOf(4))));
        assertEquals(0, NumberFieldActions.minimum(new Minimum(), null));
    }

    @Test
    public void testRound() {
        assertEquals(0L, NumberFieldActions.round(new Round(), null));
        assertEquals(2L, NumberFieldActions.round(new Round(), BigDecimal.valueOf(1.5)));
        assertEquals(1L, NumberFieldActions.round(new Round(), 1.4));
        assertEquals(2L, NumberFieldActions.round(new Round(), 1.5F));
        assertEquals(2L, NumberFieldActions.round(new Round(), new AtomicLong(2L)));
        assertEquals(2L, NumberFieldActions.round(new Round(), 2L));
        assertEquals(2L, NumberFieldActions.round(new Round(), new AtomicInteger(2)));
        assertEquals(2L, NumberFieldActions.round(new Round(), 2));
        assertEquals(2L, NumberFieldActions.round(new Round(), (byte) 2));
    }

    @Test
    public void testSubtract() {
        assertEquals(BigDecimal.valueOf(-8.0), NumberFieldActions.subtract(new Subtract(), Arrays.asList(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) })));
        assertEquals(-8.0, NumberFieldActions.subtract(new Subtract(), Arrays.asList(1.0, 2.0, 3.0, 4.0)));
        assertEquals(-8L, NumberFieldActions.subtract(new Subtract(), Arrays.asList(1, 2, 3, 4)));
        assertEquals(0, NumberFieldActions.subtract(new Subtract(), null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitIllegalArgumentException() {
        assertEquals(11, NumberFieldActions.convertMassUnit(new ConvertMassUnit(), 5).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertDistanceUnitIllegalArgumentException() {
        assertEquals(0, NumberFieldActions.convertDistanceUnit(new ConvertDistanceUnit(), null).intValue());
        NumberFieldActions.convertDistanceUnit(new ConvertDistanceUnit(), 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertAreaUnitIllegalArgumentException() {
        assertEquals(0, NumberFieldActions.convertAreaUnit(new ConvertAreaUnit(), null).intValue());
        NumberFieldActions.convertAreaUnit(new ConvertAreaUnit(), 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertVolumeUnitIllegalArgumentException() {
        assertEquals(0, NumberFieldActions.convertVolumeUnit(new ConvertVolumeUnit(), null).intValue());
        NumberFieldActions.convertVolumeUnit(new ConvertVolumeUnit(), 5);
    }

    @Test
    public void testMultiply() {
        assertNotNull(new NumberFieldActions());
        assertEquals(0, NumberFieldActions.multiply(new Multiply(), null));

        assertEquals(new BigDecimal("24.0000"), NumberFieldActions.multiply(new Multiply(), Arrays.asList(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) })));
        assertEquals(24.0, NumberFieldActions.multiply(new Multiply(), Arrays.asList(1.0, 2.0, 3.0, 4.0 )));
        assertEquals(24.0, NumberFieldActions.multiply(new Multiply(), Arrays.asList(1.0f, 2.0f, 3.0f, 4.0f)));
        assertEquals(24L, NumberFieldActions.multiply(new Multiply(), Arrays.asList(1L, 2L, 3L, 4L)));
        assertEquals(24L, NumberFieldActions.multiply(new Multiply(), Arrays.asList(1, 2, 3, 4)));
    }

}
