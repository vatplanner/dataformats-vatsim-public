package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

public class FIRBoundaryFile implements ParserLogEntryCollector {
    private final List<FIRBoundary> boundaries = new ArrayList<>();
    private final Collection<ParserLogEntry> parserLogEntries = new ArrayList<>();

    public void add(FIRBoundary boundary) {
        boundaries.add(boundary);
    }

    public List<FIRBoundary> getBoundaries() {
        return Collections.unmodifiableList(boundaries);
    }

    @Override
    public void addParserLogEntry(ParserLogEntry entry) {
        parserLogEntries.add(entry);
    }

    @Override
    public Collection<ParserLogEntry> getParserLogEntries() {
        return Collections.unmodifiableCollection(parserLogEntries);
    }
}
