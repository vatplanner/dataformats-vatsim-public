package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.time.Instant;
import java.util.Collection;
import static java.util.Collections.unmodifiableCollection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import org.vatplanner.dataformats.vatsimpublic.parser.NetworkInformation;

/**
 * A report is equal to one parsed VATSIM status data file in imported form. Not
 * all information available from parsed data files is maintained as these
 * entities are intended to keep track of flights and facilities, not meta data.
 * Instead, some additional meta information related to fetching and
 * parsing/processing the retrieved information is added on import or can
 * optionally be set to keep track of information sources:
 * <ul>
 * <li>{@link #fetchTime} is the timestamp when data file was fetched (requested
 * from VATSIM servers)</li>
 * <li>{@link #fetchUrlRequested} holds the data file source URL requested from
 * VATSIM servers (as available from
 * {@link NetworkInformation#getDataFileUrls()} file)</li>
 * <li>{@link #fetchUrlRetrieved} holds the actually retrieved data file URL
 * after resolving all HTTP redirects</li>
 * <li>{@link #fetchNode} identifies the node who fetched the data from VATSIM
 * in a cluster setup</li>
 * <li>{@link #parseTime} is the timestamp when a previously fetched data file
 * was actually parsed</li>
 * <li>{@link #parserRejectedLines} is the number of lines of a data file that
 * could not be processed by the parser (any number >0 indicates loss of
 * information)</li>
 * </ul>
 */
public class Report {

    private final Map<String, Facility> facilitiesByName = new HashMap<>();
    private final Map<String, Set<Flight>> flightsByCallsign = new HashMap<>();

    private final Instant recordTime;
    private int numberOfConnectedClients;

    private Instant fetchTime;
    private String fetchUrlRequested;
    private String fetchUrlRetrieved;
    private String fetchNode;

    private Instant parseTime;
    private int parserRejectedLines = -1;

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
     * @param name facility name to look up
     * @return facility recorded under given name on this report; null if not
     * recorded
     */
    public Facility getFacilityByName(String name) {
        return facilitiesByName.get(name);
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

    /**
     * Returns the timestamp when the source data file was fetched (requested)
     * from VATSIM servers.
     *
     * <p>
     * This is an extra field to keep track of application-specific but
     * generally useful meta-information.
     * </p>
     *
     * @return timestamp of fetching the source data file
     */
    public Instant getFetchTime() {
        return fetchTime;
    }

    public Report setFetchTime(Instant fetchTime) {
        this.fetchTime = fetchTime;
        return this;
    }

    /**
     * Returns the URL originally requested to retrieve the source data file.
     * This should usually be a URL listed in the {@link NetworkInformation}
     * used at time of request.
     *
     * <p>
     * This is an extra field to keep track of application-specific but
     * generally useful meta-information.
     * </p>
     *
     * @return URL data file was originally requested from (before redirects)
     */
    public String getFetchUrlRequested() {
        return fetchUrlRequested;
    }

    public Report setFetchUrlRequested(String fetchUrlRequested) {
        this.fetchUrlRequested = fetchUrlRequested;
        return this;
    }

    /**
     * Returns the URL the source data file was actually retrieved from after
     * following all redirects. This may be a different URL than those listed in
     * {@link NetworkInformation}.
     *
     * <p>
     * This is an extra field to keep track of application-specific but
     * generally useful meta-information.
     * </p>
     *
     * @return URL data file was actually retrieved from (after redirects)
     */
    public String getFetchUrlRetrieved() {
        return fetchUrlRetrieved;
    }

    public Report setFetchUrlRetrieved(String fetchUrlRetrieved) {
        this.fetchUrlRetrieved = fetchUrlRetrieved;
        return this;
    }

    /**
     * Returns the identification of the cluster node who fetched the data file.
     *
     * <p>
     * This is an extra field to keep track of application-specific but
     * generally useful meta-information.
     * </p>
     *
     * @return ID of cluster node who fetched the data file
     */
    public String getFetchNode() {
        return fetchNode;
    }

    public Report setFetchNode(String fetchNode) {
        this.fetchNode = fetchNode;
        return this;
    }

    /**
     * Returns the timestamp when a data file was parsed/processed.
     *
     * <p>
     * This is an extra field to keep track of application-specific but
     * generally useful meta-information.
     * </p>
     *
     * @return timestamp of parsing/processing data file
     */
    public Instant getParseTime() {
        return parseTime;
    }

    public Report setParseTime(Instant parseTime) {
        this.parseTime = parseTime;
        return this;
    }

    /**
     * Returns the number of lines rejected by the parser. A number >0 indicated
     * loss of information. If not set, a negative value will be returned.
     *
     * @return number of lines rejected by parser; negative if not set
     */
    public int getParserRejectedLines() {
        return parserRejectedLines;
    }

    public Report setParserRejectedLines(int parserRejectedLines) {
        this.parserRejectedLines = parserRejectedLines;
        return this;
    }

    // TODO: unit tests
}
