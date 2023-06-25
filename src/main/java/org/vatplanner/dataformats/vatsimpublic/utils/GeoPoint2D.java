package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.Objects;

/**
 * Represents a 2-dimensional geographic point/coordinate.
 */
public class GeoPoint2D {
    private final double latitude;
    private final double longitude;

    /**
     * Creates a new 2-dimensional geographic point.
     * <p>
     * Values must be in normal range; see {@link #normalize(double, double)} and {@link #wrap(double, double)} if you
     * need to create points from potentially out-of-range values.
     * </p>
     *
     * @param latitude  latitude (north/south coordinate, &plusmn;90&deg;)
     * @param longitude longitude (east/west coordinate, &plusmn;180&deg;)
     * @throws OutOfRange if longitude exceeds &plusmn;180&deg; or latitude exceeds &plusmn;90&deg;
     */
    public GeoPoint2D(double latitude, double longitude) {
        OutOfRange.throwIfNotWithinIncluding("latitude", latitude, -90.0, 90.0);
        OutOfRange.throwIfNotWithinIncluding("longitude", longitude, -180.0, 180.0);

        // TODO: unify full longitude deflection? (-180 equals 180 for same latitude)
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Normalizes the given coordinates to create a valid {@link GeoPoint2D} from an otherwise potentially
     * out-of-range longitude.
     * <p>
     * Note that only longitude (east/west coordinate) will be normalized into standard &plusmn;180&deg; range as it
     * usually should be safe to do so because both limits connect at each other.
     * Out-of-range latitudes (north/south coordinate, &plusmn;90&deg;) are more likely to indicate a fundamental
     * issue in the data being processed. Use {@link #wrap(double, double)} if you are sure you actually want to adapt
     * those as well.
     * </p>
     * <p>
     * Excessively huge longitudes will still fail with {@link OutOfRange} to prevent long computation times or
     * inaccuracies.
     * </p>
     *
     * @param latitude  latitude (north/south coordinate, &plusmn;90&deg;)
     * @param longitude longitude (east/west coordinate, may exceed &plusmn;180&deg;)
     * @return point with longitude normalized into &plusmn;180&deg; range, if necessary
     * @throws OutOfRange if latitude exceeds &plusmn;90&deg; or absolute value is excessively huge
     */
    public static GeoPoint2D normalize(double latitude, double longitude) {
        // time and accuracy constraint reasons:
        // - iterative addition causes high runtime
        // - high accuracy loss when calculating via trigonometric functions
        // - in general, accuracy will already be unacceptably deteriorated on input values past a certain point
        OutOfRange.throwIfNotWithinIncluding("longitude", longitude, -3.6e7, 3.6e7);

        if (longitude < -180.0) {
            while (longitude < -180.0) {
                longitude += 360.0;
            }
        } else if (longitude > 180.0) {
            while (longitude > 180.0) {
                longitude -= 360.0;
            }
        }

        return new GeoPoint2D(latitude, longitude);
    }

    /**
     * Wraps the given coordinates to create a {@link GeoPoint2D} from otherwise potentially out-of-range coordinates.
     * <p>
     * In case latitude (north/south coordinate) is out of normal &plusmn;90&deg; range, it will be wrapped over the pole,
     * extending opposite to previous direction on a 180&deg; shifted longitude. Longitude (east/west coordinate) will be
     * normalized afterwards.
     * </p>
     * <p>
     * As latitudes do not connect to each other at &plusmn;90&deg; this generally indicates a severe issue in processed
     * data as the resulting {@link GeoPoint2D} will most likely point to an unintended location somewhere on the other
     * side of the planet. Use only if such points are acceptable or the data source is known to still point to the right
     * place despite using out-of-range coordinates. Use {@link #normalize(double, double)} and handle {@link OutOfRange}
     * errors instead, if unsure.
     * </p>
     *
     * @param latitude  latitude (north/south coordinate, may exceed &plusmn;90&deg;)
     * @param longitude longitude (east/west coordinate, may exceed &plusmn;180&deg;)
     * @return point with latitude and longitude wrapped as necessary (may yield unintended results)
     */
    public static GeoPoint2D wrap(double latitude, double longitude) {
        // time and accuracy constraint reasons:
        // - iterative addition causes high runtime
        // - high accuracy loss when calculating via trigonometric functions
        // - in general, accuracy will already be unacceptably deteriorated on input values past a certain point
        OutOfRange.throwIfNotWithinIncluding("latitude", latitude, -3.6e7, 3.6e7);

        // first get latitude into a range of +- 360 degrees
        while (Math.abs(latitude) > 360.0) {
            latitude -= Math.copySign(360.0, Math.signum(latitude));
        }

        // there are three cases now:
        // 1. absolute latitude is 270 or more - then we are on the original side of the planet and just need to
        //    "reverse" the latitude
        // 2. absolute latitude is more than 90 - then we are on the other side of the planet and need to move opposite
        //    and need to rotate longitude as well (will be normalized later)
        // 3. absolute latitude is 90 or less - we are already done in that case
        double abs = Math.abs(latitude);
        if (abs >= 270.0) {
            latitude = (abs - 360.0) * Math.signum(latitude);
        } else if (abs > 90.0) {
            latitude = (180.0 - abs) * Math.signum(latitude);
            longitude += 180.0;
        }

        return normalize(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return String.format("GeoPoint2D[lat=%f, lon=%f]", latitude, longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPoint2D)) {
            return false;
        }

        GeoPoint2D other = (GeoPoint2D) obj;

        if ((other.latitude == this.latitude) && (other.longitude == this.longitude)) {
            return true;
        }

        return false;
    }
}
