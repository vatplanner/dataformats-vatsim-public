package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.Set;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;
import static org.vatplanner.dataformats.vatsimpublic.testutils.DataProviderHelpers.allEnumValuesExcept;

@RunWith(DataProviderRunner.class)
public class SubstituteObserverPrefixFilterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SubstituteObserverPrefixFilter filter;

    @Before
    public void setUp() {
        filter = new SubstituteObserverPrefixFilter();
    }

    @Test
    public void testGetAffectedFields_always_returnsOnlyCallsign() {
        // Arrange (nothing to do)

        // Act
        Set<ClientFields.FieldAccess<String>> result = filter.getAffectedFields();

        // Assert
        assertThat(result, contains(ClientFields.StringFields.CALLSIGN));
    }

    @Test
    @DataProvider({
        // no _OBS suffix and unmodified => OK
        ", , true",
        "ABC123, ABC123, true",
        "OBS0815, OBS0815, true",
        "A_OBSI, A_OBSI, true", //

        // _OBS suffix but unmodified => NOK
        "_OBS, _OBS, false",
        "AB_OBS, AB_OBS, false", //

        // _OBS suffix and aliased => OK
        "_OBS, XX_OBS, true",
        "AB_OBS, XX_OBS, true",
        "XX_OBS, XX_OBS, true", //

        // _OBS suffix but aliased wrongly => NOK
        "_OBS, XY_OBS, false",
        "AB_OBS, XY_OBS, false",
        "XX_OBS, XY_OBS, false", //

        // no _OBS suffix but modified => NOK
        "ABC123, XX_OBS, false", //
        "A_OBSI, XX_OBS, false"
    })
    public void testVerify_handledField_returnsExpectedResult(String original, String filtered, boolean expectedResult) {
        // Arrange (nothing to do)

        // Act
        boolean result = filter.verifyAffectedField(ClientFields.StringFields.CALLSIGN, original, filtered);

        // Assert
        assertThat(result, is(expectedResult));
    }

    @DataProvider
    public static Object[][] dataProviderUnhandledClientStringFields() {
        return allEnumValuesExcept(ClientFields.StringFields.class, ClientFields.StringFields.CALLSIGN);
    }

    @Test
    @UseDataProvider("dataProviderUnhandledClientStringFields")
    public void testVerifyAffectedField_unhandledField_throwsException(ClientFields.FieldAccess<String> fieldAccess) {
        // Arrange
        thrown.expect(Exception.class);

        // Act
        filter.verifyAffectedField(fieldAccess, "AB_OBS", "XX_OBS");

        // Assert (nothing to do)
    }

    @Test
    @DataProvider({
        // full online examples (pilots)
        "UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:, UNK123:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:", // no suffix => remain unmodified
        "JD_OBS:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:, XX_OBS:123456:John Doe ABCD:PILOT::52.12345:13.54321:12000:520:B738/L:420:EDDT:24000:EDDF:MYSERVER:100:1:2000:::2:I:845:0:0:50:2:30:EDDK:SOME COMMENT /v/:THE ROUTE:0:0:0:0:::20190101080000:90:29.772:1008:", // suffix => substitute

        // full online examples (ATC)
        "ABCD_GND:123456:John Doe:ATC:123.450:52.12345:13.54321:0:::0::::MYSERVER:100:2::3:300::::::::::::::::$ SOME.SERVER.NET/WHATEVER:20190311090000:20190311083000::::, ABCD_GND:123456:John Doe:ATC:123.450:52.12345:13.54321:0:::0::::MYSERVER:100:2::3:300::::::::::::::::$ SOME.SERVER.NET/WHATEVER:20190311090000:20190311083000::::", // no suffix => remain unmodified
        "JD_OBS:123456:John Doe:ATC:123.450:52.12345:13.54321:0:::0::::MYSERVER:100:2::3:300::::::::::::::::$ SOME.SERVER.NET/WHATEVER:20190311090000:20190311083000::::, XX_OBS:123456:John Doe:ATC:123.450:52.12345:13.54321:0:::0::::MYSERVER:100:2::3:300::::::::::::::::$ SOME.SERVER.NET/WHATEVER:20190311090000:20190311083000::::", // suffix => substitute

        // full prefiled example (pilots)
        "ABC987A:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::, ABC987A:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::", // no suffix => remain unmodified
        "JD_OBS:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::, XX_OBS:54321::::::::H/B772/L:500:ESSA:34000:LIRF:::::::0:I:1030:1042:3:0:5:20:EDDM:+VFPS+/V/PBN/A1B1C1D1L1O1 DOF/190311 REG/A12345 RMK/TCAS:ANOSA ROUTE FOR TSTNG:0:0:0:0:::::::", // suffix => substitute

        // minimal truncated examples
        ":, :",
        "A:, A:",
        "_OBS:, XX_OBS:",
        "JD_OBS:, XX_OBS:", //

        // do not replace anywhere but in callsign
        "ABC:123:JD_OBS:, ABC:123:JD_OBS:",
        "JD_OBS:123:JD_OBS:, XX_OBS:123:JD_OBS:"
    })
    public void testApply_handledFormat_substitutesCallsignOnExpectedSuffix(String input, String expectedOutput) {
        // Arrange (nothing to do)

        // Act
        String output = filter.apply(input);

        // Assert
        assertThat(output, is(equalTo(expectedOutput)));
    }

    @Test
    @DataProvider({
        "",
        "ABC",
        "JD_OBS" // without a field separator this is expected not to be processed
    })
    public void testApply_unhandledFormat_returnsInputUnmodified(String input) {
        // Arrange (nothing to do)

        // Act
        String output = filter.apply(input);

        // Assert
        assertThat(output, is(equalTo(input)));
    }
}
