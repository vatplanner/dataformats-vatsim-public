package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.util.ArrayList;
import static java.util.Collections.unmodifiableList;
import java.util.List;

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
    private String name;
    private FacilityType type;
    private int frequencyKilohertz; // NOTE: information may be discontinued or multiplied with "Audio for VATSIM" (frequency coupling)
    private List<FacilityMessage> messages;

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

    public Facility setName(String name) {
        this.name = name;
        return this;
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
     * Returns all messages recorded for this facility. Messages are returned in
     * order of insertion.
     *
     * @return all messages recorded for this facility, never null
     */
    public List<FacilityMessage> getMessages() {
        if (messages == null) {
            return unmodifiableList(new ArrayList<>());
        }

        return unmodifiableList(messages);
    }

    /**
     * Adds a message to this facility.
     *
     * @param message message to be added
     * @return this instance for method-chaining
     */
    public Facility addMessage(FacilityMessage message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }

        // TODO: ensure message not already added
        messages.add(message);
        // TODO: set facility on message; must not loop back; document

        return this;
    }

    // TODO: unit tests
}
