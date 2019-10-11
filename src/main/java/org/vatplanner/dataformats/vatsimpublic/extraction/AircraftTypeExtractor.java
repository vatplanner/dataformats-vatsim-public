package org.vatplanner.dataformats.vatsimpublic.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vatplanner.dataformats.vatsimpublic.entities.status.SimpleEquipmentSpecification;
import org.vatplanner.dataformats.vatsimpublic.entities.status.WakeTurbulenceCategory;
import static org.vatplanner.dataformats.vatsimpublic.utils.StringUtils.nullIfEmpty;

/**
 * Extracts information from a combined aircraft type string as encountered in
 * VATSIM data files.
 *
 * <p>
 * The supported format is <code>[X/]Y[/Z]</code> where
 * </p>
 *
 * <ul>
 * <li><code>X</code> is an optional wake turbulence category code letter</li>
 * <li><code>Y</code> is the actual aircraft type</li>
 * <li><code>Z</code> is an optional equipment code letter</li>
 * </ul>
 *
 * <p>
 * Note that all parts of extracted user input for this data field are
 * unreliable. The extractor therefore intentionally does not resolve code
 * letters to {@link WakeTurbulenceCategory} or
 * {@link SimpleEquipmentSpecification}. The aircraft type is also unreliable;
 * while it should be a valid ICAO code, some input is based on false
 * assumptions or confusion with IATA codes (like "B77F" for a 777 Freighter),
 * other input is due to missing knowledge (like "Boeing 737-800" instead of
 * "B738"). If reliable wake turbulence information is required, the aircraft
 * type should be mapped to a clean result and information extracted from an
 * aircraft type database. The equipment code is often unreliable due to
 * complexity and a conflict of codes between real-world ICAO and VATSIM flight
 * plan codes. If needed, it should be checked against a clean resolved aircraft
 * type for plausibility.
 * </p>
 */
public class AircraftTypeExtractor {

    private static final Pattern PATTERN_SPLIT = Pattern.compile("^\\s*([A-Z]/|)(.*?)(/[A-Z]|)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final int PATTERN_SPLIT_WAKE_CATEGORY = 1;
    private static final int PATTERN_SPLIT_AIRCRAFT_TYPE = 2;
    private static final int PATTERN_SPLIT_EQUIPMENT_CODE = 3;

    private final String wakeCategory;
    private final String aircraftType;
    private final String equipmentCode;

    /**
     * Parses the given data file aircraft type string to extract aircraft
     * information. See class JavaDoc for details.
     *
     * @param s aircraft type field as provided by data files
     */
    public AircraftTypeExtractor(String s) {
        Matcher matcher = PATTERN_SPLIT.matcher(s);
        if (!matcher.matches()) {
            wakeCategory = null;
            aircraftType = nullIfEmpty(s);
            equipmentCode = null;
        } else {
            String rawWakeCategory = matcher.group(PATTERN_SPLIT_WAKE_CATEGORY);
            if (rawWakeCategory.isEmpty()) {
                wakeCategory = null;
            } else {
                wakeCategory = rawWakeCategory.substring(0, 1);
            }

            aircraftType = nullIfEmpty(matcher.group(PATTERN_SPLIT_AIRCRAFT_TYPE));

            String rawEquipmentCode = matcher.group(PATTERN_SPLIT_EQUIPMENT_CODE);
            if (rawEquipmentCode.isEmpty()) {
                equipmentCode = null;
            } else {
                equipmentCode = rawEquipmentCode.substring(1, 2);
            }
        }
    }

    /**
     * Returns the extracted aircraft type. Although users should enter an ICAO
     * code, quite often the actual data entered as type is something else. This
     * will require mapping to correct for input errors if the information
     * should be interpreted further.
     *
     * @return aircraft type (should but may not be an ICAO code); null if
     * unavailable
     */
    public String getAircraftType() {
        return aircraftType;
    }

    /**
     * Returns the extracted equipment code letter. This may be inaccurate or
     * wrong due to complexity or conflicts between real-world ICAO and VATSIM
     * flight plan codes. If really needed, this information should be checked
     * for plausibility depending on the specified aircraft type.
     *
     * @return equipment code letter; null if unavailable
     */
    public String getEquipmentCode() {
        return equipmentCode;
    }

    /**
     * Returns the extracted wake category code letter. This may be inaccurate.
     * If reliable information is needed, look up the aircraft type on an
     * authoritative database.
     *
     * @return wake category code letter; null if unavailable
     */
    public String getWakeCategory() {
        return wakeCategory;
    }

    // TODO: unit tests
}
