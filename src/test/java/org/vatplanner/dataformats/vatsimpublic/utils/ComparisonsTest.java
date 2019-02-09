package org.vatplanner.dataformats.vatsimpublic.utils;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.util.function.Function;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(DataProviderRunner.class)
public class ComparisonsTest {

    private final Object a = new Object();
    private final Object b = new Object();

    private Object attributeA;
    private Object attributeB;

    private Function<Object, Object> mockGetter;

    @Before
    public void setUp() {
        attributeA = mock(Object.class);
        attributeB = mock(Object.class);

        mockGetter = mock(Function.class);

        defineGetter(a, attributeA);
        defineGetter(b, attributeB);
    }

    @Test
    public void testEqualsNullSafe_bothGettersNull_returnsTrue() {
        // Arrange
        defineGetter(a, null);
        defineGetter(b, null);

        // Act
        boolean result = Comparisons.equalsNullSafe(a, b, mockGetter);

        // Assert
        assertThat(result, is(true));
    }

    @Test
    public void testEqualsNullSafe_onlyGetterANull_returnsFalse() {
        // Arrange
        defineGetter(a, null);

        // Act
        boolean result = Comparisons.equalsNullSafe(a, b, mockGetter);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void testEqualsNullSafe_onlyGetterBNull_returnsFalse() {
        // Arrange
        defineGetter(b, null);

        // Act
        boolean result = Comparisons.equalsNullSafe(a, b, mockGetter);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    @DataProvider({"true", "false"})
    public void testEqualsNullSafe_notNull_returnsObjectEquality(boolean expectedResult) {
        // Arrange
        defineGetter(a, attributeA = new Object() {
            @Override
            public boolean equals(Object o) {
                assertThat("attributeB must be provided to attributeA.equals", o, is(sameInstance(attributeB)));
                return expectedResult;
            }
        });

        defineGetter(b, attributeB = new Object() {
            @Override
            public boolean equals(Object o) {
                assertThat("attributeA must be provided to attributeB.equals", o, is(sameInstance(attributeA)));
                return expectedResult;
            }
        });

        // Act
        boolean result = Comparisons.equalsNullSafe(a, b, mockGetter);

        // Assert
        assertThat(result, is(expectedResult));
    }

    private void defineGetter(Object object, Object attribute) {
        doReturn(attribute).when(mockGetter).apply(same(object));
    }
}
