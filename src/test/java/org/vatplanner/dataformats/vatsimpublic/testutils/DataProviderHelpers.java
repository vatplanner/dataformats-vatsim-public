package org.vatplanner.dataformats.vatsimpublic.testutils;

import com.tngtech.java.junit.dataprovider.DataProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Helper methods for defining {@link DataProvider}s.
 */
public class DataProviderHelpers {

    private DataProviderHelpers() {
        // utility class; hide constructor
    }

    /**
     * Converts the given one-dimensional array into a two-dimensional array
     * which wraps every item into its own array. Can be used to make
     * definitions more readable if only one argument needs to be provided per
     * test case.
     *
     * @param oneDimensionalArray one-dimensional array
     * @return two-dimensional array with every item wrapped in its own array
     */
    public static Object[][] asTwoDimensionalArray(Object[] oneDimensionalArray) {
        Object[][] twoDimensionalArray = new Object[oneDimensionalArray.length][1];

        for (int i = 0; i < oneDimensionalArray.length; i++) {
            twoDimensionalArray[i][0] = oneDimensionalArray[i];
        }

        return twoDimensionalArray;
    }

    /**
     * Converts all items retrieved from the given stream into a two-dimensional
     * array which wraps every item into its own array. Can be used to make
     * definitions more readable if only one argument needs to be provided per
     * test case.
     *
     * @param stream stream to read items from
     * @return two-dimensional array with every item wrapped in its own array
     */
    public static Object[][] asTwoDimensionalArray(Stream<?> stream) {
        return asTwoDimensionalArray(stream.toArray());
    }

    /**
     * Returns a two-dimensional array ready to be used for test-cases with just
     * one argument of all enum values of the given class, except for the given
     * exemptions.
     *
     * @param <T> enum type/class
     * @param enumClass class of enum to retrieve values from
     * @param exemptions values to filter out
     * @return two-dimensional array with each enum value except for given
     * exemptions wrapped in its own array
     */
    public static <T extends Enum> Object[][] allEnumValuesExcept(Class<T> enumClass, Set<T> exemptions) {
        return asTwoDimensionalArray(
                Arrays.asList(enumClass.getEnumConstants()) //
                        .stream() //
                        .filter(value -> !exemptions.contains(value))
        );
    }

    /**
     * Returns a two-dimensional array ready to be used for test-cases with just
     * one argument of all enum values of the given class, except for the given
     * exemptions.
     *
     * @param <T> enum type/class
     * @param enumClass class of enum to retrieve values from
     * @param exemptions values to filter out
     * @return two-dimensional array with each enum value except for given
     * exemptions wrapped in its own array
     */
    public static <T extends Enum> Object[][] allEnumValuesExcept(Class<T> enumClass, T... exemptions) {
        return allEnumValuesExcept(enumClass, new HashSet<>(Arrays.asList(exemptions)));
    }
}
