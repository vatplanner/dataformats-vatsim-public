package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

class SubstituteObserverPrefixFilterTest {

    private SubstituteObserverPrefixFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SubstituteObserverPrefixFilter();
    }

    @Test
    void testGetAffectedFields_always_returnsOnlyCallsign() {
        // Arrange (nothing to do)

        // Act
        Set<ClientFields.FieldAccess<String>> result = filter.getAffectedFields();

        // Assert
        assertThat(result).containsExactly(ClientFields.StringFields.CALLSIGN);
    }

    @ParameterizedTest
    @CsvSource({
        // no _OBS suffix and unmodified => OK
        "'', '', true",
        "ABC123, ABC123, true",
        "OBS0815, OBS0815, true",
        "A_OBSI, A_OBSI, true",

        // _OBS suffix but unmodified => NOK
        "_OBS, _OBS, false",
        "AB_OBS, AB_OBS, false",

        // _OBS suffix and aliased => OK
        "_OBS, XX_OBS, true",
        "AB_OBS, XX_OBS, true",
        "XX_OBS, XX_OBS, true",

        // _OBS suffix but aliased wrongly => NOK
        "_OBS, XY_OBS, false",
        "AB_OBS, XY_OBS, false",
        "XX_OBS, XY_OBS, false",

        // no _OBS suffix but modified => NOK
        "ABC123, XX_OBS, false",
        "A_OBSI, XX_OBS, false"
    })
    void testVerify_handledField_returnsExpectedResult(String original, String filtered, boolean expectedResult) {
        // Arrange (nothing to do)

        // Act
        boolean result = filter.verifyAffectedField(ClientFields.StringFields.CALLSIGN, original, filtered);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @EnumSource(
        value = ClientFields.StringFields.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"CALLSIGN"}
    )
    void testVerifyAffectedField_unhandledField_throwsException(ClientFields.FieldAccess<String> fieldAccess) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> filter.verifyAffectedField(fieldAccess, "AB_OBS", "XX_OBS");

        // Assert
        assertThatThrownBy(action).isInstanceOf(Exception.class);
    }

    @ParameterizedTest
    @CsvSource({
        // full online examples (pilots)
        // - no suffix => remain unmodified
        "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:, UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",
        // - suffix => substitute
        "JD_OBS:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:, XX_OBS:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",

        // full online examples (ATC)
        // - no suffix => remain unmodified
        "ABCD_GND:123456:John Doe:ATC:123.450:52.12345:13.54321:0:::0::::MYSERVER:100:2::3:300::::::::::::::::$ SOME.SERVER.NET/WHATEVER:20190311090000:20190311083000::::, ABCD_GND:123456:John Doe:ATC:123.450:52.12345:13.54321:0:::0::::MYSERVER:100:2::3:300::::::::::::::::$ SOME.SERVER.NET/WHATEVER:20190311090000:20190311083000::::",
        // - suffix => substitute
        "JD_OBS:123456:John Doe:ATC:123.450:52.12345:13.54321:0:::0::::MYSERVER:100:2::3:300::::::::::::::::$ SOME.SERVER.NET/WHATEVER:20190311090000:20190311083000::::, XX_OBS:123456:John Doe:ATC:123.450:52.12345:13.54321:0:::0::::MYSERVER:100:2::3:300::::::::::::::::$ SOME.SERVER.NET/WHATEVER:20190311090000:20190311083000::::",

        // full prefiled example (pilots)
        // - no suffix => remain unmodified
        "ABC987A:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::, ABC987A:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",
        // - suffix => substitute
        "JD_OBS:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::, XX_OBS:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",

        // minimal truncated examples
        ":, :",
        "A:, A:",
        "_OBS:, XX_OBS:",
        "JD_OBS:, XX_OBS:",

        // do not replace anywhere but in callsign
        "ABC:123:JD_OBS:, ABC:123:JD_OBS:",
        "JD_OBS:123:JD_OBS:, XX_OBS:123:JD_OBS:"
    })
    void testApply_handledFormat_substitutesCallsignOnExpectedSuffix(String input, String expectedOutput) {
        // Arrange (nothing to do)

        // Act
        String output = filter.apply(input);

        // Assert
        assertThat(output).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "ABC",
        "JD_OBS" // without a field separator this is expected not to be processed
    })
    void testApply_unhandledFormat_returnsInputUnmodified(String input) {
        // Arrange (nothing to do)

        // Act
        String output = filter.apply(input);

        // Assert
        assertThat(output).isEqualTo(input);
    }
}
