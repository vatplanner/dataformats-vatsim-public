package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.time.Instant;
import static java.util.Collections.unmodifiableSortedSet;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(Facility.class);

    private Connection connection;
    private final String name;
    private FacilityType type;
    private int frequencyKilohertz = -1; // NOTE: information may change or be multiplied with future "Audio for VATSIM" developments (frequency coupling etc.)
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
     * @throws IllegalArgumentException if facility name is null or empty
     */
    public Facility(String name) {
        if (name == null) {
            throw new IllegalArgumentException("facility names must not be null");
        }

        this.name = normalizeFacilityName(name);

        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("facility names must not be empty");
        }
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
        // NOTE: handling of frequencies may change in future AFV developments
        return ((type == null) || (type != FacilityType.OBSERVER))
                && ((frequencyKilohertz <= 0) || isValidFrequency(frequencyKilohertz));
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
     * Returns the first seen frequency in kilohertz (kHz) as indicated by
     * status report. Invalid frequencies may be omitted on import. Only the
     * first valid frequency is retained, later frequency changes are ignored.
     *
     * <p>
     * Also note that since Audio for VATSIM deployment the single reported
     * primary controller frequency in data files has become rather inaccurate
     * as controllers may use features such as frequency coupling which are (as
     * of December 2019) not represented in data files. Complete information may
     * be added at a later time by AFV development team but actual
     * representation is unclear (multiplication as "pseudo stations" etc.).
     * </p>
     *
     * @return first seen frequency in kilohertz (kHz); may be zero or negative
     * if information has been removed
     */
    public int getFrequencyKilohertz() {
        return frequencyKilohertz;
    }

    protected Facility setFrequencyKilohertz(int frequencyKilohertz) {
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
        } else if (frequencyKilohertz != this.frequencyKilohertz) {
            LOGGER.trace("facility {} already recorded with frequency {}, ignoring change to {}", name, this.frequencyKilohertz, frequencyKilohertz);
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

    /**
     * Normalizes a facility name. Normalization is performed by trimming the
     * input and converting it to upper case.
     *
     * @param name facility name to normalize
     * @return normalized facility name
     */
    public static String normalizeFacilityName(String name) {
        return name.trim().toUpperCase();
    }

    // TODO: unit tests
}
