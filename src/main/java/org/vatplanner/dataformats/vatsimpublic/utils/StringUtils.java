package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.regex.Pattern;

/**
 * Utility methods for common string operations.
 */
public class StringUtils {

    private static final Pattern PATTERN_NUMERIC = Pattern.compile("^[0-9]+$");

    private StringUtils() {
        // utility class; hide constructor
    }

    /**
     * Returns null if the input string is empty, otherwise the input will be
     * returned.
     *
     * @param s string to check
     * @return null if input is empty, otherwise input string is returned
     */
    public static String nullIfEmpty(String s) {
        return s.isEmpty() ? null : s;
    }

    /**
     * Returns null if the input string is numeric, otherwise the input will be
     * returned.
     *
     * @param s string to check
     * @return null if input is numeric, otherwise input string is returned
     */
    public static String nullIfNumeric(String s) {
        if (PATTERN_NUMERIC.matcher(s).matches()) {
            return null;
        }

        return s;
    }

    // TODO: unit tests
}
