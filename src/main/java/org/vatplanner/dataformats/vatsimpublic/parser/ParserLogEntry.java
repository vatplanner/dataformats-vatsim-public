package org.vatplanner.dataformats.vatsimpublic.parser;

/**
 * Holds the content of a single log entry regarding parser messages. A log
 * entry indicates if a line has been rejected by parser. This can be used to
 * check if a file was parsed without any significant errors. {@link Throwable}s
 * can also be collected by log entries.
 */
public class ParserLogEntry {

    private final String section;
    private final String lineContent;
    private final boolean isLineRejected;
    private final String message;
    private final Throwable throwable;

    /**
     * Instantiates a new log entry.
     *
     * @param section description of section message refers to, may be null
     * @param lineContent line content message refers to, may be null
     * @param isLineRejected Has line been rejected (was line deemed unparseable)?
     * @param message log message (must not be null!)
     * @param throwable throwable/exception (may be null)
     */
    ParserLogEntry(String section, String lineContent, boolean isLineRejected, String message, Throwable throwable) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null!");
        }

        this.section = section;
        this.lineContent = lineContent;
        this.isLineRejected = isLineRejected;
        this.message = message;
        this.throwable = throwable;
    }

    /**
     * Returns a description of the section the message refers to.
     *
     * @return description of section message refers to (may be null)
     */
    public String getSection() {
        return section;
    }

    /**
     * Returns the line content the message refers to.
     *
     * @return line content the message refers to (may be null)
     */
    public String getLineContent() {
        return lineContent;
    }

    /**
     * Indicates if the line had to be rejected. Evaluate all log entries for this
     * flag to check if a file contained any significant errors.
     * <p>
     * Note that multiple log entries could refer to the same line and this flag
     * only refers to the current message. An indication of false (not rejected)
     * could still mean that another log entry exists for the same line which caused
     * the line to actually have been rejected.
     * </p>
     *
     * @return Has the line been rejected by parser? (true = rejected; if false, you
     *         still may want to check other entries)
     */
    public boolean isLineRejected() {
        return isLineRejected;
    }

    /**
     * Returns the parser's log message, detailing what is wrong with the logged
     * line.
     *
     * @return parser log message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the associated {@link Throwable} if an exception has occured during
     * parsing. Just holding a {@link Throwable} does not indicate a fatal error
     * which resulted in rejection of a line. Instead, check
     * {@link #isLineRejected()} if you want to know if parsing was able to
     * continue.
     *
     * @return Associated {@link Throwable} - not to be used as an indication of
     *         rejecting lines! May be null.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return String.format(
            "%s [section: %s, line rejected: %b, exception: %s, line: %s]",
            message,
            section,
            isLineRejected,
            (throwable != null) ? throwable : "none",
            (lineContent != null) ? "\"" + lineContent + "\"" : "null" //
        );
    }
}
