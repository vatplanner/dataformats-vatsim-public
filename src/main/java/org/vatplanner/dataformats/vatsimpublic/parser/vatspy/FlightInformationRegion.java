package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.emptyIfBlank;

import java.util.Optional;

public class FlightInformationRegion {
    private final String id;
    private final String name;
    private final Optional<String> callsignPrefix;
    private final Optional<String> boundaryId;

    public FlightInformationRegion(String id, String name, String callsignPrefix, String boundaryId) {
        this.id = id;
        this.name = name;
        this.callsignPrefix = emptyIfBlank(callsignPrefix);
        this.boundaryId = emptyIfBlank(boundaryId);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getCallsignPrefix() {
        return callsignPrefix;
    }

    public Optional<String> getBoundaryId() {
        return boundaryId;
    }
}
