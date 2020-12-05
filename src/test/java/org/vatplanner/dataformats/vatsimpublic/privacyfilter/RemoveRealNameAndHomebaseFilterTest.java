package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.testutils.DataProviderHelpers.allEnumValuesExcept;

import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class RemoveRealNameAndHomebaseFilterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RemoveRealNameAndHomebaseFilter filter;

    @Before
    public void setUp() {
        filter = new RemoveRealNameAndHomebaseFilter();
    }

    @Test
    @DataProvider({
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
    public void testApply_handledFormat_clearsThirdField(String input, String expectedOutput) {
        // Arrange (nothing to do)

        // Act
        String output = filter.apply(input);

        // Assert
        assertThat(output, is(equalTo(expectedOutput)));
    }

    @Test
    @DataProvider({
        "",
        ":",
        "A:1:",
        "A:2:C"
    })
    public void testApply_unhandledFormat_returnsInputUnmodified(String input) {
        // Arrange (nothing to do)

        // Act
        String output = filter.apply(input);

        // Assert
        assertThat(output, is(equalTo(input)));
    }

    @Test
    public void testGetAffectedFields_always_returnsOnlyRealName() {
        // Arrange (nothing to do)

        // Act
        Set<ClientFields.FieldAccess<String>> result = filter.getAffectedFields();

        // Assert
        assertThat(result, contains(ClientFields.StringFields.REAL_NAME));
    }

    @DataProvider
    public static Object[][] dataProviderUnhandledClientStringFields() {
        return allEnumValuesExcept(ClientFields.StringFields.class, ClientFields.StringFields.REAL_NAME);
    }

    @Test
    @UseDataProvider("dataProviderUnhandledClientStringFields")
    public void testVerifyAffectedField_unhandledField_throwsException(ClientFields.FieldAccess<String> fieldAccess) {
        // Arrange
        thrown.expect(Exception.class);

        // Act
        filter.verifyAffectedField(fieldAccess, "", "");

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({
        ", , true",
        "abc, , true",
        "abc, abc, false",
        "abc, a, false",
        "abc, c, false",
        "abc, 1, false",
        ", a, false"
    })
    public void testVerifyAffectedField_handledField_returnsExpectedResult(String original, String filtered, boolean expectedResult) {
        // Arrange (nothing to do)

        // Act
        boolean result = filter.verifyAffectedField(ClientFields.StringFields.REAL_NAME, original, filtered);

        // Assert
        assertThat(result, is(expectedResult));
    }
}
