package org.vatplanner.dataformats.vatsimpublic.entities.status;

/**
 * Indicates the flight plan type. This has been simplified on VATSIM: Only IFR
 * and VFR are currently supported. Other real-world types would be Y and Z
 * types (mixed IFR and VFR, one changing to the other one enroute). Those have
 * not yet been observed in collected data and are thus omitted. The meaning of
 * other actually observed types (in particular D and S) is unclear given the
 * aircraft types flying under those flight plans.
 */
public enum FlightPlanType {
    IFR('I'),
    VFR('V');

    private final char code;

    private FlightPlanType(char code) {
        this.code = code;
    }

    /**
     * Returns the single-letter code used to identify this type in flight
     * plans.
     *
     * @return single-letter code used in flight plans
     */
    public char getCode() {
        return code;
    }

    // TODO: unit tests
    // TODO: implement resolution byCode
}
