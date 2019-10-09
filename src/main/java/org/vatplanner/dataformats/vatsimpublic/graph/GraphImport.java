package org.vatplanner.dataformats.vatsimpublic.graph;

import java.time.Instant;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Connection;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Facility;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Member;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Report;
import org.vatplanner.dataformats.vatsimpublic.extraction.RealNameHomeBaseExtractor;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;
import static org.vatplanner.dataformats.vatsimpublic.parser.ClientType.ATC_CONNECTED;
import static org.vatplanner.dataformats.vatsimpublic.parser.ClientType.PILOT_CONNECTED;
import static org.vatplanner.dataformats.vatsimpublic.parser.ClientType.PILOT_PREFILED;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;

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

    private Connection createConnection(final Client client) {
        int vatsimId = client.getVatsimID();

        Member member = index.getMemberByVatsimId(vatsimId);
        if (member == null) {
            member = new Member(vatsimId);
            index.add(member);
        }

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
        // TODO: implement
    }

    public GraphIndex getIndex() {
        return index;
    }

    // TODO: "unit tests"... integration tests make more sense, i.e. create a series of data files, import them and check for expected outcome
}
