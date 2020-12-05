package org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype;

/**
 * Provides parsed aircraft type information.
 */
public interface ParsedTypeData {

    /**
     * Returns the extracted aircraft type.
     *
     * @return aircraft type (should but may not be an ICAO code); null if
     *         unavailable
     */
    public String getAircraftType();

    /**
     * Returns the extracted equipment code.
     *
     * @return equipment code; null if unavailable
     */
    public String getEquipmentCode();

    /**
     * Returns the extracted wake category code letter.
     *
     * @return wake category code letter; null if unavailable
     */
    public String getWakeCategory();
}
