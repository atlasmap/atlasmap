package io.atlasmap.v2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class ConvertVolumeUnit extends BaseConvertUnit {

    private static final long serialVersionUID = 1L;

    private static final double LITERS_IN_A_CUBIC_METER = 1000.0;
    private static final double CUBIC_FEET_IN_A_CUBIC_METER = Math.pow(1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT, 3.0);
    private static final double GALLONS_US_FLUID_IN_A_CUBIC_METER = 264.17205236;

    private static Map<VolumeUnitType, Map<VolumeUnitType, Double>> VOLUME_CONVERSION_TABLE;

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
        VOLUME_CONVERSION_TABLE = Collections.unmodifiableMap(rootTable);
    }

    @AtlasFieldActionParameter(enumType = VolumeUnitType.class)
    private VolumeUnitType fromUnit;
    @AtlasFieldActionParameter(enumType = VolumeUnitType.class)
    private VolumeUnitType toUnit;

    @AtlasFieldActionInfo(name = "ConvertVolumeUnit", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Number convertVolumeUnit(Number input) {
        if (input == null) {
            return 0;
        }

        if (getFromUnit() == null || getToUnit() == null) {
            throw new IllegalArgumentException("ConvertVolumeUnit must be specified  with fromUnit and toUnit");
        }

        double rate = VOLUME_CONVERSION_TABLE.get(fromUnit).get(toUnit);
        return multiply(input, rate);
    }

    /**
     * Gets the value of the fromUnit property.
     *
     * @return
     *     possible object is
     *     {@link VolumeUnitType }
     *
     */
    public VolumeUnitType getFromUnit() {
        return fromUnit;
    }

    /**
     * Sets the value of the fromUnit property.
     *
     * @param value
     *     allowed object is
     *     {@link VolumeUnitType }
     *
     */
    public void setFromUnit(VolumeUnitType value) {
        this.fromUnit = value;
    }

    /**
     * Gets the value of the toUnit property.
     *
     * @return
     *     possible object is
     *     {@link VolumeUnitType }
     *
     */
    public VolumeUnitType getToUnit() {
        return toUnit;
    }

    /**
     * Sets the value of the toUnit property.
     *
     * @param value
     *     allowed object is
     *     {@link VolumeUnitType }
     *
     */
    public void setToUnit(VolumeUnitType value) {
        this.toUnit = value;
    }

}
