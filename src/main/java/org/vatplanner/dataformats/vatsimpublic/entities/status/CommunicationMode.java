package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.util.regex.Pattern;

/**
 * Part of a flight plan, describes how a pilot will communicate with other
 * users.
 */
public enum CommunicationMode {
    /**
     * User is unable to send or receive voice transmissions and will only
     * communicate by text.
     */
    TEXT_ONLY('T'),
    /**
     * User is unable to send but can receive voice transmissions.
     */
    RECEIVE_VOICE_ONLY('R'),
    /**
     * User is able for full voice communication.
     */
    FULL_VOICE('F');

    private final char code;
    private final Pattern patternRemarks;

    private CommunicationMode(char code) {
        this.code = code;
        patternRemarks = Pattern.compile(".*?/" + Pattern.quote(Character.toString(code)) + "/.*", Pattern.CASE_INSENSITIVE);
    }

    // TODO: add byCode
    // TODO: add fromFlightPlanRemarks
    // TODO: unit tests
}
