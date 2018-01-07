package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses {@link Client} information as given by VATSIM's data.txt status files
 * in sections <code>CLIENTS</code> and <code>PREFILE</code>.
 * Both sections share the same syntax, making it possible to use the same
 * parser implementation and entities.
 * For more information see {@link Client} class documentation.
 * Usually, {@link DataFileParser} should be used to have this information
 * parsed from an actual complete {@link DataFile}.
 */
public class ClientParser {
    private static final String SUBPATTERN_TIMESTAMP = "\\d{14}";
    private static final String SUBPATTERN_FLOAT_UNSIGNED = "\\d+(?:\\.\\d+|)(?:e\\-?\\d+|)"; // integers are valid as well
    private static final String SUBPATTERN_GEOCOORDINATES = "\\-?"+SUBPATTERN_FLOAT_UNSIGNED;
    
    // TODO: test for floating numbers like 7.62939e-08 as some pilot client submits such numbers...
    
    // TODO: airport lat/lon should be geocoordinates subpattern with optional 0
    private static final Pattern PATTERN_LINE = Pattern.compile(
    //    1       2       3       4            5                                6                                7
        "([^:]+):(\\d+|):([^:]*):(PILOT|ATC|):("+SUBPATTERN_FLOAT_UNSIGNED+"|):("+SUBPATTERN_GEOCOORDINATES+"|):("+SUBPATTERN_GEOCOORDINATES+"|):"+
    //    8           9       10      11      12      13      14      15      16      17
        "(\\-?\\d+|):(\\d+|):([^:]*):(\\d+|):([^:]*):([^:]*):([^:]*):([^:]*):(\\d+|):(\\d+|):"+
    //    18         19      20      21      22      23      24      25      26      27      28
        "(\\d{0,4}):(\\d+|):(\\d+|):(\\d+|):([^:]*):(\\d+|):(\\d+|):(\\d+|):(\\d+|):(\\d+|):(\\d+|):"+
    //    29      30      31      32      33      34      35
        "([^:]*):([^:]*):([^:]*):(\\d+|):(\\d+|):(\\d+|):(\\d+|):"+
    //    36   37                         38                         39
        "(.*):("+SUBPATTERN_TIMESTAMP+"|):("+SUBPATTERN_TIMESTAMP+"|):(\\d+|):"+
    //    40                               41
        "("+SUBPATTERN_FLOAT_UNSIGNED+"|):(\\d+|):");
    
    // TODO: allowed ATIS message to contain ":" in text as seen in the wild... may cause issues, keep an eye on it
    
    private static final int PATTERN_LINE_CALLSIGN = 1;
    private static final int PATTERN_LINE_CID = 2;
    private static final int PATTERN_LINE_REALNAME = 3;
    private static final int PATTERN_LINE_CLIENTTYPE = 4;
    private static final int PATTERN_LINE_FREQUENCY = 5;
    private static final int PATTERN_LINE_LATITUDE = 6;
    private static final int PATTERN_LINE_LONGITUDE = 7;
    private static final int PATTERN_LINE_ALTITUDE = 8;
    private static final int PATTERN_LINE_GROUNDSPEED = 9;
    private static final int PATTERN_LINE_PLANNED_AIRCRAFT = 10;
    private static final int PATTERN_LINE_PLANNED_TASCRUISE = 11;
    private static final int PATTERN_LINE_PLANNED_DEPAIRPORT = 12;
    private static final int PATTERN_LINE_PLANNED_ALTITUDE = 13;
    private static final int PATTERN_LINE_PLANNED_DESTAIRPORT = 14;
    private static final int PATTERN_LINE_SERVER = 15;
    private static final int PATTERN_LINE_PROTREVISION = 16;
    private static final int PATTERN_LINE_RATING = 17;
    private static final int PATTERN_LINE_TRANSPONDER = 18;
    private static final int PATTERN_LINE_FACILITYTYPE = 19;
    private static final int PATTERN_LINE_VISUALRANGE = 20;
    private static final int PATTERN_LINE_PLANNED_REVISION= 21;
    private static final int PATTERN_LINE_PLANNED_FLIGHTTYPE = 22;
    private static final int PATTERN_LINE_PLANNED_DEPTIME = 23;
    private static final int PATTERN_LINE_PLANNED_ACTDEPTIME = 24;
    private static final int PATTERN_LINE_PLANNED_HRSENROUTE = 25;
    private static final int PATTERN_LINE_PLANNED_MINENROUTE = 26;
    private static final int PATTERN_LINE_PLANNED_HRSFUEL = 27;
    private static final int PATTERN_LINE_PLANNED_MINFUEL = 28;
    private static final int PATTERN_LINE_PLANNED_ALTAIRPORT = 29;
    private static final int PATTERN_LINE_PLANNED_REMARKS = 30;
    private static final int PATTERN_LINE_PLANNED_ROUTE = 31;
    private static final int PATTERN_LINE_PLANNED_DEPAIRPORT_LAT = 32;
    private static final int PATTERN_LINE_PLANNED_DEPAIRPORT_LON = 33;
    private static final int PATTERN_LINE_PLANNED_DESTAIRPORT_LAT = 34;
    private static final int PATTERN_LINE_PLANNED_DESTAIRPORT_LON = 35;
    private static final int PATTERN_LINE_ATIS_MESSAGE = 36;
    private static final int PATTERN_LINE_TIME_LAST_ATIS_RECEIVED = 37;
    private static final int PATTERN_LINE_TIME_LOGON = 38;
    private static final int PATTERN_LINE_HEADING = 39;
    private static final int PATTERN_LINE_QNH_IHG = 40;
    private static final int PATTERN_LINE_QNH_MB = 41;
    
    private static final String CLIENT_TYPE_ATC = "ATC";
    private static final String CLIENT_TYPE_PILOT = "PILOT";
    
    private static final int DEFAULT_ALTITUDE = 0;
    
    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    
    private boolean isParsingPrefileSection = false;
    
    /**
     * Configures the parser to treat all following lines as belonging to
     * either the <code>CLIENTS</code> or <code>PREFILE</code> section.
     * @param isParsingPrefileSection Are following lines to be interpreted as belonging to <code>PREFILE</code> section? Set true before parsing lines from <code>PREFILE</code> and false before parsing lines from <code>CLIENTS</code> section.
     * @return this {@link ClientParser} instance (for method chaining)
     */
    public ClientParser setIsParsingPrefileSection(boolean isParsingPrefileSection) {
        this.isParsingPrefileSection = isParsingPrefileSection;
        return this;
    }
    
    /**
     * Parses all information from the given line to a {@link Client} object.
     * The line is expected to contain the proper syntax used by VATSIM data.txt
     * files and not to be empty or a comment.
     * @param line line to be parsed; must not be empty or a comment
     * @return all parsed data in a {@link Client} object
     */
    public Client parse(String line) {
        Matcher matcher = PATTERN_LINE.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("unparseable line: \""+line+"\"");
        }
        
        Client client = new Client();
        
        ClientType clientType = parseClientType(matcher.group(PATTERN_LINE_CLIENTTYPE));
        client.setClientType(clientType);
        
        // TODO: guess client type if null from other available data of this line (e.g. only ATC may define a frequency, only online pilots have a heading or GS >0)
        
        boolean isOnline = (clientType != null) && (clientType != ClientType.PILOT_PREFILED);
        boolean isATC = (clientType == ClientType.ATC_CONNECTED);
        boolean isAllowedToServeFrequency = isATC;
        //boolean isAllowedToHaveFlightPlan = !isATC;
        
        client.setCallsign(matcher.group(PATTERN_LINE_CALLSIGN));
        client.setVatsimID(parseIntWithDefault(matcher.group(PATTERN_LINE_CID), -1)); // TODO: log details if ID is missing
        client.setRealName(matcher.group(PATTERN_LINE_REALNAME));
        client.setServedFrequencyKilohertz(parseServedFrequencyMegahertzToKilohertz(matcher.group(PATTERN_LINE_FREQUENCY), isAllowedToServeFrequency));
        client.setLatitude(parseOnlineGeoCoordinate(matcher.group(PATTERN_LINE_LATITUDE), isOnline));
        client.setLongitude(parseOnlineGeoCoordinate(matcher.group(PATTERN_LINE_LONGITUDE), isOnline));
        client.setAltitudeFeet(parseOnlineAltitude(matcher.group(PATTERN_LINE_ALTITUDE), isOnline));
        client.setGroundSpeed(parseGroundSpeed(matcher.group(PATTERN_LINE_GROUNDSPEED), clientType));
        client.setAircraftType(matcher.group(PATTERN_LINE_PLANNED_AIRCRAFT));
        client.setFiledTrueAirSpeed(parseIntWithDefault(matcher.group(PATTERN_LINE_PLANNED_TASCRUISE), 0));
        client.setFiledDepartureAirportCode(matcher.group(PATTERN_LINE_PLANNED_DEPAIRPORT));
        client.setRawFiledAltitude(matcher.group(PATTERN_LINE_PLANNED_ALTITUDE));
        client.setFiledDestinationAirportCode(matcher.group(PATTERN_LINE_PLANNED_DESTAIRPORT));
        client.setServerId(filterServerId(matcher.group(PATTERN_LINE_SERVER), isOnline));
        client.setProtocolVersion(parseOnlineProtocolVersion(matcher.group(PATTERN_LINE_PROTREVISION), isOnline));
        client.setControllerRating(parseControllerRating(matcher.group(PATTERN_LINE_RATING), clientType));
        client.setTransponderCodeDecimal(parseTransponderCodeDecimal(matcher.group(PATTERN_LINE_TRANSPONDER), clientType));
        client.setFacilityType(parseFacilityType(matcher.group(PATTERN_LINE_FACILITYTYPE), clientType));
        client.setVisualRange(parseVisualRange(matcher.group(PATTERN_LINE_VISUALRANGE), clientType));
        client.setFlightPlanRevision(parseFlightPlanRevision(matcher.group(PATTERN_LINE_PLANNED_REVISION), clientType));
        client.setRawFlightPlanType(matcher.group(PATTERN_LINE_PLANNED_FLIGHTTYPE));
        client.setDepartureTimePlanned(parseLocalTimeGraceful(matcher.group(PATTERN_LINE_PLANNED_DEPTIME)));

        return client;
    }
    
    private int parseIntWithDefault(String s, int defaultOnError) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return defaultOnError;
        }
    }
    
    private ClientType parseClientType(String rawClientType) {
        if (!isParsingPrefileSection) {
            if (CLIENT_TYPE_PILOT.equals(rawClientType)) {
                return ClientType.PILOT_CONNECTED;
            } else if (CLIENT_TYPE_ATC.equals(rawClientType)) {
                return ClientType.ATC_CONNECTED;
            }
        } else if (rawClientType.isEmpty()) {
            return ClientType.PILOT_PREFILED;
        }
        
        return null;
    }
    
    /**
     * Parses the given frequency assumed to be a floating number in MHz to an
     * integer describing the frequency in kHz.
     * Returned value will be negative if no frequency is provided.
     * If serving is not allowed but a frequency is still being provided,
     * an {@link IllegalArgumentException} will be thrown.
     * An {@link IllegalArgumentException} will also be thrown if the specified
     * frequency does not make any sense, for example if it is negative or zero.
     * @param s string to be parsed, formatted as floating number in MHz
     * @param allowServing Is serving a frequency allowed?
     * @return frequency in kHz
     * @throws IllegalArgumentException if serving a frequency while not allowed or frequency does not make any sense
     */
    private int parseServedFrequencyMegahertzToKilohertz(String s, boolean allowServing) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return -1;
        } else if (!allowServing) {
            throw new IllegalArgumentException("serving a frequency is not allowed but still encountered \""+s+"\" as being served by client");
        } else {
            int frequencyKilohertz = (int) Math.round(Double.parseDouble(s) * 1000.0);
            
            if (frequencyKilohertz <= 0) {
                throw new IllegalArgumentException("served frequency is given as \""+s+"\" which does not make any sense");
            }
            
            return frequencyKilohertz;
        }
    }

    /**
     * Parses a geo coordinate from the given string.
     * Result of {@link Double#NaN} indicates that no coordinate was available
     * (empty string).
     * Offline (prefiled) clients are not permitted to define a coordinate; only
     * empty strings are allowed in that case, any other input will throw an
     * {@link IllegalArgumentException}.
     * @param s string to be parsed
     * @param isOnline Is the client we are parsing for online?
     * @return geo coordinate or {@link Double#NaN} if not available
     * @throws IllegalArgumentException if client is not online but still provides a geo coordinate
     */
    private double parseOnlineGeoCoordinate(String s, boolean isOnline) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return Double.NaN;
        } else if (isOnline) {
            return Double.parseDouble(s);
        } else {
            throw new IllegalArgumentException("client is not online but still provides a geo coordinate (latitude/longitude)");
        }
    }
    
    /**
     * Parses an altitude from the given string specified integer format.
     * If no value can be parsed (including parsing errors),
     * {@link #DEFAULT_ALTITUDE} will be assumed.
     * If the client is not online but parsing still reveals an altitude other
     * than {@link #DEFAULT_ALTITUDE} an {@link IllegalArgumentException} will
     * be thrown.
     * @param s string to be parsed, plain integer
     * @param isOnline Is the client we are parsing for online?
     * @return altitude; {@link #DEFAULT_ALTITUDE} if not specified or number parsing error
     * @throws IllegalArgumentException if client is not online but still defines an altitude other than our default value
     */
    private int parseOnlineAltitude(String s, boolean isOnline) throws IllegalArgumentException {
        int altitude = parseIntWithDefault(s, DEFAULT_ALTITUDE);
        
        if (!isOnline && altitude != DEFAULT_ALTITUDE) {
            throw new IllegalArgumentException("client is not online (prefiled flight plan?) but still defines altitude \""+s+"\"");
        }
        
        return altitude;
    }
    
    /**
     * Parses ground speed from given string, taking into account
     * client type for validation.
     * <p>
     * Only pilots connected to the network are supposed to be moving and thus
     * indicate a ground speed. Prefiled flight plans and ATC are not allowed
     * to have a GS > 0 and throw an {@link IllegalArgumentException} on
     * violation.
     * </p>
     * <p>
     * Only positive values make sense.
     * Unknown speeds (including ATC & prefiles indicating 0) will return a
     * negative value.
     * </p>
     * @param s string to parse
     * @param clientType type of client the string belongs to
     * @return ground speed >= 0 for connected pilots; negative value if unspecified, only prefiled or ATC
     * @throws IllegalArgumentException if client is not a connected pilot but still indicates movement
     */
    private int parseGroundSpeed(String s, ClientType clientType) throws IllegalArgumentException {
        int groundSpeed = parseIntWithDefault(s, -1);
        
        if (clientType == ClientType.PILOT_CONNECTED) {
            return groundSpeed;
        } else if (groundSpeed > 0) {
            throw new IllegalArgumentException(clientType.name()+" must not have a ground speed greater zero (was: \""+s+"\")");
        }
        
        return -1;
    }
    
    /**
     * If filling flight plan fields is not allowed, value is returned null if
     * empty. Non-empty values will throw an {@link IllegalArgumentException}
     * if not permitted. Returns verbatim value if filling flight plan fields is
     * allowed.
     * @param flightPlanValue value of flight plan field to filter
     * @param isAllowedToFillFlightPlan Is client permitted to fill this flight plan field?
     * @return original value if allowed, null if not allowed and empty
     * @throws IllegalArgumentException if not allowed and not empty
     */
    private String filterFlightPlanField(String flightPlanValue, boolean isAllowedToFillFlightPlan) throws IllegalArgumentException {
        if (isAllowedToFillFlightPlan) {
            return flightPlanValue;
        }
        
        boolean fieldIsEmpty = flightPlanValue.trim().isEmpty();
        
        if (fieldIsEmpty) {
            return null;
        } else {
            throw new IllegalArgumentException("client is not permitted to fill flight plan fields but shows \""+flightPlanValue+"\"");
        }
    }
    
    /**
     * Checks if definition of server ID matches online state of client and
     * returns only valid values.
     * <p>
     * Only if a client is online, it is expected to have a server ID assigned.
     * Likewise, a client who is offline (prefiled flight plan) is expected
     * not to have any server ID as it is not connected to the network.
     * </p>
     * <p>
     * If expectation does not match, {@link IllegalArgumentException} will be
     * thrown.
     * </p>
     * <p>
     * If expectation matches, null will be returned if client is offline,
     * otherwise the original server ID will be returned.
     * </p>
     * @param serverId server ID
     * @param isOnline Is the client online?
     * @return server ID if expectation matches, null for offline clients
     * @throws IllegalArgumentException if expectation of server ID is violated
     */
    private String filterServerId(String serverId, boolean isOnline) throws IllegalArgumentException {
        boolean hasNoServerId = serverId.isEmpty();
        
        boolean availabilityMatchesOnlineState = isOnline ^ hasNoServerId;
        if (!availabilityMatchesOnlineState) {
            throw new IllegalArgumentException("client is "+(isOnline ? "" : "not ")+"online but has "+(hasNoServerId ? "no" : "a")+" server ID assigned: \""+serverId+"\"");
        }
        
        if (!isOnline) {
            return null;
        }
        
        return serverId;
    }
    
    /**
     * Parses and checks if definition of protocol version matches online state
     * of client and returns only valid values.
     * <p>
     * Only if a client is online, it is expected to indicate a protocol version.
     * Likewise, a client who is offline (prefiled flight plan) is expected
     * not to have any protocol version as it is not connected to the network.
     * </p>
     * <p>
     * If expectation does not match, {@link IllegalArgumentException} will be
     * thrown.
     * </p>
     * <p>
     * If expectation matches, negative value will be returned if client is
     * offline, otherwise the parsed value will be returned.
     * </p>
     * @param s protocol version as string
     * @param isOnline Is the client online?
     * @return protocol version number if expectation matches, negative number for offline clients
     * @throws IllegalArgumentException if expectation of server ID is violated or error occurs while parsing the value
     */
    private int parseOnlineProtocolVersion(String s, boolean isOnline) throws IllegalArgumentException {
        boolean hasNoProtocolVersion = s.isEmpty();
        
        boolean availabilityMatchesOnlineState = isOnline ^ hasNoProtocolVersion;
        if (!availabilityMatchesOnlineState) {
            throw new IllegalArgumentException("client is "+(isOnline ? "" : "not ")+"online but indicates "+(hasNoProtocolVersion ? "no" : "a")+" protocol revision: \""+s+"\"");
        }
        
        if (!isOnline) {
            return -1;
        }
        
        return Integer.parseInt(s);
    }

    /**
     * Parses the given string to a controller rating.
     * If the rating does not match expectations for the selected client type
     * an {@link IllegalArgumentException} will be thrown.
     * Only {@link ClientType#PILOT_PREFILED} is allowed not to specify any
     * rating, so only prefilings can return null.
     * @param s string to be parsed into controller rating
     * @param clientType session client type
     * @return controller rating; null on prefiling
     * @throws IllegalArgumentException if specified rating does not match expectations for client type
     */
    private ControllerRating parseControllerRating(String s, ClientType clientType) throws IllegalArgumentException {
        if (clientType == ClientType.PILOT_PREFILED) {
            if (!s.isEmpty()) {
                throw new IllegalArgumentException("prefiled flight plans are not expected to indicate any controller rating but rating is \""+s+"\"");
            }
            
            return null;
        }

        ControllerRating rating = ControllerRating.resolveStatusFileId(Integer.parseInt(s));
        
        if ((clientType == ClientType.PILOT_CONNECTED) && (rating != ControllerRating.OBS)) {
            throw new IllegalArgumentException("connected pilots are not expected to indicate any controller rating except observer/pilot but actual rating is \""+s+"\"");
        }
        
        return rating;
    }

    /**
     * Parses the given string as a transponder code in numeric decimal
     * representation.
     * <p>
     * Only connected pilots are allowed to set a transponder code. Having a
     * transponder code while being connected is not mandatory.
     * </p>
     * <p>
     * ATC or prefiled flight plans are never allowed to set a transponder code
     * and thus return a negative value if not set and throw an
     * {@link IllegalArgumentException} if set.
     * </p>
     * @param s string to parse
     * @param clientType type of client
     * @return positive transponder code in decimal numeric representation; negative value if unavailable
     * @throws IllegalArgumentException if set although not allowed or parsing error
     */
    private int parseTransponderCodeDecimal(String s, ClientType clientType) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return -1;
        }
        
        if (clientType == ClientType.PILOT_CONNECTED) {
            return Integer.parseInt(s);
        } else {
            throw new IllegalArgumentException("Only connected pilots are allowed to list a transponder code but code was: \""+s+"\"");
        }
    }
    
    /**
     * Parses the given string to a facility type.
     * <p>
     * Facility types are only available to ATC.
     * {@link IllegalArgumentException} will be thrown if the type ID is unknown
     * or a non-ATC client (pilot/flight plan) attempts to define a facility type.
     * </p>
     * <p>
     * Returns null if undefined.
     * </p>
     * @param s string to parse
     * @param clientType type of client
     * @return facility type; null if unavailable
     * @throws IllegalArgumentException if set although not allowed, unknown ID or parsing error
     */
    private FacilityType parseFacilityType(String s, ClientType clientType) throws IllegalArgumentException {
        boolean isATC = (clientType == ClientType.ATC_CONNECTED);
        
        if (s.isEmpty()) {
            return null;
        }
        
        if (isATC) {
            return FacilityType.resolveStatusFileId(Integer.parseInt(s));
        } else {
            throw new IllegalArgumentException("Only ATC stations are allowed to list a facility type but type was: \""+s+"\"");
        }
    }

    /**
     * Parses the given string to a visual range.
     * <p>
     * Visual range field is only available to ATC but not mandatory.
     * {@link IllegalArgumentException} will be thrown if invalid
     * or a non-ATC client (pilot/flight plan) attempts to define a visual range.
     * </p>
     * <p>
     * Returns negative value if undefined.
     * </p>
     * @param s string to parse
     * @param clientType type of client
     * @return visual range; negative if unavailable
     * @throws IllegalArgumentException if set although not allowed or parsing error
     */
    private int parseVisualRange(String s, ClientType clientType) throws IllegalArgumentException {
        boolean isATC = (clientType == ClientType.ATC_CONNECTED);
        
        if (s.isEmpty()) {
            return -1;
        }
        
        if (isATC) {
            return Integer.parseInt(s);
        } else {
            throw new IllegalArgumentException("Only ATC stations are allowed to indicate a visual range; found: \""+s+"\"");
        }
    }

    /**
     * Parses the given string to a flight plan revision.
     * <p>
     * Flight plan revision is mandatory for prefiled flight plans.
     * </p>
     * <p>
     * Returns negative value if undefined.
     * </p>
     * @param s string to parse
     * @param clientType type of client
     * @return flight plan revision; negative if unavailable
     * @throws IllegalArgumentException if missing although mandatory or parsing error
     */
    private int parseFlightPlanRevision(String s, ClientType clientType) throws IllegalArgumentException {
        boolean isPrefiling = (clientType == ClientType.PILOT_PREFILED);
        
        if (s.isEmpty()) {
            if (isPrefiling) {
                throw new IllegalArgumentException("flight plan was prefiled but is missing revision");
            }
            
            return -1;
        }
        
        return Integer.parseInt(s);
    }
    
    /**
     * Attempts to parse the given string as {@link LocalTime} in format "hhmm"
     * with optional leading zeros.
     * Null will be returned if string was either empty or if it does not match
     * the expected time format (thus graceful instead of throwing an exception).
     * @param s string to attempt parsing
     * @return {@link LocalTime} if parsed; null if unavailable or invalid
     * @throws IllegalArgumentException if string is not a number
     */
    private LocalTime parseLocalTimeGraceful(String s) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return null;
        }
        
        int decimal = Integer.parseInt(s);
        
        boolean isExcessive = decimal > 2359;
        if (isExcessive) {
            return null;
        }
        
        String padded = String.format("%04d", decimal);
        
        try {
            LocalTime parsed = LocalTime.parse(padded, LOCAL_TIME_FORMATTER);
            return parsed;
        } catch (DateTimeParseException ex) {
            // we know that some data is just garbage we need to tolerate, so
            // no need to throw/log the resulting exception...
            return null;
        }
    }
}
