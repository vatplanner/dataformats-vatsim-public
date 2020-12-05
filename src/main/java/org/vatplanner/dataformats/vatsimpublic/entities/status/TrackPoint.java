package org.vatplanner.dataformats.vatsimpublic.entities.status;

/**
 * Groups all information about a single time-referenced point tracking an
 * aircraft.
 */
public class TrackPoint {

    private final Report report;
    private Flight flight;

    private GeoCoordinates geoCoordinates;
    private int heading = -1;
    private int groundSpeed = -1;
    private int transponderCode = -1;
    private BarometricPressure qnh;

    /**
     * Creates a new track point. A track point needs to be time-referenced to be
     * useful and thus needs to be linked to the report it appeared in.
     *
     * @param report report where this point appeared in
     */
    public TrackPoint(Report report) {
        this.report = report;
    }

    /**
     * Returns the report this track point appeared in.
     *
     * @return report this track point appeared in
     */
    public Report getReport() {
        return report;
    }

    /**
     * Returns the flight this track point belongs to.
     *
     * @return flight this track point belongs to
     */
    public Flight getFlight() {
        return flight;
    }

    public TrackPoint setFlight(Flight flight) {
        this.flight = flight;
        // TODO: add track point to flight
        return this;
    }

    /**
     * Returns the geographic coordinates (3-dimensional position) of this point.
     *
     * @return geographic coordinates (3-dimensional position)
     */
    public GeoCoordinates getGeoCoordinates() {
        return geoCoordinates;
    }

    public TrackPoint setGeoCoordinates(GeoCoordinates geoCoordinates) {
        this.geoCoordinates = geoCoordinates;
        return this;
    }

    /**
     * Returns the heading the aircraft was pointing to. Valid headings are limited
     * to 0..359 degrees.
     *
     * @return heading the aircraft was pointing to (0..359 degrees), negative if
     *         unavailable
     */
    public int getHeading() {
        return heading;
    }

    public TrackPoint setHeading(int heading) {
        // TODO: limit to 0..359
        this.heading = heading;
        return this;
    }

    /**
     * Returns the ground speed (in knots) the aircraft was moving at.
     *
     * @return ground speed (knots), negative if unavailable
     */
    public int getGroundSpeed() {
        return groundSpeed;
    }

    public TrackPoint setGroundSpeed(int groundSpeed) {
        // TODO: limit to positive numbers
        this.groundSpeed = groundSpeed;
        return this;
    }

    /**
     * Returns the aircraft's transponder code. Only 4-octal codes (0000..7777) are
     * valid as used in real-world but other codes may be indicated as well by
     * addons. Representation is always in decimal (human-readable) integers (code
     * 7777 = int 7777), not octal.
     *
     * @return transponder code as decimal integer (code 7777 = int 7777); may
     *         exceed 4-octal limits, negative if unavailable
     */
    public int getTransponderCode() {
        return transponderCode;
    }

    public TrackPoint setTransponderCode(int transponderCode) {
        this.transponderCode = transponderCode;
        return this;
    }

    /**
     * Returns the local QNH at the aircraft's position. This information is
     * provided by pilot clients and depends on the aircraft's location and the
     * client's weather simulation.
     *
     * @return local QNH at the aircraft's position, null if unavailable
     */
    public BarometricPressure getQnh() {
        return qnh;
    }

    public TrackPoint setQnh(BarometricPressure qnh) {
        this.qnh = qnh;
        return this;
    }

    /**
     * Calculates the flight level at standard QNH. This is a proxy method for
     * easier access to {@link GeoCoordinates#toFlightLevel(BarometricPressure)}.
     *
     * @return flight level at standard QNH; negative if unavailable
     * @see GeoCoordinates#toFlightLevel(BarometricPressure)
     */
    public int getFlightLevel() {
        if (geoCoordinates == null) {
            return -1;
        }

        return geoCoordinates.toFlightLevel(qnh);
    }

    // TODO: unit tests
}
