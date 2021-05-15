package org.vatplanner.dataformats.vatsimpublic.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class StringUtilsTest {
    @Test
    public void testPrefixLines_emptyContent_returnsEmpty() {
        // Arrange (nothing to do)

        // Act
        String result = StringUtils.prefixLines("ABC", "");

        // Assert
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { //
        "", //
        " ", //
        "a\nb\r\nc\rd", //
        "this\nis\na\ntest", //
        "trailing line end\n" //
    })
    public void testPrefixLines_emptyPrefix_returnsOriginalContent(String expectedContent) {
        // Arrange (nothing to do)

        // Act
        String result = StringUtils.prefixLines("", expectedContent);

        // Assert
        assertThat(result).isEqualTo(expectedContent);
    }

    public static Stream<Arguments> dataProviderPrefixAndContentWithExpectedResult() {
        return Stream.of( //
            Arguments.of(
                "; ",
                "This is\n"
                    + "a simple\n"
                    + "test",
                "; This is\n"
                    + "; a simple\n"
                    + "; test" //
            ), //

            // trailing whitespace
            Arguments.of(
                "; ",
                "trailing space \n"
                    + "trailing line end\n",
                "; trailing space \n"
                    + "; trailing line end\n" //
            ), //

            // maintain all whitespace
            Arguments.of(
                "  ",
                "\n"
                    + " \n"
                    + "  ",
                "  \n"
                    + "   \n"
                    + "    " //
            ), //

            // process and maintain different line end sequences
            Arguments.of(
                "-",
                "CRLF\r\n"
                    + "LF\n"
                    + "CR\r"
                    + "none",
                "-CRLF\r\n"
                    + "-LF\n"
                    + "-CR\r"
                    + "-none"//
            ), //

            // consecutive whitespace
            Arguments.of(
                "-",
                "\r\n"
                    + "\n",
                "-\r\n"
                    + "-\n" //
            ),
            Arguments.of(
                "-",
                "\n"
                    + "\r\n",
                "-\n"
                    + "-\r\n" //
            ),
            Arguments.of(
                "-",
                "\r"
                    + "\r\n",
                "-\r"
                    + "-\r\n" //
            ),
            Arguments.of(
                "-",
                "\r\n"
                    + "\r",
                "-\r\n"
                    + "-\r" //
            ),
            Arguments.of(
                "-",
                "\n"
                    + "\n"
                    + "\n"
                    + "\r"
                    + "\r"
                    + "\r"
                    + "\r\n"
                    + "\r\n"
                    + "\r\n"
                    + "\n"
                    + "\n", //
                "-\n"
                    + "-\n"
                    + "-\n"
                    + "-\r"
                    + "-\r"
                    + "-\r"
                    + "-\r\n"
                    + "-\r\n"
                    + "-\r\n"
                    + "-\n"
                    + "-\n"//
            ) //
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderPrefixAndContentWithExpectedResult")
    public void testPrefixLines_always_returnsExpectedResult(String prefix, String content, String expectedResult) {
        // Arrange (nothing to do)

        // Act
        String result = StringUtils.prefixLines(prefix, content);

        // Assert
        assertThat(result).inHexadecimal().isEqualTo(expectedResult);
    }

    public static Stream<Arguments> dataProviderLineEndAndContentWithExpectedResult() {
        return Stream.of(
            // empty
            Arguments.of(
                "\n",
                "",
                "" //
            ),
            Arguments.of(
                "\r",
                "",
                "" //
            ),
            Arguments.of(
                "\r\n",
                "",
                "" //
            ),

            // no line end
            Arguments.of(
                "\n",
                "abc",
                "abc" //
            ),
            Arguments.of(
                "\r",
                "abc",
                "abc" //
            ),
            Arguments.of(
                "\r\n",
                "abc",
                "abc" //
            ),

            // trailing line ends
            Arguments.of(
                "\n",
                " \r\n",
                " \n" //
            ),
            Arguments.of(
                "\r",
                " \r\n",
                " \r" //
            ),
            Arguments.of(
                "\r\n",
                " \r\n",
                " \r\n" //
            ),

            // varying line ends
            Arguments.of(
                "\n",
                " CR LF \r\n"
                    + " CR \r"
                    + " LF \n"
                    + " trailing ",
                " CR LF \n"
                    + " CR \n"
                    + " LF \n"
                    + " trailing " //
            ),
            Arguments.of(
                "\r",
                " CR LF \r\n"
                    + " CR \r"
                    + " LF \n"
                    + " trailing ",
                " CR LF \r"
                    + " CR \r"
                    + " LF \r"
                    + " trailing " //
            ),
            Arguments.of(
                "\r\n",
                " CR LF \r\n"
                    + " CR \r"
                    + " LF \n"
                    + " trailing ",
                " CR LF \r\n"
                    + " CR \r\n"
                    + " LF \r\n"
                    + " trailing " //
            ) //
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderLineEndAndContentWithExpectedResult")
    public void testUnifyLineEnds_always_returnsExpectedResult(String lineEnd, String content, String expectedResult) {
        // Arrange (nothing to do)

        // Act
        String result = StringUtils.unifyLineEnds(lineEnd, content);

        // Assert
        assertThat(result).inHexadecimal().isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(strings = { //
        "\n", //
        "\r", //
        "\r\n", //
        "Something in front\n", //
        "Multiple\nLines\n" //
    })
    public void testEndsWithLineBreak_endingWithLineBreak_returnsTrue(String s) {
        // Arrange (nothing to do)

        // Act
        boolean result = StringUtils.endsWithLineBreak(s);

        // Assert
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { //
        "", //
        "no line break at all", //
        "\n ", //
        "\r ", //
        "\r\n ", //
        "Something in front\n ", //
        "Multiple\nLines\n " //
    })
    public void testEndsWithLineBreak_endingWithoutLineBreak_returnsFalse(String s) {
        // Arrange (nothing to do)

        // Act
        boolean result = StringUtils.endsWithLineBreak(s);

        // Assert
        assertThat(result).isFalse();
    }
}
