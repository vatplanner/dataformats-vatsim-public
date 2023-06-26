package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import java.util.Collection;

import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

/**
 * {@link ErrorHandlingStrategy} which just ignores that an error occurred and
 * continues processing the line as if everything was OK.
 */
public class IgnoreErrorStrategy implements ErrorHandlingStrategy {

    @Override
    public String handleError(String rawLine, String filteredLine, Collection<ClientFields.FieldAccess> affectedFields) {
        return filteredLine;
    }
}
