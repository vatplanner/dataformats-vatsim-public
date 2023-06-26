package org.vatplanner.dataformats.vatsimpublic.utils;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility methods for common string operations.
 */
public class StringUtils {

    private static final Pattern PATTERN_NUMERIC = Pattern.compile("^[0-9]+$");

    private static final Pattern SPLIT_LINES_PATTERN = Pattern.compile("\r\n|[\r\n]");

    private StringUtils() {
        // utility class; hide constructor
    }

    /**
     * Returns null if the input string is empty, otherwise the input will be
     * returned.
     *
     * @param s string to check
     * @return null if input is empty, otherwise input string is returned
     */
    public static String nullIfEmpty(String s) {
        return s.isEmpty() ? null : s;
    }

    /**
     * Returns null if the input string is numeric, otherwise the input will be
     * returned.
     *
     * @param s string to check
     * @return null if input is numeric, otherwise input string is returned
     */
    public static String nullIfNumeric(String s) {
        if (PATTERN_NUMERIC.matcher(s).matches()) {
            return null;
        }

        return s;
    }

    /**
     * Returns the given content with all lines prefixed. Maintains line break
     * sequences and trailing line breaks.
     *
     * @param prefix  prefix to append to each line
     * @param content content to prefix lines for
     * @return content with prefixed lines
     */
    public static String prefixLines(String prefix, String content) {
        if (prefix.isEmpty() || content.isEmpty()) {
            return content;
        }

        StringBuilder sb = new StringBuilder();
        char[] chars = content.toCharArray();
        boolean isStartOfLine = true;
        int i = 0;
        for (i = 0; i < chars.length - 1; i++) {
            char thisChar = chars[i];
            char nextChar = chars[i + 1];

            boolean isEndOfLine = false;

            if (isStartOfLine) {
                sb.append(prefix);
                isStartOfLine = false;
            }

            if (thisChar == '\n') {
                isEndOfLine = true;
            } else if (thisChar == '\r') {
                if (nextChar == '\n') {
                    // CR LF sequence
                    sb.append(thisChar);
                    sb.append(nextChar);

                    // skip ahead
                    i++;
                    isStartOfLine = true;
                    continue;
                }

                isEndOfLine = true;
            }

            sb.append(thisChar);

            isStartOfLine = isEndOfLine;
        }

        char lastChar = chars[chars.length - 1];
        boolean endsWithCrLf = (chars.length > 1) && (chars[chars.length - 2] == '\r') && (lastChar == '\n');
        if (!endsWithCrLf) {
            if (isStartOfLine) {
                sb.append(prefix);
            }

            sb.append(lastChar);
        }

        return sb.toString();
    }

    /**
     * Returns the given content with all line end sequences unified to a single
     * sequence. Works for CR, LF and CR LF sequences.
     *
     * @param lineEnd wanted line end sequence
     * @param content content with varying line end sequences
     * @return content with unified line end sequences
     */
    public static String unifyLineEnds(String lineEnd, String content) {
        if (content.isEmpty()) {
            return content;
        }

        String out = SPLIT_LINES_PATTERN
            .splitAsStream(content)
            .collect(Collectors.joining(lineEnd));

        // trailing line end is omitted during split, re-add
        if (endsWithLineBreak(content)) {
            out = out + lineEnd;
        }

        return out;
    }

    /**
     * Returns true if the given string ends with a line break, false if not.
     *
     * @param s string to check for line break at end
     * @return true if the given string ends with a line break, false if not
     */
    public static boolean endsWithLineBreak(String s) {
        if (s.isEmpty()) {
            return false;
        }

        char lastChar = s.charAt(s.length() - 1);
        return (lastChar == '\r') || (lastChar == '\n');
    }

    // TODO: unit tests
}
