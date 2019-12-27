package org.vatplanner.dataformats.vatsimpublic.graph;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.entities.status.BarometricPressure;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.BarometricPressure.UNIT_HECTOPASCALS;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.BarometricPressure.UNIT_INCHES_OF_MERCURY;
import org.vatplanner.dataformats.vatsimpublic.entities.status.CommunicationMode;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Connection;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Facility;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Flight;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FlightPlan;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FlightPlan.normalizeAirportCode;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FlightPlanType;
import org.vatplanner.dataformats.vatsimpublic.entities.status.GeoCoordinates;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.GeoCoordinates.UNIT_FEET;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Member;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Report;
import org.vatplanner.dataformats.vatsimpublic.entities.status.SimpleEquipmentSpecification;
import org.vatplanner.dataformats.vatsimpublic.entities.status.StatusEntityFactory;
import org.vatplanner.dataformats.vatsimpublic.entities.status.TrackPoint;
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
import org.vatplanner.dataformats.vatsimpublic.utils.TimeHelpers;
import static org.vatplanner.dataformats.vatsimpublic.utils.TimeHelpers.findClosestPlausibleTimestampForFlightPlanField;
import static org.vatplanner.dataformats.vatsimpublic.utils.TimeHelpers.isLessOrEqualThan;
import static org.vatplanner.dataformats.vatsimpublic.utils.ValueHelpers.inRange;

/**
 * Imports parsed {@link DataFile}s to a graph of de-duplicated and indexed
 * entities.
 */
public class GraphImport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphImport.class);

    private final StatusEntityFactory entityFactory;
    private final GraphIndex index = new GraphIndex();

    private static final Duration MAXIMUM_AGE_FOR_CONTINUED_FLIGHT = Duration.ofMinutes(30); // TODO: fine-tune and/or make configurable; compensate for missed data files as well as client connection loss
    private static final Duration MAXIMUM_AGE_FOR_CONTINUED_FLIGHT_ON_RECONSTRUCTION = Duration.ofMinutes(10); // TODO: fine-tune and/or make configurable

    private static final int MINIMUM_FLIGHT_PLAN_REVISION = 0;
    private static final int MINIMUM_VATSIM_ID = 0;

    private static final double MINIMUM_LATITUDE = -90.0;
    private static final double MAXIMUM_LATITUDE = 90.0;
    private static final double MINIMUM_LONGITUDE = -180.0;
    private static final double MAXIMUM_LONGITUDE = 180.0;

    private static final double FACTOR_KPH_TO_NM = 0.539957;

    /**
     * According to Wikipedia the fastest airplane so far flew with ~3500kph
     * "air speed" (IAS? TAS?). We add a bit of extra for ground speed and allow
     * some extra margin for simmers to determine the maximum plausible ground
     * speed to be encountered online. Everything else can be seen as erroneous
     * indication (fast-forward playback or whatever).
     *
     * <p>
     * Source: https://en.wikipedia.org/wiki/Flight_airspeed_record
     * </p>
     */
    private static final int MAXIMUM_PLAUSIBLE_GROUND_SPEED = (int) Math.round(4000 * FACTOR_KPH_TO_NM);

    private static final Duration MINIMUM_FLIGHT_DURATION = Duration.ofMinutes(5);
    private static final Duration MAXIMUM_FLIGHT_DURATION = Duration.ofHours(48); // ... let's allow some in-air refueling because we are simming

    private static final Pattern PATTERN_VALID_TRANSPONDER_CODE = Pattern.compile("^[0-7]{0,4}$");

    /**
     * Creates a new graph import using the given factory to instantiate
     * entities.
     *
     * @param entityFactory factory to instantiate entities
     */
    public GraphImport(StatusEntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    /**
     * Imports the given {@link DataFile} to the graph. All files must be
     * provided sequentially in ascending order of recording time
     * ({@link DataFileMetaData#getTimestamp()}). Importing multiple files with
     * an identical timestamp is also not supported. Time must advance strictly.
     * This also means that an import must not be carried out in parallel.
     *
     * @param dataFile file to import to graph; recording time must advance
     * strictly
     * @return imported {@link Report}, null if not imported
     */
    public Report importDataFile(final DataFile dataFile) {
        DataFileMetaData metaData = dataFile.getMetaData();
        Instant recordTime = metaData.getTimestamp();

        if (index.hasReportWithRecordTime(recordTime)) {
            LOGGER.info("report recorded at {} has already been imported, not processing again", recordTime);
            return null;
        }

        if (index.hasReportAfterRecordTime(recordTime)) {
            LOGGER.warn("report recorded at {} is older than an already imported one but import must be performed in increasing order of record time; not importing", recordTime);
            return null;
        }

        Report report = entityFactory.createReport(recordTime);
        index.add(report);

        report.setNumberOfConnectedClients(metaData.getNumberOfConnectedClients());

        for (Client client : dataFile.getClients()) {
            importClient(report, client);
        }

        return report;
    }

    private void importClient(final Report report, final Client client) {
        // ignore system services
        if (isSystemService(client)) {
            return;
        }

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

    private boolean isSystemService(final Client client) {
        if (client.getVatsimID() >= MINIMUM_VATSIM_ID) {
            return false;
        }

        String callsign = client.getCallsign();

        if (client.getRawClientType() == ATC_CONNECTED) {
            if ("AFVDATA".equals(callsign) || "AFV-SLURPER".equals(callsign) || "DATASVR".equals(callsign)) {
                return true;
            }
        }

        return false;
    }

    private void importFacility(final Report report, final Client client) {
        String name = client.getCallsign();

        // continue facility from previous report if available
        Facility facility = null;
        Report previousReport = index.getLatestReportBefore(report);
        if (previousReport != null) {
            facility = previousReport.getFacilityByName(name);

            // do not continue if member changed
            if ((facility != null) && (client.getVatsimID() != facility.getConnection().getMember().getVatsimId())) {
                facility = null;
            }

            // do not continue if reconnected
            if ((facility != null) && (client.getLogonTime() != null) && !client.getLogonTime().equals(facility.getConnection().getLogonTime())) {
                facility = null;
            }
        }

        // create new facility if unavailable
        if (facility == null) {
            Connection connection = createConnection(client);
            if (connection == null) {
                LOGGER.warn("report recorded at {}: unable to record connection for connected ATC {} (internal connection record could not be found nor created)", report.getRecordTime(), name);
                return;
            }

            facility = entityFactory.createFacility(name)
                    .setConnection(connection)
                    .setType(client.getFacilityType());

            connection.getMember().addFacility(facility);
        }

        report.addFacility(facility);
        facility.getConnection().seenInReport(report);
        facility.seenOnFrequencyKilohertz(client.getServedFrequencyKilohertz());
        facility.seenMessage(report, client.getControllerMessage(), entityFactory);
    }

    private Member getMember(final Client client) {
        int vatsimId = client.getVatsimID();
        if (vatsimId < MINIMUM_VATSIM_ID) {
            return null;
        }

        Member member = index.getMemberByVatsimId(vatsimId);
        if (member == null) {
            member = entityFactory.createMember(vatsimId);
            index.add(member);
        }

        return member;
    }

    private Connection createConnection(final Client client) {
        Member member = getMember(client);
        if (member == null) {
            return null;
        }

        RealNameHomeBaseExtractor nameExtractor = new RealNameHomeBaseExtractor(client.getRealName());

        return entityFactory.createConnection(member, client.getLogonTime())
                .setProtocolVersion(client.getProtocolVersion())
                .setRating(client.getControllerRating())
                .setServerId(client.getServerId())
                .setRealName(nameExtractor.getRealName())
                .setHomeBase(nameExtractor.getHomeBase());
    }

    private void importFlightConnected(final Report report, final Client client) {
        Instant logonTime = client.getLogonTime();
        if (logonTime == null) {
            throw new UnsupportedOperationException("log on time is mandatory to import connected flights; report recorded " + report.getRecordTime());
        }

        Member member = getMember(client);
        String callsign = client.getCallsign();
        boolean clientHasFlightPlan = (client.getFlightPlanRevision() >= MINIMUM_FLIGHT_PLAN_REVISION);

        // Strange network errors exist where essential data is missing.
        // (e.g. during CTP East 2019 in reports created 0951z-1147z)
        // Try to identify the member by continuation of callsign and connection
        // matching previous report if VATSIM ID went away...
        boolean needsFuzzyReconstruction = (member == null);
        if (needsFuzzyReconstruction && (callsign != null) && !callsign.isEmpty()) {
            Report previousReport = index.getLatestReportBefore(report);
            if (previousReport != null) {
                List<Flight> matchingFlights = previousReport.getFlights()
                        .stream()
                        .filter(x -> callsign.equals(x.getCallsign()))
                        .filter(x -> x.getLatestConnection() != null)
                        .filter(
                                x -> isLessOrEqualThan(
                                        Duration.between(x.getLatestConnection().getLogonTime(), logonTime),
                                        MAXIMUM_AGE_FOR_CONTINUED_FLIGHT_ON_RECONSTRUCTION
                                )
                        )
                        .collect(Collectors.toList());

                if (matchingFlights.size() == 1) {
                    member = matchingFlights.get(0).getMember();
                }
            }
        }

        if (member == null) {
            LOGGER.warn("report recorded at {}: unable to identify member for connected pilot {} (may occur on broken datafiles if fuzzy reconstruction fails)", report.getRecordTime(), callsign);
            return;
        }

        // find last flight of member under same callsign
        Flight flight = member.getFlights()
                .stream()
                .filter(x -> x.getCallsign().equals(callsign))
                .max((x, y) -> x.getLatestVisibleTime().compareTo(y.getLatestVisibleTime()))
                .orElse(null);

        // reset flight if it no longer matches flight plan
        FlightPlan flightPlan = null;
        if (flight != null) {
            SortedSet<FlightPlan> flightPlans = flight.getFlightPlans();
            if (!flightPlans.isEmpty()) {
                flightPlan = flightPlans.last();
            }

            if (clientHasFlightPlan && (flightPlan != null) && !isSameFlight(flightPlan, client)) {
                flightPlan = null;
                flight = null;
            }
        }

        // check connection for continuation of flight
        Connection connection = null;
        if (flight != null) {
            connection = flight.getLatestConnection();

            // reset flight if last connection exceeds retention time
            if (connection != null) {
                Duration timeSinceLastSeen = Duration.between(connection.getLastReport().getRecordTime(), report.getRecordTime());
                if (!isLessOrEqualThan(timeSinceLastSeen, MAXIMUM_AGE_FOR_CONTINUED_FLIGHT)) {
                    // TODO: check if we actually got at least 1 or 2 reports since then, otherwise we may have had a network outage
                    flight = null;
                    flightPlan = null;
                }
            }

            // do not continue last connection if it's another session (logon time mismatch)
            if (!needsFuzzyReconstruction && (connection != null) && !logonTime.equals(connection.getLogonTime())) {
                connection = null;
            }
        }

        // reset flight plan if data has changed
        if (!needsFuzzyReconstruction && !isSameFlightPlan(flightPlan, client)) {
            flightPlan = null;
        }

        // create new flight if unavailable
        if (flight == null) {
            flight = entityFactory.createFlight(member, callsign);
            member.addFlight(flight);
        }

        // record flight on report
        report.addFlight(flight);

        // record reconstruction so we don't delete data if we see a pre-filing
        // pop up separate from this connection
        if (needsFuzzyReconstruction) {
            flight.markAsReconstructed(report);
        }

        // create new connection if unavailable
        if (connection == null) {
            connection = createConnection(client);

            // NOTE: creation may fail if VATSIM ID is missing
            if (connection != null) {
                flight.addConnection(connection);
            }
        }

        // record connection seen
        if (connection != null) {
            connection.seenInReport(report);
        } else {
            LOGGER.warn("report recorded at {}: unable to record connection for connected pilot {} (internal connection record could not be found nor created)", report.getRecordTime(), callsign);
        }

        // add track point
        addTrackPointToFlight(report, client, flight);

        // create new flight plan if available but not continued
        if ((flightPlan == null) && clientHasFlightPlan) {
            getFlightPlan(flight, report, client)
                    .seenInReport(report);
        }

        // TODO: check distance between last and current position for plausibility and start new flight if aircraft "jumped" to a new location?
        // TODO: track flight phases and mark flight completed after landing, trigger new flight when aircraft moves/departs again?
    }

    private boolean isSameFlight(final FlightPlan flightPlan, final Client client) {
        if (flightPlan == null) {
            return false;
        }

        if (flightPlan.getRevision() > client.getFlightPlanRevision()) {
            // flight plan revision is strictly ascending; if it moves backward, flight plan has been deleted and refiled
            return false;
        }

        // TODO: perform all checks case insensitive/normalized?
        AircraftTypeExtractor extractor = new AircraftTypeExtractor(client.getAircraftType());
        String previousType = flightPlan.getAircraftType();
        if ((previousType != null) && !previousType.equals(extractor.getAircraftType())) {
            return false;
        }

        if (!flightPlan.getDepartureAirportCode().equals(normalizeAirportCode(client.getFiledDepartureAirportCode()))) {
            return false;
        }

        if (!flightPlan.getDestinationAirportCode().equals(normalizeAirportCode(client.getFiledDestinationAirportCode()))) {
            return false;
        }

        return true;
    }

    private boolean isSameFlightPlan(final FlightPlan flightPlan, final Client client) {
        if (flightPlan == null) {
            return false;
        }

        if (flightPlan.getRevision() != client.getFlightPlanRevision()) {
            return false;
        }

        return true;
    }

    private void addTrackPointToFlight(final Report report, final Client client, final Flight flight) {
        TrackPoint trackPoint = createTrackPoint(report, client);
        if (trackPoint == null) {
            return;
        }

        trackPoint.setFlight(flight);
        flight.addTrackPoint(trackPoint);
    }

    private TrackPoint createTrackPoint(final Report report, final Client client) {
        // check coordinates for validity
        double latitude = client.getLatitude();
        double longitude = client.getLongitude();
        boolean inRange = inRange(latitude, MINIMUM_LATITUDE, MAXIMUM_LATITUDE) && inRange(longitude, MINIMUM_LONGITUDE, MAXIMUM_LONGITUDE);
        if (!inRange) {
            // don't allow track points without coordinates
            // TODO: try to correct lat/lon if out of range instead of ignoring it
            // TODO: check altitude for plausibility?
            return null;
        }

        // create track point
        TrackPoint trackPoint = entityFactory.createTrackPoint(report);

        // set coordinates
        trackPoint.setGeoCoordinates(new GeoCoordinates(latitude, longitude, client.getAltitudeFeet(), UNIT_FEET));

        // prefer QNH measured in inHg as it provides higher precision in data files (at least 2 decimal places)
        BarometricPressure localQnh = null;
        double qnhInchMercury = client.getQnhInchMercury();
        if (!Double.isNaN(qnhInchMercury)) {
            localQnh = BarometricPressure.forPlausibleQnh(qnhInchMercury, UNIT_INCHES_OF_MERCURY);
        }

        // fall back to QNH measured in hPa if inHg was unavailable or made no sense
        if (localQnh == null) {
            int qnhHectopascal = client.getQnhHectopascal();
            if (qnhHectopascal >= 0) {
                localQnh = BarometricPressure.forPlausibleQnh(qnhHectopascal, UNIT_HECTOPASCALS);
            }
        }

        // set QNH if available and plausible
        if (localQnh != null) {
            trackPoint.setQnh(localQnh);
        }

        // set GS if available and plausible
        int groundSpeed = client.getGroundSpeed();
        if (groundSpeed >= 0 && groundSpeed <= MAXIMUM_PLAUSIBLE_GROUND_SPEED) {
            trackPoint.setGroundSpeed(groundSpeed);
        }

        // set heading if available
        int heading = client.getHeading();
        if (heading >= 0) {
            trackPoint.setHeading(heading % 360);
        }

        // set transponder code if valid
        int transponder = client.getTransponderCodeDecimal();
        if (isValidTransponderCode(transponder)) {
            trackPoint.setTransponderCode(transponder);
        }

        return trackPoint;
    }

    private boolean isValidTransponderCode(int code) {
        return PATTERN_VALID_TRANSPONDER_CODE.matcher(Integer.toString(code)).matches();
    }

    /**
     * Searches for a continued flight by checking airports filed on flight
     * plan. Also, VATSIM ID and callsign has to match as that pair of
     * information uniquely identifies flights. At most the given number of
     * maximum reports recorded before the given new report are searched if
     * their age, compared to the new report, does not exceed
     * {@link #MAXIMUM_AGE_FOR_CONTINUED_FLIGHT}.
     *
     * @param newReport new report to lookup a flight for
     * @param client client to lookup a flight for
     * @param maxReports maximum number of reports to search
     * @return flight matching all criteria, null if not found
     * @see #findMatchingFlightByFlightPlanAirports(Report, Client)
     */
    private Flight findContinuedFlightByFlightPlanAirports(final Report newReport, final Client client, final int maxReports) {
        Report report = newReport;
        int checkedReports = 0;
        while ((checkedReports++ < maxReports) && (report = index.getLatestReportBefore(report)) != null) {
            Duration reportAge = Duration.between(report.getRecordTime(), newReport.getRecordTime());
            boolean isReportCurrentEnough = isLessOrEqualThan(reportAge, MAXIMUM_AGE_FOR_CONTINUED_FLIGHT);
            if (!isReportCurrentEnough) {
                return null;
            }

            Flight flight = findMatchingFlightByFlightPlanAirports(report, client);
            if (flight != null) {
                return flight;
            }
        }

        return null;
    }

    private void importFlightPrefiled(final Report report, final Client client) {
        // a prefiled flight should also have been listed in the previous report
        // TODO: check if assumption is always true or if search needs to include some older reports as well
        // TODO: check if flight plan has same revision but content has changed - in that case flight has been canceled & refiled
        Flight flight = findContinuedFlightByFlightPlanAirports(report, client, 1);
        if (flight != null && !flight.getTrack().isEmpty() && !flight.isReconstructed(report)) {
            // pre-filed flights should not have track points yet,
            // otherwise they would be "connected" flights
            flight = null;
        }

        if (flight == null) {
            Member member = getMember(client);
            if (member == null) {
                LOGGER.warn("report recorded at {}: unable to record prefiling for {} (unknown member)", report.getRecordTime(), client.getCallsign());
                return;
            }

            flight = entityFactory.createFlight(member, client.getCallsign());
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

            flightPlan = entityFactory.createFlightPlan(flight, flightPlanRevision)
                    .setAircraftType(aircraftTypeExtractor.getAircraftType())
                    .setAlternateAirportCode(client.getFiledAlternateAirportCode())
                    .setAltitudeFeet(altitudeFeet)
                    .setCommunicationMode(communicationMode)
                    .setDepartureAirportCode(client.getFiledDepartureAirportCode())
                    .setDestinationAirportCode(client.getFiledDestinationAirportCode())
                    .setEstimatedTimeEnroute(nullDurationIfOutOfRange(client.getFiledTimeEnroute(), MINIMUM_FLIGHT_DURATION, MAXIMUM_FLIGHT_DURATION))
                    .setEstimatedTimeFuel(nullDurationIfOutOfRange(client.getFiledTimeFuel(), MINIMUM_FLIGHT_DURATION, MAXIMUM_FLIGHT_DURATION))
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

    private Flight findMatchingFlightByFlightPlanAirports(final Report report, final Client client) {
        return report.getFlights()
                .stream()
                .filter(x -> x.getMember().getVatsimId() == client.getVatsimID() && x.getCallsign().equals(client.getCallsign()))
                .map(Flight::getFlightPlans)
                .filter(not(SortedSet::isEmpty))
                .map(SortedSet::last)
                .filter(x -> x.getDepartureAirportCode().equals(client.getFiledDepartureAirportCode()) // TODO: ignore case or normalize?
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

    private Duration nullDurationIfOutOfRange(Duration duration, Duration minimum, Duration maximum) {
        if (!TimeHelpers.isLessThan(duration, minimum) && TimeHelpers.isLessOrEqualThan(duration, maximum)) {
            return duration;
        }

        return null;
    }

    // TODO: "unit tests"... integration tests make more sense, i.e. create a series of data files, import them and check for expected outcome
}
