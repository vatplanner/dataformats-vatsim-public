package org.vatplanner.dataformats.vatsimpublic.testutils;

import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Provides a Hamcrest {@link TypeSafeMatcher} for testing
 * {@link ParserLogEntry}. Can be instantiated either directly by constructor or
 * by "naturally named" static method {@link matchesParserLogEntry}.
 */
public class ParserLogEntryMatcher extends TypeSafeMatcher<ParserLogEntry> {

    private final Matcher<String> sectionMatcher;
    private final Matcher<String> lineContentMatcher;
    private final Matcher<Boolean> isLineRejectedMatcher;
    private final Matcher<String> messageMatcher;
    private final Matcher<Throwable> throwableMatcher;

    /**
     * Creates a new matcher applying all given matchers to correct attributes
     * {@link ParserLogEntry}.
     *
     * @param sectionMatcher matcher to be applied to
     * {@link ParserLogEntry#getSection()}
     * @param lineContentMatcher matcher to be applied to
     * {@link ParserLogEntry#getLineContent()}
     * @param isLineRejectedMatcher matcher to be applied to
     * {@link ParserLogEntry#isLineRejected()}
     * @param messageMatcher matcher to be applied to
     * {@link ParserLogEntry#getMessage()}
     * @param throwableMatcher matcher to be applied to
     * {@link ParserLogEntry#getThrowable()}
     */
    public ParserLogEntryMatcher(Matcher<String> sectionMatcher, Matcher<String> lineContentMatcher, Matcher<Boolean> isLineRejectedMatcher, Matcher<String> messageMatcher, Matcher<Throwable> throwableMatcher) {
        this.sectionMatcher = sectionMatcher;
        this.lineContentMatcher = lineContentMatcher;
        this.isLineRejectedMatcher = isLineRejectedMatcher;
        this.messageMatcher = messageMatcher;
        this.throwableMatcher = throwableMatcher;
    }

    /**
     * Creates a new matcher applying all given matchers to correct attributes
     * {@link ParserLogEntry}.
     *
     * @param sectionMatcher matcher to be applied to
     * {@link ParserLogEntry#getSection()}
     * @param lineContentMatcher matcher to be applied to
     * {@link ParserLogEntry#getLineContent()}
     * @param isLineRejectedMatcher matcher to be applied to
     * {@link ParserLogEntry#isLineRejected()}
     * @param messageMatcher matcher to be applied to
     * {@link ParserLogEntry#getMessage()}
     * @param throwableMatcher matcher to be applied to
     * {@link ParserLogEntry#getThrowable()}
     * @return matcher checking all attributes
     */
    public static ParserLogEntryMatcher matchesParserLogEntry(Matcher<String> sectionMatcher, Matcher<String> lineContentMatcher, Matcher<Boolean> isLineRejectedMatcher, Matcher<String> messageMatcher, Matcher<Throwable> throwableMatcher) {
        return new ParserLogEntryMatcher(sectionMatcher, lineContentMatcher, isLineRejectedMatcher, messageMatcher, throwableMatcher);
    }

    @Override
    protected boolean matchesSafely(ParserLogEntry actualEntry) {
        return sectionMatcher.matches(actualEntry.getSection())
                && lineContentMatcher.matches(actualEntry.getLineContent())
                && isLineRejectedMatcher.matches(actualEntry.isLineRejected())
                && messageMatcher.matches(actualEntry.getMessage())
                && throwableMatcher.matches(actualEntry.getThrowable());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Matcher expecting ParserLogEntry {\n");

        description.appendText("    Section: ");
        description.appendDescriptionOf(sectionMatcher);
        description.appendText("\n");

        description.appendText("    Line content: ");
        description.appendDescriptionOf(lineContentMatcher);
        description.appendText("\n");

        description.appendText("    Is line rejected: ");
        description.appendDescriptionOf(isLineRejectedMatcher);
        description.appendText("\n");

        description.appendText("    Message: ");
        description.appendDescriptionOf(messageMatcher);
        description.appendText("\n");

        description.appendText("    Throwable: ");
        description.appendDescriptionOf(throwableMatcher);
        description.appendText("\n");

        description.appendText("}");
    }

    @Override
    protected void describeMismatchSafely(ParserLogEntry actualEntry, Description mismatchDescription) {
        describeOnMismatch("Section", actualEntry.getSection(), sectionMatcher, mismatchDescription);
        describeOnMismatch("Line content", actualEntry.getLineContent(), lineContentMatcher, mismatchDescription);
        describeOnMismatch("Line rejection", actualEntry.isLineRejected(), isLineRejectedMatcher, mismatchDescription);
        describeOnMismatch("Message", actualEntry.getMessage(), messageMatcher, mismatchDescription);
        describeOnMismatch("Throwable", actualEntry.getThrowable(), throwableMatcher, mismatchDescription);
    }

    /**
     * Checks if the given matcher reports a mismatch and adds the original
     * checker's mismatch description.
     *
     * @param <T> type of objects validated by matcher
     * @param attributeDescription description of attribute matcher belongs to
     * (will be used as prefix to matcher description)
     * @param actualValue actual value to be validated against configured
     * matcher
     * @param matcher matcher configured with an expectation
     * @param mismatchDescription receives detailed description on mismatch
     */
    private <T> void describeOnMismatch(String attributeDescription, T actualValue, Matcher<T> matcher, Description mismatchDescription) {
        if (!matcher.matches(actualValue)) {
            mismatchDescription.appendText("\n    " + attributeDescription + "  does not match: ");
            matcher.describeMismatch(actualValue, mismatchDescription);
        }
    }

}
