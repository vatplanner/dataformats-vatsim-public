package org.vatplanner.dataformats.vatsimpublic.entities.status;

/**
 * Specifies aircraft equipment capability with low details. "Simple"
 * specification only takes one letter in flight plan (ICAO field 10) to be
 * described as compared to more detailed "PBN/" specifications in flight plan
 * remarks (ICAO field 18).
 *
 * <p>
 * Although VATSIM bases this information on ICAO field 10, it does not match
 * current real-world field 10 codes. This may lead to situations where flight
 * planning tools may file wrong equipment codes on VATSIM flight plans. If this
 * information is needed and "PBN/" is available from ICAO field 18, it should
 * be preferred over this simple specification.
 * </p>
 *
 * <p>
 * See
 * https://www.vatsim.net/pilot-resource-centre/general-lessons/choosing-equipment-code
 * for equipment codes officially supported on VATSIM.
 * </p>
 *
 * <p>
 * Note that these codes and their interpretations do not conform to real-world
 * standards and must only be used in context of flight simulation.
 * </p>
 */
public enum SimpleEquipmentSpecification {
    // "new" VATSIM codes
    H(true, Navigation.UNSPECIFIC, Transponder.FAILED),
    W(true, Navigation.UNSPECIFIC_NO_RNAV, Transponder.MODE_C),
    Z(true, Navigation.RNAV_WITHOUT_GNSS, Transponder.MODE_C),
    L(true, Navigation.GNSS, Transponder.MODE_C),
    X(false, Navigation.NO_DME, Transponder.NONE),
    T(false, Navigation.NO_DME, Transponder.NO_MODE_C),
    U(false, Navigation.NO_DME, Transponder.MODE_C),
    D(false, Navigation.DME, Transponder.NONE),
    B(false, Navigation.DME, Transponder.NO_MODE_C), // does not match current ICAO field 10 specification
    A(false, Navigation.DME, Transponder.MODE_C), // does not match current ICAO field 10 specification
    M(false, Navigation.TACAN, Transponder.NONE),
    N(false, Navigation.TACAN, Transponder.NO_MODE_C),
    P(false, Navigation.TACAN, Transponder.MODE_C),
    Y(false, Navigation.RNAV_WITHOUT_GNSS, Transponder.NONE),
    C(false, Navigation.RNAV_WITHOUT_GNSS, Transponder.NO_MODE_C), // does not match current ICAO field 10 specification
    I(false, Navigation.RNAV_WITHOUT_GNSS, Transponder.MODE_C),
    V(false, Navigation.GNSS, Transponder.NONE),
    S(false, Navigation.GNSS, Transponder.NO_MODE_C),
    G(false, Navigation.GNSS, Transponder.MODE_C), //
    //
    // additional "legacy" VATSIM codes
    E(false, Navigation.RNAV_FMS_DME_DME, Transponder.MODE_C),
    F(false, Navigation.RNAV_FMS_DME_DME_IRU, Transponder.MODE_C),
    R(false, Navigation.GNSS, Transponder.MODE_C), // alias to G, only difference: confirming ANP meets RNP; ICAO field 10 specification: field 18 (remarks) must contain PBN/ (not mandatory on VATSIM)
    J(true, Navigation.RNAV_FMS_DME_DME, Transponder.MODE_C),
    K(true, Navigation.RNAV_FMS_DME_DME_IRU, Transponder.MODE_C),
    Q(true, Navigation.GNSS, Transponder.MODE_C); // alias to L, only difference: confirming ANP meets RNP

    /**
     * Constants used to indicate level of navigation capability. Higher level
     * values extend capabilities compared to lower levels: The higher the
     * level, the more capable/accurate an aircraft can navigate.
     */
    private static class Navigation {

        private static final byte UNSPECIFIC = -1;
        private static final byte NO_DME = 0;
        private static final byte DME = 1;
        private static final byte TACAN = 2;
        private static final byte UNSPECIFIC_NO_RNAV = 3;
        private static final byte RNAV_FMS_DME_DME = 4;
        private static final byte RNAV_WITHOUT_GNSS = 5;
        private static final byte RNAV_FMS_DME_DME_IRU = 6; // might be equal to RNAV_WITHOUT_GNSS
        private static final byte GNSS = 7;

        private static final byte MINIMUM_LEVEL_BRNAV = RNAV_FMS_DME_DME;
        private static final byte MINIMUM_LEVEL_PRNAV = GNSS;
    }

    /**
     * Constants used to indicate level of transponder capability. Higher levels
     * extend features indicated by lower levels.
     */
    private static class Transponder {

        private static final byte FAILED = -1;
        private static final byte NONE = 0;
        private static final byte NO_MODE_C = 1;
        private static final byte MODE_C = 2;

        private static final byte MINIMUM_LEVEL_MODE_C = MODE_C;
    }

    private final boolean isRvsmCapable;
    private final byte navigationCapability;
    private final byte transponderCapability;

    private SimpleEquipmentSpecification(boolean isRvsmCapable, byte navigationCapability, byte transponderCapability) {
        this.isRvsmCapable = isRvsmCapable;
        this.navigationCapability = navigationCapability;
        this.transponderCapability = transponderCapability;
    }

    /**
     * Checks if equipment is capable for Basic Area Navigation (B-RNAV). In the
     * real-world, B-RNAV is defined by Required Navigation Performance (RNP) of
     * 5 nautical miles for at least 95% of time. This may not be guaranteed in
     * context of flight simulation.
     * <p>
     * For simple evaluation in context of flight simulation, a flight
     * management system without INS is assumed sufficient. However, this
     * assumption will generally be wrong over vast open spaces without any
     * DME/VOR coverage (e.g. oceans).
     * </p>
     * <p>
     * In flight simulation, B-RNAV capable aircraft are usually "classic
     * airliners" such as (in worst case) a Boeing 727 with CIVA which may
     * actually provide even less accuracy/features than originally required but
     * will still do its job in most cases. It may also be understood by virtual
     * pilots as "only being able to fly from VOR to VOR" which would most
     * likely not qualify as B-RNAV in real-world aviation and thus be used
     * misleadingly. As a result, pilots on VATSIM indicating only B-RNAV
     * equipment usually cannot be expected to be able to follow instructions to
     * procedures not already on their flight plan or directs to fixes not
     * derivable from radio navigation.
     * </p>
     *
     * @return true if equipment indicates Basic Area Navigation (B-RNAV)
     * capability, false if not
     */
    public boolean hasBasicAreaNavigation() {
        return (navigationCapability >= Navigation.MINIMUM_LEVEL_BRNAV);
    }

    /**
     * Checks if equipment is capable for Precision Area Navigation (P-RNAV). In
     * the real-world, P-RNAV is defined by Required Navigation Performance
     * (RNP) of 1 nautical miles for at least 95% of time.
     * <p>
     * For simple evaluation in context of flight simulation, a flight
     * management system with GNSS is assumed sufficient.
     * </p>
     * <p>
     * In flight simulation, all "modern airliners" or modernized "classic
     * airliners" can be considered P-RNAV capable. On VATSIM, P-RNAV capability
     * usually indicates that the pilot is able to follow random procedures and
     * directs to fixes not derivable from radio navigation.
     * </p>
     *
     * @return true if equipment indicates Precision Area Navigation (P-RNAV)
     * capability, false if not
     */
    public boolean hasPrecisionAreaNavigation() {
        return (navigationCapability >= Navigation.MINIMUM_LEVEL_PRNAV);
    }

    /**
     * Checks if the equipment is capable for Reduced Vertical Separation
     * Minimum (RVSM). RVSM means that altimeter readings of an aircraft are
     * accurate enough to reduce vertical separation without compromising
     * safety.
     *
     * @return true if equipment indicates Reduced Vertical Separation Minimum
     * (RVSM) capability, false if not
     */
    public boolean hasReducedVerticalSeparationMinimum() {
        return isRvsmCapable;
    }

    /**
     * Checks if the transponder is capable of mode C. In the real-world, mode C
     * means that altitude information is transmitted additional to an
     * identification code.
     * <p>
     * On VATSIM, mode C capability currently does not have any relevance as
     * pilot clients always transmit altitude independent of a transponder and
     * indicate mode C for all aircraft while the transponder is set active.
     * </p>
     *
     * @return true if equipment is capable of transponder mode C, false if not
     */
    public boolean hasModeCTransponder() {
        return (transponderCapability >= Transponder.MINIMUM_LEVEL_MODE_C);
    }

    // TODO: add unit tests
}
