package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import java.util.Optional;

import org.vatplanner.dataformats.vatsimpublic.utils.GeoPoint2D;

public class Airport {
    private final String icaoCode;
    private final String name;
    private final GeoPoint2D location;
    private final Optional<String> alternativeCode; // could be IATA or FAA LID
    private final String flightInformationRegionId;
    private final boolean isPseudo;

    public Airport(String icaoCode, String name, GeoPoint2D location, String alternativeCode, String fir, boolean isPseudo) {
        this.icaoCode = icaoCode;
        this.name = name;
        this.location = location;
        this.alternativeCode = Helper.emptyIfBlank(alternativeCode);
        this.flightInformationRegionId = fir;
        this.isPseudo = isPseudo;
    }

    public String getIcaoCode() {
        return icaoCode;
    }

    public String getName() {
        return name;
    }

    public GeoPoint2D getLocation() {
        return location;
    }

    public Optional<String> getAlternativeCode() {
        return alternativeCode;
    }

    public String getFlightInformationRegionId() {
        return flightInformationRegionId;
    }

    public boolean isPseudo() {
        return isPseudo;
    }
}
