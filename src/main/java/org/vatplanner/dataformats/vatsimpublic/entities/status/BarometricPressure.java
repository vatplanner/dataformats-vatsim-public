package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.vatplanner.dataformats.vatsimpublic.utils.UnitConversion.hectopascalsToInchesOfMercury;
import static org.vatplanner.dataformats.vatsimpublic.utils.UnitConversion.inchesOfMercuryToHectopascals;
import static org.vatplanner.dataformats.vatsimpublic.utils.ValueHelpers.inRange;

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

    private static final double LOWEST_OBSERVED_QNH_HPA = 870; // according to Wikipedia
    private static final double HIGHEST_OBSERVED_QNH_HPA = 1085; // according to Wikipedia

    private static final double PLAUSIBILITY_TOLERANCE_FACTOR = 0.01;

    private static final double MINIMUM_PLAUSIBLE_QNH_HPA = //
        LOWEST_OBSERVED_QNH_HPA - LOWEST_OBSERVED_QNH_HPA * PLAUSIBILITY_TOLERANCE_FACTOR;

    private static final double MAXIMUM_PLAUSIBLE_QNH_HPA = //
        HIGHEST_OBSERVED_QNH_HPA + HIGHEST_OBSERVED_QNH_HPA * PLAUSIBILITY_TOLERANCE_FACTOR;

    private static final double MINIMUM_PLAUSIBLE_QNH_INHG = hectopascalsToInchesOfMercury(MINIMUM_PLAUSIBLE_QNH_HPA);
    private static final double MAXIMUM_PLAUSIBLE_QNH_INHG = hectopascalsToInchesOfMercury(MAXIMUM_PLAUSIBLE_QNH_HPA);

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
     * Returns the value in inches of mercury (inHg). Value will be automatically
     * converted if defined by another unit.
     *
     * @return value in inches of mercury (inHg)
     */
    public double getInchesOfMercury() {
        if (isUnitInchesOfMercury == UNIT_INCHES_OF_MERCURY) {
            return value;
        } else {
            return hectopascalsToInchesOfMercury(value);
        }
    }

    /**
     * Returns the value in hectopascals (hPa). Value will be automatically
     * converted if defined by another unit.
     *
     * @return value in hectopascals (hPa)
     */
    public double getHectopascals() {
        if (isUnitInchesOfMercury == UNIT_HECTOPASCALS) {
            return value;
        } else {
            return inchesOfMercuryToHectopascals(value);
        }
    }

    /**
     * Creates a new value container describing given value in hectopascals (hPa).
     *
     * @param value value in hectopascals (hPa)
     * @return new value container
     */
    public static BarometricPressure fromHectopascals(double value) {
        return new BarometricPressure(value, UNIT_HECTOPASCALS);
    }

    /**
     * Creates a new value container describing given value in inches of mercury
     * (inHg).
     *
     * @param value value in inches of mercury (inHg)
     * @return new value container
     */
    public static BarometricPressure fromInchesOfMercury(double value) {
        return new BarometricPressure(value, UNIT_INCHES_OF_MERCURY);
    }

    /**
     * Checks if the specified pressure is plausible to appear as a local QNH
     * (barometric pressure at sea level) on earth.
     *
     * @return true if plausible, false if not
     */
    public boolean isPlausibleQnh() {
        double minimum;
        double maximum;

        if (isUnitInchesOfMercury == UNIT_HECTOPASCALS) {
            minimum = MINIMUM_PLAUSIBLE_QNH_HPA;
            maximum = MAXIMUM_PLAUSIBLE_QNH_HPA;
        } else {
            minimum = MINIMUM_PLAUSIBLE_QNH_INHG;
            maximum = MAXIMUM_PLAUSIBLE_QNH_INHG;
        }

        return inRange(value, minimum, maximum);
    }

    /**
     * Creates a new instance holding given value with unit information if the
     * specified pressure is plausible to appear as local QNH (barometric pressure
     * at sea level) on earth. Otherwise returns null.
     *
     * @param value value
     * @param isUnitInchesOfMercury unit; use constants provided by this class
     * @return plausible QNH; null if implausible
     * @see #UNIT_INCHES_OF_MERCURY
     * @see #UNIT_HECTOPASCALS
     */
    public static BarometricPressure forPlausibleQnh(double value, boolean isUnitInchesOfMercury) {
        BarometricPressure pressure = new BarometricPressure(value, isUnitInchesOfMercury);

        if (pressure.isPlausibleQnh()) {
            return pressure;
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("BarometricPressure(%.2f %s)", value, isUnitInchesOfMercury ? "inHg" : "hPa");
    }

    // TODO: unit tests
}
