package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum identifies the wake turbulence category of an aircraft.
 */
public enum WakeTurbulenceCategory {
    LIGHT('L'),
    MEDIUM('M'),
    HEAVY('H'),
    SUPER_HEAVY('S');

    private final char flightPlanCode;

    private static final Map<Character, WakeTurbulenceCategory> byFlightPlanCode = new HashMap<>();

    static {
        for (WakeTurbulenceCategory category : values()) {
            byFlightPlanCode.put(category.flightPlanCode, category);
        }
    }

    private WakeTurbulenceCategory(char flightPlanCode) {
        this.flightPlanCode = flightPlanCode;
    }

    /**
     * Returns the code used to identify the category in flight plans.
     *
     * @return code to identify category in flight plans
     */
    public char getFlightPlanCode() {
        return flightPlanCode;
    }

    /**
     * Resolves the given flight plan code to a well-defined category. Returns
     * null if no such category has been defined or input is empty or null.
     *
     * @param flightPlanCode flight plan code to resolve
     * @return category matching the code; null if not found or no code was
     * entered
     * @throws IllegalArgumentException if code exceeds one letter
     */
    public static WakeTurbulenceCategory resolveFlightPlanCode(String flightPlanCode) {
        if ((flightPlanCode == null) || flightPlanCode.isEmpty()) {
            return null;
        }

        if (flightPlanCode.length() != 1) {
            throw new IllegalArgumentException("flight plan wake category codes must only have one letter");
        }

        return byFlightPlanCode.get(flightPlanCode.toUpperCase().charAt(0));
    }

    // TODO: unit tests
}
