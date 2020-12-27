package org.vatplanner.dataformats.vatsimpublic.parser;

import java.time.Duration;
import java.time.Instant;

import org.vatplanner.dataformats.vatsimpublic.entities.status.BarometricPressure;
import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType;

/**
 * Combines information about VATSIM online pilots, prefiled flight plans and
 * online ATC stations, distinguished by {@link #rawClientType} as read from
 * data.txt status file.
 * <p>
 * Combination into a single class follows the original record format on
 * data.txt status files which is identical for all types although some fields
 * will (and can) never be set:
 * </p>
 * <ul>
 * <li>ATC only:
 * <ul>
 * <li>{@link #facilityType}</li>
 * <li>{@link #servedFrequencyKilohertz}</li>
 * <li>{@link #controllerMessageLastUpdated}</li>
 * <li>{@link #controllerMessage}</li>
 * <li>{@link #controllerRating}</li>
 * <li>{@link #visualRange}</li>
 * </ul>
 * </li>
 * <li>Pilot and prefiled flight plans only:
 * <ul>
 * <li>{@link #aircraftType}</li>
 * <li>{@link #filedAlternateAirportCode}</li>
 * <li>{@link #altitudeFeet} (always 0 for ATCs)</li>
 * <li>{@link #filedDepartureAirportCode}</li>
 * <li>{@link #rawDepartureTimePlanned}</li>
 * <li>{@link #rawDepartureTimeActual}</li>
 * <li>{@link #filedDestinationAirportCode}</li>
 * <li>{@link #rawFiledAltitude}</li>
 * <li>{@link #filedTimeEnroute}</li>
 * <li>{@link #filedTimeFuel}</li>
 * <li>{@link #filedTrueAirSpeed}</li>
 * <li>{@link #flightPlanRevision}</li>
 * <li>{@link #rawFlightPlanType}</li>
 * <li>{@link #groundSpeed}</li>
 * <li>{@link #heading}</li>
 * <li>{@link #qnhHectopascal}</li>
 * <li>{@link #qnhInchMercury}</li>
 * <li>{@link #flightPlanRemarks}</li>
 * <li>{@link #filedRoute}</li>
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
 * <p>
 * Some fields allow for further interpretation or have a special format:
 * </p>
 * <ul>
 * <li>{@link #effectiveClientType}: While {@link #rawClientType} is the
 * original {@link ClientType} as defined in original file, it may not match the
 * role of a {@link Client} according to actual data. The perceived role is thus
 * specified as {@link #effectiveClientType}. For more information read the
 * getter JavaDoc below.</li>
 * <li>{@link #aircraftType}: ICAO aircraft type code. Should include equipment
 * code as suffix, may include wake category as prefix (examples: B738/M,
 * H/A332/X, B737). Not reliable as this is an informal free-text field and
 * sometimes contains alternate/IATA codes or common mistakes (such as B77W for
 * a Boeing 777 which is neither a valid ICAO nor IATA code).</li>
 * <li>{@link #callsign}: Callsigns can be chosen freely by pilots although some
 * codes may be reserved for virtual airlines by convention. Callsigns on VATSIM
 * omit hyphens which would be used in the real-world to separate country
 * prefixes for plane registrations (e.g. all non-airline flights).</li>
 * <li>{@link #flightPlanRevision}: Flights passing through online controlled
 * airspace will usually see many revisions of their original flight plan
 * (mostly {@link #filedRoute}) as edited by ATC when the plane changes airspace
 * or an initial clearance is given by departure airport. Flight plan revisions
 * are tracked by this counter.
 * <li>{@link #controllerMessage}: Multi-line string containing ATIS
 * controllerMessage for ATIS stations, otherwise general flightPlanRemarks
 * about ATC stations such as contact information, controller's estimated online
 * times or station's spatial coverage. May contain a URL to the voice room on
 * first line if prefixed with "$ ". Voice rooms are superseded by "Audio for
 * VATSIM" starting 14 October 2019. Update timestamps are provided by
 * {@link #controllerMessageLastUpdated}.</li>
 * <li>{@link #realName}: By convention, pilots should add a 4-letter ICAO code
 * for their "home base". Pilots often choose the closest airport to their
 * actual home.</li>
 * <li>{@link #flightPlanRemarks}: Pilot clients add voice capability flags (T =
 * Text only; R = Receive voice, send text; V = full voice). Other than that,
 * pilots are free to enter any flightPlanRemarks they may find useful. Pilots
 * sometimes attach full ICAO field 18 information which provides highly
 * detailed information generally not needed for simulation (for example
 * PBN/..., DOF/... etc.).</li>
 * </ul>
 */
public class Client {

    /**
     * If this minimum frequency (in kilohertz) is used by an ATC client the ATC
     * client is believed not to be providing any service. Such placeholder
     * frequencies can usually be found if a controller mentors a trainee or a
     * supervisor is flying as a pilot.
     */
    public static final int FREQUENCY_KILOHERTZ_PLACEHOLDER_MINIMUM = 199000;

    private String callsign; // also on prefiling
    private int vatsimID; // also on prefiling
    private String realName; // may include home base for pilots; also on prefiling
    private ClientType rawClientType;
    private ClientType effectiveClientType;
    private int servedFrequencyKilohertz; // ATC only
    private double latitude;
    private double longitude;
    private int altitudeFeet;
    private int groundSpeed;

    // filing
    private String aircraftType; // B738/M, H/A332/X, B737
    private int filedTrueAirSpeed;
    private String filedDepartureAirportCode;
    private String rawFiledAltitude; // 30000, FL300, F300, maybe even worse raw user input (don't pilot clients
                                     // validate this field?!)
    private String filedDestinationAirportCode;

    // actual data
    private String serverId;
    private int protocolVersion;
    private ControllerRating controllerRating;
    private int transponderCodeDecimal;
    private FacilityType facilityType;
    private int visualRange; // nm

    // filing
    private int flightPlanRevision;
    private String rawFlightPlanType; // I = IFR, V = VFR; unfortunately user-defined, e.g. also seen: S (scheduled)
    private int rawDepartureTimePlanned; // may be 0; values can omit leading zeros! may contain garbage
    private int rawDepartureTimeActual; // may be 0, may be equal, may be actual value - who or what sets this? Values
                                        // can omit leading zeros! may contain garbage
    private Duration filedTimeEnroute; // data: two fields, hours + minutes
    private Duration filedTimeFuel; // data: two fields, hours + minutes
    private String filedAlternateAirportCode;
    private String flightPlanRemarks;
    private String filedRoute;
    private double departureAirportLatitude; // seems unused, always 0
    private double departureAirportLongitude; // seems unused, always 0
    private double destinationAirportLatitude; // seems unused, always 0
    private double destinationAirportLongitude; // seems unused, always 0

    // ATC only
    private String controllerMessage; // decode "atis_message": first line prefixed "$ " => voice URL; multi-line
                                      // formatting with "^" and special character as CR LF?
    private Instant controllerMessageLastUpdated; // time_last_atis_received

    // ATIS only
    private String atisDesignator;

    // all connected
    private Instant logonTime;

    // Pilots only
    private int heading;
    private double qnhInchMercury;
    private int qnhHectopascal;

    /**
     * Returns the call sign the client is being identified by. The call sign is
     * supposed to be unique for logged in users (may not apply to prefiled flight
     * plans) on the network as of time of data collection.
     *
     * @return call sign the client is being identified by
     */
    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    /**
     * Returns the VATSIM user ID the client belongs to.
     * <p>
     * Although policy generally forbids regular users to connect multiple sessions
     * using the same user ID this is not enforced server-side (at least not for
     * ATC).
     * </p>
     * <p>
     * One permitted use of multiple sessions is for ATC to provide ATIS service
     * additional to a controlling session (requires two logins at once for
     * technical reasons).
     * </p>
     * <p>
     * Also, some users such as supervisors, administrators or "promotional
     * accounts" may in fact have permission to connect multiple sessions at the
     * same time, so this ID alone should not be expected to uniquely identify
     * clients on a data file.
     * </p>
     * <p>
     * In some cases, there may not be a VATSIM ID listed on data file. If that
     * happens, return value will be negative. See parser log messages for details.
     * This may be the case for "ghost" clients which are still listed in online
     * section although they do not appear to be actually connected (also missing
     * {@link #protocolVersion} and being parsed with a guessed
     * {@link #effectiveClientType}). Possible causes could be simulator crashes or
     * client misbehaviour.
     * </p>
     *
     * @return VATSIM user ID associated with client; negative if missing
     */
    public int getVatsimID() {
        return vatsimID;
    }

    public void setVatsimID(int vatsimID) {
        this.vatsimID = vatsimID;
    }

    /**
     * Returns the user's real name and possibly "home base". Guides and prefiling
     * forms ask pilots to add a "home base" as ICAO code which is supposed to
     * resemble an aircraft home base but usually gets set to the airport code
     * closest to their actual home. As this is "a pilot thing", adding "home bases"
     * is uncommon for ATC.
     * <p>
     * Note: Providing a real name does not seem to be enforced server-side, some
     * clients are able to log in without providing any name at all (or at least it
     * is not visible on data files). Returned String will be empty in those cases.
     * </p>
     *
     * @return user's real name and possibly "home base"; may be empty
     */
    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * Returns the {@link ClientType} as originally specified in a data file.
     * <p>
     * Unless you need to access the actual raw information, consider using the
     * {@link #effectiveClientType} instead. See JavaDoc on
     * {@link #getEffectiveClientType()} for an explanation of issues related to the
     * raw client type.
     * </p>
     *
     * @return raw type of client, may not match actual role of client; null if
     *         unavailable
     * @see #getEffectiveClientType()
     */
    public ClientType getRawClientType() {
        return rawClientType;
    }

    public void setRawClientType(ClientType rawClientType) {
        this.rawClientType = rawClientType;
    }

    /**
     * Returns the perceived effective {@link ClientType}.
     * <p>
     * Unfortunately, there are situations which require guessing the correct type
     * as the {@link #rawClientType} is either missing in data file or does not
     * match the actual role of a client on the network when perceived by other
     * clients.
     * </p>
     * <p>
     * Expect null if information was not available and guessing failed (check
     * parser log messages for any details).
     * </p>
     * <p>
     * Some of the issues which led to the need of guessing an effective client type
     * from actual data instead of just using raw information:
     * </p>
     * <ul>
     * <li>{@link #rawClientType} may indicate {@link ClientType#ATC_CONNECTED} for
     * clients who act as {@link ClientType#PILOT_CONNECTED}. This can be seen quite
     * frequently for unprivileged {@link ControllerRating#OBS} clients (shared
     * cockpit or bad login?) as well as {@link ControllerRating#SUP} (supervisor on
     * duty while flying?). This can be noticed by ATC clients "leaking" pilot
     * client information into the data file such as {@link #heading} or
     * {@link #groundSpeed} > 0 while indicating a placeholder frequency
     * ({@link #servedFrequencyKilohertz}) and no {@link #controllerMessage} which
     * are clear signs of a pilot client being connected without providing ATC
     * services at the same time. The {@link #effectiveClientType} shall correct
     * such clients to {@link ClientType#PILOT_CONNECTED} according to their actual
     * role on network.</li>
     * <li>Clients can become listed as "ghosts" without any {@link #rawClientType}
     * (null). This might be caused by simulator crashes or some client
     * misbehaviour. If you need to know if the client is actually online, you may
     * want to check if {@link #protocolVersion} and {@link #vatsimID} are available
     * (expected for actual online clients).</li>
     * </ul>
     *
     * @return type of client, may have been guessed from actual data; null if
     *         unavailable and guessing failed
     * @see #getRawClientType()
     */
    public ClientType getEffectiveClientType() {
        return effectiveClientType;
    }

    public void setEffectiveClientType(ClientType effectiveClientType) {
        this.effectiveClientType = effectiveClientType;
    }

    /**
     * Returns the frequency this client provides ATC service on. Pilots are not
     * able to provide services so they can never indicate a served frequency. ATC
     * clients can connect without serving a frequency, for example when connecting
     * as an observer. If no frequency is being served by this client, a negative
     * number will be returned.
     *
     * @return frequency this client provides ATC service on; negative if not
     *         serving
     */
    public int getServedFrequencyKilohertz() {
        return servedFrequencyKilohertz;
    }

    public void setServedFrequencyKilohertz(int servedFrequencyKilohertz) {
        this.servedFrequencyKilohertz = servedFrequencyKilohertz;
    }

    /**
     * Returns the latitude (north/south coordinate) the client is currently located
     * at.
     * <p>
     * Online clients are actually not required to provide a location (maybe while
     * connected in observer mode?). Prefiled flight plans are not permitted to
     * include a location.
     * </p>
     * <p>
     * If no latitude is available, {@link Double#NaN} will be returned.
     * </p>
     * <p>
     * Positive latitude is north of equator, negative latitude is south. While only
     * a value range of -90..90&deg; makes sense (spherical coordinate system),
     * returned values should still be assumed to exceed that range because parser
     * only checks for syntax and does not unify to a strict coordinate system.
     * </p>
     *
     * @return latitude the client is currently located at; {@link Double#NaN} if
     *         unavailable
     */
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the longitude (east/west coordinate) the client is currently located
     * at.
     * <p>
     * Online clients are actually not required to provide a location (maybe while
     * connected in observer mode?). Prefiled flight plans are not permitted to
     * include a location.
     * </p>
     * <p>
     * If no longitude is available, {@link Double#NaN} will be returned.
     * </p>
     * <p>
     * Positive longitude is east of Prime Meridian, negative longitude is west.
     * While only a value range of -180..180&deg; makes sense (spherical coordinate
     * system), returned values should still be assumed to exceed that range because
     * parser only checks for syntax and does not unify to a strict coordinate
     * system.
     * </p>
     *
     * @return longitude the client is currently located at; {@link Double#NaN} if
     *         unavailable
     */
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the altitude (measured in feet) the client is currently located at.
     * <p>
     * Online clients are actually not required to provide a location (maybe while
     * connected in observer mode?). Prefiled flight plans are not permitted to
     * include a location.
     * </p>
     * <p>
     * If no altitude is available, 0 will be assumed.
     * </p>
     *
     * @return longitude the client is currently located at; 0 if unavailable
     */
    public int getAltitudeFeet() {
        return altitudeFeet;
    }

    public void setAltitudeFeet(int altitudeFeet) {
        this.altitudeFeet = altitudeFeet;
    }

    /**
     * Returns the ground speed (measured in knots) the client is currently moving
     * at.
     * <p>
     * Ground speed is supposed to always be a positive value. Negative values are
     * used to indicate that no ground speed was available.
     * </p>
     * <p>
     * Prefiled flight plans and ATC are stationary and thus do not move by
     * principle. They will always return a negative value.
     * </p>
     *
     * @return ground speed in knots (valid: >= 0); negative value if unavailable
     */
    public int getGroundSpeed() {
        return groundSpeed;
    }

    public void setGroundSpeed(int groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    /**
     * Returns the aircraft type.
     * <p>
     * Pilots provide their aircraft type when prefiling a flight plan and a second
     * time when they connect to the network. ATC may be able to change pilot's
     * aircraft types while logged in as part of revising the flight plan (not
     * sure).
     * </p>
     * <p>
     * While ICAO aircraft type codes are supposed to be used with equipment code as
     * suffix and optional wake category as prefix (separated by forward slashes)
     * the actual format and value of this field is not reliable as it is an
     * informal free-text field and sometimes contains alternate/IATA codes or
     * common mistakes (such as B77W for a Boeing 777 which is neither a valid ICAO
     * nor IATA code).
     * </p>
     * <p>
     * <strong>Example values:</strong> B738/M, H/A332/X, B737
     * </p>
     * <p>
     * While it does not make any sense for ATC to file flight plans, such data can
     * actually be found on the network. If this field is filled for ATC it should
     * be ignored unless an actual use case becomes known.
     * </p>
     *
     * @return aircraft type; may deviate from wake/ICAO-type/equipment syntax and
     *         value
     */
    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    /**
     * Returns the true air speed (TAS, measured in knots) listed on flight plan of
     * this client.
     * <p>
     * If no TAS was provided, 0 will be returned. Only positive values are valid.
     * </p>
     * <p>
     * While it does not make any sense for ATC to file flight plans, such data can
     * actually be found on the network. If this field is filled for ATC it should
     * be ignored unless an actual use case becomes known.
     * </p>
     *
     * @return true air speed (TAS, measured in knots) listed on flight plan; 0 if
     *         unavailable
     */
    public int getFiledTrueAirSpeed() {
        return filedTrueAirSpeed;
    }

    public void setFiledTrueAirSpeed(int filedTrueAirSpeed) {
        this.filedTrueAirSpeed = filedTrueAirSpeed;
    }

    /**
     * Returns the departure airport code listed on flight plan of this client.
     * <p>
     * While these are usually ICAO codes, they could be anything else as this is a
     * free-text field.
     * </p>
     *
     * @return departure airport code listed on flight plan of this client
     */
    public String getFiledDepartureAirportCode() {
        return filedDepartureAirportCode;
    }

    public void setFiledDepartureAirportCode(String filedDepartureAirportCode) {
        this.filedDepartureAirportCode = filedDepartureAirportCode;
    }

    /**
     * Returns the unprocessed planned altitude listed on flight plan of this
     * client.
     * <p>
     * Unfortunately, this field is free-text and not strictly numeric, so pilots
     * are free to file some strings which require further interpretation. No
     * interpretation is attempted by parser, so this is the raw unprocessed value
     * as available from data file.
     * </p>
     * <p>
     * <strong>Example values:</strong> 30000, FL300, F300
     * </p>
     *
     * @return unprocessed planned altitude listed on flight plan
     */
    public String getRawFiledAltitude() {
        return rawFiledAltitude;
    }

    public void setRawFiledAltitude(String rawFiledAltitude) {
        this.rawFiledAltitude = rawFiledAltitude;
    }

    /**
     * Returns the destination airport code listed on flight plan of this client.
     * <p>
     * While these are usually ICAO codes, they could be anything else as this is a
     * free-text field.
     * </p>
     *
     * @return destination airport code listed on flight plan of this client
     */
    public String getFiledDestinationAirportCode() {
        return filedDestinationAirportCode;
    }

    public void setFiledDestinationAirportCode(String filedDestinationAirportCode) {
        this.filedDestinationAirportCode = filedDestinationAirportCode;
    }

    /**
     * Returns the ID of the server the client is currently connected to. Returns
     * null for clients not being online (prefiled flight plans). May also return
     * null if client is a ghost; see {@link #getEffectiveClientType()} for an
     * explanation.
     *
     * @return ID of server currently connected to; null if offline or ghost
     */
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Returns the protocol version the client is using for communication with
     * servers. Negative if not available (e.g. on prefiled flight plans).
     * <p>
     * If protocol version is negative/unavailable although {@link #rawClientType}
     * indicates an online connection, client may be a "ghost" on VATSIM servers.
     * One possible explanation for such connections is a simulator crash or client
     * misbehaviour. {@link #rawClientType} then may have been guessed by parser.
     * </p>
     *
     * @return protocol version of client; negative if unavailable
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Returns the client's effective controller rating for this session.
     * <p>
     * Controller rating is being used as ATC/network permission level, see
     * {@link ControllerRating} for details.
     * </p>
     * <p>
     * This is only meaningful for ATC clients and only for one session. If an
     * ATC-permitted user is connected as a pilot, rating will always be
     * {@link ControllerRating#OBS} for the pilot session regardless of user's
     * actual controller rating. Prefiled flight plans do not indicate any
     * controller rating and thus return null.
     * </p>
     *
     * @return client's controller rating during this session; null for prefiled
     *         flight plans
     */
    public ControllerRating getControllerRating() {
        return controllerRating;
    }

    public void setControllerRating(ControllerRating controllerRating) {
        this.controllerRating = controllerRating;
    }

    /**
     * Returns the transponder code in numeric decimal representation currently used
     * by client.
     * <p>
     * The actual, spoken transponder code can be resolved from the numeric by
     * left-padding it with 0 to a length of 4 digits.
     * </p>
     * <p>
     * Only in real world digits would be limited to a range of 0..7 as transponder
     * codes are encoded octal. Numeric values returned by this getter however are
     * decimal and pilot clients are actually submitting decimal digits 8 and 9
     * which would not be possible in real aviation. Such excessive values are to be
     * expected on this representation.
     * </p>
     * <p>
     * In rare cases, pilots on VATSIM may actually be listed with a transponder
     * code which exceeds the usual 4 digits rendering the code effectively invalid
     * as it may become incompatible with other clients.
     * </p>
     * <p>
     * Only if transponder code is not available, a negative value will be returned.
     * </p>
     * <p>
     * Transponder codes are initially selected on pilot's discretion and requested
     * to be changed by ATC upon clearance and sometimes mid-flight. Connected
     * pilots list transponder codes regardless of actual transponder mode selected
     * on aircraft (even if transponder on aircraft is turned off, clients may still
     * list a code). The listed code is always the code set by pilots, not set by
     * ATC. ATC only requests pilots to enter an assigned code, but it cannot be set
     * by ATC. Connected pilots without a transponder code can be encountered.
     * </p>
     * <p>
     * Prefiled flight plans and ATC are never setting transponder codes.
     * </p>
     *
     * @return decimal numeric transponder code, needs left-padding to 4 digits to
     *         reconstruct spoken code; negative if unavailable; may exceed 4 digits
     *         (code is supposedly invalid)
     */
    public int getTransponderCodeDecimal() {
        return transponderCodeDecimal;
    }

    public void setTransponderCodeDecimal(int transponderCodeDecimal) {
        this.transponderCodeDecimal = transponderCodeDecimal;
    }

    /**
     * Returns the type of ATC facility. Returns null if unavailable. See
     * {@link FacilityType} for a detailed description.
     *
     * @return facility type; null if unavailable
     */
    public FacilityType getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(FacilityType facilityType) {
        this.facilityType = facilityType;
    }

    /**
     * Returns the client's visual range in nautical miles (nm).
     * <p>
     * Visual range is only available and relevant to ATC stations and describes the
     * radar range. If defined, range is always positive.
     * </p>
     * <p>
     * This field is not mandatory. If missing, a negative value will be returned.
     * </p>
     *
     * @return visual (radar) range of ATC station; negative value if unavailable
     */
    public int getVisualRange() {
        return visualRange;
    }

    public void setVisualRange(int visualRange) {
        this.visualRange = visualRange;
    }

    /**
     * Returns the flight plan revision number.
     * <p>
     * Every time a flight plan gets updated, the revision number will be increased
     * by 1. First revision starts counting at 0.
     * </p>
     * <p>
     * The revision number is mandatory for prefiled flight plans, otherwise it is
     * optional. If revision number is undefined, a negative value will be returned.
     * </p>
     *
     * @return revision of currently listed flight plan, counting starts at 0;
     *         negative if unavailable
     */
    public int getFlightPlanRevision() {
        return flightPlanRevision;
    }

    public void setFlightPlanRevision(int flightPlanRevision) {
        this.flightPlanRevision = flightPlanRevision;
    }

    /**
     * Returns the raw flight plan type as chosen by user.
     * <p>
     * While flight plan types are well-defined in real world aviation (most common
     * are I, V, Y, Z) the data field on VATSIM can unfortunately contain any other
     * unexpected value such as S (I guess that is supposed to stand for "scheduled"
     * which belongs in a company preflight briefing but not an ICAO flight plan).
     * </p>
     *
     * @return raw flight plan type as chosen by user
     */
    public String getRawFlightPlanType() {
        return rawFlightPlanType;
    }

    public void setRawFlightPlanType(String rawFlightPlanType) {
        this.rawFlightPlanType = rawFlightPlanType;
    }

    /**
     * Returns the planned time of departure as it may have been entered into flight
     * plan. This is a supposed to be a rough estimation of the time a pilot plans
     * the aircraft to become airborne. The time is supposed to be entered in UTC in
     * format HHmm, represented as unsigned integer and thus omitting leading zeros
     * (30 => 0:30 UTC, 1340 => 13:40 UTC).
     * <p>
     * Returns a negative value if unavailable.
     * </p>
     * <p>
     * Unfortunately, this field as available from data.txt status file exhibits a
     * number of issues and should thus only be seen informal with a risk of
     * containing false information:
     * </p>
     * <ul>
     * <li>0 can be interpreted in multiple ways:
     * <ul>
     * <li>it could mean an intended departure time of 0000z (which is prime time in
     * American time zones)</li>
     * <li>more often it seems to be used where no departure time has been entered
     * at all</li>
     * </ul>
     * </li>
     * <li>24xx may have been used to work around the 0000z misinterpretation
     * issue</li>
     * <li>pilots may simply enter wrong/invalid information</li>
     * <li>The field could just contain numeric garbage such as 6000, 9400,
     * 78408692, 1709907214 (all seen in the wild).</li>
     * </ul>
     * <p>
     * As a result, even if validated against other information such as the actual
     * time of departure (analyzed from tracking points, not
     * {@link #rawDepartureTimeActual} which has the same issues) this may not be
     * what the pilot has actually filed on the flight plan.
     * </p>
     *
     * @return (unreliable) planned time of departure in UTC; negative if
     *         unavailable; see full description for issues
     */
    public int getRawDepartureTimePlanned() {
        return rawDepartureTimePlanned;
    }

    public void setRawDepartureTimePlanned(int rawDepartureTimePlanned) {
        this.rawDepartureTimePlanned = rawDepartureTimePlanned;
    }

    /**
     * Returns what has been entered as actual time of departure. Values seem to be
     * in UTC and in format HHmm, represented as unsigned integer and thus omitting
     * leading zeros (30 => 0:30 UTC, 1340 => 13:40 UTC).
     * <p>
     * <strong>It is not known who or what sets this field and when.</strong>
     * Instead of relying on this field it is advisable to determine the time of
     * departure by searching the actual track for first occurence of high
     * {@link #groundSpeed} (>80 kt) and a simultaneous significant (for example
     * >200 ft/min) increase of {@link #altitudeFeet} (high speed + intentional
     * climb => takeoff).
     * </p>
     * <p>
     * Additional to not knowing who sets this field, it exhibits a number of issues
     * described in detail for {@link #getRawDepartureTimePlanned()}. So,
     * unfortunately, this value may not be of any use at all in current data
     * format.
     * </p>
     * <p>
     * Returns a negative value if unavailable.
     * </p>
     *
     * @return (unreliable) actual time of departure; negative if unavailable; see
     *         full description for issues
     */
    public int getRawDepartureTimeActual() {
        return rawDepartureTimeActual;
    }

    public void setRawDepartureTimeActual(int rawDepartureTimeActual) {
        this.rawDepartureTimeActual = rawDepartureTimeActual;
    }

    /**
     * Returns the flight's estimated time enroute (airborne) as filed on flight
     * plan. Returns null if unavailable. May return a negative duration which
     * doesn't make any sense at all and should be ignored (error on submission of
     * flight plan).
     *
     * @return estimated time enroute, may be negative; null if unavailable
     */
    public Duration getFiledTimeEnroute() {
        return filedTimeEnroute;
    }

    public void setFiledTimeEnroute(Duration filedTimeEnroute) {
        this.filedTimeEnroute = filedTimeEnroute;
    }

    /**
     * Returns the flight's estimated time of fuel on board as filed on flight plan.
     * Returns null if unavailable. May return a negative duration which doesn't
     * make any sense at all and should be ignored (error on submission of flight
     * plan).
     *
     * @return estimated time of fuel on board, may be negative; null if unavailable
     */
    public Duration getFiledTimeFuel() {
        return filedTimeFuel;
    }

    public void setFiledTimeFuel(Duration filedTimeFuel) {
        this.filedTimeFuel = filedTimeFuel;
    }

    /**
     * Returns the alternate airport code listed on flight plan of this client.
     * <p>
     * While these are usually ICAO codes, they could be anything else as this is a
     * free-text field.
     * </p>
     *
     * @return alternate airport code listed on flight plan of this client
     */
    public String getFiledAlternateAirportCode() {
        return filedAlternateAirportCode;
    }

    public void setFiledAlternateAirportCode(String filedAlternateAirportCode) {
        this.filedAlternateAirportCode = filedAlternateAirportCode;
    }

    /**
     * Returns the remarks entered on flight plan.
     * <p>
     * Pilot clients or prefiling forms add voice capability flags (T = Text only; R
     * = Receive voice, send text; V = full voice). Other than that, pilots are free
     * to enter any remarks they may find useful.
     * </p>
     * <p>
     * Pilots sometimes attach full ICAO field 18 information which provides highly
     * detailed information generally not needed for simulation (for example
     * PBN/..., DOF/... etc.).
     * </p>
     *
     * @return remarks on flight plan
     */
    public String getFlightPlanRemarks() {
        return flightPlanRemarks;
    }

    public void setFlightPlanRemarks(String flightPlanRemarks) {
        this.flightPlanRemarks = flightPlanRemarks;
    }

    /**
     * Returns the route as filed on flight plan.
     * <p>
     * This does not represent the actual route taken and may in fact have a
     * non-uniform invalid/only human-readable format.
     * </p>
     * <p>
     * Online ATC stations may or may not amend the flight plan as they see need.
     * </p>
     *
     * @return route as filed on flight plan
     */
    public String getFiledRoute() {
        return filedRoute;
    }

    public void setFiledRoute(String filedRoute) {
        this.filedRoute = filedRoute;
    }

    /**
     * Returns what has been set as departure airport latitude (north/south
     * coordinate). This field seems to be unused at the moment (always set to 0).
     * Returns {@link Double#NaN} if unavailable.
     *
     * @return departure airport latitude
     */
    public double getDepartureAirportLatitude() {
        return departureAirportLatitude;
    }

    public void setDepartureAirportLatitude(double departureAirportLatitude) {
        this.departureAirportLatitude = departureAirportLatitude;
    }

    /**
     * Returns what has been set as departure airport longitude (east/west
     * coordinate). This field seems to be unused at the moment (always set to 0).
     * Returns {@link Double#NaN} if unavailable.
     *
     * @return departure airport longitude
     */
    public double getDepartureAirportLongitude() {
        return departureAirportLongitude;
    }

    public void setDepartureAirportLongitude(double departureAirportLongitude) {
        this.departureAirportLongitude = departureAirportLongitude;
    }

    /**
     * Returns what has been set as destination airport latitude (north/south
     * coordinate). This field seems to be unused at the moment (always set to 0).
     * Returns {@link Double#NaN} if unavailable.
     *
     * @return destination airport latitude
     */
    public double getDestinationAirportLatitude() {
        return destinationAirportLatitude;
    }

    public void setDestinationAirportLatitude(double destinationAirportLatitude) {
        this.destinationAirportLatitude = destinationAirportLatitude;
    }

    /**
     * Returns what has been set as destination airport longitude (east/west
     * coordinate). This field seems to be unused at the moment (always set to 0).
     * Returns {@link Double#NaN} if unavailable.
     *
     * @return destination airport longitude
     */
    public double getDestinationAirportLongitude() {
        return destinationAirportLongitude;
    }

    public void setDestinationAirportLongitude(double destinationAirportLongitude) {
        this.destinationAirportLongitude = destinationAirportLongitude;
    }

    /**
     * Returns the message set by a controller. Empty if unavailable.
     * <p>
     * Until October 2019, the message usually consists of a voice server URL
     * prefixed with "$ " on first line. Remainder is generally known as "info
     * lines" or "controller info". Voice rooms are superseded by "Audio for VATSIM"
     * on 14 October 2019, URLs after that date may be dummies or missing.
     * </p>
     * <p>
     * Multiple lines are separated by LF (\n).
     * </p>
     * <p>
     * The character set used for encoding the message does unfortunately vary and
     * needs to be guessed.
     * </p>
     *
     * @return message set by controller
     */
    public String getControllerMessage() {
        return controllerMessage;
    }

    public void setControllerMessage(String controllerMessage) {
        this.controllerMessage = controllerMessage;
    }

    /**
     * Returns the timestamp of last {@link #controllerMessage} update. Returns null
     * if unavailable.
     *
     * @return timestamp of last {@link #controllerMessage} update; null if
     *         unavailable
     */
    public Instant getControllerMessageLastUpdated() {
        return controllerMessageLastUpdated;
    }

    public void setControllerMessageLastUpdated(Instant controllerMessageLastUpdated) {
        this.controllerMessageLastUpdated = controllerMessageLastUpdated;
    }

    /**
     * Returns the ATIS designator (rotating code letter) or null if unavailable.
     * 
     * @return ATIS designator; null if unavailable
     */
    public String getAtisDesignator() {
        return atisDesignator;
    }

    public void setAtisDesignator(String atisDesignator) {
        this.atisDesignator = atisDesignator;
    }

    /**
     * Returns the timestamp when client has logged into this session. Returns null
     * if unavailable.
     *
     * @return timestamp of client's login to this session; null if unavailable
     */
    public Instant getLogonTime() {
        return logonTime;
    }

    public void setLogonTime(Instant logonTime) {
        this.logonTime = logonTime;
    }

    /**
     * Returns the heading a pilot is currently oriented towards. Returns a negative
     * value if no heading is available. Valid positive value range is 0..359 as
     * heading is measured in degrees.
     *
     * @return heading oriented towards; negative if unavailable
     */
    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    /**
     * Returns the local QNH (atmospheric pressure at sea-level) of this client as
     * measured in inches of mercury.
     * <p>
     * Local QNH is specific to a combination of time, location, settings and client
     * as it is governed by each client's individual local weather simulation (which
     * may or may not be linked to VATSIM METAR at the given time).
     * </p>
     * <p>
     * Returns {@link Double#NaN} if unavailable.
     * </p>
     * <p>
     * Pilot-submitted QNH may make no sense, so you are advised to use
     * {@link BarometricPressure} to check a QNH for plausibility before actually
     * using it.
     * </p>
     *
     * @return local QNH in inches of mercury; {@link Double#NaN} if unavailable
     * @see BarometricPressure
     */
    public double getQnhInchMercury() {
        return qnhInchMercury;
    }

    public void setQnhInchMercury(double qnhInchMercury) {
        this.qnhInchMercury = qnhInchMercury;
    }

    /**
     * Returns the local QNH (atmospheric pressure at sea-level) of this client as
     * measured in hectopascal (aka millibar).
     * <p>
     * Local QNH is specific to a combination of time, location, settings and client
     * as it is governed by each client's individual local weather simulation (which
     * may or may not be linked to VATSIM METAR at the given time).
     * </p>
     * <p>
     * Returns negative value if unavailable. Note that negative values may also
     * have been specified as input, so a negative value may also indicate an
     * available erroneous input. In general, you are advised to use
     * {@link BarometricPressure} to check a pilot-submitted QNH for plausibility
     * before actually using it.
     * </p>
     *
     * @return local QNH in hectopascal; negative if unavailable
     * @see BarometricPressure
     */
    public int getQnhHectopascal() {
        return qnhHectopascal;
    }

    public void setQnhHectopascal(int qnhHectopascal) {
        this.qnhHectopascal = qnhHectopascal;
    }

}
