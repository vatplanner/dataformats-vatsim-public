package org.vatplanner.dataformats.vatsimpublic.entities.status;

/**
 * Holds a value representing barometric pressure. Depending on region,
 * barometric pressure is measured in either inches of mercury (inHg) or
 * hectopascals (hPa). Either unit can be used to define and query pressure
 * information maintained by this class; values will automatically converted to
 * the requested unit, if required.
 */
public class BarometricPressure {

    private final double value;
    private final boolean isUnitInchesOfMercury;

    /**
     * Indicates value is measured in inches of mercury (inHg).
     */
    public static final boolean UNIT_INCHES_OF_MERCURY = true;

    /**
     * Indicates value is measured in hectopascals (hPa).
     */
    public static final boolean UNIT_HECTOPASCALS = false;

    /**
     * Creates a new instance holding given value with unit information.
     *
     * @param value value
     * @param isUnitInchesOfMercury unit; use constants provided by this class
     * @see #UNIT_INCHES_OF_MERCURY
     * @see #UNIT_HECTOPASCALS
     */
    public BarometricPressure(double value, boolean isUnitInchesOfMercury) {
        this.value = value;
        this.isUnitInchesOfMercury = isUnitInchesOfMercury;
    }

    /**
     * Returns the value in inches of mercury (inHg). Value will be
     * automatically converted if defined by another unit.
     *
     * @return value in inches of mercury (inHg)
     */
    public double getInchesOfMercury() {
        // TODO: implement automatic conversion
        return -1;
    }

    /**
     * Returns the value in hectopascals (hPa). Value will be automatically
     * converted if defined by another unit.
     *
     * @return value in hectopascals (hPa)
     */
    public double getHectopascals() {
        // TODO: implement automatic conversion
        return -1;
    }

    // TODO: unit tests
}
