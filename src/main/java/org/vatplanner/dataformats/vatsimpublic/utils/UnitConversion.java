package org.vatplanner.dataformats.vatsimpublic.utils;

/**
 * Provides helper methods calculating unit conversions.
 */
public class UnitConversion {

    private static final double FACTOR_FEET_TO_METERS = 0.3048;
    private static final double FACTOR_METERS_TO_FEET = 1.0 / FACTOR_FEET_TO_METERS;

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

    // TODO: unit tests
}
