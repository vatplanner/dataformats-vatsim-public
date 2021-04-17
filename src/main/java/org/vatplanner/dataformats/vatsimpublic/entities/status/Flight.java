package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableSortedSet;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.vatplanner.dataformats.vatsimpublic.entities.TimeSpan;

/**
 * Data container for all information related to a single flight. Flights can be
 * pre-filed which will cause them to be listed without a connection and without
 * any track points before the user logs in. Because flight simulators continue
 * to run offline, flights may span multiple connections in case of a connection
 * loss.
 *
 * <p>
 * Flights can only be identified uniquely at time of each report by a tuple of
 * both member/VATSIM ID and callsign. Just using one criteria is not
 * sufficient:
 * </p>
 * <ul>
 * <li>Members are technically able (but in most cases just not permitted) to
 * connect multiple times, so the VATSIM ID alone does not uniquely identify a
 * flight.</li>
 * <li>Different members can pre-file flight plans for the same callsign and yet
 * another user can then connect. VATSIM does not copy flight plans across
 * different members, information may be totally different. Neither will
 * pre-filed flight plans be revoked upon connection of another member using
 * that same callsign.</li>
 * </ul>
 */
public class Flight {

    private final Member member;
    private final String callsign;

    private SortedSet<Connection> connections;
    private SortedSet<FlightPlan> flightPlans;
    private SortedSet<TrackPoint> track;
    private Set<Report> reconstructedReports;
    private Map<TrackPoint, FlightEvent> events;

    private static final Comparator<Connection> CONNECTIONS_COMPARATOR = (Connection x, Connection y) -> {
        // ascending order of logon time
        return x.getLogonTime().compareTo(y.getLogonTime());
    };

    private static final Comparator<FlightPlan> FLIGHT_PLANS_COMPARATOR = (FlightPlan x, FlightPlan y) -> {
        // ascending order of revisions
        return Integer.compare(x.getRevision(), y.getRevision());
    };

    private static final Comparator<TrackPoint> TRACK_POINT_COMPARATOR = (TrackPoint x, TrackPoint y) -> {
        // ascending order of record time
        return x.getReport().getRecordTime().compareTo(y.getReport().getRecordTime());
    };

    /**
     * Creates a new flight. Flights can only be identified uniquely at time of each
     * report by a tuple of both member/VATSIM ID and callsign. See class JavaDoc
     * for further information.
     *
     * @param member member performing or filing this flight
     * @param callsign actual (connected) or intended (pre-filed) callsign to be
     *        used on this flight
     */
    public Flight(Member member, String callsign) {
        this.member = member;
        this.callsign = callsign;

        // TODO: normalize callsign (trim, upper case)
        // TODO: add flight to member; must not loop back; document
    }

    /**
     * Returns the member performing this flight.
     *
     * @return member performing this flight
     */
    public Member getMember() {
        return member;
    }

    /**
     * Returns all connections related to this flight. Connections are returned
     * sorted by their log on time in ascending order.
     *
     * @return all connections related to this flight ordered by log on time; never
     *         null
     */
    public SortedSet<Connection> getConnections() {
        if (connections == null) {
            return unmodifiableSortedSet(new TreeSet<>());
        }

        return unmodifiableSortedSet(connections);
    }

    /**
     * Returns the latest connection determined by log on time.
     *
     * @return latest connection by log on time; null if no connection has been
     *         recorded
     */
    public Connection getLatestConnection() {
        if (connections == null) {
            return null;
        }

        return connections.last();
    }

    /**
     * Adds a connection to this flight if it has not yet been recorded. Attempting
     * to add an existing connection again has no effect.
     *
     * @param connection connection to be added
     * @return this instance for method-chaining
     */
    public Flight addConnection(Connection connection) {
        if (connections == null) {
            connections = new TreeSet<>(CONNECTIONS_COMPARATOR);
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
     * Adds a new flight plan to this flight. Flight plans are uniquely identified
     * by their revision per flight. If another flight plan is requested to be added
     * having the same revision number, the operation will fail.
     *
     * @param flightPlan flight plan to be added
     * @return this instance for method-chaining
     * @throws UnsupportedOperationException if a different flight plan of that
     *         revision has already been added
     */
    public Flight addFlightPlan(FlightPlan flightPlan) {
        // TODO: check if flight plan already exists and fail if other instance

        if (flightPlans == null) {
            flightPlans = new TreeSet<>(FLIGHT_PLANS_COMPARATOR);
        }

        flightPlans.add(flightPlan);

        // TODO: set flight on flightplan; must not loop back; document
        return this;
    }

    /**
     * Returns the callsign used for this flight.
     *
     * @return callsign used for this flight
     */
    public String getCallsign() {
        return callsign;
    }

    /**
     * Returns all track points in order of recording time, forming the recorded
     * track.
     *
     * @return all track points in order of recording time
     */
    public SortedSet<TrackPoint> getTrack() {
        if (track == null) {
            return unmodifiableSortedSet(new TreeSet<>());
        }

        return unmodifiableSortedSet(track);
    }

    /**
     * Adds a single point to the track. Track points need to reference a timed
     * {@link Report} in order to be usable for a track.
     *
     * @param point point to add to this flight's track
     * @return this instance for method-chaining
     * @throws IllegalArgumentException if given track point does not reference a
     *         timed {@link Report}
     */
    public Flight addTrackPoint(TrackPoint point) {
        if (track == null) {
            track = new TreeSet<>(TRACK_POINT_COMPARATOR);
        }

        // TODO: fail on missing recording time?
        track.add(point);

        return this;
    }

    /**
     * Computes the earliest time at which the flight appeared in records.
     *
     * @return earliest time flight appeared in records
     */
    public Instant getEarliestVisibleTime() {
        Instant earliest = null;

        if (connections != null) {
            for (Connection connection : connections) {
                Instant logOnTime = connection.getLogonTime();
                if ((earliest == null) || logOnTime.isBefore(earliest)) {
                    earliest = logOnTime;
                }

                Instant recordTime = connection.getFirstReport().getRecordTime();
                if ((earliest == null) || recordTime.isBefore(earliest)) {
                    earliest = recordTime;
                }
            }
        }

        if (flightPlans != null) {
            for (FlightPlan flightPlan : flightPlans) {
                Instant recordTime = flightPlan.getReportFirstSeen().getRecordTime();
                if ((earliest == null) || recordTime.isBefore(earliest)) {
                    earliest = recordTime;
                }
            }
        }

        return earliest;
    }

    /**
     * Computes the latest time at which the flight appeared in records.
     *
     * @return latest time flight appeared in records
     */
    public Instant getLatestVisibleTime() {
        Instant latest = null;

        if (connections != null) {
            for (Connection connection : connections) {
                Instant logOnTime = connection.getLogonTime();
                if ((latest == null) || logOnTime.isAfter(latest)) {
                    latest = logOnTime;
                }

                // TODO: looks odd, I suppose we should check latest, not first report?
                Instant recordTime = connection.getFirstReport().getRecordTime();
                if ((latest == null) || recordTime.isAfter(latest)) {
                    latest = recordTime;
                }
            }
        }

        if (flightPlans != null) {
            for (FlightPlan flightPlan : flightPlans) {
                Instant recordTime = flightPlan.getReportFirstSeen().getRecordTime();
                if ((latest == null) || recordTime.isAfter(latest)) {
                    latest = recordTime;
                }
            }
        }

        return latest;
    }

    /**
     * Computes the time span from first connection or first record of pre-filed
     * flight plan to last seen record of connection.
     *
     * @return time span between first connection or pre-filing to last seen
     *         connection record
     */
    public TimeSpan getVisibleTimeSpan() {
        TimeSpan timeSpan = new TimeSpan();

        if (connections != null) {
            for (Connection connection : connections) {
                timeSpan.expandTo(connection.getLogonTime());
                timeSpan.expandTo(connection.getFirstReport().getRecordTime());
                timeSpan.expandTo(connection.getLastReport().getRecordTime());
            }
        }

        if (flightPlans != null) {
            for (FlightPlan flightPlan : flightPlans) {
                timeSpan.expandTo(flightPlan.getReportFirstSeen().getRecordTime());
            }
        }

        return timeSpan;
    }

    /**
     * Checks if the flight needed to be reconstructed for given report.
     *
     * @param report report to check for reconstruction
     * @return true if flight data needed reconstruction for given report, false if
     *         not
     */
    public boolean isReconstructed(Report report) {
        if (reconstructedReports == null) {
            return false;
        }

        return reconstructedReports.contains(report);
    }

    /**
     * Records for the given report that flight data needs to be reconstructed.
     *
     * @param report report flight data needs to be reconstructed for
     */
    public void markAsReconstructed(Report report) {
        if (reconstructedReports == null) {
            reconstructedReports = new HashSet<>();
        }

        reconstructedReports.add(report);
    }

    /**
     * Returns all reports which flight data needed to be reconstructed for.
     *
     * @return reports which required reconstruction; random order, never null
     */
    public Set<Report> getReconstructedReports() {
        if (reconstructedReports == null) {
            return unmodifiableSet(new HashSet<>());
        }

        return unmodifiableSet(reconstructedReports);
    }

    /**
     * Records given event to have occurred at track point. Track point must have
     * been registered to track before. Every track point must only have at most one
     * event assigned.
     *
     * @param trackPoint track point to record event for
     * @param event event to be recorded
     * @throws IllegalArgumentException if any argument is null, point is not on
     *         track or point already has an event assigned
     */
    public void markEvent(TrackPoint trackPoint, FlightEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }

        if (trackPoint == null) {
            throw new IllegalArgumentException("track point must not be null");
        }

        if (!track.contains(trackPoint)) {
            throw new IllegalArgumentException("point must be recorded on track");
        }

        if (events == null) {
            events = new HashMap<>();
        }

        if (events.containsKey(trackPoint)) {
            throw new IllegalArgumentException(//
                "track point is already marked with another event (old "
                    + events.get(trackPoint) //
                    + ", new " //
                    + event //
                    + ")" //
            );
        }

        events.put(trackPoint, event);
    }

    /**
     * Returns all events recorded by track point.
     *
     * @return all events recorded by track point, never null
     */
    public Map<TrackPoint, FlightEvent> getEvents() {
        if (events == null) {
            return unmodifiableMap(new HashMap<>());
        }

        return unmodifiableMap(events);
    }

    /**
     * Checks if the flight is airborne as of last recorded track point and event.
     *
     * @return true if airborne, false if not taken off yet or already landed
     */
    public boolean isAirborne() {
        if (events == null) {
            return false;
        }

        return events.containsValue(FlightEvent.AIRBORNE) && !events.containsValue(FlightEvent.LANDED);
    }

    /**
     * Checks if the flight has landed as of last recorded track point and event.
     *
     * @return true if landed, false if still airborne or not taken off yet
     */
    public boolean hasLanded() {
        if (events == null) {
            return false;
        }

        return events.containsValue(FlightEvent.LANDED);
    }

    // TODO: unit tests
    /*
     * TODO: add connected aircraft type because it may be different from prefiling?
     * (is this actually still needed?)
     */
}
