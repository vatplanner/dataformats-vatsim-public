package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

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
    //    1       2      3       4            5                                6                                7
        "([^:]+):(\\d+|):([^:]*):(PILOT|ATC|):("+SUBPATTERN_FLOAT_UNSIGNED+"|):("+SUBPATTERN_GEOCOORDINATES+"|):("+SUBPATTERN_GEOCOORDINATES+"|):"+
    //    8           9       10      11      12      13      14      15      16      17
        "(\\-?\\d+|):(\\d+|):([^:]*):(\\d+|):([^:]*):([^:]*):([^:]*):([^:]*):(\\d+|):(\\d+|):"+
    //    18      19      20      21      22      23      24      25      26      27      28
        "(\\d+|):(\\d+|):(\\d+|):(\\d+|):([^:]*):(\\d+|):(\\d+|):(\\d+|):(\\d+|):(\\d+|):(\\d+|):"+
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
        boolean isAllowedToServeFrequency = (clientType == ClientType.ATC_CONNECTED);
        
        client.setCallsign(matcher.group(PATTERN_LINE_CALLSIGN));
        client.setVatsimID(parseIntWithDefault(matcher.group(PATTERN_LINE_CID), -1)); // TODO: log details if ID is missing
        client.setRealName(matcher.group(PATTERN_LINE_REALNAME));
        client.setServedFrequencyKilohertz(parseServedFrequencyMegahertzToKilohertz(matcher.group(PATTERN_LINE_FREQUENCY), isAllowedToServeFrequency));
        client.setLatitude(parseOnlineGeoCoordinate(matcher.group(PATTERN_LINE_LATITUDE), isOnline));
        client.setLongitude(parseOnlineGeoCoordinate(matcher.group(PATTERN_LINE_LONGITUDE), isOnline));
        client.setAltitudeFeet(parseOnlineAltitude(matcher.group(PATTERN_LINE_ALTITUDE), isOnline));
        
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
    
}
