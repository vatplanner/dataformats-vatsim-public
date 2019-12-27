package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.time.Instant;
import java.util.Collection;
import static java.util.Collections.unmodifiableCollection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.Facility.normalizeFacilityName;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;

/**
 * A report is equal to one parsed VATSIM status data file in imported form. Not
 * all information available from parsed data files is maintained as these
 * entities are intended to keep track of flights and facilities, not meta data.
 */
public class Report {

    private final Map<String, Facility> facilitiesByName = new HashMap<>();
    private final Map<String, Set<Flight>> flightsByCallsign = new HashMap<>();

    private final Instant recordTime;
    private int numberOfConnectedClients;

    /**
     * Creates a new report for the given timestamp.
     * <p>
     * <b>Warning:</b> Make sure the timestamp is unique in a graph, i.e. do not
     * import multiple data files which were created at the same time. This is
     * due to {@link Report} objects being used to de-duplicate and sort other
     * entities.
     * </p>
     *
     * @param recordTime time of recording the report (must be unique in graph)
     */
    public Report(Instant recordTime) {
        this.recordTime = recordTime;
    }

    /**
     * Returns all facilities visible on this report.
     *
     * @return facilities visible on this report
     */
    public Collection<Facility> getFacilities() {
        return unmodifiableCollection(facilitiesByName.values());
    }

    /**
     * Returns the facility identified by given name on this report if recorded.
     * Null will be returned if no such facility has been recorded.
     *
     * @param name facility name to look up, will be normalized
     * @return facility recorded under given name on this report; null if not
     * recorded
     */
    public Facility getFacilityByName(String name) {
        return facilitiesByName.get(normalizeFacilityName(name));
    }

    /**
     * Adds the given facility to this report. If the facility has already been
     * added, it is not added again.
     *
     * @param facility facility to be added to this report
     * @return this instance for method-chaining
     */
    public Report addFacility(Facility facility) {
        // TODO: reject if another facility has already been added by this name

        facilitiesByName.put(facility.getName(), facility);

        // TODO: set/check report on facility's connection?
        return this;
    }

    /**
     * Returns all flights visible on this report. Interrupted flights
     * (connection loss) are not listed although they may be continued by a
     * later report.
     *
     * @return flights visible on this report
     */
    public Collection<Flight> getFlights() {
        return flightsByCallsign.values() //
                .stream() //
                .flatMap(Set::stream) //
                .collect(Collectors.toList());
    }

    /**
     * Adds the given flight to the report. If the flight has already been
     * added, it is not added again.
     *
     * @param flight flight to be added to this report
     * @return this instance for method-chaining
     */
    public Report addFlight(Flight flight) {
        Set<Flight> flightsForCallsign = flightsByCallsign.computeIfAbsent(flight.getCallsign(), x -> new HashSet<>());
        flightsForCallsign.add(flight);

        // TODO: is there any reference that should be set/check on flights?
        return this;
    }

    // TODO: add getter to lookup flights for callsign
    /**
     * Returns the time a record has been created. This is equal to the time a
     * processed data file has originally been created (see
     * {@link DataFileMetaData#getTimestamp()}).
     *
     * @return original creation time of record
     */
    public Instant getRecordTime() {
        return recordTime;
    }

    /**
     * Returns the total number of connected clients at time of record creation.
     * This is an information directly available from data files (see
     * {@link DataFileMetaData#getNumberOfConnectedClients()}) and may be
     * different from the number of parsed flights and facilities.
     *
     * @return total number of connected clients
     */
    public int getNumberOfConnectedClients() {
        return numberOfConnectedClients;
    }

    public Report setNumberOfConnectedClients(int numberOfConnectedClients) {
        this.numberOfConnectedClients = numberOfConnectedClients;
        return this;
    }

    // TODO: unit tests
}
