package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

public class VatSpyFile implements ParserLogEntryCollector {
    private final Collection<Airport> airports = new ArrayList<>();
    private final Collection<Country> countries = new ArrayList<>();
    private final Collection<FlightInformationRegion> flightInformationRegions = new ArrayList<>();
    private final Collection<UpperInformationRegion> upperInformationRegions = new ArrayList<>();
    private final List<GeoPoint2D> internationalDateLine = new ArrayList<>();

    private final Collection<ParserLogEntry> parserLogEntries = new ArrayList<>();

    public void addAirport(Airport airport) {
        airports.add(airport);
    }

    public void addCountry(Country country) {
        countries.add(country);
    }

    public void addFlightInformationRegion(FlightInformationRegion fir) {
        flightInformationRegions.add(fir);
    }

    public void addUpperInformationRegion(UpperInformationRegion uir) {
        upperInformationRegions.add(uir);
    }

    public void addInternationalDateLinePoint(GeoPoint2D point) {
        internationalDateLine.add(point);
    }

    @Override
    public void addParserLogEntry(ParserLogEntry entry) {
        parserLogEntries.add(entry);
    }

    @Override
    public Collection<ParserLogEntry> getParserLogEntries() {
        return Collections.unmodifiableCollection(parserLogEntries);
    }

    public Collection<Airport> getAirports() {
        return airports;
    }

    public Collection<Country> getCountries() {
        return countries;
    }

    public Collection<FlightInformationRegion> getFlightInformationRegions() {
        return flightInformationRegions;
    }

    public Collection<UpperInformationRegion> getUpperInformationRegions() {
        return upperInformationRegions;
    }

    public List<GeoPoint2D> getInternationalDateLine() {
        return internationalDateLine;
    }

}
