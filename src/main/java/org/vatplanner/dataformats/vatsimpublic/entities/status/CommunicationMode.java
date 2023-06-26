package org.vatplanner.dataformats.vatsimpublic.entities.status;

import java.util.HashMap;
import java.util.Map;

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
    FULL_VOICE('V');

    private final char code;

    private static final Map<Character, CommunicationMode> INDEXED_BY_CODE = new HashMap<>();

    static {
        for (CommunicationMode mode : values()) {
            INDEXED_BY_CODE.put(mode.code, mode);
        }
    }

    private CommunicationMode(char code) {
        this.code = code;
    }

    /**
     * Returns the code used to identify the communication mode in flight plan
     * remarks.
     *
     * @return code used to identify the communication mode in flight plan remarks
     */
    public String getRemarksCode() {
        return Character.toString(code);
    }

    /**
     * Resolves the given code as found in flight remarks to a communication mode.
     * Returns null if no such mode has been defined or input is empty or null.
     *
     * @param remarksCode remarks code to resolve
     * @return mode matching the code; null if not found or no code was entered
     * @throws IllegalArgumentException if code exceeds one letter
     */
    public static CommunicationMode resolveRemarksCode(String remarksCode) {
        if ((remarksCode == null) || remarksCode.isEmpty()) {
            return null;
        }

        if (remarksCode.length() != 1) {
            throw new IllegalArgumentException(
                "communication mode code must only have one letter; was: \"" + remarksCode + "\""
            );
        }

        return INDEXED_BY_CODE.get(remarksCode.toUpperCase().charAt(0));
    }

    // TODO: unit tests
}
