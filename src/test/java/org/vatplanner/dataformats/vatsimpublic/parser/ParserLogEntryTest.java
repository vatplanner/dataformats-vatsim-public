package org.vatplanner.dataformats.vatsimpublic.parser;

import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link ParserLogEntry}.
 */
@RunWith(DataProviderRunner.class)
public class ParserLogEntryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @DataProvider
    public static Object[][] dataProviderThrowablesAndExpectedClassNames() {
        return new Object[][]{
            new Object[]{new Throwable(), "java.lang.Throwable"},
            new Object[]{new IllegalArgumentException(), "java.lang.IllegalArgumentException"}
        };
    }

    @Test
    public void testConstructor_nullMessage_throwsIllegalArgumentException() {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        new ParserLogEntry("abc", "xyz", false, null, null);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"", "abc"})
    public void testConstructor_nonNullMessage_doesNotFail(String message) {
        // Arrange (nothing to do)

        // Act
        new ParserLogEntry("abc", "xyz", false, message, null);

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({"SOME_SECTION", "somethingElse"})
    public void testToString_nonNullSection_listsSection(String expectedSection) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry(expectedSection, "xyz", false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString("section: " + expectedSection));
    }

    @Test
    public void testToString_nullSection_listsNullAsSection() {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry(null, "xyz", false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString("section: null"));
    }

    @Test
    @DataProvider({"This is some line content.", "1:2:3:4:some:more content"})
    public void testToString_nonNullLineContent_listsLineContent(String expectedLineContent) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", expectedLineContent, false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString("line: \"" + expectedLineContent + "\""));
    }

    @Test
    public void testToString_nullLineContent_listsNullAsLineContent() {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", null, false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString("line: null"));
    }

    @Test
    @DataProvider({"true,true", "false,false"})
    public void testToString_anyLineContent_listsLineRejection(boolean isLineRejected, String expectedRejectionOutput) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", isLineRejected, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString("rejected: " + expectedRejectionOutput));
    }

    @Test
    @DataProvider({"Expected message #1", "This is another message to be expected."})
    public void testToString_anyMessage_containsMessage(String expectedMessage) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", false, expectedMessage, null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString(expectedMessage));
    }

    @Test
    @UseDataProvider("dataProviderThrowablesAndExpectedClassNames")
    public void testToString_nonNullThrowable_containsThrowableClassName(Throwable throwable, String expectedClassName) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", false, "some message", throwable);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString(expectedClassName));
    }

    @Test
    @DataProvider({"Message of Throwable", "Another exception detail."})
    public void testToString_nonNullThrowable_containsThrowableMessage(String expectedThrowableMessage) {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", false, "some message", new Throwable(expectedThrowableMessage));

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString(expectedThrowableMessage));
    }

    @Test
    public void testToString_nullThrowable_listsNoneAsException() {
        // Arrange
        ParserLogEntry entry = new ParserLogEntry("abc", "xyz", false, "some message", null);

        // Act
        String result = entry.toString();

        // Assert
        assertThat(result, containsString("exception: none"));
    }
}
