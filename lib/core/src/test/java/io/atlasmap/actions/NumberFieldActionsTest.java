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
        AbsoluteValue action = new AbsoluteValue();
        assertEquals(0, action.absoluteValue(null));
        assertEquals(BigDecimal.valueOf(1), action.absoluteValue(BigDecimal.valueOf(-1)));
        assertEquals(1.0, action.absoluteValue(-1.0));
        assertEquals(1.0, action.absoluteValue(-1F));
        assertEquals(1L, action.absoluteValue(new AtomicLong(-1L)));
        assertEquals(1L, action.absoluteValue(-1L));
        assertEquals(1L, action.absoluteValue(new AtomicInteger(-1)));
        assertEquals(1L, action.absoluteValue(-1));
        assertEquals(1L, action.absoluteValue((byte) -1));
    }

    @Test
    public void testAdd() {
        Add action = new Add();
        assertEquals(BigDecimal.valueOf(10.0), action.add(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) }));
        assertEquals(10.0, action.add(new double[] { 1.0, 2.0, 3.0, 4.0 }));
        assertEquals(10L, action.add(new int[] { 1, 2, 3, 4 }));
        assertEquals(10L, action.add(Arrays.asList(1, 2, 3, 4)));
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        assertEquals(10L, action.add(map));
        assertEquals(0, action.add(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOfNonNumber() {
        Add action = new Add();
        action.add(Arrays.asList(new Object[] { "1", "2", "3" }));
    }

    @Test
    public void testAverage() {
        Average action = new Average();
        assertEquals(2.5, action.average(new double[] { 1.0, 2.0, 3.0, 4.0 }));
        assertEquals(2.5, action.average(new int[] { 1, 2, 3, 4 }));
        assertEquals(2.5, action.average(Arrays.asList(1, 2, 3, 4)));
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        assertEquals(2.5, action.average(map));
        assertEquals(0, action.average(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAverageOfNonNumber() {
        Average action = new Average();
        action.average(Arrays.asList(new Object[] { "1", "2", "3" }));
    }

    @Test
    public void testCeiling() {
        Ceiling action = new Ceiling();
        assertEquals(0L, action.ceiling(null));
        assertEquals(2L, action.ceiling(BigDecimal.valueOf(1.1)));
        assertEquals(2L, action.ceiling(1.1));
        assertEquals(2L, action.ceiling(1.1F));
        assertEquals(2L, action.ceiling(new AtomicLong(2L)));
        assertEquals(2L, action.ceiling(2L));
        assertEquals(2L, action.ceiling(new AtomicInteger(2)));
        assertEquals(2L, action.ceiling(2));
        assertEquals(2L, action.ceiling((byte) 2));
    }

    @Test
    public void testConvertAreaUnit() {
        ConvertAreaUnit action = new ConvertAreaUnit();
        action.setFromUnit(AreaUnitType.SQUARE_METER);
        action.setToUnit(AreaUnitType.SQUARE_METER);
        assertEquals(1.0, action.convertAreaUnit(1.0));
        action.setToUnit(AreaUnitType.SQUARE_FOOT);
        assertEquals(21.527820833419447, action.convertAreaUnit(2.0));
        action.setToUnit(AreaUnitType.SQUARE_MILE);
        assertEquals(1.1583064756273378, action.convertAreaUnit(3000000.0));

        action.setFromUnit(AreaUnitType.SQUARE_FOOT);
        action.setToUnit(AreaUnitType.SQUARE_METER);
        assertEquals(3.7161215999999997, action.convertAreaUnit(40.0));
        action.setToUnit(AreaUnitType.SQUARE_FOOT);
        assertEquals(5.0, action.convertAreaUnit(5.0));
        action.setToUnit(AreaUnitType.SQUARE_MILE);
        assertEquals(2.1522038567493116, action.convertAreaUnit(60000000.0));

        action.setFromUnit(AreaUnitType.SQUARE_MILE);
        action.setToUnit(AreaUnitType.SQUARE_METER);
        assertEquals(18129916.772352, action.convertAreaUnit(7.0));
        action.setToUnit(AreaUnitType.SQUARE_FOOT);
        assertEquals(223027200.0, action.convertAreaUnit(8.0));
        action.setToUnit(AreaUnitType.SQUARE_MILE);
        assertEquals(9.0, action.convertAreaUnit(9.0));
        assertNotNull(action.convertAreaUnit(new BigDecimal("9")));
    }

    @Test
    public void testConvertDistanceUnit() {
        ConvertDistanceUnit action = new ConvertDistanceUnit();
        action.setFromUnit(DistanceUnitType.METER_M);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(1.0, action.convertDistanceUnit(1.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(6.561679790026247, action.convertDistanceUnit(2.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(3.2808398950131235, action.convertDistanceUnit(3.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(2.4854847689493362, action.convertDistanceUnit(4000.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(196.8503937007874, action.convertDistanceUnit(5.0));

        action.setFromUnit(DistanceUnitType.FOOT_FT);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(1.8287999999999998, action.convertDistanceUnit(6.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(7.0, action.convertDistanceUnit(7.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(27.0, action.convertDistanceUnit(81.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(1.7045454545454546, action.convertDistanceUnit(9000.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(12.0, action.convertDistanceUnit(1.0));

        action.setFromUnit(DistanceUnitType.YARD_YD);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(22.86, action.convertDistanceUnit(25.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(9.0, action.convertDistanceUnit(3.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(4.0, action.convertDistanceUnit(4.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(2.840909090909091, action.convertDistanceUnit(5000.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(216.0, action.convertDistanceUnit(6.0));

        action.setFromUnit(DistanceUnitType.MILE_MI);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(11265.408, action.convertDistanceUnit(7.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(42240.0, action.convertDistanceUnit(8.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(15840.0, action.convertDistanceUnit(9.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(1.0, action.convertDistanceUnit(1.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(126720.0, action.convertDistanceUnit(2.0));

        action.setFromUnit(DistanceUnitType.INCH_IN);
        action.setToUnit(DistanceUnitType.METER_M);
        assertEquals(7.62, action.convertDistanceUnit(300.0));
        action.setToUnit(DistanceUnitType.FOOT_FT);
        assertEquals(3.5, action.convertDistanceUnit(42.0));
        action.setToUnit(DistanceUnitType.YARD_YD);
        assertEquals(1.5, action.convertDistanceUnit(54.0));
        action.setToUnit(DistanceUnitType.MILE_MI);
        assertEquals(9.469696969696969, action.convertDistanceUnit(600000.0));
        action.setToUnit(DistanceUnitType.INCH_IN);
        assertEquals(6.0, action.convertDistanceUnit(6.0));
    }

    @Test
    public void testConvertMassUnit() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setFromUnit(MassUnitType.KILOGRAM_KG);
        action.setToUnit(MassUnitType.POUND_LB);
        assertEquals(11, action.convertMassUnit(5).intValue());
        action.setFromUnit(MassUnitType.POUND_LB);
        action.setToUnit(MassUnitType.KILOGRAM_KG);
        assertEquals(4.5359235f, action.convertMassUnit(10.0f).floatValue(), 0);
        assertEquals(0, action.convertMassUnit(null).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoFromNorToSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        assertEquals(11, action.convertMassUnit(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoFromSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setToUnit(MassUnitType.POUND_LB);
        assertEquals(11, action.convertMassUnit(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoToSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setFromUnit(MassUnitType.KILOGRAM_KG);
        assertEquals(11, action.convertMassUnit(5));
    }

    @Test
    public void testConvertVolumeUnit() {
        ConvertVolumeUnit action = new ConvertVolumeUnit();
        action.setFromUnit(VolumeUnitType.CUBIC_METER);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(1.0, action.convertVolumeUnit(1.0));
        action.setToUnit(VolumeUnitType.LITER);
        assertEquals(2000.0, action.convertVolumeUnit(2.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(105.94400016446578, action.convertVolumeUnit(3.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(1056.68820944, action.convertVolumeUnit(4.0));

        action.setFromUnit(VolumeUnitType.LITER);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(5.0, action.convertVolumeUnit(5000.0));
        action.setToUnit(VolumeUnitType.LITER);
        assertEquals(6.0, action.convertVolumeUnit(6.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(2.4720266705042016, action.convertVolumeUnit(70.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(2.11337641888, action.convertVolumeUnit(8.0));

        action.setFromUnit(VolumeUnitType.CUBIC_FOOT);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(2.54851619328, action.convertVolumeUnit(90.0));
        action.setToUnit(VolumeUnitType.LITER);
        assertEquals(28.316846591999997, action.convertVolumeUnit(1.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(2.0, action.convertVolumeUnit(2.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(22.441558441715735, action.convertVolumeUnit(3.0));

        action.setFromUnit(VolumeUnitType.GALLON_US_FLUID);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(1.5141647135893872, action.convertVolumeUnit(400.0));
        action.setToUnit(VolumeUnitType.LITER);
        assertEquals(18.92705891986734, action.convertVolumeUnit(5.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(8.020833333277116, action.convertVolumeUnit(60.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(7.0, action.convertVolumeUnit(7.0));
    }

    @Test
    public void testDivide() {
        Divide action = new Divide();
        assertEquals(BigDecimal.valueOf(2), action.divide(new BigDecimal[] { BigDecimal.valueOf(4), BigDecimal.valueOf(2) }));
        assertEquals(2.0, action.divide(new double[] { 4.0, 2.0 }));
        assertEquals(2.0, action.divide(new int[] { 4, 2 }));
        assertEquals(2.0, action.divide(Arrays.asList(4, 2)));
        Map<String, Integer> map = new HashMap<>();
        map.put("4", 1);
        map.put("2", 2);
        assertEquals(2.0, action.divide(map));
        assertEquals(0, action.divide(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideOfNonNumber() {
        Divide action = new Divide();
        action.divide(Arrays.asList(new Object[] { "1", "2", "3" }));
    }

    @Test
    public void testFloor() {
        Floor action = new Floor();
        assertEquals(0L, action.floor(null));
        assertEquals(1L, action.floor(BigDecimal.valueOf(1.5)));
        assertEquals(1L, action.floor(1.5));
        assertEquals(1L, action.floor(1.5F));
        assertEquals(2L, action.floor(new AtomicLong(2L)));
        assertEquals(2L, action.floor(2L));
        assertEquals(2L, action.floor(new AtomicInteger(2)));
        assertEquals(2L, action.floor(2));
        assertEquals(2L, action.floor((byte) 2));
    }

    @Test
    public void testMaximum() {
        Maximum action = new Maximum();
        assertEquals(BigDecimal.valueOf(4), action.maximum(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) }));
        assertEquals(4.0, action.maximum(new double[] { 1.0, 2.0, 3.0, 4.0 }));
        assertEquals(4, action.maximum(new int[] { 1, 2, 3, 4 }));
        assertEquals(4, action.maximum(Arrays.asList(1, 2, 3, 4)));
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        assertEquals(4, action.maximum(map));
        assertEquals(BigDecimal.valueOf(4), action.maximum(Arrays.asList((byte) 1, 2, 3.0, BigDecimal.valueOf(4))));
        assertEquals(0, action.maximum(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaximumOfNonNumber() {
        Maximum action = new Maximum();
        action.maximum(Arrays.asList(new Object[] { "1", "2", "3" }));
    }

    @Test
    public void testMinimum() {
        Minimum action = new Minimum();
        assertEquals(BigDecimal.valueOf(1), action.minimum(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) }));
        assertEquals(1.0, action.minimum(new double[] { 1.0, 2.0, 3.0, 4.0 }));
        assertEquals(1, action.minimum(new int[] { 1, 2, 3, 4 }));
        assertEquals(1, action.minimum(Arrays.asList(1, 2, 3, 4)));
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        assertEquals(1, action.minimum(map));
        assertEquals((byte) 1, action.minimum(Arrays.asList((byte) 1, 2, 3.0, BigDecimal.valueOf(4))));
        assertEquals(0, action.minimum(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinimumOfNonNumber() {
        Minimum action = new Minimum();
        action.minimum(Arrays.asList(new Object[] { "1", "2", "3" }));
    }

    @Test
    public void testRound() {
        Round action = new Round();
        assertEquals(0L, action.round(null));
        assertEquals(2L, action.round(BigDecimal.valueOf(1.5)));
        assertEquals(1L, action.round(1.4));
        assertEquals(2L, action.round(1.5F));
        assertEquals(2L, action.round(new AtomicLong(2L)));
        assertEquals(2L, action.round(2L));
        assertEquals(2L, action.round(new AtomicInteger(2)));
        assertEquals(2L, action.round(2));
        assertEquals(2L, action.round((byte) 2));
    }

    @Test
    public void testSubtract() {
        Subtract action = new Subtract();
        assertEquals(BigDecimal.valueOf(-8.0), action.subtract(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) }));
        assertEquals(-8.0, action.subtract(new double[] { 1.0, 2.0, 3.0, 4.0 }));
        assertEquals(-8L, action.subtract(new int[] { 1, 2, 3, 4 }));
        assertEquals(-8L, action.subtract(Arrays.asList(1, 2, 3, 4)));
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        assertEquals(-8L, action.subtract(map));
        assertEquals(0, action.subtract(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractOfNonNumber() {
        Subtract action = new Subtract();
        action.subtract(Arrays.asList(new Object[] { "1", "2", "3" }));
    }

    @Test
    public void testMultiply() {
        Multiply action = new Multiply();
        assertEquals(0, action.multiply(null));

        assertEquals(new BigDecimal("24.0000"), action.multiply(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) }));
        assertEquals(24.0, action.multiply(new double[] { 1.0, 2.0, 3.0, 4.0 }));
        assertEquals(24.0, action.multiply(new float[] { 1.0f, 2.0f, 3.0f, 4.0f }));
        assertEquals(24L, action.multiply(new int[] { 1, 2, 3, 4 }));
        assertEquals(24L, action.multiply(new byte[] { 1, 2, 3, 4 }));
        assertEquals(24L, action.multiply(new long[] { 1L, 2L, 3L, 4L }));
        assertEquals(24L, action.multiply(Arrays.asList(1, 2, 3, 4)));
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        map.put("3", 3);
        map.put("4", 4);
        assertEquals(24L, action.multiply(map));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyIllegalArgumentException() {
        Multiply action = new Multiply();
        assertEquals(24L, action.multiply(Arrays.asList("a", "b")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyIllegalArgumentExceptionCollection() {
        Multiply action = new Multiply();
        assertEquals(24L, action.multiply(new Object()));
    }

}
