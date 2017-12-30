package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Classifies {@link Client} objects by what information they actually hold.
 * The original data.txt file uses the exact same format for online pilots,
 * prefiled flight plans and online ATC. {@link Client} objects representing those
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
    ATC_CONNECTED;
}
