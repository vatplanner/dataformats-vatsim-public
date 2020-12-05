package org.vatplanner.dataformats.vatsimpublic.extraction;

import org.vatplanner.dataformats.vatsimpublic.entities.status.WakeTurbulenceCategory;
import org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype.FAADomesticTypeFormatExtractor;
import org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype.ICAOTypeFormatExtractor;
import org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype.ParsedTypeData;

/**
 * Extracts information from a combined aircraft type string as encountered in
 * VATSIM data files.
 *
 * <p>
 * Type fields combine information about actual aircraft type, wake category and
 * equipment. Two different formats are in use on VATSIM:
 * </p>
 *
 * <ul>
 * <li>a full ICAO format looking like <code>A319/M-SDE3FGHIRWY/LB1</code> or
 * <code>B737/M-S</code></li>
 * <li>a FAA-based format looking like <code>H/B744/L</code>,
 * <code>H/B744</code>, <code>B744/L</code> or just <code>B744</code></li>
 * </ul>
 *
 * <p>
 * The FAA-based format has been used on VATSIM for many years and always caused
 * confusion about the (simple) equipment codes. With the launch of a new flight
 * plan filing web form in August 2020, a migration towards the modern
 * real-world ICAO format has been started. Most notably, a different placement
 * of the actual aircraft type causes both formats to be incompatible to each
 * other and requiring their own interpretation.
 * </p>
 *
 * <p>
 * Note that all parts of extracted user input for this data field are
 * unreliable. The extractor therefore intentionally does not resolve code
 * letters to {@link WakeTurbulenceCategory} or equipment codes. The aircraft
 * type is also unreliable; while it should be a valid ICAO code, some input is
 * based on false assumptions or confusion with IATA codes (like "B77F" for a
 * 777 Freighter), other input is due to missing knowledge (like "Boeing
 * 737-800" instead of "B738"). If reliable wake turbulence information is
 * required, the aircraft type should be mapped to a clean result and
 * information extracted from an aircraft type database. The equipment code is
 * often unreliable due to complexity and a conflict of codes between (for the
 * legacy FAA-based format) real-world ICAO and VATSIM flight plan codes. If
 * needed, it should be checked against a clean resolved aircraft type for
 * plausibility.
 * </p>
 */
public class AircraftTypeExtractor implements ParsedTypeData {

    private final ParsedTypeData data;
    private final boolean isICAOFormat;

    /**
     * Parses the given data file aircraft type string to extract aircraft
     * information. See class JavaDoc for details.
     *
     * @param s aircraft type field as provided by data files
     */
    public AircraftTypeExtractor(String s) {
        ParsedTypeData data;
        try {
            data = new ICAOTypeFormatExtractor(s);
        } catch (IllegalArgumentException ex) {
            // try FAA format instead
            data = new FAADomesticTypeFormatExtractor(s);
        }

        this.data = data;
        isICAOFormat = (data instanceof ICAOTypeFormatExtractor);
    }

    /**
     * Returns the extracted aircraft type. Although users should enter an ICAO
     * code, quite often the actual data entered as type is something else. This
     * will require mapping to correct for input errors if the information should be
     * interpreted further.
     *
     * @return aircraft type (should but may not be an ICAO code); null if
     *         unavailable
     */
    @Override
    public String getAircraftType() {
        return data.getAircraftType();
    }

    /**
     * Returns the extracted equipment code. Simplified codes use just a single
     * letter while detailed modern ICAO-codes are more extensive descriptions which
     * require additional parsing. The supplied information may be inaccurate or
     * wrong due to complexity or (at least for the simple one-letter codes)
     * conflicts between real-world ICAO and VATSIM flight plan codes. If really
     * needed, this information should be checked for plausibility depending on the
     * specified aircraft type.
     *
     * @return equipment code; null if unavailable
     */
    @Override
    public String getEquipmentCode() {
        return data.getEquipmentCode();
    }

    /**
     * Returns the extracted wake category code letter. This may be inaccurate. If
     * reliable information is needed, look up the aircraft type on an authoritative
     * database.
     *
     * @return wake category code letter; null if unavailable
     */
    @Override
    public String getWakeCategory() {
        return data.getWakeCategory();
    }

    /**
     * Indicates if the information has been extracted from ICAO format. This is
     * relevant for correct interpretation of {@link #getEquipmentCode()} as
     * designators have different meanings depending on the context (if used
     * correctly).
     *
     * @return true if information was extracted from ICAO format, false if not
     */
    public boolean isICAOFormat() {
        return isICAOFormat;
    }

    // TODO: unit tests
}
