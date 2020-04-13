package org.vatplanner.dataformats.vatsimpublic.utils;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.vatplanner.dataformats.vatsimpublic.testutils.ByteConversion.rawBytes;

@RunWith(DataProviderRunner.class)
public class CharacterSetDetectorTest {

    @Test
    public void testIsCompatibleWithUTF8_empty_returnsTrue() {
        // Arrange
        CharacterSetDetector detector = new CharacterSetDetector(new byte[0]);

        // Act
        boolean result = detector.isCompatibleWithUTF8();

        // Assert
        assertThat(result, is(true));
    }

    @DataProvider
    public static Object[][] dataProviderBrokenUTF8Sequences() {
        return new Object[][]{
            // invalid high bytes
            {rawBytes(0b10000000)}, // continuation without start
            {rawBytes(0b11111000)}, // 5 bytes...?
            {rawBytes(0b11111100)}, // 6 bytes...?
            {rawBytes(0b11111110)}, // 7 bytes...?
            {rawBytes(0b11111111)}, // 8 bytes...?

            // 2-byte sequence
            {rawBytes(0b11000000)}, // incomplete
            {rawBytes(0b11000000, 0b00000000)}, // unexpected second byte
            {rawBytes(0b11000000, 0b01000000)}, // unexpected second byte
            {rawBytes(0b11000000, 0b11000000)}, // unexpected second byte

            // 3-byte sequence
            {rawBytes(0b11100000)}, // incomplete
            {rawBytes(0b11100000, 0b10000000)}, // incomplete
            {rawBytes(0b11100000, 0b00000000)}, // unexpected second byte
            {rawBytes(0b11100000, 0b01000000)}, // unexpected second byte
            {rawBytes(0b11100000, 0b11000000)}, // unexpected second byte
            {rawBytes(0b11100000, 0b10000000, 0b00000000)}, // unexpected third byte
            {rawBytes(0b11100000, 0b10000000, 0b01000000)}, // unexpected third byte
            {rawBytes(0b11100000, 0b10000000, 0b11000000)}, // unexpected third byte

            // 4-byte sequence
            {rawBytes(0b11110000)}, // incomplete
            {rawBytes(0b11110000, 0b10000000)}, // incomplete
            {rawBytes(0b11110000, 0b10000000, 0b10000000)}, // incomplete
            {rawBytes(0b11110000, 0b00000000)}, // unexpected second byte
            {rawBytes(0b11110000, 0b01000000)}, // unexpected second byte
            {rawBytes(0b11110000, 0b11000000)}, // unexpected second byte
            {rawBytes(0b11110000, 0b10000000, 0b00000000)}, // unexpected third byte
            {rawBytes(0b11110000, 0b10000000, 0b01000000)}, // unexpected third byte
            {rawBytes(0b11110000, 0b10000000, 0b11000000)}, // unexpected third byte
            {rawBytes(0b11110000, 0b10000000, 0b10000000, 0b00000000)}, // unexpected fourth byte
            {rawBytes(0b11110000, 0b10000000, 0b10000000, 0b01000000)}, // unexpected fourth byte
            {rawBytes(0b11110000, 0b10000000, 0b10000000, 0b11000000)}, // unexpected fourth byte
        };
    }

    @Test
    @UseDataProvider("dataProviderBrokenUTF8Sequences")
    public void testIsCompatibleWithUTF8_brokenUTF8Sequence_returnsFalse(byte[] brokenSequence) {
        // Arrange
        CharacterSetDetector detector = new CharacterSetDetector(brokenSequence);

        // Act
        boolean result = detector.isCompatibleWithUTF8();

        // Assert
        assertThat(result, is(false));
    }

    @DataProvider
    public static Object[][] dataProviderValidUTF8Sequences() {
        return new Object[][]{
            // 7-bit ASCII
            {rawBytes(0b00000000)},
            {rawBytes(0b01111111)}, //

            // 2-byte sequence
            {rawBytes(0b11000000, 0b10000000)},
            {rawBytes(0b11000000, 0b10111111)}, //

            // 3-byte sequence
            {rawBytes(0b11100000, 0b10000000, 0b10000000)},
            {rawBytes(0b11100000, 0b10000000, 0b10111111)},
            {rawBytes(0b11100000, 0b10111111, 0b10000000)},
            {rawBytes(0b11100000, 0b10111111, 0b10111111)}, //

            // 4-byte sequence
            {rawBytes(0b11110000, 0b10000000, 0b10000000, 0b10000000)},
            {rawBytes(0b11110000, 0b10000000, 0b10000000, 0b10111111)},
            {rawBytes(0b11110000, 0b10000000, 0b10111111, 0b10000000)},
            {rawBytes(0b11110000, 0b10000000, 0b10111111, 0b10111111)},
            {rawBytes(0b11110000, 0b10000000, 0b10000000, 0b10000000)},
            {rawBytes(0b11110000, 0b10000000, 0b10000000, 0b10111111)},
            {rawBytes(0b11110000, 0b10000000, 0b10111111, 0b10000000)},
            {rawBytes(0b11110000, 0b10000000, 0b10111111, 0b10111111)},
            {rawBytes(0b11110000, 0b10111111, 0b10000000, 0b10000000)},
            {rawBytes(0b11110000, 0b10111111, 0b10000000, 0b10111111)},
            {rawBytes(0b11110000, 0b10111111, 0b10111111, 0b10000000)},
            {rawBytes(0b11110000, 0b10111111, 0b10111111, 0b10111111)},
            {rawBytes(0b11110000, 0b10111111, 0b10000000, 0b10000000)},
            {rawBytes(0b11110000, 0b10111111, 0b10000000, 0b10111111)},
            {rawBytes(0b11110000, 0b10111111, 0b10111111, 0b10000000)},
            {rawBytes(0b11110000, 0b10111111, 0b10111111, 0b10111111)}, //
        };
    }

    @Test
    @UseDataProvider("dataProviderValidUTF8Sequences")
    public void testIsCompatibleWithUTF8_validUTF8Sequence_returnsTrue(byte[] validSequence) {
        // Arrange
        CharacterSetDetector detector = new CharacterSetDetector(validSequence);

        // Act
        boolean result = detector.isCompatibleWithUTF8();

        // Assert
        assertThat(result, is(true));
    }
}
