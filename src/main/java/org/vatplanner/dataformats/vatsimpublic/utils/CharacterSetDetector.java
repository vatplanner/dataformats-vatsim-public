package org.vatplanner.dataformats.vatsimpublic.utils;

/**
 * A basic attempt at character set detection. Instances are final and must not
 * be reused.
 */
public class CharacterSetDetector {

    private final boolean isCompatibleWithUTF8;

    // see https://en.wikipedia.org/wiki/UTF-8 for valid sequences
    private static final byte UTF8_START_4BYTE_HIGHEST = -9;   // 1111 0111
    private static final byte UTF8_START_3BYTE_HIGHEST = -17;  // 1110 1111
    private static final byte UTF8_START_2BYTE_HIGHEST = -33;  // 1101 1111
    private static final byte UTF8_CONTINUATION_HIGHEST = -65; // 1011 1111

    /**
     * Analyzes the given byte array.
     *
     * @param bytes byte array to analyze
     */
    public CharacterSetDetector(final byte[] bytes) {
        isCompatibleWithUTF8 = checkUTF8Compatibility(bytes);
    }

    private boolean checkUTF8Compatibility(final byte[] bytes) {
        int bytesRemainingInUTF8Sequence = 0;

        for (byte b : bytes) {
            if (bytesRemainingInUTF8Sequence > 0) {
                if (b > UTF8_CONTINUATION_HIGHEST) {
                    return false;
                }
                bytesRemainingInUTF8Sequence--;
            } else if (b < 0) {
                if (b > UTF8_START_4BYTE_HIGHEST) {
                    return false;
                } else if (b > UTF8_START_3BYTE_HIGHEST) {
                    bytesRemainingInUTF8Sequence = 3;
                } else if (b > UTF8_START_2BYTE_HIGHEST) {
                    bytesRemainingInUTF8Sequence = 2;
                } else if (b > UTF8_CONTINUATION_HIGHEST) {
                    bytesRemainingInUTF8Sequence = 1;
                } else {
                    return false;
                }
            }
        }

        return (bytesRemainingInUTF8Sequence == 0);
    }

    /**
     * Was the given byte array compatible with UTF-8?
     *
     * @return true if compatible with UTF-8, false if not
     */
    public boolean isCompatibleWithUTF8() {
        return isCompatibleWithUTF8;
    }
}
