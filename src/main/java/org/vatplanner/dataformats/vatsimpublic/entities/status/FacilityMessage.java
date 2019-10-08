package org.vatplanner.dataformats.vatsimpublic.entities.status;

/**
 * Holds a message published for a facility. Facility messages are free text and
 * usually contain either controller information or ATIS messages.
 */
public class FacilityMessage {

    private Facility facility;
    private Report reportFirstSeen;
    private String message;

    /**
     * Returns the facility who published the message.
     *
     * @return facility who published the message
     */
    public Facility getFacility() {
        return facility;
    }

    public FacilityMessage setFacility(Facility facility) {
        this.facility = facility;
        // TODO: add message to facility
        return this;
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
        // TODO: implement remember first report
        return this;
    }

    public String getMessage() {
        return message;
    }

    public FacilityMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    // TODO: unit tests
}
