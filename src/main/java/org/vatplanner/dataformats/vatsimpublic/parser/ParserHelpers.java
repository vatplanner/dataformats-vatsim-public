package org.vatplanner.dataformats.vatsimpublic.parser;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Provides commonly used VATSIM-specific parsing helper methods.
 */
public class ParserHelpers {
    private ParserHelpers() {
        // utility class; hide constructor
    }

    /**
     * Parses the given strings for hours and minutes to a {@link Duration} object.
     * If both strings are empty, null is returned. If only one string is empty, an
     * {@link IllegalArgumentException} will be thrown. If duration is mandatory,
     * strings must not be empty or {@link IllegalArgumentException} will be thrown.
     * Excessive values for minutes (&gt;59) are valid and add to hours.
     *
     * @param hoursString string containing hours to be parsed
     * @param minutesString string containing minutes to be parsed
     * @param isMandatory Is the duration mandatory?
     * @return strings interpreted as duration; null if unavailable
     * @throws IllegalArgumentException if mandatory but not available, only one
     *         string is empty or parsing error
     */
    public static Duration parseDuration(String hoursString, String minutesString, boolean isMandatory) throws IllegalArgumentException {
        boolean emptyHours = hoursString.isEmpty();
        boolean emptyMinutes = minutesString.isEmpty();
        boolean oneEmptyButNotTheOther = emptyHours ^ emptyMinutes;

        if (oneEmptyButNotTheOther) {
            throw new IllegalArgumentException(
                "either hours (\""
                    + hoursString
                    + "\") or minutes (\""
                    + minutesString
                    + "\") was empty but not the other; such inconsistency is not allowed");
        }

        boolean bothEmpty = emptyHours && emptyMinutes;

        if (bothEmpty) {
            if (isMandatory) {
                throw new IllegalArgumentException("hours and minutes are mandatory but both strings were empty");
            }

            return null;
        }

        int hours = Integer.parseInt(hoursString);
        int minutes = Integer.parseInt(minutesString);

        // Unfortunately, negative values can be entered. If that happens, we
        // need to use consistently negative values for hours and minutes. A mix
        // of a different sign on one part of this calculation could cause
        // a positive result, making it impossible to filter out such nonsense
        // at a later stage of processing this information.
        if ((hours < 0) && (minutes > 0)) {
            minutes = -minutes;
        } else if ((hours > 0) && (minutes < 0)) {
            hours = -hours;
        }

        return Duration.ofMinutes(hours * 60 + minutes);
    }

    /**
     * Parses the given string for hours and minutes to a {@link Duration} object.
     * String is expected to be directly concatenated without separator; negative
     * values are accepted.
     * 
     * <p>
     * This method provides compatibility for JSON data files to adapt the original
     * {@link #parseDuration(String, String, boolean)} as used on legacy data file
     * formats.
     * </p>
     * 
     * <p>
     * If duration is mandatory, strings must not be empty or
     * {@link IllegalArgumentException} will be thrown. Excessive values for minutes
     * (&gt;59) are valid and add to hours.
     * </p>
     *
     * @param s string containing hours and minutes to be parsed
     * @param isMandatory Is the duration mandatory?
     * @return string interpreted as duration; null if unavailable
     * @throws IllegalArgumentException if mandatory but not available, only one
     *         string is empty or parsing error
     * @see #parseDuration(String, String, boolean)
     */
    public static Duration parseDirectConcatenatedDuration(String s, boolean isMandatory) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return parseDuration("", "", isMandatory);
        }

        int asInt = Integer.parseInt(s);
        int signum = (asInt < 0) ? -1 : 1;
        asInt = Math.abs(asInt);

        String hoursString = Integer.toString(signum * (asInt / 100));
        String minutesString = Integer.toString(signum * (asInt % 100));

        return parseDuration(hoursString, minutesString, isMandatory);
    }

    /**
     * Tries to parse the given string via {@link Instant#parse(CharSequence)} and
     * retries with {@link LocalDateTime#parse(CharSequence)} if first attempt
     * failed. The {@link LocalDateTime} is assumed to be UTC and returned as an
     * {@link Instant}.
     * <p>
     * This has become necessary after VATSIM rolled out the "Velocity" update at
     * end of January 2022 and some clients (new protocols in effect requiring
     * updates to all clients) started showing up in data files omitting the time
     * zone information.
     * </p>
     * 
     * @param s string to be parsed
     * @return parsed {@link Instant}, assuming UTC for {@link LocalDateTime}s
     */
    public static Instant parseToInstantUtc(CharSequence s) {
        try {
            return Instant.parse(s);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.parse(s).toInstant(ZoneOffset.UTC);
        }
    }

}
