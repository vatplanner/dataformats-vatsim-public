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
 * <li>{@link #filedAlternateAirportCode}</li>
 * <li>{@link #altitudeFeet} (always 0 for ATCs)</li>
 * <li>{@link #filedDepartureAirportCode}</li>
 * <li>{@link #departureTimePlanned}</li>
 * <li>{@link #departureTimeActual}</li>
 * <li>{@link #filedDestinationAirportCode}</li>
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
    private String filedDepartureAirportCode;
    private String rawFiledAltitude; // 30000, FL300, F300, maybe even worse raw user input (don't pilot clients validate this field?!)
    private String filedDestinationAirportCode;
    
    // actual data
    private String serverId;
    private int protocolVersion;
    private ControllerRating controllerRating;
    private int transponderCodeDecimal;
    private int facilityType; // TODO: decode
    private int visualRange; // nm
    
    // filing
    private int fileRevision;
    private String flightPlanType; // I = IFR, V = VFR; unfortunately user-defined, e.g. also seen: S (scheduled)
    private Instant departureTimePlanned; // may be 0; values can omit leading zeros!
    private Instant departureTimeActual; // may be 0, may be equal, may be actual value - who or what sets this? Values can omit leading zeros!
    private Duration filedTimeEnroute; // data: two fields, hours + minutes
    private Duration filedTimeFuel; // data: two fields, hours + minutes
    private String filedAlternateAirportCode;
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

    /**
     * Returns the aircraft type.
     * <p>
     * Pilots provide their aircraft type when prefiling a flight plan and
     * a second time when they connect to the network. ATC may be able to change
     * pilot's aircraft types while logged in as part of revising the flight
     * plan (not sure).
     * </p>
     * <p>
     * While ICAO aircraft type codes are supposed to be used with equipment
     * code as suffix and optional wake category as prefix (separated by forward
     * slashes) the actual format and value of this field is not reliable as it
     * is an informal free-text field and sometimes contains alternate/IATA
     * codes or common mistakes (such as B77W for a Boeing 777 which is neither
     * a valid ICAO nor IATA code).
     * </p>
     * <p>
     * <strong>Example values:</strong> B738/M, H/A332/X, B737
     * </p>
     * <p>
     * While it does not make any sense for ATC to file flight plans, such data
     * can actually be found on the network. If this field is filled for ATC it
     * should be ignored unless an actual use case becomes known.
     * </p>
     * @return aircraft type; may deviate from wake/ICAO-type/equipment syntax and value
     */
    public String getAircraftType() {
        return aircraftType;
    }

    void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    /**
     * Returns the true air speed (TAS, measured in knots) listed on flight plan
     * of this client.
     * <p>
     * If no TAS was provided, 0 will be returned. Only positive values are
     * valid.
     * </p>
     * <p>
     * While it does not make any sense for ATC to file flight plans, such data
     * can actually be found on the network. If this field is filled for ATC it
     * should be ignored unless an actual use case becomes known.
     * </p>
     * @return true air speed (TAS, measured in knots) listed on flight plan; 0 if unavailable
     */
    public int getFiledTrueAirSpeed() {
        return filedTrueAirSpeed;
    }

    void setFiledTrueAirSpeed(int filedTrueAirSpeed) {
        this.filedTrueAirSpeed = filedTrueAirSpeed;
    }

    /**
     * Returns the departure airport code listed on flight plan of this client.
     * <p>
     * While these are usually ICAO codes, they could be anything
     * else as this is a free-text field.
     * </p>
     * @return departure airport code listed on flight plan of this client
     */
    public String getFiledDepartureAirportCode() {
        return filedDepartureAirportCode;
    }

    void setFiledDepartureAirportCode(String filedDepartureAirportCode) {
        this.filedDepartureAirportCode = filedDepartureAirportCode;
    }

    /**
     * Returns the unprocessed planned altitude listed on flight plan of this
     * client.
     * <p>
     * Unfortunately, this field is free-text and not strictly numeric, so
     * pilots are free to file some strings which require further
     * interpretation. No interpretation is attempted by parser, so this is the
     * raw unprocessed value as available from data file.
     * </p>
     * <p>
     * <strong>Example values:</strong> 30000, FL300, F300
     * </p>
     * @return unprocessed planned altitude listed on flight plan
     */
    public String getRawFiledAltitude() {
        return rawFiledAltitude;
    }

    void setRawFiledAltitude(String rawFiledAltitude) {
        this.rawFiledAltitude = rawFiledAltitude;
    }

    /**
     * Returns the destination airport code listed on flight plan of this client.
     * <p>
     * While these are usually ICAO codes, they could be anything
     * else as this is a free-text field.
     * </p>
     * @return destination airport code listed on flight plan of this client
     */
    public String getFiledDestinationAirportCode() {
        return filedDestinationAirportCode;
    }

    void setFiledDestinationAirportCode(String filedDestinationAirportCode) {
        this.filedDestinationAirportCode = filedDestinationAirportCode;
    }

    /**
     * Returns the ID of the server the client is currently connected to.
     * Returns null for clients not being online (prefiled flight plans).
     * @return ID of server currently connected to; null if offline
     */
    public String getServerId() {
        return serverId;
    }

    void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Returns the protocol version the client is using for communication with
     * servers.
     * Negative if not available (prefiled flight plans). All online clients
     * return a positive number.
     * @return protocol version of client; negative if not connected
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Returns the client's effective controller rating for this session.
     * <p>
     * Controller rating is being used as ATC/network permission level, see
     * {@link ControllerRating} for details.
     * </p>
     * <p>
     * This is only meaningful for ATC clients and only for one session.
     * If an ATC-permitted user is connected as a pilot, rating will always be
     * {@link ControllerRating#OBS} for the pilot session regardless of user's
     * actual controller rating. Prefiled flight plans do not indicate any
     * controller rating and thus return null.
     * </p>
     * @return client's controller rating during this session; null for prefiled flight plans
     */
    public ControllerRating getControllerRating() {
        return controllerRating;
    }

    void setControllerRating(ControllerRating controllerRating) {
        this.controllerRating = controllerRating;
    }

    /**
     * Returns the transponder code in numeric decimal representation currently
     * used by client.
     * <p>
     * The actual, spoken transponder code can be resolved from the numeric by
     * left-padding it with 0 to a length of 4 digits.
     * </p>
     * <p>
     * Only in real world digits would be limited to a range of 0..7 as
     * transponder codes are encoded octal. Numeric values returned by this
     * getter however are decimal and pilot clients are
     * actually submitting decimal digits 8 and 9 which would not be possible
     * in real aviation. Such excessive values are to be expected on this
     * representation. However, only 4 digits should ever be returned, meaning
     * a value of 9999 is the highest and 0 is the lowest pilot-settable code.
     * </p>
     * <p>
     * Only if transponder code is not available, a negative value will be
     * returned.
     * </p>
     * <p>
     * Transponder codes are initially selected on pilot's discretion and
     * requested to be changed by ATC upon clearance and sometimes mid-flight.
     * Connected pilots list transponder codes regardless of actual
     * transponder mode selected on aircraft (even if transponder on aircraft is
     * turned off, clients may still list a code). The listed code is always
     * the code set by pilots, not set by ATC. ATC only requests pilots to enter
     * an assigned code, but it cannot be set by ATC. Connected pilots without
     * a transponder code can be encountered.
     * </p>
     * <p>
     * Prefiled flight plans and ATC are never setting transponder codes.
     * </p>
     * @return decimal numeric transponder code, needs left-padding to 4 digits to reconstruct spoken code; negative if unavailable
     */
    public int getTransponderCodeDecimal() {
        return transponderCodeDecimal;
    }

    void setTransponderCodeDecimal(int transponderCodeDecimal) {
        this.transponderCodeDecimal = transponderCodeDecimal;
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

    public String getFiledAlternateAirportCode() {
        return filedAlternateAirportCode;
    }

    void setFiledAlternateAirportCode(String filedAlternateAirportCode) {
        this.filedAlternateAirportCode = filedAlternateAirportCode;
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
