package org.vatplanner.dataformats.vatsimpublic.icao;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Descriptions of aircraft navigation and approach capabilities as provided
 * through designators used in flight plan ICAO field 10a (type) and field 18
 * (remark) PBN.
 *
 * <p>
 * Resources used to collect these designators:
 * </p>
 * <ul>
 * <li>https://contentzone.eurocontrol.int/fpl/</li>
 * <li>https://www.icao.int/safety/FITS/DocumentLibrary/FITS-Library/Guidance_Item%2010_18.pdf</li>
 * </ul>
 *
 * <p>
 * <b>DISCLAIMER: THIS CLASS IS INTENDED TO BE USED ONLY IN THE CONTEXT OF
 * FLIGHT SIMULATION! INFORMATION PRESENTED HERE MAY NOT CONFORM TO CURRENT
 * REAL-WORLD STANDARDS OR LACK PRECISION FOR USE IN REAL-WORLD AVIATION.</b>
 * </p>
 */
public enum NavigationApproachCapability {
    NONE('N', -1),
    OTHER('Z', -1),

    GBAS_LANDING_SYSTEM('A', -1),
    RNAV10('A', 1),
    LPV('B', -1),
    RNAV5_ALL_PERMITTED_SENSORS('B', 1),
    RNAV5_GNSS('B', 2),
    RNAV5_DME_DME('B', 3),
    RNAV5_VOR_DME('B', 4),
    RNAV5_INS_IRS('B', 5),
    RNAV5_LORAN_C('B', 6),
    LORAN_C('C', -1),
    RNAV2_ALL_PERMITTED_SENSORS('C', 1),
    RNAV2_GNSS('C', 2),
    RNAV2_DME_DME('C', 3),
    RNAV2_DME_DME_IRU('C', 4),
    DME('D', -1),
    RNAV1_ALL_PERMITTED_SENSORS('D', 1),
    RNAV1_GNSS('D', 2),
    RNAV1_DME_DME('D', 3),
    RNAV1_DME_DME_IRU('D', 4),
    ADF('F', -1),
    GNSS('G', -1),
    MLS('K', -1),
    INS('I', -1),
    ILS('L', -1),
    RNP_4('L', 1),
    // @formatter:off
    /**
     * <code>M</code> has been removed by 2012 ICAO update as OMEGA navigation
     * system (dated 1971) has been shut down in 1997. <code>M</code> codes (with a
     * level indication) have been re-purposed to indicate SATCOM communication by
     * 2012 update. Keeping it in this decoder in case somebody actually tries to
     * simulate OMEGA...
     *
     * @see <a href="https://en.wikipedia.org/wiki/Omega_(navigation_system)">https://en.wikipedia.org/wiki/Omega_(navigation_system)</a>
     */
    // @formatter:on
    OMEGA('M', -1),
    VOR('O', -1),
    BASIC_RNP_1_ALL_PERMITTED_SENSORS('O', 1),
    BASIC_RNP_1_GNSS('O', 2),
    BASIC_RNP_1_DME_DME('O', 3),
    BASIC_RNP_1_DME_DME_IRU('O', 4),
    PBN_APPROVED('R', -1),
    RNP_APCH('S', 1),
    RNP_APCH_BARO_VNAV('S', 2),
    TACAN('T', -1),
    RNP_AR_APCH_WITH_RF('T', 1),
    RNP_AR_APCH_WITHOUT_RF('T', 2),
    RVSM('W', -1),
    MNPS_APPROVED('X', -1),
    /**
     * Standard capabilities combine {@link #VOR} and {@link #ILS} plus
     * {@link CommunicationCapability}. 2012 ICAO update removed {@link #ADF} from
     * standard capabilities.
     */
    STANDARD('S', -1, VOR, ILS);

    private final String designator;
    private final Set<NavigationApproachCapability> expanded;

    private static final Map<String, NavigationApproachCapability> BY_DESIGNATOR = new HashMap<>();

    static {
        for (NavigationApproachCapability capability : values()) {
            NavigationApproachCapability previous = BY_DESIGNATOR.put(capability.designator, capability);
            if (previous != null) {
                throw new RuntimeException(
                    "ambiguous designator " + capability.designator + " for " + previous + " and " + capability
                );
            }
        }
    }

    private NavigationApproachCapability(char group, int level, NavigationApproachCapability... expanded) {
        designator = level >= 0
            ? Character.toString(group) + Integer.toString(level)
            : Character.toString(group);

        Set<NavigationApproachCapability> expandedSet = new HashSet<NavigationApproachCapability>(asList(expanded));
        expandedSet.add(this);
        this.expanded = unmodifiableSet(expandedSet);
    }

    public static NavigationApproachCapability byDesignator(String designator) {
        return BY_DESIGNATOR.get(designator);
    }

    public Set<NavigationApproachCapability> expand() {
        return expanded;
    }
}
