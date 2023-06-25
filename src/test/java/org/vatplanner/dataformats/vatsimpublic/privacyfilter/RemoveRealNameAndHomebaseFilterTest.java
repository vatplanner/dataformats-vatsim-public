package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

class RemoveRealNameAndHomebaseFilterTest {

    private RemoveRealNameAndHomebaseFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RemoveRealNameAndHomebaseFilter();
    }

    @ParameterizedTest
    @CsvSource({
        // full online example
        "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:, UNK123:123456::PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",

        // full online example without airport code
        "UNK123:123456:John Doe:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:, UNK123:123456::PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",

        // full online example with just a single name
        "UNK123:123456:John:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:, UNK123:123456::PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",

        // full online example without name (e.g. filter already run)
        "UNK123:123456::PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:, UNK123:123456::PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:",

        // full prefile example
        "ABC987A:54321:Some Name ZYXW:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::, ABC987A:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",

        // full prefile example without airport code
        "ABC987A:54321:Some Name:::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::, ABC987A:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",

        // full prefile example without name (e.g. filter already run)
        "ABC987A:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::, ABC987A:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::",

        // minimal truncated examples
        "::John:, :::",
        "A:1:John:, A:1::"
    })
    void testApply_handledFormat_clearsThirdField(String input, String expectedOutput) {
        // Arrange (nothing to do)

        // Act
        String output = filter.apply(input);

        // Assert
        assertThat(output).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        ":",
        "A:1:",
        "A:2:C"
    })
    void testApply_unhandledFormat_returnsInputUnmodified(String input) {
        // Arrange (nothing to do)

        // Act
        String output = filter.apply(input);

        // Assert
        assertThat(output).isEqualTo(input);
    }

    @Test
    void testGetAffectedFields_always_returnsOnlyRealName() {
        // Arrange (nothing to do)

        // Act
        Set<ClientFields.FieldAccess<String>> result = filter.getAffectedFields();

        // Assert
        assertThat(result).containsExactly(ClientFields.StringFields.REAL_NAME);
    }

    @ParameterizedTest
    @EnumSource(
        value = ClientFields.StringFields.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"REAL_NAME"}
    )
    void testVerifyAffectedField_unhandledField_throwsException(ClientFields.FieldAccess<String> fieldAccess) {
        // Arrange (nothing to do)

        // Act
        ThrowableAssert.ThrowingCallable action = () -> filter.verifyAffectedField(fieldAccess, "", "");

        // Assert
        assertThatThrownBy(action).isInstanceOf(Exception.class);
    }

    @ParameterizedTest
    @CsvSource({
        "'', '', true",
        "abc, '', true",
        "abc, abc, false",
        "abc, a, false",
        "abc, c, false",
        "abc, 1, false",
        "'', a, false"
    })
    void testVerifyAffectedField_handledField_returnsExpectedResult(String original, String filtered, boolean expectedResult) {
        // Arrange (nothing to do)

        // Act
        boolean result = filter.verifyAffectedField(ClientFields.StringFields.REAL_NAME, original, filtered);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }
}
