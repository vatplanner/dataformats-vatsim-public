package org.vatplanner.dataformats.vatsimpublic.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.data.Offset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GeoMathTest {

    static Stream<Arguments> dataProviderAverage() {
        return Stream.of(
            // Tonga & Tuvalu example from
            // https://carto.com/blog/center-of-points/
            Arguments.of(
                new GeoPoint2D(-14.9333, -177.992),
                Arrays.asList(
                    new GeoPoint2D(-21.1333, -175.2),
                    new GeoPoint2D(-8.53333, 179.2167)
                ),
                Offset.offset(0.1) // >750km radius seems to cause high but relatively acceptable error
            ),

            // 3km centered around EDDT
            Arguments.of(
                new GeoPoint2D(52.558736, 13.290353),
                Arrays.asList(
                    new GeoPoint2D(52.585589, 13.289027), // north
                    new GeoPoint2D(52.559025, 13.334351), // east
                    new GeoPoint2D(52.558098, 13.246224), // west
                    new GeoPoint2D(52.531857, 13.291576) // south
                ),
                Offset.offset(0.0001)
            ),

            // just EDDT (single point)
            Arguments.of(
                new GeoPoint2D(52.558736, 13.290353),
                Arrays.asList(
                    new GeoPoint2D(52.558736, 13.290353)
                ),
                Offset.offset(0.000000001)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderAverage")
    void testAverage_always_returnsExpectedValue(GeoPoint2D expectedResult, Collection<GeoPoint2D> points, Offset<Double> acceptedError) {
        // Arrange (nothing to do)

        // Act
        GeoPoint2D result = GeoMath.average(points);

        // Assert
        assertCloseTo(result, expectedResult, acceptedError);
    }

    private void assertCloseTo(GeoPoint2D actual, GeoPoint2D expected, Offset<Double> acceptedError) {
        assertAll(
            () -> assertThat(actual).extracting(GeoPoint2D::getLatitude)
                                    .describedAs("latitude")
                                    .asInstanceOf(InstanceOfAssertFactories.DOUBLE)
                                    .isCloseTo(expected.getLatitude(), acceptedError),

            () -> assertThat(actual).extracting(GeoPoint2D::getLongitude)
                                    .describedAs("longitude")
                                    .asInstanceOf(InstanceOfAssertFactories.DOUBLE)
                                    .isCloseTo(expected.getLongitude(), acceptedError)
        );
    }
}
