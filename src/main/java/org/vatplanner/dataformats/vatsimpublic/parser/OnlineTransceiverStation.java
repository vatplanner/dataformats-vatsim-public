package org.vatplanner.dataformats.vatsimpublic.parser;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A single Audio for VATSIM station as available from an
 * {@link OnlineTransceiversFile}.
 */
public class OnlineTransceiverStation {
    private String callsign = "";
    private Collection<OnlineTransceiver> transceivers = new ArrayList<>();

    /**
     * Returns this station's call sign.
     *
     * @return call sign of this station
     */
    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    /**
     * Returns all {@link OnlineTransceiver}s of this station. Note that some
     * stations are listed without actually having any transceivers.
     *
     * @return all {@link OnlineTransceiver}s of this station, may be empty
     */
    public Collection<OnlineTransceiver> getTransceivers() {
        return transceivers;
    }

    public void setTransceivers(Collection<OnlineTransceiver> transceivers) {
        this.transceivers = transceivers;
    }
}
