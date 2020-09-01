package org.vatplanner.dataformats.vatsimpublic.icao;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses ICAO flight plan field 10a/b and field 18 PBN capabilities.
 *
 * <p>
 * PBN capabilities are accepted on field 10a data and currently only supported
 * through inclusion in field 10a. This does not match real-world specification
 * but makes sense when parsing potentially "dirty" VATSIM user input.
 * </p>
 *
 * <p>
 * Surveillance equipment codes (field 10b) are not fully parsed as it makes no
 * sense in the context of being used for VATSIM (there always is surveillance
 * equivalent to Mode S with barometric pressure information and satellite-based
 * ADS-B). Only if support for transponders has explicitely declared as
 * unavailable, {@link #hasTransponder()} will return <code>false</code>. Even
 * if 10b information is absent transponder capability will be assumed.
 * </p>
 *
 * <p>
 * <b>DISCLAIMER: THIS CLASS IS INTENDED TO BE USED ONLY IN THE CONTEXT OF
 * FLIGHT SIMULATION! INFORMATION PRESENTED HERE MAY NOT CONFORM TO CURRENT
 * REAL-WORLD STANDARDS OR LACK PRECISION FOR USE IN REAL-WORLD AVIATION.</b>
 * </p>
 */
public class ICAOField10PBNParser {

    private final Set<NavigationApproachCapability> navigationApproachCapabilities = new HashSet<>();
    private final Set<CommunicationCapability> communicationCapabilities = new HashSet<>();
    private final boolean hasTransponder;

    private static final String NO_TRANSPONDER = "N";
    private static final Pattern PATTERN_DESIGNATORS = Pattern.compile("[A-Z][0-9]*");

    /**
     * Parses the given combined field 10 information, also supporting PBN codes
     * to be used in field 10a.
     *
     * Field 10b will not be fully parsed as it makes no sense on VATSIM.
     *
     * @param combinedField10 combined field 10 information; a and b are
     * separated by a slash /
     */
    public ICAOField10PBNParser(String combinedField10) {
        if (combinedField10 == null) {
            hasTransponder = true;
            return;
        }

        String[] field10Parts = combinedField10.split("/");
        String field10a = field10Parts.length > 0 ? field10Parts[0] : "";
        String field10b = field10Parts.length > 1 ? field10Parts[1] : "";

        if (!field10a.isEmpty()) {
            navigationApproachCapabilities.add(null);
            communicationCapabilities.add(null);
        }

        hasTransponder = !NO_TRANSPONDER.equalsIgnoreCase(field10b);

        Matcher matcher = PATTERN_DESIGNATORS.matcher(field10a.toUpperCase());
        //System.out.println(field10a); // DEBUG
        while (matcher.find()) {
            String designator = matcher.group();
            //System.out.print(" => " + designator); // DEBUG

            CommunicationCapability communicationCapability = CommunicationCapability.byDesignator(designator);
            //System.out.print(" " + communicationCapability); // DEBUG

            NavigationApproachCapability navigationApproachCapability = NavigationApproachCapability.byDesignator(designator);
            //System.out.print(" " + navigationApproachCapability); // DEBUG

            //System.out.println(); // DEBUG
        }
    }

    public Set<CommunicationCapability> getCommunicationCapabilities() {
        return communicationCapabilities;
    }

    public Set<NavigationApproachCapability> getNavigationApproachCapabilities() {
        return navigationApproachCapabilities;
    }

    public boolean hasTransponder() {
        return hasTransponder;
    }

}
