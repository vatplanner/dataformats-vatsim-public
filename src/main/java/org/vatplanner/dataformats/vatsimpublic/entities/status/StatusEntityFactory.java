package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.time.Instant;

/**
 * Interface for factories producing entities used for status graphs. This
 * abstraction is used to provide extended entity implementations for example to
 * keep track of database relations. Use {@link DefaultStatusEntityFactory} if
 * default implementations are sufficient.
 *
 * @see DefaultStatusEntityFactory
 */
public interface StatusEntityFactory {

    /**
     * Creates a new instance of {@link Connection}.
     *
     * <p>
     * Please refer to {@link Connection#Connection(Member, Instant)} for latest
     * complete documentation.
     * </p>
     *
     * @param member member holding this connection
     * @param logonTime timestamp of this connection's login
     * @return new instance of {@link Connection} created with given arguments
     * @see Connection#Connection(Member, Instant)
     */
    Connection createConnection(Member member, Instant logonTime);

    /**
     * Creates a new instance of {@link Facility}.
     *
     * <p>
     * Please refer to {@link Facility#Facility(String)} for latest complete
     * documentation.
     * </p>
     *
     * @param name raw facility name
     * @return new instance of {@link Facility} created with given argument
     * @see Facility#Facililty(String)
     */
    Facility createFacility(String name);

    /**
     * Creates a new instance of {@link FacilityMessage}.
     *
     * <p>
     * Please refer to {@link FacilityMessage#FacilityMessage(Facility)} for
     * latest complete documentation.
     * </p>
     *
     * @param facility facility who published the message
     * @return new instance of {@link FacilityMessage} created with given
     * argument
     * @see FacilityMessage#FacilityMessage(Facility)
     */
    FacilityMessage createFacilityMessage(Facility facility);

    /**
     * Creates a new instance of {@link Flight}.
     *
     * <p>
     * Please refer to {@link Flight#Flight(Member, String)} for latest complete
     * documentation.
     * </p>
     *
     * @param member member performing or filing this flight
     * @param callsign actual (connected) or intended (pre-filed) callsign to be
     * used on this flight
     * @return new instance of {@link Flight} created with given arguments
     * @see Flight#Flight(Member, String)
     */
    Flight createFlight(Member member, String callsign);

    /**
     * Creates a new instance of {@link FlightPlan}.
     *
     * <p>
     * Please refer to {@link FlightPlan#FlightPlan(Flight, int)} for latest
     * complete documentation.
     * </p>
     *
     * @param flight the flight described by this flight plan
     * @param revision revision number to index flight plans by
     * @return new instance of {@link FlightPlan} created with given arguments
     * @see FlightPlan#FlightPlan(Flight, int)
     */
    FlightPlan createFlightPlan(Flight flight, int revision);

    /**
     * Creates a new instance of {@link Member}.
     *
     * <p>
     * Please refer to {@link Member#Member(int)} for latest complete
     * documentation.
     * </p>
     *
     * @param vatsimId VATSIM ID of member
     * @return new instance of {@link Member} created with given argument
     * @see Member#Member(int)
     */
    Member createMember(int vatsimId);

    /**
     * Creates a new instance of {@link Report}.
     *
     * <p>
     * Please refer to {@link Report#Report(Instant)} for latest complete
     * documentation.
     * </p>
     *
     * @param recordTime time of recording the report (must be unique in graph)
     * @return new instance of {@link Report} created with given argument
     * @see Report#Report(Instant)
     */
    Report createReport(Instant recordTime);

    /**
     * Creates a new instance of {@link TrackPoint}.
     *
     * <p>
     * Please refer to {@link TrackPoint#TrackPoint(Report)} for latest complete
     * documentation.
     * </p>
     *
     * @param report report where this point appeared in
     * @return new instance of {@link TrackPoint} created with given argument
     *
     */
    TrackPoint createTrackPoint(Report report);

}
