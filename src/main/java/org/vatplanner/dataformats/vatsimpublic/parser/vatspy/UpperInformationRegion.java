package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UpperInformationRegion {
    private final String id;
    private final String name;
    private final Set<String> flightInformationRegionIds;

    public UpperInformationRegion(String id, String name, Collection<String> firs) {
        this.id = id;
        this.name = name;
        this.flightInformationRegionIds = Collections.unmodifiableSet(new HashSet<>(firs));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getFlightInformationRegionIds() {
        return Collections.unmodifiableSet(flightInformationRegionIds);
    }
}
