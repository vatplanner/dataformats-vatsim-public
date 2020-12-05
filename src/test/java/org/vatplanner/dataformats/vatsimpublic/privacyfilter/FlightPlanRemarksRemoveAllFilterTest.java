package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.vatplanner.dataformats.vatsimpublic.testutils.DataProviderHelpers.allEnumValuesExcept;
import static org.vatplanner.dataformats.vatsimpublic.testutils.DataProviderHelpers.asTwoDimensionalArray;

import java.util.Collection;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class FlightPlanRemarksRemoveAllFilterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetAffectedFields_always_returnsOnlyFlightPlanRemarks() {
        // Arrange
        FlightPlanRemarksRemoveAllFilter filter = new FlightPlanRemarksRemoveAllFilter(null);

        // Act
        Set<ClientFields.FieldAccess<String>> result = filter.getAffectedFields();

        // Assert
        assertThat(result, contains(ClientFields.StringFields.FLIGHT_PLAN_REMARKS));
    }

    @DataProvider
    public static Object[][] dataProviderInvalidTriggers() {
        return new Object[][] {
            { asList((Object) null) },
            { asList("") },
            { asList(" ") },
            { asList("\n") },
            { asList("valid", "", "also valid") },
            { asList("valid", " ", "also valid") },
            { asList("valid", "\n", "also valid") },
            { asList("valid", " \r\n \n \n\r ", "also valid") },
            { asList("valid", null, "also valid") }
        };
    }

    @Test
    @UseDataProvider("dataProviderInvalidTriggers")
    public void testConstructor_invalidTriggers_throwsIllegalArgumentException(Collection<String> invalidTriggers) {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        new FlightPlanRemarksRemoveAllFilter(invalidTriggers);

        // Assert (nothing to do)
    }

    @DataProvider
    public static Object[][] dataProviderIsConditionMet() {
        return new Object[][] {
            // always met if triggers is null or empty
            { null, "does not matter", true }, //
            { asList(), "does not matter", true }, //

            // trigger found in content
            { asList("trigger"), "this one should trigger", true }, //
            { asList("abc", "x y/z"), "when abc goes x y/z it's bad", true }, //
            { asList("abc", "x y/z"), "just abc is bad too", true }, //
            { asList("abc", "x y/z"), "x y/z looks quite mathematical", true }, //
            { asList("abc", "x y/z"), "whenwordscollapseabcshallbebadtoo", true }, //

            // case-insensitive matches
            { asList("TriGgeR"), "This should trigger too.", true }, //
            { asList("trigger"), "This should tRIgGER too.", true }, //

            // trigger not found in content
            { asList("trigger"), "", false }, //
            { asList("trigger"), "this one is clean", false }, //
            { asList("abc", "x y/z"), "should pass fine", false }, //
        };
    }

    @Test
    @UseDataProvider("dataProviderIsConditionMet")
    public void testIsConditionMet_withFieldContent_returnsExpectedResult(Collection<String> triggers, String fieldContent, boolean expectedResult) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter filter = new FlightPlanRemarksRemoveAllFilter(triggers);

        // Act
        boolean result = filter.isConditionMet(fieldContent);

        // Assert
        assertThat(result, is(expectedResult));
    }

    @DataProvider
    public static Object[][] dataProviderEmptyFieldContent() {
        return asTwoDimensionalArray(
            null,
            "",
            " ",
            " \n \r\n \n\r " //
        );
    }

    @Test
    @UseDataProvider("dataProviderEmptyFieldContent")
    public void testIsConditionMet_withoutFieldContentUnconditionally_returnsFalse(String fieldContent) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter filter = new FlightPlanRemarksRemoveAllFilter(null);

        // Act
        boolean result = filter.isConditionMet(fieldContent);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    @DataProvider({ //
        // empty input
        ", , false, true",
        ", , true, true", //

        // VFPS prefix
        "+VFPS+/V/some text in here, +VFPS+/V/some text in here, false, true", // not matched and unchanged => OK
        "+VFPS+/V/some text in here, +VFPS+/V/some text in Here, false, false", // not matched but changed => NOK
        "+VFPS+/V/some text in here, +VFPS+/V/, true, true", // matched and fully filtered => OK
        "+VFPS+/V/some text in here, +VFPS+/V/a, true, false", // matched but not fully filtered => NOK
        "+VFPS+/V/some text in here, +VFPS+, true, false", // matched but comm flag missing => NOK
        "+VFPS+/V/some text in here, /V/, true, false", // matched but VFPS flag missing => NOK
        "+VFPS+/R/some text in here, +VFPS+/R/, true, true", // matched and fully filtered => OK
        "+VFPS+/T/some text in here, +VFPS+/T/, true, true", // matched and fully filtered => OK
        "+VFPS+/v/some text in here, +VFPS+/v/, true, true", // matched and fully filtered => OK
        "+VFPS+/r/some text in here, +VFPS+/r/, true, true", // matched and fully filtered => OK
        "+VFPS+/t/some text in here, +VFPS+/t/, true, true", // matched and fully filtered => OK
        "+VFPS+/v/some text in here, +VFPS+/V/, true, true", // matched and fully filtered => OK (case change OK)
        "+VFPS+/r/some text in here, +VFPS+/R/, true, true", // matched and fully filtered => OK (case change OK)
        "+VFPS+/t/some text in here, +VFPS+/T/, true, true", // matched and fully filtered => OK (case change OK)

        // VFPS prefix + variable position of comm flag
        "+VFPS+some text in/V/here, +VFPS+/V/, true, true", // matched but not fully filtered => NOK
        "+VFPS+some text in here/R/, +VFPS+/R/, true, true", // matched and fully filtered => OK
        "+VFPS+some text/T/in here, +VFPS+/T/, true, true", // matched and fully filtered => OK
        "+VFPS+some/v/ text in here, +VFPS+/v/, true, true", // matched and fully filtered => OK
        "+VFPS+some text in /r/ here , +VFPS+/r/, true, true", // matched and fully filtered => OK
        "+VFPS+some text in here /t/ , +VFPS+/t/, true, true", // matched and fully filtered => OK
        "+VFPS+some/v/ text in here, +VFPS+/V/, true, true", // matched and fully filtered => OK (case change OK)
        "+VFPS+some text in /r/ here , +VFPS+/R/, true, true", // matched and fully filtered => OK (case change OK)
        "+VFPS+some text in here /t/ , +VFPS+/T/, true, true", // matched and fully filtered => OK (case change OK)
        "+VFPS+some text in/V/here, +VFPS+, true, false", // matched but comm flag missing => NOK
        "+VFPS+some text in here/R/, +VFPS+, true, false", // matched but comm flag missing => NOK
        "+VFPS+some text/T/in here, +VFPS+, true, false", // matched but comm flag missing => NOK
        "+VFPS+some/v/ text in here, +VFPS+, true, false", // matched but comm flag missing => NOK
        "+VFPS+some text in /r/ here , +VFPS+, true, false", // matched but comm flag missing => NOK
        "+VFPS+some text in here /t/ , +VFPS+, true, false", // matched but comm flag missing => NOK

        // VFPS prefix + variable position + custom meta data
        "+VFPS+some text/T/ in/VA/ here, +VFPS+/T/, true, true", // matched and fully filtered => OK
        "+VFPS+some text/VA/ in/T/ here, +VFPS+/T/, true, true", // matched and fully filtered => OK
        "+VFPS+some text/T/ in/VA/ here, +VFPS+/V/, true, false", // matched but misinterpreted meta data => NOK
        "+VFPS+some text/VA/ in/T/ here, +VFPS+/V/, true, false", // matched but misinterpreted meta data => NOK
        "+VFPS+some text/T/ in/VA/ here, +VFPS+/V/, false, false", // not matched but changed => NOK
        "+VFPS+some text/T/ in/VA/ here, +VFPS+some text/T/ in/VA/ here, false, true", // not matched and unchanged =>
                                                                                       // OK

        // no VFPS flag
        "/V/here goes some content, /V/, true, true", // matched and fully filtered => OK
        "/V/here goes some content, /V/, false, false", // not matched but changed => NOK
        "/V/here goes some content, /V/a, false, false", // not matched but changed => NOK
        "/V/here goes some content, /V/here goes some content, false, true", // not matched and unchanged => OK
        "/R/here goes some content, /R/, true, true", // matched and fully filtered => OK
        "/T/here goes some content, /T/, true, true", // matched and fully filtered => OK
        "/v/here goes some content, /v/, true, true", // matched and fully filtered => OK
        "/r/here goes some content, /r/, true, true", // matched and fully filtered => OK
        "/t/here goes some content, /t/, true, true", // matched and fully filtered => OK
        "/v/here goes some content, /V/, true, true", // matched and fully filtered => OK (case change OK)
        "/r/here goes some content, /R/, true, true", // matched and fully filtered => OK (case change OK)
        "/t/here goes some content, /T/, true, true", // matched and fully filtered => OK (case change OK)
        "/V/here goes some content, /v/, true, true", // matched and fully filtered => OK (case change OK)
        "/R/here goes some content, /r/, true, true", // matched and fully filtered => OK (case change OK)
        "/T/here goes some content, /t/, true, true", // matched and fully filtered => OK (case change OK)

        // no VFPS flag + variable position
        "here goes /V/ some content, /V/, true, true", // matched and fully filtered => OK
        "here /R/goes some content, /R/, true, true", // matched and fully filtered => OK
        "here goes some content/T/, /T/, true, true", // matched and fully filtered => OK
        "here goes some/v/ content, /v/, true, true", // matched and fully filtered => OK
        "here goes some content /r/, /r/, true, true", // matched and fully filtered => OK
        "he/t/re goes some content, /t/, true, true", // matched and fully filtered => OK
        "here goes some/v/ content, /V/, true, true", // matched and fully filtered => OK (case change OK)
        "here goes some content /r/, /R/, true, true", // matched and fully filtered => OK (case change OK)
        "he/t/re goes some content, /T/, true, true", // matched and fully filtered => OK (case change OK)
        "here goes /V/ some content, , true, false", // matched but comm flag missing => NOK
        "here /R/goes some content, , true, false", // matched but comm flag missing => NOK
        "here goes some content/T/, , true, false", // matched but comm flag missing => NOK
        "here goes some/v/ content, , true, false", // matched but comm flag missing => NOK
        "here goes some content /r/, , true, false", // matched but comm flag missing => NOK
        "he/t/re goes some content, , true, false", // matched but comm flag missing => NOK

        // no VFPS flag + variable position + custom meta data
        "here goes /VA/ some /T/ content, /T/, true, true", // matched and fully filtered => OK
        "here goes /T/ some /VA/ content, /T/, true, true", // matched and fully filtered => OK
        "here goes /RMK/ some /T/ content, /T/, true, true", // matched and fully filtered => OK
        "here goes /TCAS/ some /V/ content, /V/, true, true", // matched and fully filtered => OK
        "here goes /V/ some /TCAS/ content, /V/, true, true", // matched and fully filtered => OK
        "here goes /RMK/ some /T/ content, /T/, false, false", // not matched but changed => NOK
        "here goes /RMK/ some /T/ content, here goes /RMK/ some /T/ content, false, true", // not matched and unchanged
                                                                                           // => OK

        // multiple flags, expecting precedence of V over R over T; case changes OK
        "a/T/b/r//v/c, /V/, true, true", // T, R, V => V
        "a/T/b/r//v/c, /v/, true, true", // T, R, V => V
        "a/r/b/v//t/c, /V/, true, true", // R, V, T => V
        "a/r/b/v//t/c, /v/, true, true", // R, V, T => V
        "a/r/b/v//t/c, /R/, true, false", // R, V, T =/=> R
        "a/r/b/v//t/c, /r/, true, false", // R, V, T =/=> R
        "a/r/b/v//t/c, /T/, true, false", // R, V, T =/=> T
        "a/r/b/v//t/c, /t/, true, false", // R, V, T =/=> T
        "a/t/b/r//t/c, /R/, true, true", // T, R, T => R
        "a/t/b/r//t/c, /r/, true, true", // T, R, T => R
        "a/t/b/r//t/c, /T/, true, false", // T, R, T =/=> T
        "a/t/b/r//t/c, /t/, true, false", // T, R, T =/=> T
        "a/t/b/t//t/, /T/, true, true", // T
        "a/t/b/t//t/, /t/, true, true", // T
    })
    public void testVerifyAffectedField_handledField_returnsExpectedResult(String original, String filtered, boolean isConditionMet, boolean expectedResult) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter spyFilter = createSpyFilter(null);
        doReturn(isConditionMet).when(spyFilter).isConditionMet(original);

        // Act
        boolean result = spyFilter.verifyAffectedField(
            ClientFields.StringFields.FLIGHT_PLAN_REMARKS,
            original,
            filtered //
        );

        // Assert
        assertThat(result, is(expectedResult));
    }

    @DataProvider
    public static Object[][] dataProviderUnhandledClientFields() {
        return allEnumValuesExcept(ClientFields.StringFields.class, ClientFields.StringFields.FLIGHT_PLAN_REMARKS);
    }

    @Test
    @UseDataProvider("dataProviderUnhandledClientFields")
    public void testVerifyAffectedField_unhandledField_throwsException(ClientFields.FieldAccess<String> unhandledFieldAccess) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter filter = new FlightPlanRemarksRemoveAllFilter(null);

        thrown.expect(Exception.class);

        // Act
        filter.verifyAffectedField(unhandledFieldAccess, "", "");

        // Assert (nothing to do)
    }

    @DataProvider
    public static Object[][] dataProviderApplicationInputAndExpectedFilteredOutput() {
        return new Object[][] {
            // field content needs to be passed in order to verify that
            // isConditionMet is actually called with correct parameter to
            // evaluate conditions properly (checked in separate test cases to
            // reduce complexity of tests)

            // full online examples, no VFPS, communication flag switching positions and
            // case
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "SOME COMMENT /v/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME /r/ COMMENT:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "SOME /r/ COMMENT",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/t/SOME COMMENT:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/t/SOME COMMENT",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/SOME COMMENT:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/V/SOME COMMENT",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME /T/ COMMENT:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "SOME /T/ COMMENT",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT/r/ :THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "SOME COMMENT/r/ ",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //

            // full online examples, with VFPS, communication flag switching positions and
            // case
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/r/something else:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/r/something else",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+something in between/t/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+something in between/t/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //

            // full online examples, no VFPS, input already as filtered (e.g. reapplying
            // filter); upper-case is expected as output
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/V/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/R/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/T/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/v/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/r/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/r/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/t/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/t/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //

            // full online examples, with VFPS, input already as filtered (e.g. reapplying
            // filter); upper-case is expected as output
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/V/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/R/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/T/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/v/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/r/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/r/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //
            {
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/t/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/t/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            }, //

            // variations (prefiling, additional markup)
            {
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            }, //
            {
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/RMK/123 /t/ //:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/RMK/123 /t/ //",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            }, //

            // multiple comm flags => precedence according to expected priority V over R
            // over T
            {
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//R//v/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//R//v/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/V/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            }, //
            {
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/T//r//V/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/T//r//V/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/V/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            }, //
            {
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//R//T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//R//T/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/R/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            }, //
            {
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//r//T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//r//T/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/R/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            }, //
            {
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//T/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            }, //
            {
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//t/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//t/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            }, //

            // minimal examples
            { ":::::::::::::::::::::::::::::", "", ":::::::::::::::::::::::::::::" }, //
            { ":::::::::::::::::::::::::::::a", "a", ":::::::::::::::::::::::::::::" }, //
            { ":::::::::::::::::::::::::::::a:b", "a", "::::::::::::::::::::::::::::::b" }, //
            { ":::::::::::::::::::::::::::::+VFPS+/r/", "+VFPS+/r/", ":::::::::::::::::::::::::::::+VFPS+/R/" }, //

            // missing expected field
            { "::::::::::::::::::::::::::::", "never to be called", "::::::::::::::::::::::::::::" }, //
            { "", "never to be called", "" }, //
            { "whatever", "never to be called", "whatever" }, //
        };
    }

    @Test
    @UseDataProvider("dataProviderApplicationInputAndExpectedFilteredOutput")
    public void testApply_conditionsMet_returnsExpectedFilteredOutput(String input, String expectedFieldContent, String expectedOutput) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter spyFilter = createSpyFilter(null);
        doReturn(true).when(spyFilter).isConditionMet(expectedFieldContent);

        // Act
        String output = spyFilter.apply(input);

        // Assert
        assertThat(output, is(equalTo(expectedOutput)));
    }

    @Test
    @UseDataProvider("dataProviderApplicationInputAndExpectedFilteredOutput")
    public void testApply_conditionsNotMet_returnsOriginalInput(String input, String expectedFieldContent, String unexpectedOutput) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter spyFilter = createSpyFilter(null);
        doReturn(false).when(spyFilter).isConditionMet(expectedFieldContent);

        // Act
        String output = spyFilter.apply(input);

        // Assert
        assertThat(output, is(equalTo(input)));
    }

    private FlightPlanRemarksRemoveAllFilter createSpyFilter(Collection<String> triggers) {
        FlightPlanRemarksRemoveAllFilter templateFilter = new FlightPlanRemarksRemoveAllFilter(triggers);
        FlightPlanRemarksRemoveAllFilter spyFilter = spy(templateFilter);

        return spyFilter;
    }
}
