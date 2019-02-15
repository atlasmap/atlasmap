package io.atlasmap.v2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class ConvertAreaUnit extends BaseConvertUnit {

    private static final long serialVersionUID = 1L;

    private static final double SQUARE_FEET_IN_A_SQUARE_METER = Math.pow(1.0 / METERS_IN_A_INCH / INCHES_IN_A_FOOT, 2.0);
    private static final double SQUARE_METERS_IN_A_SQUARE_MILE = Math.pow(YARDS_IN_A_MILE * FEET_IN_A_YARD * INCHES_IN_A_FOOT * METERS_IN_A_INCH, 2.0);
    private static final double SQUARE_FEET_IN_A_SQUARE_MILE = Math.pow(YARDS_IN_A_MILE * FEET_IN_A_YARD, 2.0);

    private static final Map<AreaUnitType, Map<AreaUnitType, Double>> AREA_CONVERSION_TABLE;

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
        AREA_CONVERSION_TABLE = Collections.unmodifiableMap(rootTable);
    }

    @AtlasFieldActionParameter(enumType = AreaUnitType.class)
    private AreaUnitType fromUnit;
    @AtlasFieldActionParameter(enumType = AreaUnitType.class)
    private AreaUnitType toUnit;

    @AtlasFieldActionInfo(name = "ConvertAreaUnit", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Number convertAreaUnit(Number input) {
        if (input == null) {
            return 0;
        }

        if (getFromUnit() == null || getToUnit() == null) {
            throw new IllegalArgumentException("ConvertAreaUnit must be specified  with fromUnit and toUnit");
        }

        double rate = AREA_CONVERSION_TABLE.get(fromUnit).get(toUnit);
        return multiply(input, rate);
    }

    /**
     * Gets the value of the fromUnit property.
     *
     * @return
     *     possible object is
     *     {@link AreaUnitType }
     *
     */
    public AreaUnitType getFromUnit() {
        return fromUnit;
    }

    /**
     * Sets the value of the fromUnit property.
     *
     * @param value
     *     allowed object is
     *     {@link AreaUnitType }
     *
     */
    public void setFromUnit(AreaUnitType value) {
        this.fromUnit = value;
    }

    /**
     * Gets the value of the toUnit property.
     *
     * @return
     *     possible object is
     *     {@link AreaUnitType }
     *
     */
    public AreaUnitType getToUnit() {
        return toUnit;
    }

    /**
     * Sets the value of the toUnit property.
     *
     * @param value
     *     allowed object is
     *     {@link AreaUnitType }
     *
     */
    public void setToUnit(AreaUnitType value) {
        this.toUnit = value;
    }
}
