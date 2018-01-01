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
        
        client.setCallsign(matcher.group(PATTERN_LINE_CALLSIGN));
        client.setVatsimID(parseIntWithDefault(matcher.group(PATTERN_LINE_CID), -1)); // TODO: log details if ID is missing
        client.setRealName(matcher.group(PATTERN_LINE_REALNAME));
        client.setClientType(parseClientType(matcher.group(PATTERN_LINE_CLIENTTYPE)));
        
        // TODO: post-processing should check for client type null and guess from other available data what this line was describing (ATC, connected pilot or prefiling)
        
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
}
