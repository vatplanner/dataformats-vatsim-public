package org.vatplanner.dataformats.vatsimpublic.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.vatplanner.dataformats.vatsimpublic.utils.UnitConversion.metersToFeet;

/**
 * Attempts to parse the given altitude string. Altitudes can be specified in
 * feet and meters; an automatically converted value is available from
 * {@link #getFeet()}.
 *
 * <p>
 * If a user input could not be parsed, a negative value is returned instead.
 * </p>
 *
 * <p>
 * All values out of valid range defined by {@link #MINIMUM_THRESHOLD_FEET} and
 * {@link #MAXIMUM_THRESHOLD_FEET} are disregarded as they are not plausible.
 * </p>
 */
public class AltitudeParser {

    private final int value;
    private final boolean isUnitFeet;

    private final int feet;

    private static final int MINIMUM_THRESHOLD_FEET = 200;
    private static final int MAXIMUM_THRESHOLD_FEET = 70000; // Concorde ~60.000ft

    private static final Pattern PATTERN_UNWANTED_CHARS = Pattern.compile("[^FTLMAS0-9\\-]");
    private static final Pattern PATTERN_UNWANTED_LEADING_CHARS = Pattern.compile("^[0\\-]+");

    private static final Pattern PATTERN_METRIC = Pattern.compile(".*[MS].*");
    private static final Pattern PATTERN_ICAO_METRIC = Pattern.compile("^[MS]0.*");
    private static final Pattern PATTERN_EXACT_VALUE = Pattern.compile("^(\\s*[0-9]{4,}|[0-9\\.,\\s]+F?T)$");

    private static final Pattern PATTERN_FIRST_NUMBERS = Pattern.compile("^[A-Z]*([0-9]+).*");
    private static final int PATTERN_FIRST_NUMBERS_NUMBERS = 1;

    private static final int INVALID = -1;

    /**
     * Parses the given string to extract altitude information.
     *
     * @param s string holding altitude information
     */
    public AltitudeParser(String s) {
        s = s.toUpperCase();

        boolean isExactValue = PATTERN_EXACT_VALUE.matcher(s).matches();

        // remove any garbage
        s = PATTERN_UNWANTED_CHARS.matcher(s).replaceAll("");
        s = PATTERN_UNWANTED_LEADING_CHARS.matcher(s).replaceFirst("");

        // metric format may be used in ICAO format (to be multiplied) or as an exact number
        boolean isIcaoMetric = PATTERN_ICAO_METRIC.matcher(s).matches();
        isUnitFeet = !PATTERN_METRIC.matcher(s).matches();

        // continue processing only the first numeric value found in string
        Matcher matcherFirstNumbers = PATTERN_FIRST_NUMBERS.matcher(s);
        if (!matcherFirstNumbers.matches()) {
            value = INVALID;
            feet = INVALID;
            return;
        }

        s = matcherFirstNumbers.group(PATTERN_FIRST_NUMBERS_NUMBERS);

        // excessively long numbers will fail parsing, don't try at all
        int numDigits = s.length();
        if (numDigits > 9) {
            value = INVALID;
            feet = INVALID;
            return;
        }

        // determine factor to apply to value
        int factor = 1;
        if (isUnitFeet && numDigits <= 3 && !isExactValue) {
            factor = 100;
        } else if (isIcaoMetric && numDigits == 4) {
            factor = 10;
        }

        // parse to int and multiply
        int tempValue = Integer.parseUnsignedInt(s) * factor;
        int tempFeet = isUnitFeet ? tempValue : (int) Math.round(metersToFeet(tempValue));

        if ((tempFeet < MINIMUM_THRESHOLD_FEET) || (tempFeet > MAXIMUM_THRESHOLD_FEET)) {
            value = INVALID;
            feet = INVALID;
        } else {
            value = tempValue;
            feet = tempFeet;
        }
    }

    /**
     * Returns the parsed value, unit-agnostic.
     *
     * @return parsed value without unit; negative if unavailable
     * @see #isUnitFeet() to check if unit is feet or meters
     * @see #getFeet() to query for an altitude in feet
     */
    public int getValue() {
        return value;
    }

    /**
     * Indicates if the parsed value should be interpreted in feet or meters.
     *
     * @return true if value is given in feet, false if it is in meters
     * @see #getFeet() to query for an altitude in feet
     */
    public boolean isUnitFeet() {
        return isUnitFeet;
    }

    /**
     * Returns the parsed altitude value in feet. The value has automatically
     * been converted from meters, if necessary.
     *
     * @return altitude in feet; negative if unavailable
     */
    public int getFeet() {
        return feet;
    }
}
