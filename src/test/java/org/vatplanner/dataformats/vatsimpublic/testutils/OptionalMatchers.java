package org.vatplanner.dataformats.vatsimpublic.testutils;

import java.util.Optional;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest matchers to work with {@link Optional}s.
 */
public class OptionalMatchers {

    /**
     * Returns a matcher checking if an {@link Optional} is empty.
     *
     * @return matcher checking if an Optional is empty
     */
    public static TypeSafeMatcher<Optional<?>> emptyOptional() {
        return new TypeSafeMatcher<Optional<?>>(Optional.class) {
            @Override
            protected boolean matchesSafely(Optional<?> item) {
                return !item.isPresent();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("empty Optional");
            }
        };
    }

    /**
     * Returns a matcher checking if an {@link Optional} holds the expected
     * value.
     *
     * @param <T> type of expectation
     * @param expectation expected value to be held
     * @return matcher checking if an Optional holds the expected value
     */
    public static <T> TypeSafeMatcher<Optional<T>> optionalOf(T expectation) {
        return new TypeSafeMatcher<Optional<T>>(Optional.class) {
            @Override
            protected boolean matchesSafely(Optional<T> item) {
                return item.isPresent() && (expectation == item.get());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Optional of ");
                description.appendValue(expectation);
            }
        };
    }
}
