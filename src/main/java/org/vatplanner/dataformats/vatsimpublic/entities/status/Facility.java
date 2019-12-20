package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.time.Instant;
import static java.util.Collections.unmodifiableSortedSet;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A facility is a stationary client usually providing services to pilots. If
 * the facility is an {@link FacilityType#OBSERVER} or indicates an invalid
 * frequency, it is not providing ATC service. Being a client-specific
 * information means it is tied to a single {@link Connection}. If the
 * connection ends, ATC service terminates as well. Facilities are unique by
 * name at time of a status record. ATC names are well-defined by VACCs to link
 * them to air space definitions and frequencies.
 */
public class Facility {

    private Connection connection;
    private final String name;
    private FacilityType type;
    private int frequencyKilohertz; // NOTE: information may be discontinued or multiplied with "Audio for VATSIM" (frequency coupling)
    private final SortedSet<FacilityMessage> messagesSortedByRecordTime = new TreeSet<>(COMPARATOR_MESSAGE_REPORT_RECORD_TIME);

    private static final int LOWEST_VALID_FREQUENCY_KILOHERTZ = 118000;
    private static final int HIGHEST_VALID_FREQUENCY_KILOHERTZ = 136975;

    private static final Comparator<FacilityMessage> COMPARATOR_MESSAGE_REPORT_RECORD_TIME = (a, b) -> {
        return a.getReportFirstSeen().getRecordTime().compareTo(b.getReportFirstSeen().getRecordTime());
    };

    /**
     * Creates a new facility. If ATC service is provided, the name is
     * well-defined by VACCs and can be resolved to air spaces. Names are
     * mandatory and facilities are, at each time of record, uniquely identified
     * by their name.
     *
     * @param name raw facility name
     */
    public Facility(String name) {
        // TODO: reject empty/null names
        // TODO: normalize name (trim, upper case)
        this.name = name;
    }

    /**
     * Returns the associated client connection.
     *
     * @return associated client connection; may be null if information has been
     * removed
     */
    public Connection getConnection() {
        return connection;
    }

    public Facility setConnection(Connection connection) {
        this.connection = connection;

        // TODO: add facility to member if available from connection; must not loop back; document
        return this;
    }

    /**
     * Returns the raw facility name. If ATC service is provided, the name is
     * well-defined by VACCs and can be resolved to air spaces.
     *
     * @return raw facility name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the facility provides air traffic control (ATC) service. This
     * includes ATIS and FSS stations.
     * <p>
     * The check evaluates facility type and frequency. If any information is
     * missing, it will be skipped. If all information is missing, ATC service
     * will be assumed.
     * </p>
     *
     * @return true if facility provides ATC service; false if not
     */
    public boolean providesATCService() {
        // TODO: not OBSERVER and (if set) valid frequency
        return false;
    }

    /**
     * Returns the type of facility as indicated by status report.
     *
     * @return type of facility; may be null if information has been removed
     */
    public FacilityType getType() {
        return type;
    }

    public Facility setType(FacilityType type) {
        this.type = type;
        return this;
    }

    /**
     * Returns the frequency in kilohertz (kHz) as indicated by status report.
     *
     * @return frequency in kilohertz (kHz); may be zero or negative if
     * information has been removed
     */
    public int getFrequencyKilohertz() {
        return frequencyKilohertz;
    }

    public Facility setFrequencyKilohertz(int frequencyKilohertz) {
        this.frequencyKilohertz = frequencyKilohertz;
        return this;
    }

    /**
     * Remembers the given frequency if no valid frequency has been set yet.
     *
     * @param frequencyKilohertz frequency to set if previously set frequency
     * was invalid
     * @return this instance for method-chaining
     */
    public Facility seenOnFrequencyKilohertz(int frequencyKilohertz) {
        // QUESTION: frequency can change during session, record all?

        if (!isValidFrequency(this.frequencyKilohertz)) {
            setFrequencyKilohertz(frequencyKilohertz);
        }

        return this;
    }

    private boolean isValidFrequency(int frequencyKilohertz) {
        return (LOWEST_VALID_FREQUENCY_KILOHERTZ <= frequencyKilohertz) && (frequencyKilohertz <= HIGHEST_VALID_FREQUENCY_KILOHERTZ);
    }

    /**
     * Returns all messages recorded for this facility, sorted by report record
     * time.
     *
     * @return all messages recorded for this facility, never null
     */
    public SortedSet<FacilityMessage> getMessages() {
        return unmodifiableSortedSet(messagesSortedByRecordTime);
    }

    /**
     * Adds a message to this facility. See
     * {@link #seenMessage(Report, String, Instant)} for easier handling of
     * updates.
     *
     * @param message message to be added
     * @return this instance for method-chaining
     * @see #seenMessage(Report, String, Instant)
     */
    public Facility addMessage(FacilityMessage message) {
        // TODO: ensure message not already added
        messagesSortedByRecordTime.add(message);
        // TODO: set facility on message; must not loop back; document

        // QUESTION: remove in favor of seenMessage?
        return this;
    }

    /**
     * Checks if the message is already known and updates it, otherwise adds it
     * to the facility.
     *
     * @param report report the message appears in
     * @param content message content
     * @param factory to instantiate new message if needed
     * @return this instance for method-chaining
     */
    public Facility seenMessage(Report report, String content, StatusEntityFactory factory) {
        FacilityMessage lastMessage = null;
        if (!messagesSortedByRecordTime.isEmpty()) {
            lastMessage = messagesSortedByRecordTime.last();
        }

        boolean isLastMessage = (lastMessage != null)
                && content.equals(lastMessage.getMessage());

        if (isLastMessage) {
            lastMessage.seenInReport(report);
        } else {
            messagesSortedByRecordTime.add(
                    factory.createFacilityMessage(this)
                            .setMessage(content)
                            .seenInReport(report)
            );
        }

        return this;
    }

    // TODO: unit tests
}
