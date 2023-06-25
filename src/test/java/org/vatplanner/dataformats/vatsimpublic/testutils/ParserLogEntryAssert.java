package org.vatplanner.dataformats.vatsimpublic.testutils;

import java.util.Objects;
import java.util.regex.Pattern;

import org.assertj.core.api.AbstractAssert;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;

/**
 * Enables AssertJ verification of {@link ParserLogEntry} objects.
 */
public class ParserLogEntryAssert extends AbstractAssert<ParserLogEntryAssert, ParserLogEntry> {

    private ParserLogEntryAssert(ParserLogEntry actual) {
        super(actual, ParserLogEntryAssert.class);
    }

    public static ParserLogEntryAssert assertThat(ParserLogEntry actual) {
        return new ParserLogEntryAssert(actual);
    }

    public static ParserLogEntryAssert assertThatParserLogEntry(ParserLogEntry actual) {
        return assertThat(actual);
    }

    /**
     * Verifies that the {@link ParserLogEntry} has exactly the specified section name.
     *
     * @param section expected section name
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert hasSection(String section) {
        isNotNull();

        if (!Objects.equals(actual.getSection(), section)) {
            throw failureWithActualExpected(
                actual.getSection(),
                section,
                "Expected section to be <%s> but was <%s>",
                section,
                actual.getSection()
            );
        }

        return this;
    }

    /**
     * Verifies that the {@link ParserLogEntry} does not have any line content attached (meaning line content should be {@code null}.
     *
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert doesNotHaveLineContent() {
        isNotNull();

        if (actual.getLineContent() != null) {
            throw failureWithActualExpected(
                actual.getLineContent(),
                null,
                "Expected no line content (null) but was <%s>",
                actual.getLineContent()
            );
        }

        return this;
    }

    /**
     * Verifies that the {@link ParserLogEntry} has exactly the specified line content attached.
     *
     * @param lineContent line content to be matched by equality
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert hasLineContent(String lineContent) {
        isNotNull();

        if (!Objects.equals(actual.getLineContent(), lineContent)) {
            throw failureWithActualExpected(
                actual.getLineContent(),
                lineContent,
                "Expected line content to be <%s> but was <%s>",
                lineContent,
                actual.getLineContent()
            );
        }

        return this;
    }

    /**
     * Verifies that the {@link ParserLogEntry} indicates the specified line rejection flag.
     *
     * @param lineRejected expected line rejection flag
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert indicatesLineRejection(boolean lineRejected) {
        isNotNull();

        if (actual.isLineRejected() != lineRejected) {
            throw failureWithActualExpected(
                actual.isLineRejected(),
                lineRejected,
                "Expected line rejection to be indicated as <%s> but was <%s>",
                lineRejected,
                actual.isLineRejected()
            );
        }

        return this;
    }

    /**
     * Verifies that the {@link ParserLogEntry} indicates a rejected line.
     *
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert indicatesRejectedLine() {
        return indicatesLineRejection(true);
    }

    /**
     * Verifies that the {@link ParserLogEntry} indicates an accepted line.
     *
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert indicatesAcceptedLine() {
        return indicatesLineRejection(false);
    }

    /**
     * Verifies that the {@link ParserLogEntry} holds exactly the specified message.
     *
     * @param message expected message to be matched by equality
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert hasMessage(String message) {
        isNotNull();

        if (!Objects.equals(actual.getMessage(), message)) {
            throw failureWithActualExpected(
                actual.getMessage(),
                message,
                "Expected message to be <%s> but was <%s>",
                message,
                actual.getMessage()
            );
        }

        return this;
    }

    /**
     * Verifies that the {@link ParserLogEntry} holds a message which contains the specified part.
     *
     * @param message expected part of message
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert hasMessageContaining(String message) {
        isNotNull();

        if (!actual.getMessage().contains(message)) {
            throw failureWithActualExpected(
                actual.getMessage(),
                message,
                "Expected message to contain <%s> but was <%s>",
                message,
                actual.getMessage()
            );
        }

        return this;
    }

    /**
     * Verifies that the {@link ParserLogEntry} holds a message which matches the specified {@link Pattern}.
     *
     * @param pattern {@link Pattern} the message has to fulfill
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert hasMessageMatching(Pattern pattern) {
        isNotNull();

        if (!pattern.matcher(actual.getMessage()).matches()) {
            throw failureWithActualExpected(
                actual.getMessage(),
                pattern.pattern(),
                "Expected message to match <%s> but was <%s>",
                pattern.pattern(),
                actual.getMessage()
            );
        }

        return this;
    }

    /**
     * Verifies that the {@link ParserLogEntry} holds a message which matches the specified regular expression.
     *
     * @param regex regular expression pattern the message has to fulfill
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert hasMessageMatching(String regex) {
        return hasMessageMatching(Pattern.compile(regex));
    }

    /**
     * Verifies that the {@link ParserLogEntry} does not have a {@link Throwable} attached.
     *
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert doesNotHaveThrowable() {
        isNotNull();

        if (actual.getThrowable() != null) {
            throw failureWithActualExpected(
                actual.getThrowable(),
                null,
                "Expected no throwable (null) but was <%s>",
                actual.getThrowable()
            );
        }

        return this;
    }

    /**
     * Verifies that the {@link ParserLogEntry} has the given {@link Throwable} attached, checked by instance comparison.
     *
     * @param throwable expected {@link Throwable}
     * @return this {@link ParserLogEntryAssert} for method-chaining
     */
    public ParserLogEntryAssert hasThrowable(Throwable throwable) {
        isNotNull();

        if (actual.getThrowable() != throwable) {
            throw failureWithActualExpected(
                actual.getThrowable(),
                throwable,
                "Expected same Throwable as <%s> but was <%s>",
                throwable,
                actual.getThrowable()
            );
        }

        return this;
    }
}
