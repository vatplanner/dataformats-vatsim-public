package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.util.Collection;

/**
 * Specifies addition and retrieval of {@link ParserLogEntry} objects.
 */
public interface ParserLogEntryCollector {
    /**
     * Adds a new parser log entry.
     * @param entry log entry to be added
     */
    void addParserLogEntry(ParserLogEntry entry);
    
    /**
     * Returns all collected parser log entries.
     * @return all collected parser log entries
     */
    public Collection<ParserLogEntry> getParserLogEntries();
}
