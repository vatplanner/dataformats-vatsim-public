package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * A single transceiver of an {@link OnlineTransceiverStation}.
 */
public class OnlineTransceiver {
    private int id = UNAVAILABLE_INTEGER;
    private int frequencyHertz = UNAVAILABLE_INTEGER;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private double heightMeters = Double.NaN;
    private double altitudeMeters = Double.NaN;

    public static final int UNAVAILABLE_INTEGER = Integer.MIN_VALUE;

    /**
     * Returns the ID of this transceiver.
     * <p>
     * {@link #UNAVAILABLE_INTEGER} will be returned if unavailable.
     * </p>
     *
     * @return transceiver ID, {@link #UNAVAILABLE_INTEGER} if unavailable
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the frequency (in hertz) a transceiver is tuned to.
     * <p>
     * {@link #UNAVAILABLE_INTEGER} will be returned if unavailable.
     * </p>
     *
     * @return frequency in hertz, {@link #UNAVAILABLE_INTEGER} if unavailable
     */
    public int getFrequencyHertz() {
        return frequencyHertz;
    }

    public void setFrequencyHertz(int frequencyHertz) {
        this.frequencyHertz = frequencyHertz;
    }

    /**
     * Returns the transceiver's latitude (north/south coordinate).
     *
     * @return transceiver latitude, {@link Double#NaN} if unavailable
     */
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the transceiver's longitude (east/west coordinate).
     *
     * @return transceiver longitude, {@link Double#NaN} if unavailable
     */
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the transceiver's height above ground in meters.
     *
     * @return transceiver height above ground (metric), {@link Double#NaN} if unavailable
     * @see #getAltitudeMeters()
     */
    public double getHeightMeters() {
        return heightMeters;
    }

    public void setHeightMeters(double heightMeters) {
        this.heightMeters = heightMeters;
    }

    /**
     * Returns the transceiver's altitude above mean sea-level in meters.
     *
     * @return transceiver altitude above mean sea-level (metric), {@link Double#NaN} if unavailable
     * @see #getHeightMeters()
     */
    public double getAltitudeMeters() {
        return altitudeMeters;
    }

    public void setAltitudeMeters(double altitudeMeters) {
        this.altitudeMeters = altitudeMeters;
    }
}
