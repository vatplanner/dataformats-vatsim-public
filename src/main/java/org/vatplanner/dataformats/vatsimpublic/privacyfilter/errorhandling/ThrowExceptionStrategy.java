package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import java.util.Collection;
import java.util.stream.Collectors;

import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

/**
 * {@link ErrorHandlingStrategy} which simply throws an exception when
 * application of the filter has failed. This causes the filter to fully fail
 * without any output at all.
 */
public class ThrowExceptionStrategy implements ErrorHandlingStrategy {

    @Override
    public String handleError(String rawLine, String filteredLine, Collection<ClientFields.FieldAccess> affectedFields) {
        throw new FailWithException(
            "Filtering failed on client fields: "
                + affectedFields
                .stream()
                .map(ClientFields.FieldAccess::name)
                .collect(Collectors.joining(", "))
        );
    }
}
