package io.atlasmap.v2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class ConvertDistanceUnit extends BaseConvertUnit {

    private static final long serialVersionUID = 1L;

    private static final Map<DistanceUnitType, Map<DistanceUnitType, Double>> DISTANCE_CONVERSION_TABLE;

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
        DISTANCE_CONVERSION_TABLE = Collections.unmodifiableMap(rootTable);
    }

    @AtlasFieldActionParameter(enumType = DistanceUnitType.class)
    private DistanceUnitType fromUnit;
    @AtlasFieldActionParameter(enumType = DistanceUnitType.class)
    private DistanceUnitType toUnit;

    @AtlasFieldActionInfo(name = "ConvertDistanceUnit", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Number convertDistanceUnit(Number input) {
        if (input == null) {
            return 0;
        }

        if (getFromUnit() == null || getToUnit() == null) {
            throw new IllegalArgumentException("ConvertDistanceUnit must be specified  with fromUnit and toUnit");
        }

        double rate = DISTANCE_CONVERSION_TABLE.get(fromUnit).get(toUnit);
        return multiply(input, rate);
    }

    /**
     * Gets the value of the fromUnit property.
     *
     * @return
     *     possible object is
     *     {@link DistanceUnitType }
     *
     */
    public DistanceUnitType getFromUnit() {
        return fromUnit;
    }

    /**
     * Sets the value of the fromUnit property.
     *
     * @param value
     *     allowed object is
     *     {@link DistanceUnitType }
     *
     */
    public void setFromUnit(DistanceUnitType value) {
        this.fromUnit = value;
    }

    /**
     * Gets the value of the toUnit property.
     *
     * @return
     *     possible object is
     *     {@link DistanceUnitType }
     *
     */
    public DistanceUnitType getToUnit() {
        return toUnit;
    }

    /**
     * Sets the value of the toUnit property.
     *
     * @param value
     *     allowed object is
     *     {@link DistanceUnitType }
     *
     */
    public void setToUnit(DistanceUnitType value) {
        this.toUnit = value;
    }
}
