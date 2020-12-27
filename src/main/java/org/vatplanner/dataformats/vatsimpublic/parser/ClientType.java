package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Classifies {@link Client} objects by what information they actually hold. The
 * original data.txt file uses the exact same format for online pilots, prefiled
 * flight plans and online ATC. {@link Client} objects representing those
 * records thus need to be classified further by this enum.
 */
public enum ClientType {
    /**
     * A pilot currently connected to VATSIM network.
     */
    PILOT_CONNECTED,

    /**
     * A prefiled flight plan, meaning the pilot has not connected yet but
     * pre-registered an upcoming flight with the network.
     */
    PILOT_PREFILED,

    /**
     * A controller currently connected to VATSIM network.
     */
    ATC_CONNECTED,

    /**
     * ATIS stations are indicated separate from regular controller clients
     * ({@link #ATC_CONNECTED}) in JSON data files. Legacy format lists ATIS
     * stations as {@link #ATC_CONNECTED}.
     * 
     * <p>
     * As of December 2020 every ATIS station still is provided by an active
     * controller but this might change in the future according to plans laid out at
     * time of AFV introduction (Q3 2019). The formerly announced plan is to
     * separate ATIS stations from controllers and provide permanent "offline"
     * service at some point. This has not been implemented yet but may warrant a
     * dedicated {@link ClientType} indication as already available on JSON v3
     * format.
     * </p>
     * 
     * <p>
     * For compatibility, unless needed otherwise, {@link #ATIS} stations can be
     * processed as {@link #ATC_CONNECTED}.
     * </p>
     */
    ATIS;
}
