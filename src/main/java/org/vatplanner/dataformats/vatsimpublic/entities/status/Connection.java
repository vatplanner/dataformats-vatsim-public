package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.time.Instant;

/**
 * Holds information regarding a single connection to VATSIM.
 */
public class Connection {

    private final Member member;
    private final Instant logonTime;

    private Report firstReport;
    private Report lastReport;

    private String realName;
    private String homeBase;
    private ControllerRating rating;

    private String serverId;
    private int protocolVersion;

    /**
     * Creates a new connection record.
     *
     * @param member member holding this connection
     * @param logonTime timestamp of this connection's login
     */
    public Connection(Member member, Instant logonTime) {
        this.member = member;
        this.logonTime = logonTime;
    }

    /**
     * Returns the member holding this connection.
     *
     * @return member holding this connection
     */
    public Member getMember() {
        return member;
    }

    /**
     * Returns the first processed {@link Report} this connection appears in.
     *
     * @return first processed report this connection appears in
     */
    public Report getFirstReport() {
        return firstReport;
    }

    /**
     * Returns the last processed {@link Report} (so far) this connection
     * appears in.
     *
     * @return last processed report this connection appears in
     */
    public Report getLastReport() {
        return lastReport;
    }

    /**
     * Updates first/last report if the given report exceeds currently known
     * time span.
     *
     * @param report report to process updates for
     * @return this instance for method-chaining
     */
    public Connection seenInReport(Report report) {
        Instant recordTime = report.getRecordTime();

        if ((firstReport == null) || recordTime.isBefore(firstReport.getRecordTime())) {
            firstReport = report;
        }

        if ((lastReport == null) || recordTime.isAfter(lastReport.getRecordTime())) {
            lastReport = report;
        }

        return this;
    }

    /**
     * Returns the timestamp of initiating this client connection.
     *
     * @return timestamp of initiation of client connection
     */
    public Instant getLogonTime() {
        return logonTime;
    }

    /**
     * Returns the user's real name as entered for this connection. This may be
     * different from the user's actual name as it can be decided on each
     * connection and is not checked. Since 2019 VATSIM Code of Conduct also
     * permits to use a shortened name, just the given name or the VATSIM ID in
     * place of the full name previously required. This information thus only
     * has limited value.
     *
     * @return user's "real name" as entered for this connection
     */
    public String getRealName() {
        return realName;
    }

    public Connection setRealName(String realName) {
        this.realName = realName;
        return this;
    }

    /**
     * Returns the "home base" the user has entered for this connection. In
     * theory this should be a 4-letter ICAO code for an airport. While most
     * pilots set this to the airport closest to their actual home, it can be
     * set to anything and thus only holds limited value.
     *
     * @return "home base" entered for this connection
     */
    public String getHomeBase() {
        return homeBase;
    }

    public Connection setHomeBase(String homeBase) {
        this.homeBase = homeBase;
        return this;
    }

    /**
     * Returns the controller rating effective for this connection.
     * <p>
     * Controller rating is being used as ATC/network permission level, see
     * {@link ControllerRating} for details.
     * </p>
     * <p>
     * This is only meaningful for ATC clients and only for one session. If an
     * ATC-permitted user is connected as a pilot, rating will always be
     * {@link ControllerRating#OBS} for the pilot session regardless of user's
     * actual controller rating.
     * </p>
     *
     * @return effective controller rating for this connection
     */
    public ControllerRating getRating() {
        return rating;
    }

    public Connection setRating(ControllerRating rating) {
        this.rating = rating;
        return this;
    }

    /**
     * Returns the ID of the currently connected server.
     *
     * @return ID of currently connected server; may be null
     */
    public String getServerId() {
        return serverId;
    }

    public Connection setServerId(String serverId) {
        // TODO: parser result may be null; ignore null - we may have seen a ghost, importer should set server ID for every report again
        this.serverId = serverId;
        return this;
    }

    /**
     * Returns the client's protocol version.
     *
     * @return client's protocol version
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    public Connection setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    // TODO: unit tests
}
