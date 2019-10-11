package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.util.HashMap;
import java.util.Map;

/**
 * Indicates the flight plan type. This has been simplified on VATSIM: Only IFR
 * and VFR are currently supported. Other real-world types would be Y and Z
 * types (mixed IFR and VFR, one changing to the other one enroute). Those have
 * not yet been observed in collected data and are thus omitted. The meaning of
 * other actually observed types (in particular D and S) is unclear given the
 * aircraft types flying under those flight plans.
 */
public enum FlightPlanType {
    IFR('I'),
    VFR('V');

    private final char code;

    private static final Map<Character, FlightPlanType> INDEXED_BY_CODE = new HashMap<>();

    static {
        for (FlightPlanType type : values()) {
            INDEXED_BY_CODE.put(type.code, type);
        }
    }

    private FlightPlanType(char code) {
        this.code = code;
    }

    /**
     * Returns the single-letter code used to identify this type in flight
     * plans.
     *
     * @return single-letter code used in flight plans
     */
    public char getCode() {
        return code;
    }

    /**
     * Resolves the given flight plan code to a flight plan type. Returns null
     * if no such type has been defined or input is empty or null.
     *
     * @param flightPlanCode flight plan code to resolve
     * @return type matching the code; null if not found or no code was entered
     * @throws IllegalArgumentException if code exceeds one letter
     */
    public static FlightPlanType resolveFlightPlanCode(String flightPlanCode) {
        if ((flightPlanCode == null) || flightPlanCode.isEmpty()) {
            return null;
        }

        if (flightPlanCode.length() != 1) {
            throw new IllegalArgumentException("flight plan type code must only have one letter; was: \"" + flightPlanCode + "\"");
        }

        return INDEXED_BY_CODE.get(flightPlanCode.toUpperCase().charAt(0));
    }

    // TODO: unit tests
}
