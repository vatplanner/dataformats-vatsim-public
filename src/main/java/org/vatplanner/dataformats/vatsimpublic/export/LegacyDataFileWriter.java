package org.vatplanner.dataformats.vatsimpublic.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import org.vatplanner.dataformats.vatsimpublic.parser.FSDServer;

/**
 * Provides serialization for a {@link DataFile} object back to the legacy
 * CSV-like format whose service was officially terminated in March 2021.
 */
public class LegacyDataFileWriter implements Writer<DataFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDataFileWriter.class);

    private static final String LINE_END = "\n";
    private static final char SEPARATOR = ':';

    private static final String FORMAT_VERSION = "8";

    private static final String DEFAULT_CLIENT_PROTOCOL_VERSION = "100";
    private static final String DEFAULT_CLIENT_VISUAL_RANGE = "0";
    private static final String DEFAULT_CLIENT_FLIGHT_PLAN_REVISION = "0";
    private static final String DEFAULT_CLIENT_DEPARTURE_TIME_PLANNED = ""; // TODO: 0 may be more common
    private static final String DEFAULT_CLIENT_DEPARTURE_TIME_ACTUAL = DEFAULT_CLIENT_DEPARTURE_TIME_PLANNED;
    private static final String DEFAULT_CLIENT_HEADING = "0";
    private static final String DEFAULT_CLIENT_QNH_INCH_MERCURY = "0";
    private static final String DEFAULT_CLIENT_QNH_HECTOPASCALS = "0";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        .withZone(ZoneId.of("UTC"));

    private static final String CONTROLLER_MESSAGE_LINEBREAK = new String( //
        new byte[] { (byte) 0x5E, (byte) 0xA7 },
        Charset.forName("ISO-8859-1") //
    );

    private void encodeGeneralSection(DataFile content, BufferedWriter bw) throws IOException {
        // TODO: share constants with parser

        DataFileMetaData metaData = content.getMetaData();
        bw.append("!GENERAL:");
        bw.append(LINE_END);

        bw.append("VERSION = ");
        bw.append(FORMAT_VERSION);
        bw.append(LINE_END);

        bw.append("RELOAD = ");
        bw.append(Integer.toString(
            (int) Math.ceil(metaData.getMinimumDataFileRetrievalInterval().getSeconds() / 60.0) //
        ));
        bw.append(LINE_END);

        bw.append("UPDATE = ");
        bw.append(TIME_FORMATTER.format(metaData.getTimestamp()));
        bw.append(LINE_END);

        bw.append("CONNECTED CLIENTS = ");
        bw.append(Integer.toString(metaData.getNumberOfConnectedClients()));
        bw.append(LINE_END);

        bw.append("UNIQUE USERS = ");
        bw.append(Integer.toString(metaData.getNumberOfUniqueConnectedUsers()));
        bw.append(LINE_END);
    }

    private void encodeSectionStart(String sectionName, BufferedWriter bw) throws IOException {
        bw.append(";");
        bw.append(LINE_END);
        bw.append(";");
        bw.append(LINE_END);
        bw.append("!" + sectionName + ":");
        bw.append(LINE_END);
    }

    private void encodeClientSection(DataFile content, BufferedWriter bw) throws IOException {
        encodeSectionStart("CLIENTS", bw);

        for (Client client : content.getClients()) {
            if (client.getRawClientType() == ClientType.PILOT_PREFILED) {
                // prefilings have their own section
                continue;
            }

            encodeClient(client, bw);
        }
    }

    private void encodePrefileSection(DataFile content, BufferedWriter bw) throws IOException {
        encodeSectionStart("PREFILE", bw);

        for (Client client : content.getClients()) {
            if (client.getRawClientType() != ClientType.PILOT_PREFILED) {
                // online clients have their own section
                continue;
            }

            encodeClient(client, bw);
        }
    }

    private void encodeClient(Client client, BufferedWriter bw) throws IOException {
        bw.append(sanitize(client.getCallsign()));
        bw.append(SEPARATOR);

        bw.append(Integer.toString(client.getVatsimID()));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getRealName()));
        bw.append(SEPARATOR);

        bw.append(encodeClientType(client.getRawClientType()));
        bw.append(SEPARATOR);

        bw.append(encodeFrequency(client.getServedFrequencyKilohertz()));
        bw.append(SEPARATOR);

        bw.append(encodeCoordinate(client.getLatitude()));
        bw.append(SEPARATOR);

        bw.append(encodeCoordinate(client.getLongitude()));
        bw.append(SEPARATOR);

        bw.append(Integer.toString(client.getAltitudeFeet()));
        bw.append(SEPARATOR);

        bw.append(toStringOrDefaultIfNegative(client.getGroundSpeed(), ""));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getAircraftType()));
        bw.append(SEPARATOR);

        bw.append(Integer.toString(client.getFiledTrueAirSpeed()));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getFiledDepartureAirportCode()));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getRawFiledAltitude()));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getFiledDestinationAirportCode()));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getServerId()));
        bw.append(SEPARATOR);

        bw.append(toStringOrDefaultIfNegative(client.getProtocolVersion(), DEFAULT_CLIENT_PROTOCOL_VERSION));
        bw.append(SEPARATOR);

        bw.append(encodeControllerRating(client.getControllerRating()));
        bw.append(SEPARATOR);

        bw.append(encodeTransponder(client.getTransponderCodeDecimal()));
        bw.append(SEPARATOR);

        bw.append(encodeFacilityType(client.getFacilityType()));
        bw.append(SEPARATOR);

        bw.append(toStringOrDefaultIfNegative(client.getVisualRange(), DEFAULT_CLIENT_VISUAL_RANGE));
        bw.append(SEPARATOR);

        bw.append(toStringOrDefaultIfNegative(client.getFlightPlanRevision(), DEFAULT_CLIENT_FLIGHT_PLAN_REVISION));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getRawFlightPlanType()));
        bw.append(SEPARATOR);

        bw.append(toStringOrDefaultIfNegative( //
            client.getRawDepartureTimePlanned(), //
            DEFAULT_CLIENT_DEPARTURE_TIME_PLANNED //
        ));
        bw.append(SEPARATOR);

        bw.append(toStringOrDefaultIfNegative( //
            client.getRawDepartureTimeActual(), //
            DEFAULT_CLIENT_DEPARTURE_TIME_ACTUAL //
        ));
        bw.append(SEPARATOR);

        bw.append(encodeHours(client.getFiledTimeEnroute()));
        bw.append(SEPARATOR);

        bw.append(encodeMinutes(client.getFiledTimeEnroute()));
        bw.append(SEPARATOR);

        bw.append(encodeHours(client.getFiledTimeFuel()));
        bw.append(SEPARATOR);

        bw.append(encodeMinutes(client.getFiledTimeFuel()));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getFiledAlternateAirportCode()));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getFlightPlanRemarks()));
        bw.append(SEPARATOR);

        bw.append(sanitize(client.getFiledRoute()));
        bw.append(SEPARATOR);

        // airport coordinates - those were never seen in the wild (always just 0) and
        // removed from JSON
        bw.append("0");
        bw.append(SEPARATOR);
        bw.append("0");
        bw.append(SEPARATOR);
        bw.append("0");
        bw.append(SEPARATOR);
        bw.append("0");
        bw.append(SEPARATOR);

        bw.append(encodeControllerMessage(client.getControllerMessage()));
        bw.append(SEPARATOR);

        bw.append(encodeControllerMessageUpdateTimestamp(client));
        bw.append(SEPARATOR);

        bw.append(encodeLogOnTimestamp(client));
        bw.append(SEPARATOR);

        bw.append(toStringOrDefaultIfNegative(client.getHeading(), DEFAULT_CLIENT_HEADING));
        bw.append(SEPARATOR);

        bw.append(encodeQnhInchMercury(client.getQnhInchMercury()));
        bw.append(SEPARATOR);

        bw.append(toStringOrDefaultIfNegative(client.getQnhHectopascal(), DEFAULT_CLIENT_QNH_HECTOPASCALS));
        bw.append(SEPARATOR);

        bw.append(LINE_END);
    }

    private String encodeQnhInchMercury(double qnh) {
        if (Double.isNaN(qnh)) {
            // NaN is used to indicate that no QNH is available
            return DEFAULT_CLIENT_QNH_INCH_MERCURY;
        }

        return String.format("%.2f", qnh);
    }

    private CharSequence encodeLogOnTimestamp(Client client) {
        Instant logonTime = client.getLogonTime();
        boolean isOnline = (client.getRawClientType() != ClientType.PILOT_PREFILED);
        if (!isOnline || (logonTime == null)) {
            return "";
        }

        return TIME_FORMATTER.format(logonTime);
    }

    private CharSequence encodeControllerMessageUpdateTimestamp(Client client) {
        ClientType clientType = client.getRawClientType();
        boolean isATC = (clientType == ClientType.ATC_CONNECTED) || (clientType == ClientType.ATIS);
        boolean hasControllerMessage = !client.getControllerMessage().isEmpty();

        Instant timestamp = client.getLastUpdated();
        boolean isTimestampAllowed = (isATC && hasControllerMessage);
        if (!isTimestampAllowed || (timestamp == null)) {
            return "";
        }

        return TIME_FORMATTER.format(timestamp);
    }

    private String encodeControllerMessage(String msg) {
        return sanitize(msg.replace("\n", CONTROLLER_MESSAGE_LINEBREAK));
    }

    private String encodeMinutes(Duration duration) {
        if (duration == null) {
            return "";
        }

        return Long.toString(duration.toMinutes() % 60);
    }

    private String encodeHours(Duration duration) {
        if (duration == null) {
            return "";
        }

        return Long.toString(duration.toHours());
    }

    private String encodeFacilityType(FacilityType facilityType) {
        if (facilityType == null) {
            facilityType = FacilityType.OBSERVER;
        }

        return Integer.toString(facilityType.getLegacyId());
    }

    private String encodeTransponder(int transponderCodeDecimal) {
        if (transponderCodeDecimal < 0) {
            return "0";
        }

        // TODO: check what is correct - 4-digit padded or not?
        // return String.format("%04d", transponderCodeDecimal);
        return Integer.toString(transponderCodeDecimal);
    }

    private String encodeControllerRating(ControllerRating controllerRating) {
        if (controllerRating == null) {
            controllerRating = ControllerRating.OBS;
        }

        return Integer.toString(controllerRating.getLegacyId());
    }

    private String toStringOrDefaultIfNegative(int x, String defaultValue) {
        if (x < 0) {
            return defaultValue;
        }

        return Integer.toString(x);
    }

    private CharSequence encodeCoordinate(double coordinate) {
        if (Double.isNaN(coordinate)) {
            // NaN is used to indicate that no coordinate is available
            return "";
        }

        return String.format("%.5f", coordinate);
    }

    private String encodeFrequency(int frequency) {
        if (frequency < 0) {
            // negative means not set; leave empty
            return "";
        }

        return String.format("%d.%03d", frequency / 1000, frequency % 1000);
    }

    private String encodeClientType(ClientType clientType) {
        switch (clientType) {
            case PILOT_CONNECTED:
                return "PILOT";

            case ATIS:
            case ATC_CONNECTED:
                return "ATC";

            case PILOT_PREFILED:
                return "";

            default:
                throw new IllegalArgumentException("unsupported client type: " + clientType);
        }
    }

    private String sanitize(String s) {
        // TODO: sanitize more characters (line break etc.)

        if (s == null) {
            return "";
        }

        if (s.indexOf(SEPARATOR) < 0) {
            return s;
        }

        return s.replace(SEPARATOR, ' ');
    }

    private void encodeServersSection(DataFile content, BufferedWriter bw) throws IOException {
        encodeSectionStart("SERVERS", bw);

        for (FSDServer server : content.getFsdServers()) {
            encodeFsdServer(server, bw);
        }
    }

    private void encodeFsdServer(FSDServer server, BufferedWriter bw) throws IOException {
        bw.append(sanitize(server.getName()));
        bw.append(SEPARATOR);

        bw.append(sanitize(server.getAddress()));
        bw.append(SEPARATOR);

        bw.append(sanitize(server.getLocation()));
        bw.append(SEPARATOR);

        bw.append(sanitize(server.getId()));
        bw.append(SEPARATOR);

        bw.append(server.isClientConnectionAllowed() ? "1" : "0");
        bw.append(SEPARATOR);

        bw.append(LINE_END);
    }

    @Override
    public void serialize(DataFile content, OutputStream os) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.ISO_8859_1);
            BufferedWriter bw = new BufferedWriter(osw);

            // Header as present in last revision (including whitespace). Not sure why this
            // was kept while other headers had been removed; maybe some client needs these?
            // Let's do some cargo cult...
            bw.append(
                "; !CLIENTS section -         " //
                    + "callsign:cid:realname:clienttype:frequency:latitude:longitude:altitude:groundspeed:" //
                    + "planned_aircraft:planned_tascruise:planned_depairport:planned_altitude:planned_destairport:" //
                    + "server:protrevision:rating:transponder:facilitytype:visualrange:" //
                    + "planned_revision:planned_flighttype:planned_deptime:planned_actdeptime:planned_hrsenroute:planned_minenroute:" //
                    + "planned_hrsfuel:planned_minfuel:planned_altairport:planned_remarks:planned_route:" //
                    + "planned_depairport_lat:planned_depairport_lon:planned_destairport_lat:planned_destairport_lon:" //
                    + "atis_message:time_last_atis_received:time_logon:heading:QNH_iHg:QNH_Mb:" //
            );
            bw.append(LINE_END);

            encodeGeneralSection(content, bw);
            encodeClientSection(content, bw);
            encodeServersSection(content, bw);
            encodePrefileSection(content, bw);

            bw.flush();
            osw.flush();
        } catch (Exception ex) {
            LOGGER.warn("Serialization of legacy DataFile failed", ex);
        }
    }

}
