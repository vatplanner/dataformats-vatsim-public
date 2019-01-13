package org.vatplanner.dataformats.vatsimpublic.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses information about VATSIM voice servers as available from VATSIM's
 * data.txt status files in section <code>VOICE SERVERS</code>. Usually,
 * {@link DataFileParser} should be used to have this information parsed from an
 * actual complete {@link DataFile}.
 */
public class VoiceServerParser {

    private static final Pattern PATTERN_LINE = Pattern.compile("([^:]+):([^:]+):([^:]+):([01]):(([^:]*):|)");
    private static final int PATTERN_LINE_HOSTNAME_OR_IP = 1;
    private static final int PATTERN_LINE_LOCATION = 2;
    private static final int PATTERN_LINE_NAME = 3;
    private static final int PATTERN_LINE_CLIENTS_CONNECTION_ALLOWED = 4;
    private static final int PATTERN_LINE_TYPE_OF_VOICE_SERVER = 6;

    private static final String CLIENTS_CONNECTION_ALLOWED_TRUE = "1";

    /**
     * Parses all information from the given line to a {@link VoiceServer}
     * object. The line is expected to contain the proper syntax used by VATSIM
     * data.txt files and not to be empty or a comment.
     *
     * @param line line to be parsed; must not be empty or a comment
     * @return all parsed data in a {@link VoiceServer} object
     */
    public VoiceServer parse(String line) {
        Matcher matcher = PATTERN_LINE.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("unparseable line: \"" + line + "\"");
        }

        VoiceServer server = new VoiceServer();

        server.setAddress(matcher.group(PATTERN_LINE_HOSTNAME_OR_IP));
        server.setLocation(matcher.group(PATTERN_LINE_LOCATION));
        server.setName(matcher.group(PATTERN_LINE_NAME));

        // NOTE: Actually, this could be a misinterpretation - instead of resembling a boolean this "flag" could also mean a minimum permission level as described by ControllerRating enum which coincides for 1 meaning OBS, the lowest (pilot-accessible) level. I haven't encountered any value other than 1 yet so I can only guess that 0 will mean "no" but have no clue about other possible values. Same for FSDServer. Extend if necessary.
        String flagClientsConnectionAllowed = matcher.group(PATTERN_LINE_CLIENTS_CONNECTION_ALLOWED);
        server.setClientConnectionAllowed(CLIENTS_CONNECTION_ALLOWED_TRUE.equals(flagClientsConnectionAllowed));

        server.setRawServerType(matcher.group(PATTERN_LINE_TYPE_OF_VOICE_SERVER));

        return server;
    }

}
