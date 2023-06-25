package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.Objects;

/**
 * Represents a 2-dimensional geographic point/coordinate.
 */
public class GeoPoint2D {
    private final double latitude;
    private final double longitude;

    // TODO: add static method to create normalized point by modulo

    public GeoPoint2D(double latitude, double longitude) {
        OutOfRange.throwIfNotWithinIncluding("latitude", latitude, -90.0, 90.0);
        OutOfRange.throwIfNotWithinIncluding("longitude", longitude, -180.0, 180.0);

        // TODO: unify full longitude deflection? (-180 equals 180 for same latitude)
        this.latitude = latitude;
        this.longitude = longitude;
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
