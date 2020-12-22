package org.vatplanner.dataformats.vatsimpublic.parser.legacy;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;

/**
 * Parses {@link Client} information as given by VATSIM's data.txt status files
 * in sections <code>CLIENTS</code> and <code>PREFILE</code>. Both sections
 * share the same syntax, making it possible to use the same parser
 * implementation and entities. For more information see {@link Client} class
 * documentation. Usually, {@link DataFileParser} should be used to have this
 * information parsed from an actual complete {@link DataFile}.
 */
public class ClientParser {

    private static final String SUBPATTERN_TIMESTAMP = "\\d{14}";
    private static final String SUBPATTERN_FLOAT_UNSIGNED = "\\d+(?:\\.\\d+|)(?:[eE][\\-+]?\\d+|)"; // integers are
                                                                                                    // valid as well
    private static final String SUBPATTERN_GEOCOORDINATES = "\\-?" + SUBPATTERN_FLOAT_UNSIGNED;

    /*
     * TODO: test for floating numbers like 7.62939e-08 as some pilot client submits
     * such numbers...
     */
    // TODO: airport lat/lon should be geocoordinates subpattern with optional 0
    // @formatter:off
    private static final Pattern PATTERN_LINE = Pattern.compile(
            //   1       2       3       4             5                                    6                                    7
            "([^:]+):(\\d+|):([^:]*):(PILOT|ATC|):(" + SUBPATTERN_FLOAT_UNSIGNED + "|):(" + SUBPATTERN_GEOCOORDINATES + "|):(" + SUBPATTERN_GEOCOORDINATES + "|):"
            + // 8         9       10      11      12      13      14      15      16      17
            "(\\-?\\d+|):(\\d+|):([^:]*):(\\d+|):([^:]*):([^:]*):([^:]*):([^:]*):(\\d+|):(\\d+|):"
            + // 18    19      20      21      22      23      24      25          26          27          28
            "(\\d*):(\\d+|):(\\d+|):(\\d+|):([^:]*):(\\d+|):(\\d+|):(\\-?\\d+|):(\\-?\\d+|):(\\-?\\d+|):(\\-?\\d+|):"
            + // 29    30      31         32                                   33
            "([^:]*):([^:]*):([^:]*):(" + SUBPATTERN_GEOCOORDINATES + "|):(" + SUBPATTERN_GEOCOORDINATES + "|):"
            + //  34                                   35
            "(" + SUBPATTERN_GEOCOORDINATES + "|):(" + SUBPATTERN_GEOCOORDINATES + "|):"
            + //36     37                              38                            39
            "(.*):(" + SUBPATTERN_TIMESTAMP + "|):(" + SUBPATTERN_TIMESTAMP + "|):(\\d+|):"
            + //      40                                41
            "(\\-?" + SUBPATTERN_FLOAT_UNSIGNED + "|):(\\-?\\d+|):");
    // @formatter:on

    /*
     * TODO: allowed ATIS message to contain ":" in text as seen in the wild... may
     * cause issues, keep an eye on it
     */
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
    private static final int PATTERN_LINE_PLANNED_REVISION = 21;
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
    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String CONTROLLER_MESSAGE_LINEBREAK = new String( //
        new byte[] { (byte) 0x5E, (byte) 0xA7 },
        Charset.forName("ISO-8859-1") //
    );
    private static final String LINEBREAK = "\n";

    private static final String DUMMY_TIMESTAMP = "00010101000000";

    private boolean isParsingPrefileSection = false;

    /**
     * Configures the parser to treat all following lines as belonging to either the
     * <code>CLIENTS</code> or <code>PREFILE</code> section.
     *
     * @param isParsingPrefileSection Are following lines to be interpreted as
     *        belonging to <code>PREFILE</code> section? Set true before parsing
     *        lines from <code>PREFILE</code> and false before parsing lines from
     *        <code>CLIENTS</code> section.
     * @return this {@link ClientParser} instance (for method chaining)
     */
    public ClientParser setIsParsingPrefileSection(boolean isParsingPrefileSection) {
        this.isParsingPrefileSection = isParsingPrefileSection;
        return this;
    }

    /**
     * Parses all information from the given line to a {@link Client} object. The
     * line is expected to contain the proper syntax used by VATSIM data.txt files
     * and not to be empty or a comment.
     *
     * @param line line to be parsed; must not be empty or a comment
     * @return all parsed data in a {@link Client} object
     * @throws IllegalArgumentException if parsing for given line fails
     */
    public Client parse(String line) throws IllegalArgumentException {
        Matcher matcher = PATTERN_LINE.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("unparseable line, does not match expected syntax: \"" + line + "\"");
        }

        Client client = new Client();

        try {
            ClientType rawClientType = parseRawClientType(matcher.group(PATTERN_LINE_CLIENTTYPE));
            client.setRawClientType(rawClientType);

            ClientType effectiveClientType = guessClientType(matcher, rawClientType);
            client.setEffectiveClientType(effectiveClientType);

            boolean isRawClientTypeOnline = isOnlineClientType(rawClientType);
            boolean isEffectiveClientTypeOnline = isOnlineClientType(effectiveClientType);
            boolean hasChangedOnlineStateByGuessing = (isRawClientTypeOnline != isEffectiveClientTypeOnline);

            boolean isATC = (effectiveClientType == ClientType.ATC_CONNECTED);
            boolean isPrefiling = (effectiveClientType == ClientType.PILOT_PREFILED);
            boolean isConnectedPilot = (effectiveClientType == ClientType.PILOT_CONNECTED);
            boolean isAllowedToServeFrequency = isATC;
            // boolean isAllowedToHaveFlightPlan = !isATC;
            boolean isFiledTimeMandatory = isPrefiling;

            // TODO: log details if VATSIM ID is missing
            client.setCallsign(matcher.group(PATTERN_LINE_CALLSIGN));
            client.setVatsimID(parseIntWithDefault(matcher.group(PATTERN_LINE_CID), -1));
            client.setRealName(matcher.group(PATTERN_LINE_REALNAME));
            client.setServedFrequencyKilohertz( //
                parseServedFrequencyMegahertzToKilohertz( //
                    matcher.group(PATTERN_LINE_FREQUENCY), isAllowedToServeFrequency //
                ) //
            );
            client.setLatitude( //
                parseOnlineGeoCoordinate( //
                    matcher.group(PATTERN_LINE_LATITUDE), isEffectiveClientTypeOnline //
                ) //
            );
            client.setLongitude( //
                parseOnlineGeoCoordinate( //
                    matcher.group(PATTERN_LINE_LONGITUDE), //
                    isEffectiveClientTypeOnline //
                ) //
            );
            client.setAltitudeFeet( //
                parseOnlineAltitude( //
                    matcher.group(PATTERN_LINE_ALTITUDE), isEffectiveClientTypeOnline //
                ) //
            );
            client.setGroundSpeed(parseGroundSpeed(matcher.group(PATTERN_LINE_GROUNDSPEED), effectiveClientType));
            client.setAircraftType(matcher.group(PATTERN_LINE_PLANNED_AIRCRAFT));
            client.setFiledTrueAirSpeed(parseIntWithDefault(matcher.group(PATTERN_LINE_PLANNED_TASCRUISE), 0));
            client.setFiledDepartureAirportCode(matcher.group(PATTERN_LINE_PLANNED_DEPAIRPORT));
            client.setRawFiledAltitude(matcher.group(PATTERN_LINE_PLANNED_ALTITUDE));
            client.setFiledDestinationAirportCode(matcher.group(PATTERN_LINE_PLANNED_DESTAIRPORT));
            client.setServerId( //
                filterServerId( //
                    matcher.group(PATTERN_LINE_SERVER), //
                    isEffectiveClientTypeOnline, //
                    hasChangedOnlineStateByGuessing //
                ) //
            );
            client.setProtocolVersion( //
                parseOnlineProtocolVersion( //
                    matcher.group(PATTERN_LINE_PROTREVISION), isEffectiveClientTypeOnline //
                ) //
            );
            client.setControllerRating(parseControllerRating(matcher.group(PATTERN_LINE_RATING), effectiveClientType));
            client.setTransponderCodeDecimal( //
                parseTransponderCodeDecimal( //
                    matcher.group(PATTERN_LINE_TRANSPONDER), effectiveClientType //
                ) //
            );
            client.setFacilityType(parseFacilityType(matcher.group(PATTERN_LINE_FACILITYTYPE), rawClientType));
            client.setVisualRange(parseVisualRange(matcher.group(PATTERN_LINE_VISUALRANGE), rawClientType));
            client.setFlightPlanRevision( //
                parseFlightPlanRevision( //
                    matcher.group(PATTERN_LINE_PLANNED_REVISION), effectiveClientType //
                ) //
            );
            client.setRawFlightPlanType(matcher.group(PATTERN_LINE_PLANNED_FLIGHTTYPE));
            client.setRawDepartureTimePlanned(parseIntWithDefault(matcher.group(PATTERN_LINE_PLANNED_DEPTIME), -1));
            client.setRawDepartureTimeActual(parseIntWithDefault(matcher.group(PATTERN_LINE_PLANNED_ACTDEPTIME), -1));
            client.setFiledTimeEnroute( //
                parseDuration( //
                    matcher.group(PATTERN_LINE_PLANNED_HRSENROUTE), //
                    matcher.group(PATTERN_LINE_PLANNED_MINENROUTE), //
                    isFiledTimeMandatory //
                ) //
            );
            client.setFiledTimeFuel( //
                parseDuration( //
                    matcher.group(PATTERN_LINE_PLANNED_HRSFUEL), //
                    matcher.group(PATTERN_LINE_PLANNED_MINFUEL), //
                    isFiledTimeMandatory //
                ) //
            );
            client.setFiledAlternateAirportCode(matcher.group(PATTERN_LINE_PLANNED_ALTAIRPORT));
            client.setFlightPlanRemarks(matcher.group(PATTERN_LINE_PLANNED_REMARKS));
            client.setFiledRoute(matcher.group(PATTERN_LINE_PLANNED_ROUTE));
            client.setDepartureAirportLatitude(parseGeoCoordinate(matcher.group(PATTERN_LINE_PLANNED_DEPAIRPORT_LAT)));
            client.setDepartureAirportLongitude(parseGeoCoordinate(matcher.group(PATTERN_LINE_PLANNED_DEPAIRPORT_LON)));
            client.setDestinationAirportLatitude( //
                parseGeoCoordinate( //
                    matcher.group(PATTERN_LINE_PLANNED_DESTAIRPORT_LAT) //
                ) //
            );
            client.setDestinationAirportLongitude( //
                parseGeoCoordinate( //
                    matcher.group(PATTERN_LINE_PLANNED_DESTAIRPORT_LON) //
                ) //
            );
            client.setControllerMessage(decodeControllerMessage(matcher.group(PATTERN_LINE_ATIS_MESSAGE), isATC));

            Instant lastAtisReceived = parseFullTimestamp( //
                matcher.group(PATTERN_LINE_TIME_LAST_ATIS_RECEIVED), !isPrefiling //
            );
            if (!isATC) {
                lastAtisReceived = null;
            }
            client.setControllerMessageLastUpdated(lastAtisReceived);

            client.setLogonTime( //
                requireNonNullIf( //
                    "logon time", //
                    isEffectiveClientTypeOnline,
                    parseFullTimestamp( //
                        matcher.group(PATTERN_LINE_TIME_LOGON), isEffectiveClientTypeOnline //
                    ) //
                ) //
            );
            client.setHeading(parseHeading(matcher.group(PATTERN_LINE_HEADING), effectiveClientType));
            client.setQnhInchMercury( //
                requireNaNIf( //
                    "QNH Inch Mercury",
                    !(isConnectedPilot || isZeroOrEmpty(matcher.group(PATTERN_LINE_QNH_IHG))), //
                    parseDouble(matcher.group(PATTERN_LINE_QNH_IHG))) //
            );

            int qnhHectopascals = parseIntWithDefault(matcher.group(PATTERN_LINE_QNH_MB), -1);
            if (!isConnectedPilot) {
                qnhHectopascals = -1;
            }
            client.setQnhHectopascal(qnhHectopascals);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                "unparseable line in "
                    + (isParsingPrefileSection ? "preflight" : "online client")
                    + " section, error while parsing individual fields: \""
                    + line
                    + "\"",
                ex);
        }

        return client;
    }

    private static boolean isOnlineClientType(ClientType effectiveClientType) {
        return (effectiveClientType != null) && (effectiveClientType != ClientType.PILOT_PREFILED);
    }

    /**
     * Parses the given string to a {@link Double}. Returns {@link Double#NaN} if
     * the string is empty.
     *
     * @param s string to be parsed
     * @return value of string as {@link Double}
     * @throws IllegalArgumentException on parsing error
     */
    private double parseDouble(String s) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return Double.NaN;
        }

        return Double.parseDouble(s);
    }

    private int parseIntWithDefault(String s, int defaultOnError) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return defaultOnError;
        }
    }

    /**
     * Parses the raw client type as specified by given String in given context.
     * Returns null for invalid client types.
     *
     * @param s string to be parsed to client type
     * @return parsed raw client type; null if invalid
     */
    private ClientType parseRawClientType(String s) {
        if (!isParsingPrefileSection) {
            if (CLIENT_TYPE_PILOT.equals(s)) {
                return ClientType.PILOT_CONNECTED;
            } else if (CLIENT_TYPE_ATC.equals(s)) {
                return ClientType.ATC_CONNECTED;
            }
        } else if (s.isEmpty()) {
            return ClientType.PILOT_PREFILED;
        }

        return null;
    }

    /**
     * Guesses client type from available data of currently parsed line. This is
     * necessary for some lines to be processed because
     * <ul>
     * <li>some clients (due to sim crashes?) do not specify a client type although
     * being listed in online section of data file</li>
     * <li>ATC-logged clients may actually be flying as pilots (commonly observed
     * with {@link ControllerRating#OBS} and {@link ControllerRating#SUP}) which
     * must be - surprisingly - a permitted (or at least unprevented) way of
     * connecting to the network.</li>
     * </ul>
     *
     * @param matcher matcher retrieved via {@link #PATTERN_LINE}, containing all
     *        field information in matcher groups
     * @param rawClientType raw client type as available from data file
     * @return most-likely client type, null if no decision could be made
     */
    private ClientType guessClientType(Matcher matcher, ClientType rawClientType) {
        // TODO: only ATC may define a frequency
        // TODO: only online pilots have GS >0 (implement only if necessary)

        if (!isParsingPrefileSection) {
            boolean hasAtLeastOneFilledPilotField = !( //
            isZeroOrEmpty(matcher.group(PATTERN_LINE_HEADING))
                && isZeroOrEmpty(matcher.group(PATTERN_LINE_GROUNDSPEED))
                && isZeroOrEmpty(matcher.group(PATTERN_LINE_QNH_IHG))
                && isZeroOrEmpty(matcher.group(PATTERN_LINE_QNH_MB))
                && isZeroOrEmpty(matcher.group(PATTERN_LINE_TRANSPONDER)) //
            );

            if (hasAtLeastOneFilledPilotField) {
                return ClientType.PILOT_CONNECTED;
            }
        }

        if (rawClientType != null) {
            return rawClientType;
        }

        return null;
    }

    private boolean isZeroOrEmpty(String s) {
        return s.isEmpty() || "0".equals(s);
    }

    /**
     * Parses the given frequency assumed to be a floating number in MHz to an
     * integer describing the frequency in kHz. Returned value will be negative if
     * no frequency is provided. If serving is not allowed but a served
     * (non-placeholder) frequency is still being provided, an
     * {@link IllegalArgumentException} will be thrown. An
     * {@link IllegalArgumentException} will also be thrown if the specified
     * frequency does not make any sense, for example if it is negative or zero.
     *
     * @param s string to be parsed, formatted as floating number in MHz
     * @param allowServing Is serving a frequency allowed?
     * @return frequency in kHz (may be an unserved placeholder frequency)
     * @throws IllegalArgumentException if serving a frequency while not allowed or
     *         frequency does not make any sense
     */
    private int parseServedFrequencyMegahertzToKilohertz(String s, boolean allowServing) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return -1;
        } else {
            int frequencyKilohertz = (int) Math.round(Double.parseDouble(s) * 1000.0);

            if (frequencyKilohertz <= 0) {
                throw new IllegalArgumentException(
                    "served frequency is given as \"" + s + "\" which does not make any sense" //
                );
            }

            boolean isServedFrequency = frequencyKilohertz < Client.FREQUENCY_KILOHERTZ_PLACEHOLDER_MINIMUM;
            if (isServedFrequency && !allowServing) {
                throw new IllegalArgumentException(
                    "serving a frequency is not allowed but still encountered \"" + s + "\" as being served by client" //
                );
            }

            return frequencyKilohertz;
        }
    }

    /**
     * Parses a geo coordinate from the given string. Result of {@link Double#NaN}
     * indicates that no coordinate was available (empty string). Offline (prefiled)
     * clients are not permitted to define a coordinate; only empty strings are
     * allowed in that case, any other input will throw an
     * {@link IllegalArgumentException}.
     *
     * @param s string to be parsed
     * @param isOnline Is the client we are parsing for online?
     * @return geo coordinate or {@link Double#NaN} if not available
     * @throws IllegalArgumentException if client is not online but still provides a
     *         geo coordinate
     */
    private double parseOnlineGeoCoordinate(String s, boolean isOnline) throws IllegalArgumentException {
        if (isOnline) {
            return s.isEmpty() ? Double.NaN : Double.parseDouble(s);
        } else if (isZeroOrEmpty(s)) {
            return Double.NaN;
        } else {
            throw new IllegalArgumentException(
                "client is not online but still provides a geo coordinate (latitude/longitude)" //
            );
        }
    }

    /**
     * Parses a geo coordinate from the given string. Result of {@link Double#NaN}
     * indicates that no coordinate was available (empty string).
     *
     * @param s string to be parsed
     * @return geo coordinate or {@link Double#NaN} if not available
     */
    private double parseGeoCoordinate(String s) {
        if (s.isEmpty()) {
            return Double.NaN;
        }

        return Double.parseDouble(s);
    }

    /**
     * Parses an altitude from the given string specified integer format. If no
     * value can be parsed (including parsing errors), {@link #DEFAULT_ALTITUDE}
     * will be assumed. If the client is not online but parsing still reveals an
     * altitude other than {@link #DEFAULT_ALTITUDE} an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param s string to be parsed, plain integer
     * @param isOnline Is the client we are parsing for online?
     * @return altitude; {@link #DEFAULT_ALTITUDE} if not specified or number
     *         parsing error
     * @throws IllegalArgumentException if client is not online but still defines an
     *         altitude other than our default value
     */
    private int parseOnlineAltitude(String s, boolean isOnline) throws IllegalArgumentException {
        int altitude = parseIntWithDefault(s, DEFAULT_ALTITUDE);

        if (!isOnline && altitude != DEFAULT_ALTITUDE) {
            throw new IllegalArgumentException(
                "client is not online (prefiled flight plan?) but still defines altitude \"" + s + "\"" //
            );
        }

        return altitude;
    }

    /**
     * Parses ground speed from given string, taking into account client type for
     * validation.
     * <p>
     * Only pilots connected to the network are supposed to be moving and thus
     * indicate a ground speed. Prefiled flight plans and ATC are not allowed to
     * have a GS > 0 and throw an {@link IllegalArgumentException} on violation.
     * </p>
     * <p>
     * Only positive values make sense. Unknown speeds (including ATC & prefiles
     * indicating 0) will return a negative value.
     * </p>
     *
     * @param s string to parse
     * @param clientType type of client the string belongs to
     * @return ground speed >= 0 for connected pilots; negative value if
     *         unspecified, only prefiled or ATC
     * @throws IllegalArgumentException if client is not a connected pilot but still
     *         indicates movement
     */
    private int parseGroundSpeed(String s, ClientType clientType) throws IllegalArgumentException {
        int groundSpeed = parseIntWithDefault(s, -1);

        if (clientType == ClientType.PILOT_CONNECTED) {
            return groundSpeed;
        } else if (groundSpeed > 0) {
            throw new IllegalArgumentException(
                clientType.name() + " must not have a ground speed greater zero (was: \"" + s + "\")" //
            );
        }

        return -1;
    }

    /**
     * If filling flight plan fields is not allowed, value is returned null if
     * empty. Non-empty values will throw an {@link IllegalArgumentException} if not
     * permitted. Returns verbatim value if filling flight plan fields is allowed.
     *
     * @param flightPlanValue value of flight plan field to filter
     * @param isAllowedToFillFlightPlan Is client permitted to fill this flight plan
     *        field?
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
            throw new IllegalArgumentException(
                "client is not permitted to fill flight plan fields but shows \"" + flightPlanValue + "\"" //
            );
        }
    }

    /**
     * Checks if definition of server ID matches online state of client and returns
     * only valid values.
     * <p>
     * Only if a client is online, it is expected to have a server ID assigned.
     * Likewise, a client who is offline (prefiled flight plan) is expected not to
     * have any server ID as it is not connected to the network.
     * </p>
     * <p>
     * If expectation does not match, {@link IllegalArgumentException} will be
     * thrown.
     * </p>
     * <p>
     * If expectation matches, null will be returned if client is offline, otherwise
     * the original server ID will be returned.
     * </p>
     * <p>
     * Expectation is not checked if online state has been changed by guessing an
     * effective client type which is different from raw client type. As a result,
     * server ID may not be available although client has been interpreted to be
     * online. See {@link Client#getEffectiveClientType()} for an explanation of
     * when such guessing may occur.
     * </p>
     *
     * @param serverId server ID
     * @param isEffectiveClientTypeOnline Is the client online by effective client
     *        type?
     * @param hasChangedOnlineStateByGuessing Did online state change by guessing of
     *        effective client type?
     * @return server ID if expectation matches, null for offline clients or if not
     *         set
     * @throws IllegalArgumentException if expectation of server ID is violated
     */
    private String filterServerId(String serverId, boolean isEffectiveClientTypeOnline, boolean hasChangedOnlineStateByGuessing) throws IllegalArgumentException {
        boolean hasNoServerId = serverId.isEmpty();

        boolean availabilityMatchesOnlineState = isEffectiveClientTypeOnline ^ hasNoServerId;
        if (!availabilityMatchesOnlineState && !hasChangedOnlineStateByGuessing) {
            throw new IllegalArgumentException(
                "client is "
                    + (isEffectiveClientTypeOnline ? "" : "not ")
                    + "online but has "
                    + (hasNoServerId ? "no" : "a")
                    + " server ID assigned: \""
                    + serverId
                    + "\"" //
            );
        }

        if (!isEffectiveClientTypeOnline || serverId.isEmpty()) {
            return null;
        }

        return serverId;
    }

    /**
     * Parses and checks if definition of protocol version matches online state of
     * client and returns only valid values.
     * <p>
     * Only if a client is online, it may indicate a protocol version. A client who
     * is offline (prefiled flight plan) is expected not to have any protocol
     * version as it is not connected to the network.
     * </p>
     * <p>
     * If expectation does not match, {@link IllegalArgumentException} will be
     * thrown.
     * </p>
     * <p>
     * If expectation matches, negative value will be returned if client is offline,
     * otherwise the parsed value will be returned.
     * </p>
     *
     * @param s protocol version as string
     * @param isOnline Is the client online?
     * @return protocol version number if expectation matches, negative number for
     *         offline clients
     * @throws IllegalArgumentException if expectation of server ID is violated or
     *         error occurs while parsing the value
     */
    private int parseOnlineProtocolVersion(String s, boolean isOnline) throws IllegalArgumentException {
        boolean hasEmptyOrZeroProtocolVersion = isZeroOrEmpty(s);

        boolean availabilityMatchesOnlineState = isOnline || hasEmptyOrZeroProtocolVersion;
        if (!availabilityMatchesOnlineState) {
            throw new IllegalArgumentException(
                "client is "
                    + (isOnline ? "" : "not ")
                    + "online but indicates "
                    + (hasEmptyOrZeroProtocolVersion ? "no" : "a")
                    + " non-zero protocol revision: \""
                    + s
                    + "\"");
        }

        if (!isOnline) {
            return -1;
        }

        return parseIntWithDefault(s, -1);
    }

    /**
     * Parses the given string to a controller rating. If the rating does not match
     * expectations for the selected client type an {@link IllegalArgumentException}
     * will be thrown. Only {@link ClientType#PILOT_PREFILED} is allowed not to
     * specify any rating, so only prefilings can return null.
     *
     * @param s string to be parsed into controller rating
     * @param clientType session client type
     * @return controller rating; null on prefiling
     * @throws IllegalArgumentException if specified rating does not match
     *         expectations for client type
     */
    private ControllerRating parseControllerRating(String s, ClientType clientType) throws IllegalArgumentException {
        if (clientType == ClientType.PILOT_PREFILED) {
            if (!isZeroOrEmpty(s)) {
                throw new IllegalArgumentException(
                    "prefiled flight plans are not expected to indicate any controller rating but rating is \"" //
                        + s + "\"" //
                );
            }

            return null;
        }

        ControllerRating rating = ControllerRating.resolveStatusFileId(Integer.parseInt(s));

        if ((clientType == ClientType.PILOT_CONNECTED) && (rating != ControllerRating.OBS)) {
            throw new IllegalArgumentException(
                "connected pilots are not expected to indicate any controller rating except observer/pilot but actual rating is \""
                    + s + "\"" //
            );
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
     * ATC or prefiled flight plans are never allowed to set a transponder code and
     * thus return a negative value if not set and throw an
     * {@link IllegalArgumentException} if set.
     * </p>
     *
     * @param s string to parse
     * @param clientType type of client
     * @return positive transponder code in decimal numeric representation; negative
     *         value if unavailable
     * @throws IllegalArgumentException if set although not allowed or parsing error
     */
    private int parseTransponderCodeDecimal(String s, ClientType clientType) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return -1;
        }

        if ((clientType != ClientType.PILOT_CONNECTED) && !"0".equals(s)) {
            throw new IllegalArgumentException(
                "Only connected pilots are allowed to list a transponder code but code was: \"" + s + "\"" //
            );
        }

        return Integer.parseInt(s);
    }

    /**
     * Parses the given string to a facility type.
     * <p>
     * Facility types are only available to clients logged in as ATC (given by raw
     * client type). {@link IllegalArgumentException} will be thrown if the type ID
     * is unknown or a non-ATC client (pilot/flight plan) attempts to define a
     * facility type.
     * </p>
     * <p>
     * Returns null if undefined.
     * </p>
     *
     * @param s string to parse
     * @param rawClientType raw type of client, must not be effective type
     * @return facility type; null if unavailable
     * @throws IllegalArgumentException if set although not allowed, unknown ID or
     *         parsing error
     */
    private FacilityType parseFacilityType(String s, ClientType rawClientType) throws IllegalArgumentException {
        boolean isATC = (rawClientType == ClientType.ATC_CONNECTED);

        if (isATC) {
            return s.isEmpty() ? null : FacilityType.resolveStatusFileId(Integer.parseInt(s));
        } else if (isZeroOrEmpty(s)) {
            return null;
        } else {
            throw new IllegalArgumentException(
                "Only ATC stations are allowed to list a facility type but type was: \"" + s + "\"" //
            );
        }
    }

    /**
     * Parses the given string to a visual range.
     * <p>
     * Visual range field is only available to clients logged in as ATC (given by
     * raw client type). The field is not mandatory.
     * {@link IllegalArgumentException} will be thrown if invalid or a prefiled
     * flight plan attempts to define a visual range.
     * </p>
     * <p>
     * Returns negative value if undefined. Visual ranges defined for connected
     * pilots are ignored and a negative value is returned instead.
     * </p>
     *
     * @param s string to parse
     * @param rawClientType raw type of client, must not be effective type
     * @return visual range; negative if unavailable
     * @throws IllegalArgumentException if set although not allowed or parsing error
     */
    private int parseVisualRange(String s, ClientType rawClientType) throws IllegalArgumentException {
        boolean isATC = (rawClientType == ClientType.ATC_CONNECTED);
        boolean isConnectedPilot = (rawClientType == ClientType.PILOT_CONNECTED);

        if (isATC) {
            return s.isEmpty() ? -1 : Integer.parseInt(s);
        } else if (isConnectedPilot || isZeroOrEmpty(s)) {
            return -1;
        } else {
            throw new IllegalArgumentException(
                "Prefilings are not allowed to indicate a visual range; found: \"" + s + "\"" //
            );
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
     *
     * @param s string to parse
     * @param clientType type of client
     * @return flight plan revision; negative if unavailable
     * @throws IllegalArgumentException if missing although mandatory or parsing
     *         error
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
     * Attempts to parse the given string as {@link LocalTime} in format "hhmm" with
     * optional leading zeros. Null will be returned if string was either empty or
     * if it does not match the expected time format (thus graceful instead of
     * throwing an exception).
     *
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

    /**
     * Parses the given strings for hours and minutes to a {@link Duration} object.
     * If both strings are empty, null is returned. If only one string is empty, an
     * {@link IllegalArgumentException} will be thrown. If duration is mandatory,
     * strings must not be empty or {@link IllegalArgumentException} will be thrown.
     * Excessive values for minutes (>59) are valid and add to hours.
     *
     * @param hoursString string containing hours to be parsed
     * @param minutesString string containing minutes to be parsed
     * @param isMandatory Is the duration mandatory?
     * @return strings interpreted as duration; null if unavailable
     * @throws IllegalArgumentException if mandatory but not available, only one
     *         string is empty or parsing error
     */
    private Duration parseDuration(String hoursString, String minutesString, boolean isMandatory) throws IllegalArgumentException {
        boolean emptyHours = hoursString.isEmpty();
        boolean emptyMinutes = minutesString.isEmpty();
        boolean oneEmptyButNotTheOther = emptyHours ^ emptyMinutes;

        if (oneEmptyButNotTheOther) {
            throw new IllegalArgumentException(
                "either hours (\""
                    + hoursString
                    + "\") or minutes (\""
                    + minutesString
                    + "\") was empty but not the other; such inconsistency is not allowed");
        }

        boolean bothEmpty = emptyHours && emptyMinutes;

        if (bothEmpty) {
            if (isMandatory) {
                throw new IllegalArgumentException("hours and minutes are mandatory but both strings were empty");
            }

            return null;
        }

        int hours = Integer.parseInt(hoursString);
        int minutes = Integer.parseInt(minutesString);

        // Unfortunately, negative values can be entered. If that happens, we
        // need to use consistently negative values for hours and minutes. A mix
        // of a different sign on one part of this calculation could cause
        // a positive result, making it impossible to filter out such nonsense
        // at a later stage of processing this information.
        if ((hours < 0) && (minutes > 0)) {
            minutes = -minutes;
        } else if ((hours > 0) && (minutes < 0)) {
            hours = -hours;
        }

        return Duration.ofMinutes(hours * 60 + minutes);
    }

    /**
     * Decodes a controller message from encoding used in data.txt to an easily
     * readable Java String. If messages are not allowed, anything but empty
     * messages will throw an {@link IllegalArgumentException}.
     *
     * @param s original representation as in data.txt file
     * @param allowMessage Is a non-empty message allowed?
     * @return controller message decoded to a Java String
     * @throws IllegalArgumentException if a message is not allowed but not empty
     */
    private String decodeControllerMessage(String s, boolean allowMessage) throws IllegalArgumentException {
        if (!allowMessage && !s.isEmpty()) {
            throw new IllegalArgumentException("controller message is not allowed but was: \"" + s + "\"");
        }

        s = s.replace(CONTROLLER_MESSAGE_LINEBREAK, LINEBREAK);

        // TODO: guess character set and recode
        return s;
    }

    /**
     * Parses a UTC timestamp in full date/time format to an {@link Instant}.
     * Returns null if not set. If no timestamp is allowed but still set, an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param s string to parse
     * @param isAllowed Is a timestamp allowed?
     * @return timestamp as {@link Instant} referenced to UTC
     * @throws IllegalArgumentException if not allowed but set or parsing error
     */
    private Instant parseFullTimestamp(String s, boolean isAllowed) throws IllegalArgumentException {
        if (isEmptyOrDummyTimestamp(s)) {
            return null;
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("timestamp is not allowed but was \"" + s + "\"");
        }

        return LocalDateTime.parse(s, LOCAL_DATE_TIME_FORMATTER).toInstant(ZoneOffset.UTC);
    }

    private boolean isEmptyOrDummyTimestamp(String s) {
        return s.isEmpty() || DUMMY_TIMESTAMP.equals(s);
    }

    /**
     * Requires the given object to be not null if condition is true.
     *
     * @param <T> class of object to be checked
     * @param description description of object value (used for exception message)
     * @param condition condition which has to be true for object to be required not
     *        to be null
     * @param obj object to be checked
     * @return original object
     * @throws IllegalArgumentException if condition is true and object is null
     */
    private <T> T requireNonNullIf(String description, boolean condition, T obj) throws IllegalArgumentException {
        if (condition && (obj == null)) {
            throw new IllegalArgumentException("expected " + description + " not to be null");
        }

        return obj;
    }

    /**
     * Parses the given string as a heading in degrees. Returns negative value if
     * not set.
     *
     * @param s string to be parsed
     * @param clientType client type string is to be parsed for
     * @return heading value; negative if not set
     * @throws IllegalArgumentException if not permitted but still defined or value
     *         is out of range
     */
    private int parseHeading(String s, ClientType clientType) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return -1;
        }

        boolean isAllowed = (clientType == ClientType.PILOT_CONNECTED) || "0".equals(s);
        if (!isAllowed) {
            throw new IllegalArgumentException("heading is only allowed to be set by connected pilots");
        }

        int heading = Integer.parseInt(s);

        if (heading == 360) {
            heading = 0;
        } else if (heading > 359) {
            throw new IllegalArgumentException("heading is out of range: \"" + s + "\"");
        }

        return heading;
    }

    /**
     * Requires the given value to be {@link Double#NaN} if condition is met.
     * Violation will cause {@link IllegalArgumentException} to be thrown.
     *
     * @param description description of value (used for exception message)
     * @param condition condition which has to be true for value to be required to
     *        be {@link Double#NaN}
     * @param value value to be checked
     * @return original value
     * @throws IllegalArgumentException if condition is true and value is not
     *         {@link Double#NaN}
     */
    private double requireNaNIf(String description, boolean condition, double value) throws IllegalArgumentException {
        if (condition && !Double.isNaN(value)) {
            throw new IllegalArgumentException(
                "expected "
                    + description
                    + " to be NaN but was "
                    + Double.toString(value) //
            );
        }

        return value;
    }

    /**
     * Requires the given value to be negative if condition is met. Violation will
     * cause {@link IllegalArgumentException} to be thrown.
     *
     * @param description description of value (used for exception message)
     * @param condition condition which has to be true for value to be required to
     *        be negative
     * @param value value to be checked
     * @return original value
     * @throws IllegalArgumentException if condition is true and value is not
     *         negative
     */
    private int requireNegativeIf(String description, boolean condition, int value) throws IllegalArgumentException {
        if (condition && (value >= 0)) {
            throw new IllegalArgumentException(
                "expected "
                    + description
                    + " to be negative but was "
                    + Integer.toString(value) //
            );
        }

        return value;
    }

    // TODO: remove unused methods
}
