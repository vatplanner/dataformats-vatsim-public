package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static java.util.Collections.unmodifiableSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Identifies a VATSIM member and keeps track of all associated flights and
 * facilities.
 */
public class Member {

    private int vatsimId;
    private Set<Flight> flights;
    private Set<Facility> facilities;

    /**
     * Returns the member's VATSIM ID.
     *
     * @return member's VATSIM ID
     */
    public int getVatsimId() {
        return vatsimId;
    }

    /**
     * Returns all flights currently recorded for this member.
     *
     * @return all flights currently recorded for this member; never null
     */
    public Set<Flight> getFlights() {
        if (flights == null) {
            return unmodifiableSet(new HashSet<>());
        }

        return unmodifiableSet(flights);
    }

    /**
     * Adds the given flight to this member's records.
     *
     * @param flight flight to be recorded
     * @return this instance for method-chaining
     */
    public Member addFlight(Flight flight) {
        if (flights == null) {
            flights = new HashSet<>();
        }

        flights.add(flight);
        // TODO: set/check member on flight?

        return this;
    }

    /**
     * Returns all facilities currently recorded for this member.
     *
     * @return all facilities currently recorded for this member; never null
     */
    public Set<Facility> getFacilities() {
        if (facilities == null) {
            return unmodifiableSet(new HashSet<>());
        }

        return unmodifiableSet(facilities);
    }

    /**
     * Adds the given facility to this member's records.
     *
     * @param facility facility to be recorded
     * @return this instance for method-chaining
     */
    public Member addFacility(Facility facility) {
        if (facilities == null) {
            facilities = new HashSet<>();
        }

        facilities.add(facility);
        // TODO: set/check member on facility connection?

        return this;
    }

    // TODO: unit tests
}
