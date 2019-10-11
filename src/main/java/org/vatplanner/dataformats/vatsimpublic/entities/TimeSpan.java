package org.vatplanner.dataformats.vatsimpublic.entities;

import java.time.Instant;

/**
 * Holds two timestamps, spanning a period of time.
 */
public class TimeSpan {

    private Instant start;
    private Instant end;

    /**
     * Sets the currently held timestamps to include the given time.
     *
     * @param timestamp timestamp to include in period
     * @return this instance for method-chaining
     */
    public TimeSpan expandTo(Instant timestamp) {
        if ((start == null) || timestamp.isBefore(start)) {
            start = timestamp;
        }

        if ((end == null) || timestamp.isAfter(end)) {
            end = timestamp;
        }

        return this;
    }

    /**
     * Returns the start of the spanned time period.
     *
     * @return start of this time span; null if no time set
     */
    public Instant getStart() {
        return start;
    }

    /**
     * Returns the end of the spanned time period.
     *
     * @return end of this time span; null if no time set
     */
    public Instant getEnd() {
        return end;
    }

    // TODO: unit tests
}
