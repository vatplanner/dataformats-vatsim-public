package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.vatplanner.dataformats.vatsimpublic.utils.UnitConversion.feetToMeters;
import static org.vatplanner.dataformats.vatsimpublic.utils.UnitConversion.metersToFeet;

/**
 * Holds a 3-dimensional tuple of geographic coordinates.
 *
 * <ul>
 * <li>latitude: positive = north, negative = south (in degrees)</li>
 * <li>longitude: positive = east, negative = west (in degrees)</li>
 * <li>altitude: can be given in either feet or meters</li>
 * </ul>
 *
 * Altitude will be automatically converted by getter methods if specified in a
 * different unit.
 */
public class GeoCoordinates {

    private final double latitude;
    private final double longitude;
    private final int altitude;
    private final boolean isAltitudeUnitFeet;

    public static final boolean UNIT_FEET = true;
    public static final boolean UNIT_METERS = false;

    /**
     * Creates a new geographic coordinate.
     *
     * @param latitude           latitude in degrees (positive = north, negative = south)
     * @param longitude          longitude in degrees (positive = east, negative = west)
     * @param altitude           altitude value
     * @param isAltitudeUnitFeet altitude unit; use constants
     * @see #UNIT_FEET
     * @see #UNIT_METERS
     */
    public GeoCoordinates(double latitude, double longitude, int altitude, boolean isAltitudeUnitFeet) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.isAltitudeUnitFeet = isAltitudeUnitFeet;
    }

    /**
     * Returns the latitude in degrees.
     * <p>
     * Positive latitude is north of equator, negative latitude is south. Valid
     * value range is -90..90&deg;.
     * </p>
     *
     * @return latitude (-90..90&deg;)
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude in degrees.
     * <p>
     * Positive longitude is east of Prime Meridian, negative longitude is west.
     * Valid value range is -180..180&deg;.
     * </p>
     *
     * @return longitude (-180..180&deg;)
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Returns the altitude measured in feet. If altitude was given in meters, it
     * will be automatically converted.
     *
     * @return altitude in feet
     */
    public int getAltitudeFeet() {
        if (isAltitudeUnitFeet == UNIT_FEET) {
            return altitude;
        } else {
            return (int) Math.round(metersToFeet(altitude));
        }
    }

    /**
     * Returns the altitude measured in meters. If altitude was given in feet, it
     * will be automatically converted.
     *
     * @return altitude in meters
     */
    public int getAltitudeMeters() {
        if (isAltitudeUnitFeet == UNIT_METERS) {
            return altitude;
        } else {
            return (int) Math.round(feetToMeters(altitude));
        }
    }

    /**
     * Converts the altitude to a flight level (hundreds of feet at standard QNH).
     * The local QNH valid at current location is required to calculate this; it is
     * specific to the client's simulation environment, and available from the
     * associated {@link TrackPoint}.
     *
     * @param qnh local QNH specific to client's simulation environment
     * @return flight level; negative if unavailable
     * @see TrackPoint#getFlightLevel()
     */
    public int toFlightLevel(BarometricPressure qnh) {
        if (qnh == null) {
            return -1;
        }

        // FIXME: check if really correct
        return (int) Math.round((getAltitudeFeet() + (1013 - qnh.getHectopascals()) * 27.0d) / 100.0);
    }

    @Override
    public String toString() {
        return String.format(
            "GeoCoordinates(%.5f, %.5f, %d%s)",
            latitude, longitude, altitude,
            isAltitudeUnitFeet ? "ft" : "m"
        );
    }

    // TODO: unit tests
}
