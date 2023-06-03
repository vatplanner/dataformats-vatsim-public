package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.util.HashMap;
import java.util.Map;

// @formatter:off
/**
 * Pilot ratings indicate the attested competence of pilots on the VATSIM
 * network. Ratings are earned through training and tests by an "Authorized
 * Training Organization". Training is completely optional apart from an initial
 * test of common sense to allow entry to the network.
 * 
 * <p>
 * Ratings were revamped in 2020 to resemble real-world license levels (e.g. PPL
 * and ATPL). Note that while it is possible for real-world pilots to get their
 * real-world licenses accepted for a VATSIM pilot rating, the ratings (despite
 * their name and training content) must not be mistaken to indicate any
 * real-world license/competence level. Pilot's holding no or low pilot ratings
 * also must not be mistaken as incompetent as certified training is completely
 * optional and pilots on VATSIM are still able to fly by simply uncertified
 * self-education.
 * </p>
 * 
 * <p>
 * While VATSIM pilot ratings still can be mapped to a bit mask of levels from
 * P0 to P4 (see forum posts linked below) the newly introduced naming scheme
 * gives an indication that it may not be wise to refer to pilot ratings by
 * numeric levels as they might be subject to change. In particular, the former
 * level P5 has been removed in the 2020 relaunch. Enums provided by this class
 * thus refer to the pseudo-license level names instead.
 * </p>
 * 
 * <p>
 * It appears that no publicly accessible policy has been published that would
 * describe the new ratings. For more information on pilot ratings as of 2020
 * see the following announcements and forum posts:
 * </p>
 * 
 * <ul>
 * <li><a href="https://www.vatsim.net/news/restructuring-pilot-rating-system">https://www.vatsim.net/news/restructuring-pilot-rating-system</a></li>
 * <li><a href="https://forums.vatsim.net/topic/28712-new-vatsim-rating/?do=findComment&amp;comment=166767">https://forums.vatsim.net/topic/28712-new-vatsim-rating/?do=findComment&amp;comment=166767</a></li>
 * <li><a href="https://forums.vatsim.net/topic/28695-vatsim-webmasters-and-pilot-ratings/?do=findComment&amp;comment=166638">https://forums.vatsim.net/topic/28695-vatsim-webmasters-and-pilot-ratings/?do=findComment&amp;comment=166638</a></li>
 * </ul>
 */
//@formatter:on
public enum PilotRating {
    /**
     * Held by pilots who have not completed any pilot training yet.
     */
    UNRATED("NEW"),

    /**
     * VATSIM equivalent to a "Private Pilot Licence" which attests basic knowledge
     * to fly under VFR conditions.
     */
    PPL("PPL"),

    /**
     * VATSIM equivalent to an "Instrument Rating" which attests knowledge on how to
     * fly by IFR without any visual reference. Builds upon {@link #PPL}.
     */
    IR("IR"),

    /**
     * VATSIM equivalent to a "Commercial Multi-Engine Licence [sic!]". Real-world
     * equivalent would be "Commercial Multi-Engine Land" (CMEL), also known as "CPL
     * with ME", which attests proficiency to single-pilot commercial operations and
     * allows small-scale commercial flights to be conducted. Builds upon
     * {@link #IR}.
     */
    CMEL("CMEL"),

    /**
     * VATSIM equivalent to a "Airline Transport Licence [sic!]". Real-world
     * equivalent is the "Airline Transport Pilot Licence" which authorizes pilots
     * to conduct larger-scale commercial flights and act as pilot in command (i.e.
     * participating in multi-crew operation of an aircraft). A real-world ATPL is
     * the standard (and legal requirement) in scheduled airline operations. Builds
     * upon {@link #CMEL}.
     */
    ATPL("ATPL"),

    /**
     * VATSIM equivalent to a "Flight Instructor". This rating allows holders to
     * provide official training for the VATSIM Pilot Training Department (PTD),
     * similar to an real-world "Certified Flight Instructor" (CFI). VATSIM-specific
     * details are described in documents PTD 1001, PTD 1600 and PTD 4001, which can
     * be retrieved after login to my.vatsim.net.
     */
    FI("FI"),

    /**
     * VATSIM equivalent to a "Flight Examiner". As in the real-world, holders of
     * this rating are allowed to conduct skill tests to certify pilots, i.e. issue
     * ratings to pilots. VATSIM-specific details are described in documents PTD
     * 1001, PTD 1100 and PTD 4001, which can be retrieved after login to
     * my.vatsim.net.
     */
    FE("FE");

    private final String shortName;

    private static final Map<String, PilotRating> ratingByShortName = new HashMap<>();

    static {
        for (PilotRating rating : values()) {
            ratingByShortName.put(rating.shortName, rating);
        }
    }

    private PilotRating(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Resolves the given short name to the corresponding {@link PilotRating} enum.
     *
     * @param shortName short name of pilot rating to resolve
     * @return resolved enumeration object or null if unknown
     */
    public static PilotRating resolveShortName(String shortName) {
        return ratingByShortName.get(shortName);
    }
}
