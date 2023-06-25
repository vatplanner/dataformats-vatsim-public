package org.vatplanner.dataformats.vatsimpublic.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GeoPoint2DTest {
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
}
