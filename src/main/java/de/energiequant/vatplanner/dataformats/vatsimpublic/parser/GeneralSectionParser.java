package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.util.Collection;

/**
 * Parses meta information about a VATSIM data.txt status file
 * as available from section <code>GENERAL</code>.
 * Usually, {@link DataFileParser} should be used to have this information
 * parsed from an actual complete {@link DataFile}.
 */
public class GeneralSectionParser {
    /**
     * Parses all information from the given lines to a {@link DataFileMetaData}
     * object.
     * All lines are expected to contain the proper syntax used by VATSIM
     * data.txt files and not to be empty or a comment.
     * @param lines lines to be parsed; lines must not be empty, comments or null
     * @return all parsed data in a {@link DataFileMetaData} object
     */
    public DataFileMetaData parse(Collection<String> lines) {
        // TODO: implement
        return null;
    }
}
