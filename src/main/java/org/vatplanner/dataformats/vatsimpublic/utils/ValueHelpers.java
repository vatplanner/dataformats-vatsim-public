package org.vatplanner.dataformats.vatsimpublic.utils;

/**
 * Helper methods to work with generic values.
 */
public class ValueHelpers {

    private ValueHelpers() {
        // utility class; hide constructor
    }

    /**
     * Checks if the given values are in specified range.
     *
     * @param value value to check
     * @param minimumValue minimum allowed value (inclusive)
     * @param maximumValue maximum allowed value (inclusive)
     * @return true if value is in range; false if not
     */
    public static boolean inRange(double value, double minimumValue, double maximumValue) {
        return (value >= minimumValue) && (value <= maximumValue);
    }

}
