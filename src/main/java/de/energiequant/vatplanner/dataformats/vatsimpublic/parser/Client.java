package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.time.Duration;
import java.time.Instant;

/**
 * Combines information about VATSIM online pilots, prefiled flight plans and
 * online ATC stations, distinguished by {@link #clientType} as read from
 * data.txt status file.
 * <p>Combination into a single class follows the original record format on
 * data.txt status files which is identical for all types although some
 * fields will (and can) never be set:</p>
 * <ul>
 * <li>ATC only:
 * <ul>
 * <li>{@link #facilityType}</li>
 * <li>{@link #servedFrequencyKilohertz}</li>
 * <li>{@link #lastMessageUpdate}</li>
 * <li>{@link #message}</li>
 * <li>{@link #rating}</li>
 * <li>{@link #visualRange}</li>
 * </ul>
 * </li>
 * <li>Pilot and prefiled flight plans only:
 * <ul>
 * <li>{@link #aircraftType}</li>
 * <li>{@link #alternateAirportCode}</li>
 * <li>{@link #altitudeFeet} (always 0 for ATCs)</li>
 * <li>{@link #departureAirportCode}</li>
 * <li>{@link #departureTimePlanned}</li>
 * <li>{@link #departureTimeActual}</li>
 * <li>{@link #destinationAirportCode}</li>
 * <li>{@link #rawFiledAltitude}</li>
 * <li>{@link #filedTimeEnroute}</li>
 * <li>{@link #filedTimeFuel}</li>
 * <li>{@link #filedTrueAirSpeed}</li>
 * <li>{@link #fileRevision}</li>
 * <li>{@link #flightPlanType}</li>
 * <li>{@link #groundSpeed}</li>
 * <li>{@link #heading}</li>
 * <li>{@link #qnhHectopascal}</li>
 * <li>{@link #qnhInchMercury}</li>
 * <li>{@link #remarks}</li>
 * <li>{@link #route}</li>
 * </ul>
 * </li>
 * <li>Not seen being used in the wild but still defined by format:
 * <ul>
 * <li>{@link #departureAirportLatitude}</li>
 * <li>{@link #departureAirportLongitude}</li>
 * <li>{@link #destinationAirportLatitude}</li>
 * <li>{@link #destinationAirportLongitude}</li>
 * </ul>
 * </li>
 * </ul>
 * <p>Some fields allow for further interpretation or have a special format:</p>
 * <ul>
 * <li>{@link #aircraftType}: ICAO aircraft type code. Should include equipment code as suffix, may include wake category as prefix (examples: B738/M, H/A332/X, B737). Not reliable as this is an informal free-text field and sometimes contains alternate/IATA codes or common mistakes (such as B77W for a Boeing 777 which is neither a valid ICAO nor IATA code).</li>
 * <li>{@link #callsign}: Callsigns can be chosen freely by pilots although some codes may be reserved for virtual airlines by convention. Callsigns on VATSIM omit hyphens which would be used in the real-world to separate country prefixes for plane registrations (e.g. all non-airline flights).</li>
 * <li>{@link #fileRevision}: Flights passing through online controlled airspace will usually see many revisions of their original flight plan (mostly {@link #route}) as edited by ATC when the plane changes airspace or an initial clearance is given by departure airport. Flight plan revisions are tracked by this counter.
 * <li>{@link #message}: Multi-line string containing ATIS message for ATIS stations, otherwise general remarks about ATC stations such as contact information, controller's estimated online times or station's spatial coverage. May contain a URL to the voice room on first line if prefixed with "$ ". Update timestamps are provided by {@link #lastMessageUpdate}.</li>
 * <li>{@link #realName}: By convention, pilots should add a 4-letter ICAO code for their "home base". Pilots often choose the closest airport to their actual home.</li>
 * <li>{@link #remarks}: Pilot clients add voice capability flags (T = Text only; R = Receive voice, send text; V = full voice). Other than that, pilots are free to enter any remarks they may find useful. Pilots sometimes attach full ICAO field 18 information which provides highly detailed information generally not needed for simulation (for example PBN/..., DOF/... etc.).</li>
 * </ul>
 */
public class Client {
    private String callsign; // also on prefiling
    private int vatsimID; // also on prefiling
    private String realName; // may include home base for pilots; also on prefiling
    private ClientType clientType;
    private int servedFrequencyKilohertz; // ATC only
    private double latitude;
    private double longitude;
    private int altitudeFeet;
    private int groundSpeed;
    
    // filing
    private String aircraftType; // B738/M, H/A332/X, B737
    private int filedTrueAirSpeed;
    private String departureAirportCode;
    private String rawFiledAltitude; // 30000, FL300, F300, maybe even worse raw user input (don't pilot clients validate this field?!)
    private String destinationAirportCode;
    
    // actual data
    private String serverId;
    private int protocolVersion;
    private ControllerRating controllerRating;
    private int transponderCode;
    private int facilityType; // TODO: decode
    private int visualRange; // nm
    
    // filing
    private int fileRevision;
    private String flightPlanType; // I = IFR, V = VFR; unfortunately user-defined, e.g. also seen: S (scheduled)
    private Instant departureTimePlanned; // may be 0; values can omit leading zeros!
    private Instant departureTimeActual; // may be 0, may be equal, may be actual value - who or what sets this? Values can omit leading zeros!
    private Duration filedTimeEnroute; // data: two fields, hours + minutes
    private Duration filedTimeFuel; // data: two fields, hours + minutes
    private String alternateAirportCode;
    private String remarks;
    private String route;
    private double departureAirportLatitude; // seems unused, always 0
    private double departureAirportLongitude; // seems unused, always 0
    private double destinationAirportLatitude; // seems unused, always 0
    private double destinationAirportLongitude; // seems unused, always 0
    
    // ATC only
    private String message; // decode "atis_message": first line prefixed "$ " => voice URL; multi-line formatting with "^" and special character as CR LF?
    private Instant lastMessageUpdate; // time_last_atis_received
    
    // all connected
    private Instant logonTime;
    
    // Pilots only
    private int heading;
    private double qnhInchMercury;
    private double qnhHectopascal;

    /**
     * Returns the call sign the client is being identified by.
     * The call sign is supposed to be unique for logged in users (may not
     * apply to prefiled flight plans) on the network as of time of data
     * collection.
     * @return call sign the client is being identified by
     */
    public String getCallsign() {
        return callsign;
    }

    void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    /**
     * Returns the VATSIM user ID the client belongs to.
     * <p>
     * Although policy generally forbids regular users to connect multiple
     * sessions using the same user ID this is not enforced server-side (at
     * least not for ATC).
     * </p>
     * <p>
     * One permitted use of multiple sessions is for ATC to provide
     * ATIS service additional to a controlling session (requires two logins
     * at once for technical reasons).
     * </p>
     * <p>
     * Also, some users such as supervisors, administrators or "promotional accounts" may
     * in fact have permission to connect multiple sessions at the same time, so
     * this ID alone should not be expected to uniquely identify clients on a
     * data file.
     * </p>
     * <p>
     * In some cases, there may not be a VATSIM ID listed on data file. If that
     * happens, return value will be negative. See parser log messages for
     * details.
     * </p>
     * @return VATSIM user ID associated with client; negative if missing
     */
    public int getVatsimID() {
        return vatsimID;
    }

    void setVatsimID(int vatsimID) {
        this.vatsimID = vatsimID;
    }

    /**
     * Returns the user's real name and possibly "home base".
     * Guides and prefiling forms ask pilots to add a "home base" as ICAO code
     * which is supposed to resemble an aircraft home base but usually gets set
     * to the airport code closest to their actual home. As this is "a pilot
     * thing", adding "home bases" is uncommon for ATC.
     * <p>
     * Note: Providing a real name does not seem to be enforced server-side,
     * some clients are able to log in without providing any name at all (or at
     * least it is not visible on data files). Returned String will be empty in
     * those cases.
     * </p>
     * @return user's real name and possibly "home base"; may be empty
     */
    public String getRealName() {
        return realName;
    }

    void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * Returns the {@link ClientType}.
     * Unfortunately, there are situations which require guessing the correct
     * type as it is not in all cases provided by the data file (although it
     * looks like it should). Expect null if guessing fails (check parser log
     * messages for any details).
     * @return type of client
     */
    public ClientType getClientType() {
        return clientType;
    }

    void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    /**
     * Returns the frequency this client provides ATC service on.
     * Pilots are not able to provide services so they can never indicate a
     * served frequency.
     * ATC clients can connect without serving a frequency, for example when
     * connecting as an observer.
     * If no frequency is being served by this client, a negative number will be
     * returned.
     * @return frequency this client provides ATC service on; negative if not serving
     */
    public int getServedFrequencyKilohertz() {
        return servedFrequencyKilohertz;
    }

    void setServedFrequencyKilohertz(int servedFrequencyKilohertz) {
        this.servedFrequencyKilohertz = servedFrequencyKilohertz;
    }

    /**
     * Returns the latitude (north/south coordinate) the client is currently
     * located at.
     * <p>
     * Online clients are actually not required to provide a location
     * (maybe while connected in observer mode?). Prefiled flight plans
     * are not permitted to include a location.
     * </p>
     * <p>
     * If no latitude is available, {@link Double#NaN} will be returned.
     * </p>
     * <p>
     * Positive latitude is north of equator, negative latitude is south.
     * While only a value range of -90..90&deg; makes sense (spherical
     * coordinate system), returned values should still be assumed to exceed
     * that range because parser only checks for syntax and does not
     * unify to a strict coordinate system.
     * </p>
     * @return latitude the client is currently located at; {@link Double#NaN} if unavailable
     */
    public double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    /**
     * Returns the longitude (east/west coordinate) the client is currently
     * located at.
     * <p>
     * Online clients are actually not required to provide a location
     * (maybe while connected in observer mode?). Prefiled flight plans
     * are not permitted to include a location.
     * </p>
     * <p>
     * If no longitude is available, {@link Double#NaN} will be returned.
     * </p>
     * <p>
     * Positive longitude is east of Prime Meridian, negative longitude is west.
     * While only a value range of -180..180&deg; makes sense (spherical
     * coordinate system), returned values should still be assumed to exceed
     * that range because parser only checks for syntax and does not
     * unify to a strict coordinate system.
     * </p>
     * @return longitude the client is currently located at; {@link Double#NaN} if unavailable
     */
    public double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the altitude (measured in feet) the client is currently located
     * at.
     * <p>
     * Online clients are actually not required to provide a location
     * (maybe while connected in observer mode?). Prefiled flight plans
     * are not permitted to include a location.
     * </p>
     * <p>
     * If no altitude is available, 0 will be assumed.
     * </p>
     * @return longitude the client is currently located at; 0 if unavailable
     */
    public int getAltitudeFeet() {
        return altitudeFeet;
    }

    void setAltitudeFeet(int altitudeFeet) {
        this.altitudeFeet = altitudeFeet;
    }

    /**
     * Returns the ground speed (measured in knots) the client is currently
     * moving at.
     * <p>
     * Ground speed is supposed to always be a positive value. Negative values
     * are used to indicate that no ground speed was available.
     * </p>
     * <p>
     * Prefiled flight plans and ATC are stationary and thus do not move by
     * principle. They will always return a negative value.
     * </p>
     * @return ground speed in knots (valid: >= 0); negative value if unavailable
     */
    public int getGroundSpeed() {
        return groundSpeed;
    }

    void setGroundSpeed(int groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public int getFiledTrueAirSpeed() {
        return filedTrueAirSpeed;
    }

    void setFiledTrueAirSpeed(int filedTrueAirSpeed) {
        this.filedTrueAirSpeed = filedTrueAirSpeed;
    }

    public String getDepartureAirportCode() {
        return departureAirportCode;
    }

    void setDepartureAirportCode(String departureAirportCode) {
        this.departureAirportCode = departureAirportCode;
    }

    public String getRawFiledAltitude() {
        return rawFiledAltitude;
    }

    void setRawFiledAltitude(String rawFiledAltitude) {
        this.rawFiledAltitude = rawFiledAltitude;
    }

    public String getDestinationAirportCode() {
        return destinationAirportCode;
    }

    void setDestinationAirportCode(String destinationAirportCode) {
        this.destinationAirportCode = destinationAirportCode;
    }

    public String getServerId() {
        return serverId;
    }

    void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public ControllerRating getControllerRating() {
        return controllerRating;
    }

    void setControllerRating(ControllerRating controllerRating) {
        this.controllerRating = controllerRating;
    }

    public int getTransponderCode() {
        return transponderCode;
    }

    void setTransponderCode(int transponderCode) {
        this.transponderCode = transponderCode;
    }

    public int getFacilityType() {
        // FIXME: should be an enum
        return facilityType;
    }

    void setFacilityType(int facilityType) {
        this.facilityType = facilityType;
    }

    public int getVisualRange() {
        return visualRange;
    }

    void setVisualRange(int visualRange) {
        this.visualRange = visualRange;
    }

    public int getFileRevision() {
        return fileRevision;
    }

    void setFileRevision(int fileRevision) {
        this.fileRevision = fileRevision;
    }

    public String getFlightPlanType() {
        return flightPlanType;
    }

    void setFlightPlanType(String flightPlanType) {
        this.flightPlanType = flightPlanType;
    }

    public Instant getDepartureTimePlanned() {
        return departureTimePlanned;
    }

    void setDepartureTimePlanned(Instant departureTimePlanned) {
        this.departureTimePlanned = departureTimePlanned;
    }

    public Instant getDepartureTimeActual() {
        return departureTimeActual;
    }

    void setDepartureTimeActual(Instant departureTimeActual) {
        this.departureTimeActual = departureTimeActual;
    }

    public Duration getFiledTimeEnroute() {
        return filedTimeEnroute;
    }

    void setFiledTimeEnroute(Duration filedTimeEnroute) {
        this.filedTimeEnroute = filedTimeEnroute;
    }

    public Duration getFiledTimeFuel() {
        return filedTimeFuel;
    }

    void setFiledTimeFuel(Duration filedTimeFuel) {
        this.filedTimeFuel = filedTimeFuel;
    }

    public String getAlternateAirportCode() {
        return alternateAirportCode;
    }

    void setAlternateAirportCode(String alternateAirportCode) {
        this.alternateAirportCode = alternateAirportCode;
    }

    public String getRemarks() {
        return remarks;
    }

    void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRoute() {
        return route;
    }

    void setRoute(String route) {
        this.route = route;
    }

    public double getDepartureAirportLatitude() {
        return departureAirportLatitude;
    }

    void setDepartureAirportLatitude(double departureAirportLatitude) {
        this.departureAirportLatitude = departureAirportLatitude;
    }

    public double getDepartureAirportLongitude() {
        return departureAirportLongitude;
    }

    void setDepartureAirportLongitude(double departureAirportLongitude) {
        this.departureAirportLongitude = departureAirportLongitude;
    }

    public double getDestinationAirportLatitude() {
        return destinationAirportLatitude;
    }

    void setDestinationAirportLatitude(double destinationAirportLatitude) {
        this.destinationAirportLatitude = destinationAirportLatitude;
    }

    public double getDestinationAirportLongitude() {
        return destinationAirportLongitude;
    }

    void setDestinationAirportLongitude(double destinationAirportLongitude) {
        this.destinationAirportLongitude = destinationAirportLongitude;
    }

    public String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

    public Instant getLastMessageUpdate() {
        return lastMessageUpdate;
    }

    void setLastMessageUpdate(Instant lastMessageUpdate) {
        this.lastMessageUpdate = lastMessageUpdate;
    }

    public Instant getLogonTime() {
        return logonTime;
    }

    void setLogonTime(Instant logonTime) {
        this.logonTime = logonTime;
    }

    public int getHeading() {
        return heading;
    }

    void setHeading(int heading) {
        this.heading = heading;
    }

    public double getQnhInchMercury() {
        return qnhInchMercury;
    }

    void setQnhInchMercury(double qnhInchMercury) {
        this.qnhInchMercury = qnhInchMercury;
    }

    public double getQnhHectopascal() {
        return qnhHectopascal;
    }

    void setQnhHectopascal(double qnhHectopascal) {
        this.qnhHectopascal = qnhHectopascal;
    }
    
}
