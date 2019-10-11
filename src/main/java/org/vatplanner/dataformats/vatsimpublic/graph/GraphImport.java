package org.vatplanner.dataformats.vatsimpublic.graph;

import java.time.Instant;
import java.util.SortedSet;
import java.util.function.Predicate;
import org.vatplanner.dataformats.vatsimpublic.entities.status.CommunicationMode;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Connection;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Facility;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Flight;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FlightPlan;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FlightPlanType;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Member;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Report;
import org.vatplanner.dataformats.vatsimpublic.entities.status.SimpleEquipmentSpecification;
import org.vatplanner.dataformats.vatsimpublic.entities.status.WakeTurbulenceCategory;
import org.vatplanner.dataformats.vatsimpublic.extraction.AircraftTypeExtractor;
import org.vatplanner.dataformats.vatsimpublic.extraction.AltitudeParser;
import org.vatplanner.dataformats.vatsimpublic.extraction.RealNameHomeBaseExtractor;
import org.vatplanner.dataformats.vatsimpublic.extraction.RemarksExtractor;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;
import static org.vatplanner.dataformats.vatsimpublic.parser.ClientType.ATC_CONNECTED;
import static org.vatplanner.dataformats.vatsimpublic.parser.ClientType.PILOT_CONNECTED;
import static org.vatplanner.dataformats.vatsimpublic.parser.ClientType.PILOT_PREFILED;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import static org.vatplanner.dataformats.vatsimpublic.utils.TimeHelpers.findClosestPlausibleTimestampForFlightPlanField;

/**
 * Imports parsed {@link DataFile}s to a graph of de-duplicated and indexed
 * entities.
 */
public class GraphImport {

    private final GraphIndex index = new GraphIndex();

    /**
     * Imports the given {@link DataFile} to the graph. All files must be
     * provided sequentially in ascending order of recording time
     * ({@link DataFileMetaData#getTimestamp()}). Importing multiple files with
     * an identical timestamp is also not supported. Time must advance strictly.
     * This also means that an import must not be carried out in parallel.
     *
     * @param dataFile file to import to graph; recording time must advance
     * strictly
     */
    public void importDataFile(final DataFile dataFile) {
        // TODO: log warning if recording time is not strictly advanced (same or earlier than last import)

        DataFileMetaData metaData = dataFile.getMetaData();
        Instant recordTime = metaData.getTimestamp();

        Report report = new Report(recordTime);
        index.add(report);

        report.setNumberOfConnectedClients(metaData.getNumberOfConnectedClients());

        for (Client client : dataFile.getClients()) {
            importClient(report, client);
        }
    }

    private void importClient(final Report report, final Client client) {
        ClientType clientType = client.getEffectiveClientType();

        if (clientType == PILOT_CONNECTED) {
            importFlightConnected(report, client);
        } else if (clientType == PILOT_PREFILED) {
            importFlightPrefiled(report, client);
        } else if (clientType == ATC_CONNECTED) {
            importFacility(report, client);
        } else if (clientType == null) {
            // TODO: log client not imported
        } else {
            throw new UnsupportedOperationException("Unsupported client type: " + clientType);
        }
    }

    private void importFacility(final Report report, final Client client) {
        String name = client.getCallsign();

        // continue facility from previous report if available
        Facility facility = null;
        Report previousReport = index.getLatestReportBefore(report);
        if (previousReport != null) {
            facility = previousReport.getFacilityByName(name);
            // FIXME: check for same member, do not reuse if member changed
        }

        // create new facility if unavailable
        if (facility == null) {
            Connection connection = createConnection(client);

            facility = new Facility(name)
                    .setConnection(connection)
                    .setType(client.getFacilityType());

            connection.getMember().addFacility(facility);
        }

        report.addFacility(facility);
        facility.getConnection().seenInReport(report);
        facility.seenOnFrequencyKilohertz(client.getServedFrequencyKilohertz());
        facility.seenMessage(report, client.getControllerMessage());
    }

    private Member getMember(final Client client) {
        int vatsimId = client.getVatsimID();

        Member member = index.getMemberByVatsimId(vatsimId);
        if (member == null) {
            member = new Member(vatsimId);
            index.add(member);
        }

        return member;
    }

    private Connection createConnection(final Client client) {
        Member member = getMember(client);
        RealNameHomeBaseExtractor nameExtractor = new RealNameHomeBaseExtractor(client.getRealName());

        return new Connection(member)
                .setLogonTime(client.getLogonTime())
                .setProtocolVersion(client.getProtocolVersion())
                .setRating(client.getControllerRating())
                .setServerId(client.getServerId())
                .setRealName(nameExtractor.getRealName())
                .setHomeBase(nameExtractor.getHomeBase());
    }

    private void importFlightConnected(final Report report, final Client client) {
        // TODO: implement
    }

    private void importFlightPrefiled(final Report report, final Client client) {
        // a prefiled flight should also have been listed in the previous report
        // TODO: check if assumption is always true or if search needs to include some older reports as well
        Flight flight = null;

        Report previousReport = index.getLatestReportBefore(report);
        if (previousReport != null) {
            flight = findMatchingFlightByAirports(previousReport, client);
            if (flight != null && !flight.getTrack().isEmpty()) {
                // pre-filed flights should not have track points yet,
                // otherwise they would be "connected" flights
                flight = null;
            }
        }

        if (flight == null) {
            Member member = getMember(client);
            flight = new Flight(member, client.getCallsign());
            member.addFlight(flight);
        }

        report.addFlight(flight);

        getFlightPlan(flight, report, client)
                .seenInReport(report);
    }

    private FlightPlan getFlightPlan(final Flight flight, final Report report, final Client client) {
        int flightPlanRevision = client.getFlightPlanRevision();
        FlightPlan flightPlan = flight.getFlightPlans() // TODO: move to Flight
                .stream()
                .filter(x -> x.getRevision() == flightPlanRevision)
                .findFirst()
                .orElse(null);

        if (flightPlan == null) {
            int altitudeFeet = new AltitudeParser(client.getRawFiledAltitude()).getFeet();
            CommunicationMode communicationMode = new RemarksExtractor(client.getFlightPlanRemarks()).getCommunicationMode();
            FlightPlanType flightPlanType = FlightPlanType.resolveFlightPlanCode(client.getRawFlightPlanType());

            AircraftTypeExtractor aircraftTypeExtractor = new AircraftTypeExtractor(client.getAircraftType());
            WakeTurbulenceCategory wakeTurbulenceCategory = WakeTurbulenceCategory.resolveFlightPlanCode(aircraftTypeExtractor.getWakeCategory());
            SimpleEquipmentSpecification simpleEquipmentSpecification = SimpleEquipmentSpecification.resolveFlightPlanCode(aircraftTypeExtractor.getEquipmentCode());

            flightPlan = new FlightPlan(flight, flightPlanRevision)
                    .setAircraftType(aircraftTypeExtractor.getAircraftType())
                    .setAlternateAirportCode(client.getFiledAlternateAirportCode())
                    .setAltitudeFeet(altitudeFeet)
                    .setCommunicationMode(communicationMode)
                    .setDepartureAirportCode(client.getFiledDepartureAirportCode())
                    .setDestinationAirportCode(client.getFiledDestinationAirportCode())
                    .setEstimatedTimeEnroute(client.getFiledTimeEnroute())
                    .setEstimatedTimeFuel(client.getFiledTimeFuel())
                    .setFlightPlanType(flightPlanType)
                    .setRemarks(client.getFlightPlanRemarks())
                    .setRoute(client.getFiledRoute())
                    .setSimpleEquipmentSpecification(simpleEquipmentSpecification)
                    .setTrueAirSpeed(client.getFiledTrueAirSpeed())
                    .setWakeTurbulenceCategory(wakeTurbulenceCategory)
                    .seenInReport(report); // needs to be set for timestamps to be guessed

            flight.addFlightPlan(flightPlan);

            Instant departureTimeActual = findClosestPlausibleTimestampForFlightPlanField(flight, client.getRawDepartureTimeActual());
            Instant departureTimePlanned = findClosestPlausibleTimestampForFlightPlanField(flight, client.getRawDepartureTimePlanned());
            flightPlan.setDepartureTimeActual(departureTimeActual);
            flightPlan.setDepartureTimePlanned(departureTimePlanned);
        }

        return flightPlan;
    }

    private Flight findMatchingFlightByAirports(final Report report, final Client client) {
        return report.getFlights()
                .stream()
                .filter(x -> x.getMember().getVatsimId() == client.getVatsimID() && x.getCallsign().equals(client.getCallsign()))
                .map(Flight::getFlightPlans)
                .filter(not(SortedSet::isEmpty))
                .map(SortedSet::last)
                .filter(x -> x.getDepartureAirportCode().equals(client.getFiledDepartureAirportCode())
                /*     */ && x.getDestinationAirportCode().equals(client.getFiledDestinationAirportCode()))
                .map(FlightPlan::getFlight)
                .findFirst()
                .orElse(null);
    }

    private static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    public GraphIndex getIndex() {
        return index;
    }

    // TODO: "unit tests"... integration tests make more sense, i.e. create a series of data files, import them and check for expected outcome
}
