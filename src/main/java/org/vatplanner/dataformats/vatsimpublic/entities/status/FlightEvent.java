package org.vatplanner.dataformats.vatsimpublic.entities.status;

/**
 * Used to mark specific events throughout a tracked flight.
 */
public enum FlightEvent {
    /**
     * Marks the last track point just before a flight has been observed to be
     * airborne for the first time.
     */
    BEFORE_AIRBORNE,
    /**
     * Marks the first track point a flight has been observed to be airborne at.
     */
    AIRBORNE,
    /**
     * Marks the first track point a flight has been observed to have landed.
     */
    LANDED;
}
