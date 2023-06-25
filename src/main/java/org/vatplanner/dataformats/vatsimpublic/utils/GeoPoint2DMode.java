package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.function.BiFunction;

/**
 * Factory-like wrapper around {@link GeoPoint2D} construction methods. Please refer to the linked methods for more
 * information about each option.
 */
public enum GeoPoint2DMode {

    /**
     * Non-normalized coordinates fail parsing with {@link OutOfRange}.
     *
     * @see GeoPoint2D#GeoPoint2D(double, double)
     */
    STRICT(GeoPoint2D::new),

    /**
     * Longitude gets normalized, excessive latitudes still fail parsing with {@link OutOfRange}.
     *
     * @see GeoPoint2D#normalize(double, double)
     */
    NORMALIZE(GeoPoint2D::normalize),

    /**
     * Coordinates fully wrap, excessive latitudes can cross poles and rotate the planet.
     *
     * @see GeoPoint2D#wrap(double, double)
     */
    WRAP(GeoPoint2D::wrap);

    private final BiFunction<Double, Double, GeoPoint2D> factory;

    GeoPoint2DMode(BiFunction<Double, Double, GeoPoint2D> factory) {
        this.factory = factory;
    }

    /**
     * Creates a {@link GeoPoint2D} from given coordinates according to this mode.
     *
     * @param latitude  latitude (north/south coordinate)
     * @param longitude longitude (east/west coordinate)
     * @return point representing the coordinates as per selected mode
     * @throws OutOfRange if the mode does not accept the given coordinates
     */
    public GeoPoint2D createPoint(double latitude, double longitude) {
        return factory.apply(latitude, longitude);
    }

    /**
     * Creates a {@link GeoPoint2D} from given coordinates according to this mode.
     *
     * @param latitude  latitude (north/south coordinate)
     * @param longitude longitude (east/west coordinate)
     * @return point representing the coordinates as per selected mode
     * @throws OutOfRange if the mode does not accept the given coordinates
     */
    public GeoPoint2D createPoint(String latitude, String longitude) {
        return createPoint(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }
}
