package org.vatplanner.dataformats.vatsimpublic.entities.status;

// @formatter:off
/**
 * Pilots can earn military ratings through training offered by VSOs on VATSIM.
 *
 * <p>
 * Simulation of military activity is highly regulated on VATSIM. Only members of
 * "VATSIM Special Operations" (VSO) organizations are allowed to partake in such
 * activity and trainings.
 * </p>
 *
 * <p>
 * Military pilot ratings were added in <a href="https://tech.vatsim.net/blog/vatsim_services/new_pilot_ratings">May 2023</a>.
 * </p>
 *
 * <p>
 * Policy documents are available from <a href="https://my.vatsim.net/">my.vatsim.net</a> after login.
 * </p>
 */
//@formatter:on
public enum MilitaryRating {
    /**
     * Default for pilots who do not hold any military rating.
     */
    M0,

    /**
     * Described as "Military Pilot License" as of May 2023.
     */
    M1,

    /**
     * Described as "Military Instrument Rating" as of May 2023.
     */
    M2,

    /**
     * Described as "Military Multi-Engine Rating" as of May 2023.
     */
    M3,

    /**
     * Described as "Military Mission Ready Pilot" as of May 2023.
     */
    M4;

    /**
     * Resolves the given short name to the corresponding {@link MilitaryRating} enum.
     *
     * @param shortName short name of pilot military rating to resolve
     * @return resolved enumeration object or null if unknown
     */
    public static MilitaryRating resolveShortName(String shortName) {
        try {
            return MilitaryRating.valueOf(shortName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
