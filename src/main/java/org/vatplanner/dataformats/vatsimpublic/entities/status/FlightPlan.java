package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.time.Duration;
import java.time.Instant;

/**
 * Holds all data of a flight plan filed on VATSIM. Flight plans go through
 * multiple revisions. The revision index is incremented by VATSIM each time the
 * flight plan is changed.
 */
public class FlightPlan {

    private final Flight flight;
    private final int revision;

    private Report reportFirstSeen;

    private FlightPlanType flightPlanType;
    private Instant departureTimePlanned;
    private Instant departureTimeActual;

    private String route;
    private String remarks;
    private CommunicationMode communicationMode;

    private WakeTurbulenceCategory wakeTurbulenceCategory;
    private String aircraftType;
    private SimpleEquipmentSpecification simpleEquipmentSpecification;
    private int trueAirSpeed;
    private int altitudeFeet;
    private Duration estimatedTimeEnroute;
    private Duration estimatedTimeFuel;

    private String departureAirportCode;
    private String destinationAirportCode;
    private String alternateAirportCode;

    /**
     * Creates a new flight plan. Flight plans always require a revision number for
     * indexing and a flight as reference.
     *
     * @param flight   the flight described by this flight plan
     * @param revision revision number to index flight plans by
     */
    public FlightPlan(Flight flight, int revision) {
        // TODO: add flightplan to flight; must not loop back; document
        this.flight = flight;
        this.revision = revision;
    }

    /**
     * Returns the flight this flight plan belongs to.
     *
     * @return flight belonging to
     */
    public Flight getFlight() {
        return flight;
    }

    /**
     * Returns the first processed {@link Report} this flight plan appears in.
     *
     * @return first processed report this flight plan appears in
     */
    public Report getReportFirstSeen() {
        return reportFirstSeen;
    }

    /**
     * Updates first report if the given report is older than currently known.
     *
     * @param report report to process update for
     * @return this instance for method-chaining
     */
    public FlightPlan seenInReport(Report report) {
        if ((reportFirstSeen == null) || report.getRecordTime().isBefore(reportFirstSeen.getRecordTime())) {
            reportFirstSeen = report;
        }

        return this;
    }

    /**
     * Returns the revision number of this flight plan. The revision index is
     * incremented by VATSIM each time the flight plan is changed.
     *
     * @return revision number of this flight plan
     */
    public int getRevision() {
        return revision;
    }

    /**
     * Returns the type of this flight plan.
     *
     * @return type of this flight plan
     */
    public FlightPlanType getFlightPlanType() {
        return flightPlanType;
    }

    public FlightPlan setFlightPlanType(FlightPlanType flightPlanType) {
        this.flightPlanType = flightPlanType;
        return this;
    }

    /**
     * Returns the planned departure time.
     *
     * @return planned departure time
     */
    public Instant getDepartureTimePlanned() {
        return departureTimePlanned;
    }

    public FlightPlan setDepartureTimePlanned(Instant departureTimePlanned) {
        this.departureTimePlanned = departureTimePlanned;
        return this;
    }

    /**
     * Returns the "actual" departure time as set manually by ATC. This time can be
     * set by controllers but often remains unchanged. There is no guarantee if this
     * will really reflect the time of actual departure. Analyze the recorded track
     * available from {@link Flight#getTrack()} instead, if actual times are needed.
     *
     * @return "actual" departure time (optional, manual entry to flight plan)
     */
    public Instant getDepartureTimeActual() {
        return departureTimeActual;
    }

    public FlightPlan setDepartureTimeActual(Instant departureTimeActual) {
        this.departureTimeActual = departureTimeActual;
        return this;
    }

    /**
     * Returns the route as entered into flight plan. Note that this is a free-text
     * field. IFR flights usually hold parseable flight plans but VFR flight plans
     * may actually hold non-standard verbose comments.
     *
     * @return route as entered into flight plan
     */
    public String getRoute() {
        return route;
    }

    public FlightPlan setRoute(String route) {
        this.route = route;
        return this;
    }

    /**
     * Returns the flight plan remarks. Remarks are free text and can hold any kind
     * of information. This includes parseable tokens such as communication mode,
     * flight planning/dispatch system and operational flight details (ICAO field 18
     * format) but also URLs of social video streams, virtual airlines or personal
     * information about the pilot's system configuration or even personal
     * (dis)abilities and handicaps.
     *
     * @return flight plan remarks (free text)
     */
    public String getRemarks() {
        return remarks;
    }

    public FlightPlan setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    /**
     * Returns the pilot's ability of communication.
     *
     * @return pilot's ability of communication
     */
    public CommunicationMode getCommunicationMode() {
        return communicationMode;
    }

    public FlightPlan setCommunicationMode(CommunicationMode communicationMode) {
        this.communicationMode = communicationMode;
        return this;
    }

    /**
     * Returns the aircraft's wake turbulence category as entered to the flight
     * plan. If read from the original user-supplied flight plan, this information
     * is potentially unreliable.
     *
     * @return wake turbulence category of the filed aircraft (information may be
     *     unreliable)
     */
    public WakeTurbulenceCategory getWakeTurbulenceCategory() {
        return wakeTurbulenceCategory;
    }

    public FlightPlan setWakeTurbulenceCategory(WakeTurbulenceCategory wakeTurbulenceCategory) {
        this.wakeTurbulenceCategory = wakeTurbulenceCategory;
        return this;
    }

    /**
     * Returns the aircraft type without wake category and equipment code.
     * Generally, this should be an ICAO code but users sometimes enter wrong or
     * free text information.
     *
     * @return aircraft type (should be ICAO code; may hold invalid information)
     */
    public String getAircraftType() {
        return aircraftType;
    }

    public FlightPlan setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
        return this;
    }

    /**
     * Returns the simple equipment specification, specifying navigational
     * capability of the aircraft. This information is only provided if pilots file
     * flight plans in the old FAA-based format. If used, pilots encode the
     * information with a single letter (thus "simple"). Unfortunately, this may
     * sometimes not have been reliably determined or be misinterpreted by a
     * mismatch between real world and VATSIM codes. See
     * {@link SimpleEquipmentSpecification} for details.
     *
     * @return equipment specification as determined by single-letter code, null if
     *     unavailable or invalid
     * @see SimpleEquipmentSpecification
     */
    public SimpleEquipmentSpecification getSimpleEquipmentSpecification() {
        return simpleEquipmentSpecification;
    }

    public FlightPlan setSimpleEquipmentSpecification(SimpleEquipmentSpecification simpleEquipmentSpecification) {
        this.simpleEquipmentSpecification = simpleEquipmentSpecification;
        return this;
    }

    /**
     * Returns the estimated true air speed (TAS) in knots.
     *
     * @return estimated true air speed (TAS) in knots
     */
    public int getTrueAirSpeed() {
        return trueAirSpeed;
    }

    public FlightPlan setTrueAirSpeed(int trueAirSpeed) {
        this.trueAirSpeed = trueAirSpeed;
        return this;
    }

    /**
     * Returns the initially (before performing any step climbs/descends) requested
     * cruise-level altitude in feet.
     *
     * @return initially requested cruise-level altitude in feet
     */
    public int getAltitudeFeet() {
        return altitudeFeet;
    }

    public FlightPlan setAltitudeFeet(int altitudeFeet) {
        this.altitudeFeet = altitudeFeet;
        return this;
    }

    /**
     * Returns the estimated time spent enroute (from take-off to landing).
     *
     * @return estimated time spent enroute
     */
    public Duration getEstimatedTimeEnroute() {
        return estimatedTimeEnroute;
    }

    public FlightPlan setEstimatedTimeEnroute(Duration estimatedTimeEnroute) {
        this.estimatedTimeEnroute = estimatedTimeEnroute;
        return this;
    }

    /**
     * Returns the estimated maximum time of fuel availability (from planned
     * take-off time).
     *
     * @return estimated maximum time of fuel
     */
    public Duration getEstimatedTimeFuel() {
        return estimatedTimeFuel;
    }

    public FlightPlan setEstimatedTimeFuel(Duration estimatedTimeFuel) {
        this.estimatedTimeFuel = estimatedTimeFuel;
        return this;
    }

    /**
     * Returns the departure airport code. This should usually be an ICAO code
     * (generally 4 letters), not IATA (3 letter).
     *
     * @return departure airport code (usually ICAO), normalized (trimmed
     *     upper-case)
     */
    public String getDepartureAirportCode() {
        return departureAirportCode;
    }

    public FlightPlan setDepartureAirportCode(String departureAirportCode) {
        this.departureAirportCode = normalizeAirportCode(departureAirportCode);
        return this;
    }

    /**
     * Returns the destination airport code. This should usually be an ICAO code
     * (generally 4 letters), not IATA (3 letter).
     *
     * @return destination airport code (usually ICAO), normalized (trimmed
     *     upper-case)
     */
    public String getDestinationAirportCode() {
        return destinationAirportCode;
    }

    public FlightPlan setDestinationAirportCode(String destinationAirportCode) {
        this.destinationAirportCode = normalizeAirportCode(destinationAirportCode);
        return this;
    }

    /**
     * Returns the alternate (primary choice for diversions) airport code. This
     * should usually be an ICAO code (generally 4 letters), not IATA (3 letter).
     *
     * @return alternate airport code (usually ICAO), normalized (trimmed
     *     upper-case)
     */
    public String getAlternateAirportCode() {
        return alternateAirportCode;
    }

    public FlightPlan setAlternateAirportCode(String alternateAirportCode) {
        this.alternateAirportCode = normalizeAirportCode(alternateAirportCode);
        return this;
    }

    /**
     * Checks if the given airport code matches this flight plan's departure
     * airport. Input is normalized before check as airport codes are always stored
     * in normalized form. Null values are evaluated as true only if both input and
     * flight plan airport code are null.
     *
     * @param departureAirportCode code of departure airport, will be normalized
     * @return true if this flight plan matches the given departure airport, false
     *     if not; true if input and flight plan airport code are null
     */
    public boolean equalsDepartureAirportCode(String departureAirportCode) {
        if (this.departureAirportCode == null) {
            return (departureAirportCode == null);
        }

        return this.departureAirportCode.equals(normalizeAirportCode(departureAirportCode));
    }

    /**
     * Checks if the given airport code matches this flight plan's destination
     * airport. Input is normalized before check as airport codes are always stored
     * in normalized form. Null values are evaluated as true only if both input and
     * flight plan airport code are null.
     *
     * @param destinationAirportCode code of destination airport, will be normalized
     * @return true if this flight plan matches the given destination airport, false
     *     if not; true if input and flight plan airport code are null
     */
    public boolean equalsDestinationAirportCode(String destinationAirportCode) {
        if (this.destinationAirportCode == null) {
            return (destinationAirportCode == null);
        }

        return this.destinationAirportCode.equals(normalizeAirportCode(destinationAirportCode));
    }

    /**
     * Normalizes an airport code. By default, normalization is performed by
     * trimming the input and converting it to upper case but exact behavior can be
     * overridden by inheriting class. Null input will be preserved.
     *
     * @param airportCode code to normalize
     * @return normalized airport code
     */
    public String normalizeAirportCode(String airportCode) {
        if (airportCode == null) {
            return null;
        }

        return airportCode.trim().toUpperCase();
    }

    // TODO: could also have real name and home base (no connection if only prefiled)
    // TODO: unit tests
}
