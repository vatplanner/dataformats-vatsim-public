package org.vatplanner.dataformats.vatsimpublic.entities.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.APPROACH_DEPARTURE;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.CENTER;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.DELIVERY;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.FSS;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.GROUND;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.OBSERVER;
import static org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType.TOWER;

import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class FacilityTypeTest {

    public static Stream<Arguments> dataProviderIdAndEnum() {
        return Stream.of(
            Arguments.of(0, OBSERVER),
            Arguments.of(1, FSS),
            Arguments.of(2, DELIVERY),
            Arguments.of(3, GROUND),
            Arguments.of(4, TOWER),
            Arguments.of(5, APPROACH_DEPARTURE),
            Arguments.of(6, CENTER)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderIdAndEnum")
    void testResolveStatusFileId_knownId_expectedEnum(int id, FacilityType expectedFacilityType) {
        // Arrange (nothing to do)

        // Act
        FacilityType result = FacilityType.resolveStatusFileId(id);

        // Assert
        assertThat(result).isSameAs(expectedFacilityType);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 7, 100})
    void testResolveStatusFileId_unknownId_throwsIllegalArgumentException(int unknownId) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> FacilityType.resolveStatusFileId(unknownId);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> dataProviderShortNameAndEnum() {
        return Stream.of(
            Arguments.of("OBS", OBSERVER),
            Arguments.of("FSS", FSS),
            Arguments.of("DEL", DELIVERY),
            Arguments.of("GND", GROUND),
            Arguments.of("TWR", TOWER),
            Arguments.of("APP", APPROACH_DEPARTURE),
            Arguments.of("CTR", CENTER)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderShortNameAndEnum")
    void testResolveShortName_knownShortName_expectedEnum(String shortName, FacilityType expectedFacilityType) {
        // Arrange (nothing to do)

        // Act
        FacilityType result = FacilityType.resolveShortName(shortName);

        // Assert
        assertThat(result).isSameAs(expectedFacilityType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ctr", "GN"})
    void testResolveShortName_unknownShortName_returnsNull(String unknownShortName) {
        // Arrange (nothing to do)

        // Act
        FacilityType result = FacilityType.resolveShortName(unknownShortName);

        // Assert
        assertThat(result).isNull();
    }
}
