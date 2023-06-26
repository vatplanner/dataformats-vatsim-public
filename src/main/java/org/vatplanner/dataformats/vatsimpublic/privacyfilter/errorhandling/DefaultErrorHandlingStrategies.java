package org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling;

/**
 * Holds default instances of simple {@link ErrorHandlingStrategy}s to make them
 * easier to access as pre-defined constants. See class JavaDocs for their
 * individual effects.
 */
public class DefaultErrorHandlingStrategies {

    public static final ErrorHandlingStrategy KEEP_ORIGINAL_CONTENT = new KeepOriginalContentStrategy();
    public static final ErrorHandlingStrategy REMOVE_LINE = new RemoveLineStrategy();
    public static final ErrorHandlingStrategy THROW_EXCEPTION = new ThrowExceptionStrategy();
    public static final ErrorHandlingStrategy IGNORE_ERROR = new IgnoreErrorStrategy();

    private DefaultErrorHandlingStrategies() {
        // hide constructor
    }
}
