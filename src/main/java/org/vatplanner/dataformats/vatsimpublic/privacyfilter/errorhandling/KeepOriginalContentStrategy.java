package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import java.util.Collection;

import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

/**
 * {@link ErrorHandlingStrategy} which returns lines to original, non-filtered
 * content in order to resolve errors. Using this strategy saves information
 * that with other strategies might be lost but results in an unclean filter
 * output that still contains data which was previously identified for removal.
 */
public class KeepOriginalContentStrategy implements ErrorHandlingStrategy {

    @Override
    public String handleError(String rawLine, String filteredLine, Collection<ClientFields.FieldAccess> affectedFields) {
        return rawLine;
    }
}
