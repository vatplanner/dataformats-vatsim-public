package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.ATPL;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.CMEL;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.IR;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.PPL;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating.UNRATED;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PilotRatingTest {

    static Stream<Arguments> dataProviderIdAndShortName() {
        return Stream.of(
            Arguments.of("NEW", UNRATED),
            Arguments.of("PPL", PPL),
            Arguments.of("IR", IR),
            Arguments.of("CMEL", CMEL),
            Arguments.of("ATPL", ATPL)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderIdAndShortName")
    void testResolveShortName_knownShortName_expectedEnum(String shortName, PilotRating expectedRating) {
        // Arrange (nothing to do)

        // Act
        PilotRating result = PilotRating.resolveShortName(shortName);

        // Assert
        assertThat(result).isSameAs(expectedRating);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "P1", "ATP"})
    void testResolveShortName_unknownShortName_throwsIllegalArgumentException(String unknownShortName) {
        // Arrange (nothing to do)

        // Act
        PilotRating result = PilotRating.resolveShortName(unknownShortName);

        // Assert
        assertThat(result).isNull();
    }
}
