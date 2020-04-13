package org.vatplanner.dataformats.vatsimpublic.testutils;

public class ByteConversion {

    /**
     * Converts the given sequence of integers to be specified in binary form to
     * the binary read equivalent byte sequence. See {@link #rawByte(int)} for a
     * more detailed explanation.
     *
     * @param in input sequence (binary sequence interpreted as 16-bit integer)
     * @return signed 2-complement byte values; equivalent to what Java reads
     * from streams
     * @see #rawByte(int)
     */
    public static final byte[] rawBytes(int... in) {
        byte[] out = new byte[in.length];

        for (int i = 0; i < in.length; i++) {
            out[i] = rawByte(in[i]);
        }

        return out;
    }

    /**
     * Converts the given integer to be specified in binary form to the binary
     * read equivalent byte. Needed because Java uses byte[] for stream contents
     * but specifying binary sequences in Java results in 16-bit integers. If a
     * human-readable binary sequence is wanted as file content bytes they need
     * to be reinterpreted as 2-complement 8-bit integers.
     *
     * @param in input value (binary sequence interpreted as 16-bit integer)
     * @return signed 2-complement byte value; equivalent to what Java reads
     * from streams
     */
    public static final byte rawByte(int in) {
        if (in < 0 || in > 255) {
            throw new IllegalArgumentException("out of range: " + Integer.toString(in));
        }

        if (in <= 127) {
            return (byte) in;
        }

        return (byte) (-~in - 1);
    }
}
