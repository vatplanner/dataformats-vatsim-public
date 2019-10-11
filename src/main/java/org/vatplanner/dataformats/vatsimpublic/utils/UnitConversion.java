package org.vatplanner.dataformats.vatsimpublic.utils;

/**
 * Provides helper methods calculating unit conversions. Note that factors and
 * converted values may lack precision but should be sufficient for use when
 * processing VATSIM (not real-world) data.
 */
public class UnitConversion {

    private static final double FACTOR_FEET_TO_METERS = 0.3048;
    private static final double FACTOR_METERS_TO_FEET = 1.0 / FACTOR_FEET_TO_METERS;

    private static final double FACTOR_HECTOPASCALS_TO_INCHES_OF_MERCURY = 0.029529983071445;
    private static final double FACTOR_INCHES_OF_MERCURY_TO_HECTOPASCALS = 1.0 / FACTOR_HECTOPASCALS_TO_INCHES_OF_MERCURY;

    private UnitConversion() {
        // utility class; hide constructor
    }

    /**
     * Converts given length from feet to meters.
     *
     * @param feet value in feet
     * @return value in meters
     */
    public static double feetToMeters(double feet) {
        return feet * FACTOR_FEET_TO_METERS;
    }

    /**
     * Converts given length from meters to feet.
     *
     * @param meters value in meters
     * @return value in feet
     */
    public static double metersToFeet(double meters) {
        return meters * FACTOR_METERS_TO_FEET;
    }

    /**
     * Converts the given barometric pressure from inches of mercury (inHg) to
     * hectopascals (hPa).
     *
     * @param inchesOfMercury value in inches of mercury (inHg)
     * @return value in hectopascals (hPa)
     */
    public static double inchesOfMercuryToHectopascals(double inchesOfMercury) {
        return inchesOfMercury * FACTOR_INCHES_OF_MERCURY_TO_HECTOPASCALS;
    }

    /**
     * Converts the given barometric pressure from hectopascals (hPa) to inches
     * of mercury (inHg).
     *
     * @param hectopascals value in hectopascals (hPa)
     * @return value in inches of mercury (inHg)
     */
    public static double hectopascalsToInchesOfMercury(double hectopascals) {
        return hectopascals * FACTOR_HECTOPASCALS_TO_INCHES_OF_MERCURY;
    }

    // TODO: unit tests
}
