package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import java.util.Collections;
import java.util.List;

import org.vatplanner.dataformats.vatsimpublic.utils.GeoPoint2D;

public class FIRBoundary {
    private final String id;
    private final boolean isOceanic;
    private final boolean isExtension;

    private final GeoPoint2D boundsMinimum;
    private final GeoPoint2D boundsMaximum;
    private final GeoPoint2D centerPoint;

    private final List<GeoPoint2D> points;

    public FIRBoundary(String id, boolean isOceanic, boolean isExtension, GeoPoint2D boundsMinimum, GeoPoint2D boundsMaximum, GeoPoint2D centerPoint, List<GeoPoint2D> points) {
        this.id = id;
        this.isOceanic = isOceanic;
        this.isExtension = isExtension;
        this.boundsMinimum = boundsMinimum;
        this.boundsMaximum = boundsMaximum;
        this.centerPoint = centerPoint;
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public boolean isOceanic() {
        return isOceanic;
    }

    public boolean isExtension() {
        return isExtension;
    }

    public GeoPoint2D getBoundsMinimum() {
        return boundsMinimum;
    }

    public GeoPoint2D getBoundsMaximum() {
        return boundsMaximum;
    }

    public GeoPoint2D getCenterPoint() {
        return centerPoint;
    }

    public List<GeoPoint2D> getPoints() {
        return Collections.unmodifiableList(points);
    }
}
