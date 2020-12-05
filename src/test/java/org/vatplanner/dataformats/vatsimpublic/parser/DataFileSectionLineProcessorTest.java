package org.vatplanner.dataformats.vatsimpublic.parser;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.function.UnaryOperator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class DataFileSectionLineProcessorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @DataProvider
    public static Object[][] dataProviderStrings() {
        return new Object[][] { //
            { "" }, //
            { "just a one-liner" }, //
            { "just a one-liner terminated\n" }, //
            { "\n\r\n\r\r\n\r" }, //
            { ";some comment\r\n!GENERAL:\r\nABC = 1\r\nSECOND = 123\r\n!SOMETHINGELSE:\r\n!GENERAL:\r\nABC = 0\r\n" }, //
        };
    }

    @DataProvider
    public static Object[][] dataProviderApplicationToSection() {
        UnaryOperator<String> function1 = s -> "prefix " + s + " postfix";
        UnaryOperator<String> function2 = String::toLowerCase;

        return new Object[][] { //
            { "", "SECTION", function1, "" }, //
            { "\n", "SECTION", function1, "\n" }, //
            { "A", "SECTION", function1, "A" }, //
            { "!SECTION:\nA\n\n", "SECTION", function1, "!SECTION:\nprefix A postfix\n\n" }, //
            { "!SECTION:\nA\n\n", "sEcTIOn", function1, "!SECTION:\nprefix A postfix\n\n" }, //
            {
                "!SECTION:\n;comments should not be processed\nA\n; trailing\n",
                "SECTION",
                function1,
                "!SECTION:\n;comments should not be processed\nprefix A postfix\n; trailing\n"
            }, //
            {
                "!SECTION:\nA\nB\n\rC",
                "SECTION",
                function1,
                "!SECTION:\nprefix A postfix\nprefix B postfix\n\rprefix C postfix"
            }, //
            {
                "!SECTION:\nA\n!ANOTHERSECTION:\nB\n!SECTION:\nC",
                "SECTION",
                function1,
                "!SECTION:\nprefix A postfix\n!ANOTHERSECTION:\nB\n!SECTION:\nprefix C postfix"
            }, //
            {
                "!SECTION:\nA\n!ANOTHERSECTION:\nB\n!SECTION:\nC",
                "ANOTHERSECTION",
                function1,
                "!SECTION:\nA\n!ANOTHERSECTION:\nprefix B postfix\n!SECTION:\nC"
            }, //
            {
                "!SECTION:\r\nA\r\n\nB\nC",
                "SECTION",
                function2,
                "!SECTION:\r\na\r\n\nb\nc"
            }, //
            {
                ";WHATEVER\n!SECTION:\nSOMECONTENT = 1",
                "SECTION",
                function2,
                ";WHATEVER\n!SECTION:\nsomecontent = 1"
            }, //
        };
    }

    @Test
    @UseDataProvider("dataProviderStrings")
    public void testGetResultAsString_notApplied_returnsInput(String input) {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor(input);

        // Act
        String result = processor.getResultAsString();

        // Assert
        assertThat(result, is(equalTo(input)));
    }

    @Test
    @UseDataProvider("dataProviderApplicationToSection")
    public void testGetResultAsString_applied_returnsAlteredOutput(String input, String sectionName, UnaryOperator<String> function, String expectedResult) {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor(input);
        processor.apply(sectionName, function);

        // Act
        String result = processor.getResultAsString();

        // Assert
        assertThat(result, is(equalTo(expectedResult)));
    }

    @Test
    public void testGetResultAsString_appliedTwice_returnsResultCombinedInCorrectOrder() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("!TEST:\nA\nB");
        processor.apply("TEST", s -> s.toLowerCase());
        processor.apply("TEST", s -> s + s.toUpperCase());

        // Act
        String result = processor.getResultAsString();

        // Assert
        assertThat(result, is(equalTo("!TEST:\naA\nbB")));
    }

    @Test
    public void testApply_nullFunction_throwsIllegalArgumentException() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("");

        thrown.expect(IllegalArgumentException.class);

        // Act
        processor.apply("A", null);

        // Assert (nothing to do)
    }

    @Test
    public void testApply_nullSectionName_throwsIllegalArgumentException() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("");

        thrown.expect(IllegalArgumentException.class);

        // Act
        processor.apply(null, UnaryOperator.identity());

        // Assert (nothing to do)
    }

    @Test
    public void testApply_emptySectionName_throwsIllegalArgumentException() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("");

        thrown.expect(IllegalArgumentException.class);

        // Act
        processor.apply("", UnaryOperator.identity());

        // Assert (nothing to do)
    }

    @Test
    public void testApply_valid_returnsSameInstance() {
        // Arrange
        DataFileSectionLineProcessor processor = new DataFileSectionLineProcessor("");

        // Act
        DataFileSectionLineProcessor returned = processor.apply("a", UnaryOperator.identity());

        // Assert
        assertThat(returned, is(sameInstance(processor)));
    }

}
