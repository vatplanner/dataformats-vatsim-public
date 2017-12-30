package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

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
    /**
     * Configures the parser to treat all following lines as belonging to
     * either the <code>CLIENTS</code> or <code>PREFILE</code> section.
     * @param isParsingPrefileSection Are following lines to be interpreted as belonging to <code>PREFILE</code> section? Set true before parsing lines from <code>PREFILE</code> and false before parsing lines from <code>CLIENTS</code> section.
     * @return this {@link ClientParser} instance (for method chaining)
     */
    public ClientParser setIsParsingPrefileSection(boolean isParsingPrefileSection) {
        // TODO: implement
        return null;
    }
    
    /**
     * Parses all information from the given line to a {@link Client} object.
     * The line is expected to contain the proper syntax used by VATSIM data.txt
     * files and not to be empty or a comment.
     * @param line line to be parsed; must not be empty or a comment
     * @return all parsed data in a {@link Client} object
     */
    public Client parse(String line) {
        // TODO: implement
        return null;
    }
}
