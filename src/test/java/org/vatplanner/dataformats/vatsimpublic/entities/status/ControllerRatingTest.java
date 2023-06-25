package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.ADM;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.C1;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.C2;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.C3;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.I;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.I2;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.I3;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.INAC;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.OBS;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.S1;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.S2;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.S3;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.SUP;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating.SUS;

import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ControllerRatingTest {

    public static Stream<Arguments> dataProviderIdAndEnum() {
        return Stream.of(
            Arguments.of(-1, INAC),
            Arguments.of(0, SUS),
            Arguments.of(1, OBS),
            Arguments.of(2, S1),
            Arguments.of(3, S2),
            Arguments.of(4, S3),
            Arguments.of(5, C1),
            Arguments.of(6, C2),
            Arguments.of(7, C3),
            Arguments.of(8, I),
            Arguments.of(9, I2),
            Arguments.of(10, I3),
            Arguments.of(11, SUP),
            Arguments.of(12, ADM)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderIdAndEnum")
    void testResolveStatusFileId_knownId_expectedEnum(int id, ControllerRating expectedRating) {
        // Arrange (nothing to do)

        // Act
        ControllerRating result = ControllerRating.resolveStatusFileId(id);

        // Assert
        assertThat(result).isEqualTo(expectedRating);
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, 13, 100})
    void testResolveStatusFileId_unknownId_throwsIllegalArgumentException(int unknownId) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> ControllerRating.resolveStatusFileId(unknownId);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> dataProviderShortNameAndEnum() {
        return Stream.of(
            Arguments.of("INAC", INAC),
            Arguments.of("SUS", SUS),
            Arguments.of("OBS", OBS),
            Arguments.of("S1", S1),
            Arguments.of("S2", S2),
            Arguments.of("S3", S3),
            Arguments.of("C1", C1),
            Arguments.of("C2", C2),
            Arguments.of("C3", C3),
            Arguments.of("I1", I),
            Arguments.of("I2", I2),
            Arguments.of("I3", I3),
            Arguments.of("SUP", SUP),
            Arguments.of("ADM", ADM)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderShortNameAndEnum")
    void testResolveShortName_knownShortName_expectedEnum(String shortName, ControllerRating expectedRating) {
        // Arrange (nothing to do)

        // Act
        ControllerRating result = ControllerRating.resolveShortName(shortName);

        // Assert
        assertThat(result).isSameAs(expectedRating);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "INA", "I"})
    void testResolveShortName_unknownShortName_throwsIllegalArgumentException(String unknownShortName) {
        // Arrange (nothing to do)

        // Act
        ControllerRating result = ControllerRating.resolveShortName(unknownShortName);

        // Assert
        assertThat(result).isNull();
    }
}
