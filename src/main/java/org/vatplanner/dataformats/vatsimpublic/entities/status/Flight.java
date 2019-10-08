package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableSortedSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Data container for all information related to a single flight. Flights can be
 * pre-filed which will cause them to be listed without a connection and without
 * any track points before the user logs in. Because flight simulators continue
 * to run offline, flights may span multiple connections in case of a connection
 * loss.
 */
public class Flight {

    private Member member;
    private Set<Connection> connections;
    private SortedSet<FlightPlan> flightPlans;
    private String callsign;
    private SortedSet<TrackPoint> track;

    private static final Comparator<FlightPlan> FLIGHT_PLANS_COMPARATOR = (FlightPlan x, FlightPlan y) -> {
        // TODO: implement, ascending order of revisions
        return 0;
    };

    private static final Comparator<TrackPoint> TRACK_POINT_COMPARATOR = (TrackPoint x, TrackPoint y) -> {
        // TODO: implement, ascending order of recording time
        return 0;
    };

    /**
     * Returns the member performing this flight.
     *
     * @return member performing this flight
     */
    public Member getMember() {
        return member;
    }

    public Flight setMember(Member member) {
        this.member = member;

        // TODO: add flight to member; must not loop back; document
        return this;
    }

    /**
     * Returns all connections related to this flight. Note that all connections
     * are unordered, remember to sort them if needed.
     *
     * @return all connections related to this flight in random order; never
     * null
     */
    public Set<Connection> getConnections() {
        if (connections == null) {
            return unmodifiableSet(new HashSet<>());
        }

        return unmodifiableSet(connections);
    }

    /**
     * Adds a connection to this flight if it has not yet been recorded.
     * Attempting to add an existing connection again has no effect.
     *
     * @param connection connection to be added
     * @return this instance for method-chaining
     */
    public Flight addConnection(Connection connection) {
        if (connections == null) {
            connections = new HashSet<>();
        }

        connections.add(connection);

        return this;
    }

    /**
     * Returns all flight plans filed for this flight. Flight plans are returned
     * sorted by their revision number in ascending order.
     *
     * @return all flight plans filed for this flight ordered by revision number
     */
    public SortedSet<FlightPlan> getFlightPlans() {
        if (flightPlans == null) {
            return unmodifiableSortedSet(new TreeSet<>());
        }

        return unmodifiableSortedSet(flightPlans);
    }

    /**
     * Adds a flight plan to this flight if it has not yet been recorded.
     * Attempting to add an existing flight plan again has no effect.
     *
     * @param flightPlan flight plan to be added
     * @return this instance for method-chaining
     */
    public Flight addFlightPlan(FlightPlan flightPlan) {
        if (flightPlans == null) {
            flightPlans = new TreeSet<>(FLIGHT_PLANS_COMPARATOR);
        }

        flightPlans.add(flightPlan);

        // TODO: set flight on flightplan; must not loop back; document
        return this;
    }

    /**
     * Sets the callsign used for this flight.
     *
     * @return this instance for method-chaining
     */
    public String getCallsign() {
        return callsign;
    }

    public Flight setCallsign(String callsign) {
        this.callsign = callsign;
        return this;
    }

    /**
     * Returns all track points in order of recording time, forming the recorded
     * track.
     *
     * @return all track points in order of recording time
     */
    public SortedSet<TrackPoint> getTrack() {
        if (track == null) {
            track = unmodifiableSortedSet(new TreeSet<>());
        }

        return unmodifiableSortedSet(track);
    }

    /**
     * Adds a single point to the track. Track points need to reference a timed
     * {@link Report} in order to be usable for a track.
     *
     * @param point point to add to this flight's track
     * @return this instance for method-chaining
     * @throws IllegalArgumentException if given track point does not reference
     * a timed {@link Report}
     */
    public Flight addTrackPoint(TrackPoint point) {
        if (track == null) {
            track = new TreeSet<>(TRACK_POINT_COMPARATOR);
        }

        // TODO: fail on missing recording time?
        track.add(point);

        return this;
    }

    // TODO: unit tests
}
