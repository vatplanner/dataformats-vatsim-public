package org.vatplanner.dataformats.vatsimpublic.utils;

public class GeoPoint2D {
    private final double latitude;
    private final double longitude;

    public GeoPoint2D(double latitude, double longitude) {
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
}
