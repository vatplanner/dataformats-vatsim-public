package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.Collection;

/**
 * Helper methods to perform geographic calculations.
 */
public class GeoMath {
    private static final double DEGREES_TO_RADIANS_FACTOR = Math.PI / 180.0;
    private static final double RADIANS_TO_DEGREES_FACTOR = 180.0 / Math.PI;

    private GeoMath() {
        // utility class, hide constructor
    }

    /**
     * Calculates the average center of given points. Implemented using the formula described on
     * <a href="https://web.archive.org/web/20221205184246/https://carto.com/blog/center-of-points/">
     * https://carto.com/blog/center-of-points/ [archive.org, retrieved 5 Dec 2022]
     * </a>.
     *
     * @param points points to calculate average center for, must not be empty
     * @return center point calculated by average
     */
    public static GeoPoint2D average(Collection<GeoPoint2D> points) {
        int numPoints = points.size();

        if (numPoints == 1) {
            return points.iterator().next();
        } else if (numPoints == 0) {
            throw new IllegalArgumentException("No points given to calculate center for.");
        }

        double sumLatitudes = 0.0;
        double sumZeta = 0.0;
        double sumXi = 0.0;

        for (GeoPoint2D point : points) {
            double latitude = point.getLatitude();
            sumLatitudes += latitude;

            double longitudeRad = point.getLongitude() * DEGREES_TO_RADIANS_FACTOR;
            sumZeta += Math.sin(longitudeRad);
            sumXi += Math.cos(longitudeRad);
        }

        double centerLatitude = sumLatitudes / numPoints;
        double centerLongitude = Math.atan2(sumZeta / numPoints, sumXi / numPoints) * RADIANS_TO_DEGREES_FACTOR;

        return new GeoPoint2D(centerLatitude, centerLongitude);
    }
}
