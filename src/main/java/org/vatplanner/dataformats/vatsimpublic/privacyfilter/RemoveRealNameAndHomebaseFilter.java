package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.vatplanner.dataformats.vatsimpublic.utils.CollectionHelpers.asUnmodifiableSet;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

/**
 * Privacy filter which implements clearing of real name field (containing real
 * name and homebase). Do not use directly; use {@link DataFileFilter} instead
 * and enable this filter via
 * {@link DataFileFilterConfiguration#setRemoveRealNameAndHomebase(boolean)}.
 */
public class RemoveRealNameAndHomebaseFilter implements VerifiableClientFilter<String> {

    private static final Set<ClientFields.FieldAccess<String>> AFFECTED_FIELDS = asUnmodifiableSet(
        ClientFields.StringFields.REAL_NAME
    );

    private static final Pattern PATTERN = Pattern.compile("^([^:]*:[^:]*:)[^:]*(:.*)$");
    private static final String PATTERN_REPLACEMENT = "$1$2";

    @Override
    public String apply(String t) {
        Matcher matcher = PATTERN.matcher(t);

        if (!matcher.matches()) {
            return t;
        }

        return matcher.replaceFirst(PATTERN_REPLACEMENT);
    }

    @Override
    public Set<ClientFields.FieldAccess<String>> getAffectedFields() {
        return AFFECTED_FIELDS;
    }

    @Override
    public boolean verifyAffectedField(ClientFields.FieldAccess<String> fieldAccess, String original, String filtered) {
        if (fieldAccess == ClientFields.StringFields.REAL_NAME) {
            return filtered.isEmpty();
        }

        throw new IllegalArgumentException("attempted to verify an unhandled field");
    }
}
