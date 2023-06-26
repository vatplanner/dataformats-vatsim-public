package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

import java.util.Collection;

import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

/**
 * Describes how to deal with errors that may occur during privacy filtering.
 */
public interface ErrorHandlingStrategy {

    /**
     * An exception to be thrown by {@link #handleError(String, String, Collection)}
     * in case error handling wants to abort all filtering.
     */
    class FailWithException extends RuntimeException {

        public FailWithException(String msg) {
            super(msg);
        }
    }

    /**
     * Handles an error that was detected in filteredLine result while processing
     * the affectedFields on rawLine.
     *
     * @param rawLine        content of line before filter was applied; may be an
     *                       intermediate result of previous filters
     * @param filteredLine   erroneous filter output of line contents
     * @param affectedFields {@link ClientFields} affected by the erroneous filter
     * @return content to replace line with; null if line should be removed
     * @throws FailWithException if processing should be aborted by an exception
     */
    String handleError(String rawLine, String filteredLine, Collection<ClientFields.FieldAccess> affectedFields);
}
