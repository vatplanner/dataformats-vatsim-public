package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

class FlightPlanRemarksRemoveAllFilterTest {

    @Test
    void testGetAffectedFields_always_returnsOnlyFlightPlanRemarks() {
        // Arrange
        FlightPlanRemarksRemoveAllFilter filter = new FlightPlanRemarksRemoveAllFilter(null);

        // Act
        Set<ClientFields.FieldAccess<String>> result = filter.getAffectedFields();

        // Assert
        assertThat(result).containsExactly(ClientFields.StringFields.FLIGHT_PLAN_REMARKS);
    }

    static Stream<Arguments> dataProviderInvalidTriggers() {
        return Stream.of(
            asList((Object) null),
            asList(""),
            asList(" "),
            asList("\n"),
            asList("valid", "", "also valid"),
            asList("valid", " ", "also valid"),
            asList("valid", "\n", "also valid"),
            asList("valid", " \r\n \n \n\r ", "also valid"),
            asList("valid", null, "also valid")
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("dataProviderInvalidTriggers")
    void testConstructor_invalidTriggers_throwsIllegalArgumentException(Collection<String> invalidTriggers) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new FlightPlanRemarksRemoveAllFilter(invalidTriggers);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> dataProviderIsConditionMet() {
        return Stream.of(
            // always met if triggers is null or empty
            Arguments.of(null, "does not matter", true),
            Arguments.of(asList(), "does not matter", true),

            // trigger found in content
            Arguments.of(asList("trigger"), "this one should trigger", true),
            Arguments.of(asList("abc", "x y/z"), "when abc goes x y/z it's bad", true),
            Arguments.of(asList("abc", "x y/z"), "just abc is bad too", true),
            Arguments.of(asList("abc", "x y/z"), "x y/z looks quite mathematical", true),
            Arguments.of(asList("abc", "x y/z"), "whenwordscollapseabcshallbebadtoo", true),

            // case-insensitive matches
            Arguments.of(asList("TriGgeR"), "This should trigger too.", true),
            Arguments.of(asList("trigger"), "This should tRIgGER too.", true),

            // trigger not found in content
            Arguments.of(asList("trigger"), "", false),
            Arguments.of(asList("trigger"), "this one is clean", false),
            Arguments.of(asList("abc", "x y/z"), "should pass fine", false)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderIsConditionMet")
    void testIsConditionMet_withFieldContent_returnsExpectedResult(Collection<String> triggers, String fieldContent, boolean expectedResult) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter filter = new FlightPlanRemarksRemoveAllFilter(triggers);

        // Act
        boolean result = filter.isConditionMet(fieldContent);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    static Stream<Arguments> dataProviderEmptyFieldContent() {
        return Stream.of(
            null,
            "",
            " ",
            " \n \r\n \n\r "
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("dataProviderEmptyFieldContent")
    void testIsConditionMet_withoutFieldContentUnconditionally_returnsFalse(String fieldContent) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter filter = new FlightPlanRemarksRemoveAllFilter(null);

        // Act
        boolean result = filter.isConditionMet(fieldContent);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        // empty input
        "'', '', false, true",
        "'', '', true, true",

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
        "here goes /V/ some content, '', true, false", // matched but comm flag missing => NOK
        "here /R/goes some content, '', true, false", // matched but comm flag missing => NOK
        "here goes some content/T/, '', true, false", // matched but comm flag missing => NOK
        "here goes some/v/ content, '', true, false", // matched but comm flag missing => NOK
        "here goes some content /r/, '', true, false", // matched but comm flag missing => NOK
        "he/t/re goes some content, '', true, false", // matched but comm flag missing => NOK

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
    void testVerifyAffectedField_handledField_returnsExpectedResult(String original, String filtered, boolean isConditionMet, boolean expectedResult) {
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
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @EnumSource(
        value = ClientFields.StringFields.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"FLIGHT_PLAN_REMARKS"}
    )
    void testVerifyAffectedField_unhandledField_throwsException(ClientFields.FieldAccess<String> unhandledFieldAccess) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter filter = new FlightPlanRemarksRemoveAllFilter(null);

        // Act
        ThrowingCallable action = () -> filter.verifyAffectedField(unhandledFieldAccess, "", "");

        // Assert
        assertThatThrownBy(action).isInstanceOf(Exception.class);
    }

    static Stream<Arguments> dataProviderApplicationInputAndExpectedFilteredOutput() {
        return Stream.of(
            // field content needs to be passed in order to verify that
            // isConditionMet is actually called with correct parameter to
            // evaluate conditions properly (checked in separate test cases to
            // reduce complexity of tests)

            // full online examples, no VFPS, communication flag switching positions and
            // case
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "SOME COMMENT /v/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME /r/ COMMENT:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "SOME /r/ COMMENT",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/t/SOME COMMENT:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/t/SOME COMMENT",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/SOME COMMENT:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/V/SOME COMMENT",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME /T/ COMMENT:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "SOME /T/ COMMENT",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT/r/ :THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "SOME COMMENT/r/ ",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),

            // full online examples, with VFPS, communication flag switching positions and
            // case
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/r/something else:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/r/something else",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+something in between/t/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+something in between/t/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),

            // full online examples, no VFPS, input already as filtered (e.g. reapplying
            // filter); upper-case is expected as output
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/V/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/R/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/T/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/v/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/r/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/r/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/t/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "/t/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),

            // full online examples, with VFPS, input already as filtered (e.g. reapplying
            // filter); upper-case is expected as output
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/V/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/R/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/T/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/v/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/V/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/r/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/r/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/R/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),
            Arguments.of(
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/t/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
                "+VFPS+/t/",
                "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:+VFPS+/T/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:"
            ),

            // variations (prefiling, additional markup)
            Arguments.of(
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            ),
            Arguments.of(
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/RMK/123 /t/ //:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/RMK/123 /t/ //",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            ),

            // multiple comm flags => precedence according to expected priority V over R
            // over T
            Arguments.of(
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//R//v/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//R//v/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/V/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            ),
            Arguments.of(
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/T//r//V/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/T//r//V/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/V/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            ),
            Arguments.of(
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//R//T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//R//T/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/R/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            ),
            Arguments.of(
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//r//T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//r//T/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/R/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            ),
            Arguments.of(
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//T/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            ),
            Arguments.of(
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/t//t/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
                "/t//t/",
                "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:/T/:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::"
            ),

            // minimal examples
            Arguments.of(":::::::::::::::::::::::::::::", "", ":::::::::::::::::::::::::::::"),
            Arguments.of(":::::::::::::::::::::::::::::a", "a", ":::::::::::::::::::::::::::::"),
            Arguments.of(":::::::::::::::::::::::::::::a:b", "a", "::::::::::::::::::::::::::::::b"),
            Arguments.of(":::::::::::::::::::::::::::::+VFPS+/r/", "+VFPS+/r/", ":::::::::::::::::::::::::::::+VFPS+/R/"),

            // missing expected field
            Arguments.of("::::::::::::::::::::::::::::", "never to be called", "::::::::::::::::::::::::::::"),
            Arguments.of("", "never to be called", ""),
            Arguments.of("whatever", "never to be called", "whatever")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderApplicationInputAndExpectedFilteredOutput")
    void testApply_conditionsMet_returnsExpectedFilteredOutput(String input, String expectedFieldContent, String expectedOutput) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter spyFilter = createSpyFilter(null);
        doReturn(true).when(spyFilter).isConditionMet(expectedFieldContent);

        // Act
        String output = spyFilter.apply(input);

        // Assert
        assertThat(output).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @MethodSource("dataProviderApplicationInputAndExpectedFilteredOutput")
    void testApply_conditionsNotMet_returnsOriginalInput(String input, String expectedFieldContent, String unexpectedOutput) {
        // Arrange
        FlightPlanRemarksRemoveAllFilter spyFilter = createSpyFilter(null);
        doReturn(false).when(spyFilter).isConditionMet(expectedFieldContent);

        // Act
        String output = spyFilter.apply(input);

        // Assert
        assertThat(output).isEqualTo(input);
    }

    private FlightPlanRemarksRemoveAllFilter createSpyFilter(Collection<String> triggers) {
        return spy(new FlightPlanRemarksRemoveAllFilter(triggers));
    }
}
