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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.atlasmap.v2.AreaUnitType;
import io.atlasmap.v2.ConvertAreaUnit;
import io.atlasmap.v2.ConvertDistanceUnit;
import io.atlasmap.v2.ConvertMassUnit;
import io.atlasmap.v2.ConvertVolumeUnit;
import io.atlasmap.v2.DistanceUnitType;
import io.atlasmap.v2.MassUnitType;
import io.atlasmap.v2.NumberType;
import io.atlasmap.v2.SumUp;
import io.atlasmap.v2.VolumeUnitType;

public class NumberFieldActionsTest {

    @Test
    public void testSumUp() {
        assertEquals(15.0000000000015, NumberFieldActions.sumUp(new SumUp(),
                new double[] { 1.0000000000001, 2.0000000000002, 3.0000000000003, 4.0000000000004, 5.0000000000005 }));
        assertEquals(16.5f, NumberFieldActions.sumUp(new SumUp(), new float[] { 1.1f, 2.2f, 3.3f, 4.4f, 5.5f }));
        assertEquals(3000000000000000000l,
                NumberFieldActions.sumUp(new SumUp(), new long[] { 1000000000000000000l, 2000000000000000000l }));
        assertEquals(15, NumberFieldActions.sumUp(new SumUp(), new int[] { 1, 2, 3, 4, 5 }));
        assertEquals((byte) 0xf, NumberFieldActions.sumUp(new SumUp(), new byte[] { 1, 2, 3, 4, 5 }));
        assertEquals(16.0000000000015, NumberFieldActions.sumUp(new SumUp(), new Number[] { 1.0000000000001,
                2.0000000000002, 3.0000000000003, 4.0000000000004, 5.0000000000005, 1 }));
        assertEquals(16.5f,
                NumberFieldActions.sumUp(new SumUp(), Arrays.asList(new Float[] { 1.1f, 2.2f, 3.3f, 4.4f, 5.5f })));
        Map<String, Integer> values = new HashMap<>();
        values.put("a", 1);
        values.put("b", 2);
        values.put("c", 3);
        values.put("d", 4);
        values.put("e", 5);
        assertEquals(15, NumberFieldActions.sumUp(new SumUp(), values));
        SumUp intSumUp = new SumUp();
        intSumUp.setNumberType(NumberType.INTEGER);
        assertEquals(15, NumberFieldActions.sumUp(intSumUp,
                new double[] { 1.0000000000001, 2.0000000000002, 3.0000000000003, 4.0000000000004, 5.0000000000005 }));
        assertEquals(6.0, NumberFieldActions.sumUp(new SumUp(), Arrays.asList(new Object[] { "1", "2", "3" })));
    }

    @Test(expected = NumberFormatException.class)
    public void testSumUpErrorNotNumber() {
        NumberFieldActions.sumUp(new SumUp(), Arrays.asList(new Object[] { "a", "b", "c" }));
    }

    @Test
    public void testConvertMassUnit() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setFromUnit(MassUnitType.KILO_GRAM);
        action.setToUnit(MassUnitType.POUND);
        assertEquals(11, NumberFieldActions.convertMassUnit(action, 5));
        action.setFromUnit(MassUnitType.POUND);
        action.setToUnit(MassUnitType.KILO_GRAM);
        assertEquals(4.5359235f, NumberFieldActions.convertMassUnit(action, 10.0f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoFromNorToSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        assertEquals(11, NumberFieldActions.convertMassUnit(action, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoFromSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setToUnit(MassUnitType.POUND);
        assertEquals(11, NumberFieldActions.convertMassUnit(action, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertMassUnitErrorNoToSpecified() {
        ConvertMassUnit action = new ConvertMassUnit();
        action.setFromUnit(MassUnitType.KILO_GRAM);
        assertEquals(11, NumberFieldActions.convertMassUnit(action, 5));
    }

    @Test
    public void testConvertDistanceUnit() {
        ConvertDistanceUnit action = new ConvertDistanceUnit();
        action.setFromUnit(DistanceUnitType.METER);
        action.setToUnit(DistanceUnitType.METER);
        assertEquals(1.0, NumberFieldActions.convertDistanceUnit(action, 1.0));
        action.setToUnit(DistanceUnitType.FOOT);
        assertEquals(6.561679790026247, NumberFieldActions.convertDistanceUnit(action, 2.0));
        action.setToUnit(DistanceUnitType.YARD);
        assertEquals(3.2808398950131235, NumberFieldActions.convertDistanceUnit(action, 3.0));
        action.setToUnit(DistanceUnitType.MILE);
        assertEquals(2.4854847689493362, NumberFieldActions.convertDistanceUnit(action, 4000.0));
        action.setToUnit(DistanceUnitType.INCH);
        assertEquals(196.8503937007874, NumberFieldActions.convertDistanceUnit(action, 5.0));

        action.setFromUnit(DistanceUnitType.FOOT);
        action.setToUnit(DistanceUnitType.METER);
        assertEquals(1.8287999999999998, NumberFieldActions.convertDistanceUnit(action, 6.0));
        action.setToUnit(DistanceUnitType.FOOT);
        assertEquals(7.0, NumberFieldActions.convertDistanceUnit(action, 7.0));
        action.setToUnit(DistanceUnitType.YARD);
        assertEquals(27.0, NumberFieldActions.convertDistanceUnit(action, 81.0));
        action.setToUnit(DistanceUnitType.MILE);
        assertEquals(1.7045454545454546, NumberFieldActions.convertDistanceUnit(action, 9000.0));
        action.setToUnit(DistanceUnitType.INCH);
        assertEquals(12.0, NumberFieldActions.convertDistanceUnit(action, 1.0));

        action.setFromUnit(DistanceUnitType.YARD);
        action.setToUnit(DistanceUnitType.METER);
        assertEquals(22.86, NumberFieldActions.convertDistanceUnit(action, 25.0));
        action.setToUnit(DistanceUnitType.FOOT);
        assertEquals(9.0, NumberFieldActions.convertDistanceUnit(action, 3.0));
        action.setToUnit(DistanceUnitType.YARD);
        assertEquals(4.0, NumberFieldActions.convertDistanceUnit(action, 4.0));
        action.setToUnit(DistanceUnitType.MILE);
        assertEquals(2.840909090909091, NumberFieldActions.convertDistanceUnit(action, 5000.0));
        action.setToUnit(DistanceUnitType.INCH);
        assertEquals(216.0, NumberFieldActions.convertDistanceUnit(action, 6.0));

        action.setFromUnit(DistanceUnitType.MILE);
        action.setToUnit(DistanceUnitType.METER);
        assertEquals(11265.408, NumberFieldActions.convertDistanceUnit(action, 7.0));
        action.setToUnit(DistanceUnitType.FOOT);
        assertEquals(42240.0, NumberFieldActions.convertDistanceUnit(action, 8.0));
        action.setToUnit(DistanceUnitType.YARD);
        assertEquals(15840.0, NumberFieldActions.convertDistanceUnit(action, 9.0));
        action.setToUnit(DistanceUnitType.MILE);
        assertEquals(1.0, NumberFieldActions.convertDistanceUnit(action, 1.0));
        action.setToUnit(DistanceUnitType.INCH);
        assertEquals(126720.0, NumberFieldActions.convertDistanceUnit(action, 2.0));

        action.setFromUnit(DistanceUnitType.INCH);
        action.setToUnit(DistanceUnitType.METER);
        assertEquals(7.62, NumberFieldActions.convertDistanceUnit(action, 300.0));
        action.setToUnit(DistanceUnitType.FOOT);
        assertEquals(3.5, NumberFieldActions.convertDistanceUnit(action, 42.0));
        action.setToUnit(DistanceUnitType.YARD);
        assertEquals(1.5, NumberFieldActions.convertDistanceUnit(action, 54.0));
        action.setToUnit(DistanceUnitType.MILE);
        assertEquals(9.469696969696969, NumberFieldActions.convertDistanceUnit(action, 600000.0));
        action.setToUnit(DistanceUnitType.INCH);
        assertEquals(6.0, NumberFieldActions.convertDistanceUnit(action, 6.0));
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
    }

    @Test
    public void testConvertVolumeUnit() {
        ConvertVolumeUnit action = new ConvertVolumeUnit();
        action.setFromUnit(VolumeUnitType.CUBIC_METER);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(1.0, NumberFieldActions.convertVolumeUnit(action, 1.0));
        action.setToUnit(VolumeUnitType.LITTER);
        assertEquals(2000.0, NumberFieldActions.convertVolumeUnit(action, 2.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(105.94400016446578, NumberFieldActions.convertVolumeUnit(action, 3.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(1056.68820944, NumberFieldActions.convertVolumeUnit(action, 4.0));

        action.setFromUnit(VolumeUnitType.LITTER);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(5.0, NumberFieldActions.convertVolumeUnit(action, 5000.0));
        action.setToUnit(VolumeUnitType.LITTER);
        assertEquals(6.0, NumberFieldActions.convertVolumeUnit(action, 6.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(2.4720266705042016, NumberFieldActions.convertVolumeUnit(action, 70.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(2.11337641888, NumberFieldActions.convertVolumeUnit(action, 8.0));

        action.setFromUnit(VolumeUnitType.CUBIC_FOOT);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(2.54851619328, NumberFieldActions.convertVolumeUnit(action, 90.0));
        action.setToUnit(VolumeUnitType.LITTER);
        assertEquals(28.316846591999997, NumberFieldActions.convertVolumeUnit(action, 1.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(2.0, NumberFieldActions.convertVolumeUnit(action, 2.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(22.441558441715735, NumberFieldActions.convertVolumeUnit(action, 3.0));

        action.setFromUnit(VolumeUnitType.GALLON_US_FLUID);
        action.setToUnit(VolumeUnitType.CUBIC_METER);
        assertEquals(1.5141647135893872, NumberFieldActions.convertVolumeUnit(action, 400.0));
        action.setToUnit(VolumeUnitType.LITTER);
        assertEquals(18.92705891986734, NumberFieldActions.convertVolumeUnit(action, 5.0));
        action.setToUnit(VolumeUnitType.CUBIC_FOOT);
        assertEquals(8.020833333277116, NumberFieldActions.convertVolumeUnit(action, 60.0));
        action.setToUnit(VolumeUnitType.GALLON_US_FLUID);
        assertEquals(7.0, NumberFieldActions.convertVolumeUnit(action, 7.0));
    }
}
