package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.MilitaryRating.M0;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.MilitaryRating.M1;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.MilitaryRating.M2;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.MilitaryRating.M3;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.MilitaryRating.M4;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MilitaryRatingTest {

    static Stream<Arguments> dataProviderIdAndShortName() {
        return Stream.of(
            Arguments.of("M0", M0),
            Arguments.of("M1", M1),
            Arguments.of("M2", M2),
            Arguments.of("M3", M3),
            Arguments.of("M4", M4)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderIdAndShortName")
    void testResolveShortName_knownShortName_expectedEnum(String shortName, MilitaryRating expectedRating) {
        // Arrange (nothing to do)

        // Act
        MilitaryRating result = MilitaryRating.resolveShortName(shortName);

        // Assert
        assertThat(result).isSameAs(expectedRating);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "M5", "m1"})
    void testResolveShortName_unknownShortName_throwsIllegalArgumentException(String unknownShortName) {
        // Arrange (nothing to do)

        // Act
        MilitaryRating result = MilitaryRating.resolveShortName(unknownShortName);

        // Assert
        assertThat(result).isNull();
    }
}
