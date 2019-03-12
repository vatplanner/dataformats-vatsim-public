package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;
import static org.vatplanner.dataformats.vatsimpublic.utils.CollectionHelpers.asUnmodifiableSet;

/**
 * Privacy filter which implements removal of free-text from flight plan
 * remarks. Do not use directly; use {@link DataFileFilter} instead and enable
 * this filter via
 * {@link DataFileFilterConfiguration#setFlightPlanRemarksRemoveAll(boolean)} or
 * {@link DataFileFilterConfiguration#setFlightPlanRemarksRemoveAllIfContaining(java.util.Collection)}.
 */
public class FlightPlanRemarksRemoveAllFilter implements VerifiableClientFilter<String> {
    // TODO: check and maintain other prefiling system markers such as SimBrief

    private static final Set<ClientFields.FieldAccess<String>> AFFECTED_FIELDS = asUnmodifiableSet(ClientFields.StringFields.FLIGHT_PLAN_REMARKS);

    private final Pattern patternFieldContentTrigger;

    private static final String VFPS_PREFIX = "+VFPS+";
    private static final String COMMUNICATION_FLAG_VOICE = "/V/";
    private static final String COMMUNICATION_FLAG_RECEIVE_ONLY = "/R/";
    private static final String COMMUNICATION_FLAG_TEXT = "/T/";

    /**
     * Creates a new filter to remove free-text from flight plan remarks.
     * Communication type flag as well as pre-filing system indication (e.g.
     * <code>+VFPS+</code>) will be preserved during removal.
     *
     * <p>
     * If triggers are specified (non-null, non-empty), the filter will only
     * activate if any of the strings given as triggers are found in flight plan
     * remarks. Triggers are finalized and cannot be changed after construction.
     * While the given {@link Collection} itself is allowed to be null or empty
     * for unconditional filtering, its items must not be null or empty
     * (including strings consisting of just white-spaces).
     * </p>
     *
     * @param triggers search strings to trigger removal; filter will be
     * unconditional if left null or empty
     */
    public FlightPlanRemarksRemoveAllFilter(Collection<String> triggers) {
        if ((triggers == null) || triggers.isEmpty()) {
            patternFieldContentTrigger = null;
            return;
        }

        boolean containsBadItems = triggers.stream().anyMatch(s -> s == null || s.trim().isEmpty());
        if (containsBadItems) {
            throw new IllegalArgumentException("Triggers contain invalid search phrases (null or white-space only).");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(".*(");
        sb.append(
                triggers.stream()//
                        .map(Pattern::quote) //
                        .collect(Collectors.joining("|"))
        );
        sb.append(").*");

        patternFieldContentTrigger = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    /**
     * Checks if the criteria (configured at construction time) to perform
     * filtering are met by the given field content.
     *
     * @param fieldContent field content to be checked for filtering
     * @return true if given field content should be filtered
     */
    boolean isConditionMet(String fieldContent) {
        if (patternFieldContentTrigger == null) {
            boolean isEmpty = (fieldContent == null) || fieldContent.trim().isEmpty();
            return !isEmpty;
        }

        Matcher matcher = patternFieldContentTrigger.matcher(fieldContent);
        return matcher.matches();
    }

    private String extractFieldContent(String rawLine) {
        // FIXME: implement
        // field 30
        return "";
    }

    @Override
    public Set<ClientFields.FieldAccess<String>> getAffectedFields() {
        return AFFECTED_FIELDS;
    }

    @Override
    public boolean verifyAffectedField(ClientFields.FieldAccess<String> fieldAccess, String original, String filtered) {
        if (fieldAccess == ClientFields.StringFields.FLIGHT_PLAN_REMARKS) {
            // if filter should not be applied, we expect original to remain
            // unchanged
            boolean shouldFilter = isConditionMet(original);
            if (!shouldFilter) {
                return filtered.equals(original);
            }

            // VFPS prefix must be maintained, if set
            boolean isFiledByVFPS = original.startsWith(VFPS_PREFIX);
            if (isFiledByVFPS && !filtered.startsWith(VFPS_PREFIX)) {
                return false;
            }
            String expectedPrefix = isFiledByVFPS ? VFPS_PREFIX : "";

            // find communication flags
            String originalUpperCase = original.toUpperCase();
            boolean hadCommunicationFlagVoice = originalUpperCase.contains(COMMUNICATION_FLAG_VOICE);
            boolean hadCommunicationFlagReceiveOnly = originalUpperCase.contains(COMMUNICATION_FLAG_RECEIVE_ONLY);
            boolean hadCommunicationFlagText = originalUpperCase.contains(COMMUNICATION_FLAG_TEXT);

            // precedence: voice over receive-only over text
            String expectedCommunicationFlag = "";
            if (hadCommunicationFlagVoice) {
                expectedCommunicationFlag = COMMUNICATION_FLAG_VOICE;
            } else if (hadCommunicationFlagReceiveOnly) {
                expectedCommunicationFlag = COMMUNICATION_FLAG_RECEIVE_ONLY;
            } else if (hadCommunicationFlagText) {
                expectedCommunicationFlag = COMMUNICATION_FLAG_TEXT;
            }

            String expectedOutput = expectedPrefix + expectedCommunicationFlag;

            return filtered.equals(expectedOutput);
        }

        throw new IllegalArgumentException("attempted to verify an unhandled field");
    }

    @Override
    public String apply(String t) {
        // FIXME: implement, use extractFieldContent
        return "";
    }

}
