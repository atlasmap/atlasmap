package io.atlasmap.v2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.spi.AtlasFieldActionParameter;

public class ConvertMassUnit extends BaseConvertUnit {

    private static final long serialVersionUID = 1L;

    private static final Map<MassUnitType, Map<MassUnitType, Double>> MASS_CONVERSION_TABLE;

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
        MASS_CONVERSION_TABLE = Collections.unmodifiableMap(rootTable);
    }

    @AtlasFieldActionParameter(enumType = MassUnitType.class)
    private MassUnitType fromUnit;
    @AtlasFieldActionParameter(enumType = MassUnitType.class)
    private MassUnitType toUnit;

    @AtlasFieldActionInfo(name = "ConvertMassUnit", sourceType = FieldType.NUMBER, targetType = FieldType.NUMBER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public Number convertMassUnit(Number input) {
        if (input == null) {
            return 0;
        }

        if (getFromUnit() == null || getToUnit() == null) {
            throw new IllegalArgumentException("ConvertMassUnit must be specified  with fromUnit and toUnit");
        }

        double rate = MASS_CONVERSION_TABLE.get(fromUnit).get(toUnit);
        return multiply(input, rate);
    }

    /**
     * Gets the value of the fromUnit property.
     *
     * @return
     *     possible object is
     *     {@link MassUnitType }
     *
     */
    public MassUnitType getFromUnit() {
        return fromUnit;
    }

    /**
     * Sets the value of the fromUnit property.
     *
     * @param value
     *     allowed object is
     *     {@link MassUnitType }
     *
     */
    public void setFromUnit(MassUnitType value) {
        this.fromUnit = value;
    }

    /**
     * Gets the value of the toUnit property.
     *
     * @return
     *     possible object is
     *     {@link MassUnitType }
     *
     */
    public MassUnitType getToUnit() {
        return toUnit;
    }

    /**
     * Sets the value of the toUnit property.
     *
     * @param value
     *     allowed object is
     *     {@link MassUnitType }
     *
     */
    public void setToUnit(MassUnitType value) {
        this.toUnit = value;
    }

}
