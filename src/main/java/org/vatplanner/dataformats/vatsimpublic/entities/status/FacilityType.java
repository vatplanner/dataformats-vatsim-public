package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.util.Map;
import java.util.TreeMap;

/**
 * A {@link FacilityType} describes the area of responsibility of an ATC
 * station.
 * <p>
 * In general, all controlling facilities provide full replacement service if a
 * lower, more specific facility is offline.
 * </p>
 * <p>
 * Note: Below descriptions of individual stations may be inaccurate and do of
 * course not apply to real-world aviation.
 * </p>
 */
public enum FacilityType {
    // TODO: check status file IDs against more data

    /**
     * Observers only watch and do not control or provide information.
     */
    OBSERVER(0),
    /**
     * Flight service stations (FSS, FIS, "Info") do not control traffic but
     * only provide information to pilots.
     */
    FSS(1),
    /**
     * Delivery stations provide only clearances on ground.
     */
    DELIVERY(2),
    /**
     * Ground stations control all traffic on ground except for tower-controlled
     * area (for example ground is not allowed to control taxi routes crossing
     * runways). Ground service includes Delivery if a dedicated station is
     * offline (top-bottom service).
     */
    GROUND(3),
    /**
     * Tower stations control the immediate lower airspace around an airport as
     * well as runways on ground. Tower service includes Ground and Delivery if
     * dedicated stations are offline (top-bottom service).
     */
    TOWER(4),
    /**
     * Approach and departure stations control IFR traffic which is in- or
     * outbound a specific airport and neither in Tower nor Center control area.
     * Approach/Departure service includes Tower, Ground and Delivery services
     * if dedicated stations are offline (top-bottom service).
     */
    APPROACH_DEPARTURE(5),
    /**
     * Center stations control IFR traffic outside Approach/Departure stations.
     * Center service includes all other station services if dedicated stations
     * are offline (top-bottom service).
     */
    CENTER(6);

    private final int statusFileId;

    private static final Map<Integer, FacilityType> typeById = new TreeMap<>();

    static {
        for (FacilityType facilityType : values()) {
            typeById.put(facilityType.statusFileId, facilityType);
        }
    }

    private FacilityType(int statusFileId) {
        this.statusFileId = statusFileId;
    }

    /**
     * Resolves the given ID as used on status files (data.txt) to the
     * corresponding {@link FacilityType} enum.
     *
     * @param statusFileId ID as used on data.txt status file
     * @return resolved enumeration object or null if unknown
     * @throws IllegalArgumentException if the ID is unknown and could not be
     * resolved
     */
    public static FacilityType resolveStatusFileId(int statusFileId) throws IllegalArgumentException {
        FacilityType resolved = typeById.get(statusFileId);

        if (resolved != null) {
            return resolved;
        }

        throw new IllegalArgumentException(String.format("unknown facility type ID %d", statusFileId));
    }
}
