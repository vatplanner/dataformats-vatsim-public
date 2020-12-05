package org.vatplanner.dataformats.vatsimpublic.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import org.vatplanner.dataformats.vatsimpublic.entities.status.Flight;

/**
 * Helper methods to work with time-based information.
 */
public class TimeHelpers {

    private static final int DAY_MINUTES = 24 * 60;
    private static final Duration MAX_FLIGHT_PLAN_RETENTION_TIME = Duration.ofHours(2); // as per VATSIM
                                                                                        // policies/documentation
    private static final int MIN_FLIGHT_PLAN_DAY_MINUTES = 0;
    private static final int MAX_FLIGHT_PLAN_DAY_MINUTES = DAY_MINUTES + 59; // 24:xx time is a valid input to
                                                                             // circumvent zero hour detection issue

    private TimeHelpers() {
        // utility class; hide constructor
    }

    /**
     * Searches for a timestamp that seems plausible for the user input.
     *
     * @param flight flight the timestamp should be interpreted for
     * @param fieldValue minutes of day as available from a VATSIM flight plan field
     *        in data files
     * @return plausible timestamp for given flight plan field; null if unknown
     */
    public static Instant findClosestPlausibleTimestampForFlightPlanField(Flight flight, int fieldValue) {
        /*
         * FIXME: just a quick and possibly broken implementation; this needs heavy unit
         * testing with all sorts of problematic input
         */

        // abort if value is out of interpretable range
        if (fieldValue < MIN_FLIGHT_PLAN_DAY_MINUTES || fieldValue > MAX_FLIGHT_PLAN_DAY_MINUTES) {
            return null;
        }

        Instant earliestVisibleInstant = flight.getEarliestVisibleTime();
        Instant lastRetentionInstant = earliestVisibleInstant.plus(MAX_FLIGHT_PLAN_RETENTION_TIME);

        /*
         * DEBUG System.out.println(String.format("-- input: %02d:%02d", fieldValue /
         * 60, fieldValue % 60)); System.out.println("visible: " +
         * earliestVisibleInstant);
         */
        OffsetTime fieldTime;
        if (fieldValue >= DAY_MINUTES) {
            fieldTime = LocalTime.ofSecondOfDay((fieldValue % DAY_MINUTES) * 60).atOffset(ZoneOffset.UTC);
        } else {
            fieldTime = LocalTime.ofSecondOfDay(fieldValue * 60).atOffset(ZoneOffset.UTC);
        }

        LocalDate earliestVisibleDate = earliestVisibleInstant.atOffset(ZoneOffset.UTC).toLocalDate();
        LocalDate lastRetentionDate = lastRetentionInstant.atOffset(ZoneOffset.UTC).toLocalDate();

        Instant fieldInstant = fieldTime.atDate(earliestVisibleDate).toInstant();
        if (earliestVisibleInstant.isAfter(fieldInstant)) {
            fieldInstant = fieldTime.atDate(lastRetentionDate).toInstant();
        }

        if (earliestVisibleInstant.isBefore(fieldInstant) && fieldInstant.isBefore(lastRetentionInstant)) {
            // System.out.println("return: " + fieldInstant); // DEBUG

            return fieldInstant;
        }

        // System.out.println("failed: " + fieldInstant); // DEBUG
        return null;
    }

    /**
     * Checks if {@link Duration} <code>a</code> is less than or equal to
     * {@link Duration} <code>b</code>.
     *
     * @param a left operand
     * @param b right operand
     * @return true if <code>a</code>&le;<code>b</code>, false if
     *         <code>a</code>><code>b</code>
     */
    public static boolean isLessOrEqualThan(Duration a, Duration b) {
        return a.compareTo(b) <= 0;
    }

    /**
     * Checks if {@link Duration} <code>a</code> is less than {@link Duration}
     * <code>b</code>.
     *
     * @param a left operand
     * @param b right operand
     * @return true if <code>a</code>&lt;<code>b</code>, false if
     *         <code>a</code>&ge;<code>b</code>
     */
    public static boolean isLessThan(Duration a, Duration b) {
        return a.compareTo(b) < 0;
    }

    // TODO: unit tests
}
