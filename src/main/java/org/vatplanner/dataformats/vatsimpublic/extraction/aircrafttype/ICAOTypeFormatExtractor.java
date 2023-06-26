package org.vatplanner.dataformats.vatsimpublic.extraction.aircrafttype;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vatplanner.dataformats.vatsimpublic.entities.status.WakeTurbulenceCategory;
import org.vatplanner.dataformats.vatsimpublic.extraction.AircraftTypeExtractor;

/**
 * Extracts information from an ICAO-like combined aircraft type string as
 * encountered in VATSIM data files.
 *
 * <p>
 * The supported format is <code>A/B-C[/D]</code> where
 * </p>
 *
 * <ul>
 * <li><code>A</code> is the actual aircraft type</li>
 * <li><code>B</code> is the wake turbulence category code letter</li>
 * <li><code>C</code> is the ICAO field 10a communication/navigation/approach
 * aid equipment code</li>
 * <li><code>D</code> is the (on VATSIM) optional ICAO field 10b surveillance
 * equipment code</li>
 * </ul>
 *
 * <p>
 * Before August 2020 another format based on FAA US domestic flight plans has
 * been used globally. Since the actual contents of the VATSIM flight plan field
 * depend on the application submitting it, a mix of both formats is to be
 * expected. Use {@link AircraftTypeExtractor} to support both.
 * </p>
 *
 * <p>
 * Note that all parts of extracted user input for this data field are
 * unreliable. The extractor therefore intentionally does not resolve code
 * letters to {@link WakeTurbulenceCategory} or parses any equipment codes. The
 * aircraft type is also unreliable; while it should be a valid ICAO code, some
 * input may be based on false assumptions or confusion with IATA codes (like
 * "B77F" for a 777 Freighter), other input is due to missing knowledge (like
 * "Boeing 737-800" instead of "B738"). If reliable wake turbulence information
 * is required, the aircraft type should be mapped to a clean result and
 * information extracted from an aircraft type database. The equipment code may
 * be even more unreliable than in the old FAA-like format due to its extra
 * complexity, unless submitted from a proper flight planning software. If
 * needed, it should be checked against a clean resolved aircraft type for
 * plausibility.
 * </p>
 */
public class ICAOTypeFormatExtractor implements ParsedTypeData {

    private static final Pattern PATTERN_SPLIT = Pattern.compile(
        "^\\s*([^/]+)/([A-Z])\\-([A-Z0-9]+(?:/[A-Z0-9]*|))\\s*$",
        Pattern.CASE_INSENSITIVE
    );

    private static final int PATTERN_SPLIT_AIRCRAFT_TYPE = 1;
    private static final int PATTERN_SPLIT_WAKE_CATEGORY = 2;
    private static final int PATTERN_SPLIT_EQUIPMENT_CODE = 3;

    private final String aircraftType;
    private final String wakeCategory;
    private final String equipmentCode;

    /**
     * Parses the given data file aircraft type string to extract aircraft
     * information assuming it is ICAO-style information. See class JavaDoc for
     * details.
     *
     * <p>
     * Note that an {@link IllegalArgumentException} will be thrown if the format
     * does not match expectation. If this happens, you should consider falling back
     * to {@link FAADomesticTypeFormatExtractor} which will also allow completely
     * invalid data to pass as a free-form aircraft type. The exception can be used
     * to test if the input is in ICAO format.
     * </p>
     *
     * @param s aircraft type field as provided by data files
     * @throws IllegalArgumentException if input does not match expected ICAO-style
     *                                  format or input was null
     */
    public ICAOTypeFormatExtractor(String s) throws IllegalArgumentException {
        if (s == null) {
            throw new IllegalArgumentException("input must not be null");
        }

        Matcher matcher = PATTERN_SPLIT.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("input is not expected ICAO format");
        }

        aircraftType = matcher.group(PATTERN_SPLIT_AIRCRAFT_TYPE);
        wakeCategory = matcher.group(PATTERN_SPLIT_WAKE_CATEGORY);
        equipmentCode = matcher.group(PATTERN_SPLIT_EQUIPMENT_CODE);
    }

    /**
     * Returns the extracted aircraft type. Although users should enter an ICAO code
     * it may happen that the actual data entered as type is something else. This
     * will require mapping to correct for input errors if the information should be
     * interpreted further.
     *
     * @return aircraft type (should but may not be an ICAO code)
     */
    @Override
    public String getAircraftType() {
        return aircraftType;
    }

    /**
     * Returns the extracted equipment code. This may be inaccurate or wrong due to
     * complexity or confusion with old simple equipment or FAA codes. If really
     * needed, this information should be checked for plausibility depending on the
     * specified aircraft type.
     *
     * <p>
     * Note that equipment codes in ICAO-style VATSIM flight plans are usually very
     * extensive ICAO field 10a + 10b declarations. Field 10b is separated from 10a
     * by a slash <code>/</code> and comes last.
     * </p>
     *
     * <p>
     * Returned information should be expected to not only contain field 10 encoded
     * information but also field 18 PBN codes due to complexity of ICAO flight plan
     * filing and user confusion.
     * </p>
     *
     * @return equipment code
     */
    @Override
    public String getEquipmentCode() {
        return equipmentCode;
    }

    /**
     * Returns the extracted wake category code letter. This may be inaccurate. If
     * reliable information is needed, look up the aircraft type on an authoritative
     * database.
     *
     * @return wake category code letter
     */
    @Override
    public String getWakeCategory() {
        return wakeCategory;
    }
}
