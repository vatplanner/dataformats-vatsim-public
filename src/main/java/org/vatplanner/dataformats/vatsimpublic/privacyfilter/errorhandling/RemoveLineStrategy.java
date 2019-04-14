package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import java.util.Collection;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

/**
 * {@link ErrorHandlingStrategy} which instructs filter to remove the complete
 * line which filtering has failed on. This is highly destructive as the
 * information will be missing completely.
 */
public class RemoveLineStrategy implements ErrorHandlingStrategy {

    @Override
    public String handleError(String rawLine, String filteredLine, Collection<ClientFields.FieldAccess> affectedFields) {
        return null;
    }

}
