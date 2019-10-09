package org.vatplanner.dataformats.vatsimpublic.entities.status;

/**
 * Holds a message published for a facility. Facility messages are free text and
 * usually contain either controller information or ATIS messages. Data files
 * may or may not provide a "last updated" timestamp which is not tracked as
 * it's availability is not guaranteed and report recording time should
 * generally be sufficient time information.
 */
public class FacilityMessage {

    private final Facility facility;

    private Report reportFirstSeen;
    private String message;

    /**
     * Creates a new message posted for a facility.
     *
     * @param facility facility who published the message
     */
    public FacilityMessage(Facility facility) {
        this.facility = facility;
    }

    /**
     * Returns the facility who published the message.
     *
     * @return facility who published the message
     */
    public Facility getFacility() {
        return facility;
    }

    /**
     * Returns the first processed {@link Report} this message appears in.
     *
     * @return first processed report this message appears in
     */
    public Report getReportFirstSeen() {
        return reportFirstSeen;
    }

    /**
     * Updates first report if the given report is older than currently known.
     *
     * @param report report to process update for
     * @return this instance for method-chaining
     */
    public FacilityMessage seenInReport(Report report) {
        if ((reportFirstSeen == null) || report.getRecordTime().isBefore(reportFirstSeen.getRecordTime())) {
            reportFirstSeen = report;
        }

        return this;
    }

    /**
     * Returns the message content.
     *
     * @return message content
     */
    public String getMessage() {
        return message;
    }

    public FacilityMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    // TODO: unit tests
}
