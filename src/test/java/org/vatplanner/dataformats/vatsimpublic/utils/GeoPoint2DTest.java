package org.vatplanner.dataformats.vatsimpublic.utils;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class GeoPoint2DTest {
    private static final Offset<Double> ACCEPTED_OFFSET = Offset.offset(0.00000001);
    private static final Duration ACCEPTED_COMPUTATION_TIME = Duration.ofMillis(50);

    @ParameterizedTest
    @ValueSource(doubles = {91.0, 90.0001, -90.0001, -91.0})
    void testConstructor_latitudeOutOfRange_throwsOutOfRange(double latitude) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new GeoPoint2D(latitude, 0.0);

        // Assert
        assertThatThrownBy(action).isInstanceOf(OutOfRange.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {90.0000, 0.0, -90.0000})
    void testConstructor_validLatitude_retainsValue(double expectedLatitude) {
        // Arrange (nothing to do)

        // Act
        GeoPoint2D result = new GeoPoint2D(expectedLatitude, 0.0);

        // Assert
        assertThat(result).extracting(GeoPoint2D::getLatitude)
                          .isEqualTo(expectedLatitude);
    }

    @ParameterizedTest
    @ValueSource(doubles = {181.0, 180.0001, -180.0001, -181.0})
    void testConstructor_longitudeOutOfRange_throwsOutOfRange(double longitude) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> new GeoPoint2D(0.0, longitude);

        // Assert
        assertThatThrownBy(action).isInstanceOf(OutOfRange.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {180.0000, 0.0, -180.0000})
    void testConstructor_validLongitude_retainsValue(double expecteLongitude) {
        // Arrange (nothing to do)

        // Act
        GeoPoint2D result = new GeoPoint2D(0.0, expecteLongitude);

        // Assert
        assertThat(result).extracting(GeoPoint2D::getLongitude)
                          .isEqualTo(expecteLongitude);
    }

    @Test
    void testEquals_null_returnsFalse() {
        // Arrange
        GeoPoint2D point = new GeoPoint2D(0.0, 0.0);

        // Act
        boolean result = point.equals(null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        GeoPoint2D point = new GeoPoint2D(0.0, 0.0);

        // Act
        boolean result = point.equals(new Object());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testEquals_sameInstance_returnsTrue() {
        // Arrange
        GeoPoint2D point = new GeoPoint2D(12.3, 123.4);

        // Act
        boolean result = point.equals(point);

        // Assert
        assertThat(result).isTrue();
    }

    static Stream<Arguments> dataProviderEqualCoordinates() {
        return Stream.of(
            Arguments.of(new GeoPoint2D(-90.0, -180.0), new GeoPoint2D(-90.0, -180.0)),
            Arguments.of(new GeoPoint2D(90.0, 180.0), new GeoPoint2D(90.0, 180.0)),
            Arguments.of(new GeoPoint2D(0.1, 0.1), new GeoPoint2D(0.1, 0.1)) //
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderEqualCoordinates")
    void testEquals_equalCoordinates_returnsTrue(GeoPoint2D pointA, GeoPoint2D pointB) {
        // Arrange (nothing to do)

        // Act
        boolean result = pointA.equals(pointB);

        // Assert
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("dataProviderEqualCoordinates")
    void testHashCode_equalCoordinates_equalHashCode(GeoPoint2D pointA, GeoPoint2D pointB) {
        // Arrange (nothing to do)

        // Act (nothing to do)

        // Assert
        assertThat(pointA).hasSameHashCodeAs(pointB);
    }

    static Stream<Arguments> dataProviderDifferentCoordinates() {
        return Stream.of(
            // full deflection
            Arguments.of(new GeoPoint2D(-90.0, -180.0), new GeoPoint2D(90.0, 180.0)),

            // moving
            Arguments.of(new GeoPoint2D(0.1, 0.0), new GeoPoint2D(0.0, 0.0)),
            Arguments.of(new GeoPoint2D(0.0, 0.1), new GeoPoint2D(0.0, 0.0)),

            // crossed
            Arguments.of(new GeoPoint2D(0.1, 0.0), new GeoPoint2D(0.0, 0.1)) //
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderDifferentCoordinates")
    void testEquals_differentCoordinatesCheckingAEqualsB_returnsFalse(GeoPoint2D pointA, GeoPoint2D pointB) {
        // Arrange (nothing to do)

        // Act
        boolean result = pointA.equals(pointB);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("dataProviderDifferentCoordinates")
    void testEquals_differentCoordinatesCheckingBEqualsA_returnsFalse(GeoPoint2D pointA, GeoPoint2D pointB) {
        // Arrange (nothing to do)

        // Act
        boolean result = pointB.equals(pointA);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        //     input      |     output
        " 42.0,    0.0,     42.0,    0.0",
        " 42.0,  179.9999,  42.0,  179.9999",
        " 42.0, -179.9999,  42.0, -179.9999",
        " 42.0,  180.0001,  42.0, -179.9999",
        " 42.0, -180.0001,  42.0,  179.9999",
        "-12.3,  360.0,    -12.3,    0.0",
        "-12.3,  720.0,    -12.3,    0.0",
        "-12.3,  480.0,    -12.3,  120.0",
        " 90.0,  180.0001,  90.0, -179.9999",
        "-90.0, -180.0001, -90.0,  179.9999",
        "  0.0,    3.6e7,    0.0,    0.0",
    })
    void testNormalize_validLatitude_returnsExpectedResult(double latitude, double longitude, double expectedLatitude, double expectedLongitude) {
        // Arrange (nothing to do)

        // Act
        GeoPoint2D result = GeoPoint2D.normalize(latitude, longitude);

        // Assert
        assertAll(
            () -> assertThat(result).describedAs("latitude")
                                    .extracting(GeoPoint2D::getLatitude, as(DOUBLE))
                                    .isEqualTo(expectedLatitude),

            () -> assertThat(result).describedAs("longitude")
                                    .extracting(GeoPoint2D::getLongitude, as(DOUBLE))
                                    .isCloseTo(expectedLongitude, ACCEPTED_OFFSET)
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {3.6e8, -3.6e8, 3.6e11, -3.6e11})
    void testNormalize_hugeLongitude_throwsOutOfRange(double longitude) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> GeoPoint2D.normalize(0.0, longitude);

        // Assert
        assertThatThrownBy(action).isInstanceOf(OutOfRange.class);
    }

    @ParameterizedTest
    @CsvSource({
        " 90.0001,    0.0",
        "-90.0001,    0.0",
        " 90.0001,  270.0",
        "-90.0001, -270.0",
    })
    void testNormalize_excessiveLatitude_throwsOutOfRange(double latitude, double longitude) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> GeoPoint2D.normalize(latitude, longitude);

        // Assert
        assertThatThrownBy(action).isInstanceOf(OutOfRange.class);
    }

    @ParameterizedTest
    @CsvSource({
        //     input      |     output
        "  90.01,   0.0,      89.99, 180.0", // just over the North Pole
        "  90.5,   90.0,      89.5,  -90.0", // turn longitude
        " 179.0,    0.0,       1.0,  180.0", // just above the equator on other side of planet
        " 180.0,    0.0,       0.0,  180.0", // equator on other site
        " 180.0,   90.0,       0.0,  -90.0", // turn the planet by 90 degrees longitude
        "-180.0,   90.0,       0.0,  -90.0", // negative latitude should be the same
        " 180.5,    0.0,      -0.5,  180.0", // continue original line up, actually down
        " 269.5,    0.0,     -89.5,  180.0", // just before coming back over the South Pole
        " 270.0,    0.0,     -90.0,    0.0", // on the South Pole, back on original longitude
        " 270.5,    0.0,     -89.5,    0.0", // moving away from the South Pole, towards the equator on this side again
        " 359.5,    0.0,      -0.5,    0.0", // almost a full turn
        " -90.01,   0.0,     -89.99, 180.0", // ... and now the other way around: just past the South Pole
        " -90.5,   90.0,     -89.5,  -90.0", // turn planet by longitude
        "-179.0,   0.0,       -1.0,  180.0", // just below the equator on the other side
        "-181.0,   0.0,        1.0,  180.0", // and now above
        "-269.5,   0.0,       89.5,  180.0", // getting close to the North Pole
        "-270.0,   0.0,       90.0,    0.0", // exactly on the North Pole, longitude is restored
        "-270.5,   0.0,       89.5,    0.0", // moving away on this side again
        "-359.5,   0.0,        0.5,    0.0", // turn almost completed
        " 360.0,   90.0,       0.0,   90.0", // full turn
        "-360.0,   90.0,       0.0,   90.0",
        " 120.0,   90.0,      60.0,  -90.0",
        "-120.0,   90.0,     -60.0,  -90.0",
        " 390.0,   90.0,      30.0,   90.0",
        "-390.0,   90.0,     -30.0,   90.0",
        " 690.0,   90.0,     -30.0,   90.0",
        "-690.0,   90.0,      30.0,   90.0",
        " 720.0,   90.0,       0.0,   90.0",
        "-720.0,   90.0,       0.0,   90.0",

        // copied from normalize test:
        " 42.0,    0.0,     42.0,    0.0",
        " 42.0,  179.9999,  42.0,  179.9999",
        " 42.0, -179.9999,  42.0, -179.9999",
        " 42.0,  180.0001,  42.0, -179.9999",
        " 42.0, -180.0001,  42.0,  179.9999",
        "-12.3,  360.0,    -12.3,    0.0",
        "-12.3,  720.0,    -12.3,    0.0",
        "-12.3,  480.0,    -12.3,  120.0",
        " 90.0,  180.0001,  90.0, -179.9999",
        "-90.0, -180.0001, -90.0,  179.9999",
        "  0.0,    3.6e7,    0.0,    0.0",
    })
    void testWrap_always_returnsExpectedResult(double latitude, double longitude, double expectedLatitude, double expectedLongitude) {
        // Arrange (nothing to do)

        // Act
        GeoPoint2D result = GeoPoint2D.wrap(latitude, longitude);

        // Assert
        assertAll(
            () -> assertThat(result).describedAs("latitude")
                                    .extracting(GeoPoint2D::getLatitude, as(DOUBLE))
                                    .isCloseTo(expectedLatitude, ACCEPTED_OFFSET),

            () -> assertThat(result).describedAs("longitude")
                                    .extracting(GeoPoint2D::getLongitude, as(DOUBLE))
                                    .isCloseTo(expectedLongitude, ACCEPTED_OFFSET)
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {3.6e8, -3.6e8, 3.6e11, -3.6e11})
    void testWrap_hugeLongitudeWithNormalLatitude_throwsOutOfRange(double longitude) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> GeoPoint2D.wrap(0.0, longitude);

        // Assert
        assertThatThrownBy(action).isInstanceOf(OutOfRange.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {3.6e8, -3.6e8, 3.6e11, -3.6e11})
    void testWrap_hugeLongitudeWithExcessiveLatitude_throwsOutOfRange(double longitude) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> GeoPoint2D.wrap(270.0, longitude);

        // Assert
        assertThatThrownBy(action).isInstanceOf(OutOfRange.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {3.6e8, -3.6e8, 3.6e11, -3.6e11})
    void testWrap_hugeLatitude_throwsOutOfRange(double latitude) {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> GeoPoint2D.wrap(latitude, 0.0);

        // Assert
        assertThatThrownBy(action).isInstanceOf(OutOfRange.class);
    }
}
