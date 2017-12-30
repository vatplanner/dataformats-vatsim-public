package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Parses information about VATSIM voice servers as available from VATSIM's
 * data.txt status files in section <code>VOICE SERVERS</code>.
 * Usually, {@link DataFileParser} should be used to have this information
 * parsed from an actual complete {@link DataFile}.
 */
public class VoiceServerParser {
    /**
     * Parses all information from the given line to a {@link VoiceServer} object.
     * The line is expected to contain the proper syntax used by VATSIM data.txt
     * files and not to be empty or a comment.
     * @param line line to be parsed; must not be empty or a comment
     * @return all parsed data in a {@link VoiceServer} object
     */
    public VoiceServer parse(String line) {
        return null;
    }
}
