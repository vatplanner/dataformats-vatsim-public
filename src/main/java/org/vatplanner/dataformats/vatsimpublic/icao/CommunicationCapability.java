package org.vatplanner.dataformats.vatsimpublic.icao;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Descriptions of aircraft communication capabilities as provided through
 * designators used in flight plan ICAO field 10a (type).
 *
 * <p>
 * Note that this information is only partially useful on VATSIM: If users
 * indicate their actual capability, then looking for PDC and CPDLC support
 * could be meaningful to controllers. All VATSIM clients can communicate by VHF
 * by default (although the user may not communicate by voice but text instead).
 * HF may require a mapping to VHF band if an add-on does not support HF, but
 * since those frequencies are aliased anyway HF indication should not have any
 * actual meaning for VATSIM. Unfortunately, if users just file what a generic
 * flight planning tool applies based on common real-world aircraft operations,
 * then those indications may indicate PDC/CPDLC capability although unsupported
 * by the exact add-on or not set up/used by the pilot. The same goes for
 * 8.33kHz frequency spacing which is currently not used on VATSIM due to a lack
 * of add-on/simulator compatibility.
 * </p>
 *
 * <p>
 * Resources used to collect these designators:
 * <ul>
 * <li>https://contentzone.eurocontrol.int/fpl/</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>DISCLAIMER: THIS CLASS IS INTENDED TO BE USED ONLY IN THE CONTEXT OF
 * FLIGHT SIMULATION! INFORMATION PRESENTED HERE MAY NOT CONFORM TO CURRENT
 * REAL-WORLD STANDARDS OR LACK PRECISION FOR USE IN REAL-WORLD AVIATION.</b>
 * </p>
 */
public enum CommunicationCapability {
    NONE('N', -1),
    OTHER('Z', -1),

    ACARS_FMC_WPR('E', 1),
    ACARS_D_FIS('E', 2),
    ACARS_PDC('E', 3),
    HF_RTF('H', -1),
    /**
     * <code>J</code> without any level is legacy designator (removed in 2012 ICAO
     * update).
     */
    CPDLC('J', -1),
    CPDLC_ATN_VDL_MODE_2('J', 1, CPDLC),
    CPDLC_FANS1A_HFDL('J', 2, CPDLC),
    CPDLC_FANS1A_VDL_MODE_A('J', 3, CPDLC),
    CPDLC_FANS1A_VDL_MODE_2('J', 4, CPDLC),
    CPDLC_FANS1A_SATCOM_INMARSAT('J', 5, CPDLC),
    CPDLC_FANS1A_SATCOM_MTSAT('J', 6, CPDLC),
    CPDLC_FANS1A_SATCOM_IRIDIUM('J', 7, CPDLC),
    ATC_SATVOICE_INMARSAT('M', 1),
    ATC_SATVOICE_MTSAT('M', 2),
    ATC_SATVOICE_IRIDIUM('M', 3),
    /**
     * Required Communication Performance of 400 seconds provided through CPDLC.
     * CPDLC in flight simulation is provided through networks which also have a
     * latency that cannot be guaranteed by users and is not intentionally simulated
     * but will generally be less than established real-world RCP.
     *
     * @see https://code7700.com/communications_rcp.htm
     */
    RCP_400_CPDLC('P', 1),
    /**
     * Required Communication Performance of 240 seconds provided through CPDLC.
     * CPDLC in flight simulation is provided through networks which also have a
     * latency that cannot be guaranteed by users and is not intentionally simulated
     * but will generally be less than established real-world RCP.
     *
     * @see https://code7700.com/communications_rcp.htm
     */
    RCP_240_CPDLC('P', 2),
    /**
     * Required Communication Performance of 400 seconds provided through satellite.
     * Does not make any sense to declare in flight simulation as there is no
     * SATCOM.
     *
     * @see https://code7700.com/communications_rcp.htm
     */
    RCP_400_SATVOICE('P', 3),
    UHF_RTF('U', -1),
    VHF_RTF('V', -1),
    VHF_RTF_8_33_KHZ('Y', -1, VHF_RTF),
    /**
     * Standard capability is only {@link #VHF_RTF} plus
     * {@link NavigationApproachCapability}. It is not sure if
     * {@link #VHF_RTF_8_33_KHZ} is implicitly included in "standard" for regions
     * that require it.
     */
    STANDARD('S', -1, VHF_RTF);

    private final String designator;
    private final Set<CommunicationCapability> expanded;

    private static final Map<String, CommunicationCapability> BY_DESIGNATOR = new HashMap<>();

    public static final Set<CommunicationCapability> STANDARD_EXPANDED = unmodifiableSet(
        new HashSet<CommunicationCapability>( //
            asList( //
                VHF_RTF //
            ) //
        ) //
    );

    static {
        for (CommunicationCapability capability : values()) {
            CommunicationCapability previous = BY_DESIGNATOR.put(capability.designator, capability);
            if (previous != null) {
                throw new RuntimeException(
                    "ambiguous designator " + capability.designator + " for " + previous + " and " + capability);
            }
        }
    }

    private CommunicationCapability(char group, int level, CommunicationCapability... expanded) {
        designator = level >= 0
            ? Character.toString(group) + Integer.toString(level)
            : Character.toString(group);

        Set<CommunicationCapability> expandedSet = new HashSet<CommunicationCapability>(asList(expanded));
        expandedSet.add(this);
        this.expanded = unmodifiableSet(expandedSet);
    }

    public static CommunicationCapability byDesignator(String designator) {
        return BY_DESIGNATOR.get(designator);
    }

    public Set<CommunicationCapability> expand() {
        return expanded;
    }
}
