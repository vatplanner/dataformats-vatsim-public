package org.vatplanner.dataformats.vatsimpublic.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ComparisonsTest {

    private final Object a = new Object();
    private final Object b = new Object();

    private Object attributeA;
    private Object attributeB;

    private Function<Object, Object> mockGetter;

    @BeforeEach
    public void setUp() {
        attributeA = mock(Object.class);
        attributeB = mock(Object.class);

        mockGetter = mock(Function.class);

        defineGetter(a, attributeA);
        defineGetter(b, attributeB);
    }

    @Test
    void testEqualsNullSafe_bothGettersNull_returnsTrue() {
        // Arrange
        defineGetter(a, null);
        defineGetter(b, null);

        // Act
        boolean result = Comparisons.equalsNullSafe(a, b, mockGetter);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testEqualsNullSafe_onlyGetterANull_returnsFalse() {
        // Arrange
        defineGetter(a, null);

        // Act
        boolean result = Comparisons.equalsNullSafe(a, b, mockGetter);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testEqualsNullSafe_onlyGetterBNull_returnsFalse() {
        // Arrange
        defineGetter(b, null);

        // Act
        boolean result = Comparisons.equalsNullSafe(a, b, mockGetter);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testEqualsNullSafe_notNull_returnsObjectEquality(boolean expectedResult) {
        // Arrange
        defineGetter(a, attributeA = new Object() {
            @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
            @Override
            public boolean equals(Object o) {
                assertThat(o).describedAs("attributeB must be provided to attributeA.equals")
                             .isSameAs(attributeB);
                return expectedResult;
            }
        });

        defineGetter(b, attributeB = new Object() {
            @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
            @Override
            public boolean equals(Object o) {
                assertThat(o).describedAs("attributeA must be provided to attributeB.equals")
                             .isSameAs(attributeA);
                return expectedResult;
            }
        });

        // Act
        boolean result = Comparisons.equalsNullSafe(a, b, mockGetter);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    private void defineGetter(Object object, Object attribute) {
        doReturn(attribute).when(mockGetter).apply(same(object));
    }
}
