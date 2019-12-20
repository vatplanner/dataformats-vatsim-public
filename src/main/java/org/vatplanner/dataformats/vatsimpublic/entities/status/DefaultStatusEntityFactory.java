package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.time.Instant;

/**
 * Factory producing default implementations of graph entities.
 */
public class DefaultStatusEntityFactory implements StatusEntityFactory {

    @Override
    public Connection createConnection(Member member, Instant logonTime) {
        return new Connection(member, logonTime);
    }

    @Override
    public Facility createFacility(String name) {
        return new Facility(name);
    }

    @Override
    public FacilityMessage createFacilityMessage(Facility facility) {
        return new FacilityMessage(facility);
    }

    @Override
    public Flight createFlight(Member member, String callsign) {
        return new Flight(member, callsign);
    }

    @Override
    public FlightPlan createFlightPlan(Flight flight, int revision) {
        return new FlightPlan(flight, revision);
    }

    @Override
    public Member createMember(int vatsimId) {
        return new Member(vatsimId);
    }

    @Override
    public Report createReport(Instant recordTime) {
        return new Report(recordTime);
    }

    @Override
    public TrackPoint createTrackPoint(Report report) {
        return new TrackPoint(report);
    }
}
