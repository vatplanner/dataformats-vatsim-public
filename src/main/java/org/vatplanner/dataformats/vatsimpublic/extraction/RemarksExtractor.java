package org.vatplanner.dataformats.vatsimpublic.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vatplanner.dataformats.vatsimpublic.entities.status.CommunicationMode;

/**
 * Extracts information from (flight plan) remarks.
 */
public class RemarksExtractor {

    private static final Pattern PATTERN_SPLIT = Pattern.compile(//
        "^.*?/" + groupForCommunicationModes() + "/.*$", //
        Pattern.CASE_INSENSITIVE //
    );
    private static final int PATTERN_SPLIT_COMMUNICATION_MODE = 1;

    private final CommunicationMode communicationMode;

    /**
     * Parses the given flight plan remarks.
     *
     * @param s flight plan remarks
     */
    public RemarksExtractor(String s) {
        Matcher matcher = PATTERN_SPLIT.matcher(s);
        if (!matcher.matches()) {
            communicationMode = null;
        } else {
            communicationMode = CommunicationMode.resolveRemarksCode(matcher.group(PATTERN_SPLIT_COMMUNICATION_MODE));
        }
    }

    /**
     * Returns the indicated communication mode.
     *
     * @return communication mode
     */
    public CommunicationMode getCommunicationMode() {
        return communicationMode;
    }

    private static String groupForCommunicationModes() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");

        boolean isFirst = true;
        for (CommunicationMode mode : CommunicationMode.values()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("|");
            }

            sb.append(Pattern.quote(mode.getRemarksCode()));
        }

        sb.append(")");

        return sb.toString();
    }

    // TODO: unit tests
}
