package org.vatplanner.dataformats.vatsimpublic.utils;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.vatplanner.dataformats.vatsimpublic.testutils.OptionalMatchers.emptyOptional;
import static org.vatplanner.dataformats.vatsimpublic.testutils.OptionalMatchers.optionalOf;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class CollectionHelpersTest {

    @Test
    public void testFindPrevious_emptyCollection_returnsEmpty() {
        // Arrange
        List<Integer> empty = asList();

        // Act
        Optional<Integer> result = CollectionHelpers.findPrevious(empty, 0);

        // Assert
        assertThat(result, is(emptyOptional()));
    }

    @Test
    public void testFindPrevious_noPreviousItem_returnsEmpty() {
        // Arrange
        Integer needle = 0;
        List<Integer> empty = asList(needle);

        // Act
        Optional<Integer> result = CollectionHelpers.findPrevious(empty, needle);

        // Assert
        assertThat(result, is(emptyOptional()));
    }

    @DataProvider
    public static Object[][] dataProviderFindPrevious() {
        return new Object[][] {
            { asList(0, 1), 1, 0 },
            { asList(0, 1, 2), 2, 1 },
            { asList(5, 23, 42, 73), 23, 5 },
            { asList(5, 23, 42, 73), 42, 23 },
            { asList(5, 23, 42, 73), 73, 42 }
        };
    }

    @Test
    @UseDataProvider("dataProviderFindPrevious")
    public void testFindPrevious_inCollection_returnsExpectedElement(List<Integer> haystack, Integer needle, Integer expectedResult) {
        // Arrange (nothing to do)

        // Act
        Optional<Integer> result = CollectionHelpers.findPrevious(haystack, needle);

        // Assert
        assertThat(result, is(optionalOf(expectedResult)));
    }
}
