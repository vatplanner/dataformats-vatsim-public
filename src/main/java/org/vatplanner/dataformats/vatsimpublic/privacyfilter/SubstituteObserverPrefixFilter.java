package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.vatplanner.dataformats.vatsimpublic.utils.CollectionHelpers.asUnmodifiableSet;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

/**
 * Privacy filter which implements substitution of all observer call signs to a
 * shared pseudonym. Do not use directly; use {@link DataFileFilter} instead and
 * enable this filter via
 * {@link DataFileFilterConfiguration#setSubstituteObserverPrefix(boolean)}.
 */
public class SubstituteObserverPrefixFilter implements VerifiableClientFilter<String> {

    private static final Set<ClientFields.FieldAccess<String>> AFFECTED_FIELDS = asUnmodifiableSet(
        ClientFields.StringFields.CALLSIGN //
    );

    private static final String OBSERVER_SUFFIX = "_OBS";
    private static final String OBSERVER_REPLACEMENT = "XX_OBS";

    private static final Pattern PATTERN = Pattern.compile("^[^:]*" + Pattern.quote(OBSERVER_SUFFIX) + "(:.*)$");
    private static final String PATTERN_REPLACEMENT = OBSERVER_REPLACEMENT + "$1";

    @Override
    public Set<ClientFields.FieldAccess<String>> getAffectedFields() {
        return AFFECTED_FIELDS;
    }

    @Override
    public boolean verifyAffectedField(ClientFields.FieldAccess<String> fieldAccess, String original, String filtered) {
        if (fieldAccess == ClientFields.StringFields.CALLSIGN) {
            if (!original.endsWith(OBSERVER_SUFFIX)) {
                return filtered.equals(original);
            }

            return filtered.equals(OBSERVER_REPLACEMENT);
        }

        throw new IllegalArgumentException("attempted to verify an unhandled field");
    }

    @Override
    public String apply(String t) {
        Matcher matcher = PATTERN.matcher(t);

        if (!matcher.matches()) {
            return t;
        }

        return matcher.replaceFirst(PATTERN_REPLACEMENT);
    }

}
