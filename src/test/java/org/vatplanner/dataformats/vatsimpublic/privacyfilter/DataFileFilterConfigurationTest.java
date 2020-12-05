package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling.ErrorHandlingStrategy;
import org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling.ThrowExceptionStrategy;

public class DataFileFilterConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSetFlightPlanRemarksRemoveAllIfContaining_null_throwsIllegalArgumentException() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        thrown.expect(IllegalArgumentException.class);

        // Act
        configuration.setFlightPlanRemarksRemoveAllIfContaining(null);

        // Assert (nothing to do)
    }

    @Test
    public void testGetFlightPlanRemarksRemoveAllIfContaining_setNull_returnsNotNull() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        try {
            configuration.setFlightPlanRemarksRemoveAllIfContaining(null);
        } catch (Exception ex) {
            // ignore
        }

        // Act
        Collection<String> result = configuration.getFlightPlanRemarksRemoveAllIfContaining();

        // Assert
        assertThat(result, is(not(nullValue())));
    }

    @Test
    public void testSetFlightPlanRemarksRemoveAllIfContaining_emptyList_doesNotThrowAnyException() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();
        ArrayList<String> emptyList = new ArrayList<>();

        // Act
        configuration.setFlightPlanRemarksRemoveAllIfContaining(emptyList);

        // Assert (nothing to do)
    }

    @Test
    public void testGetUnwantedModificationErrorHandlingStrategy_unconfigured_returnsThrowExceptionStrategy() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        // Act
        ErrorHandlingStrategy strategy = configuration.getUnwantedModificationErrorHandlingStrategy();

        // Assert
        assertThat(strategy, is(instanceOf(ThrowExceptionStrategy.class)));
    }

    @Test
    public void testGetIncompleteFilteringErrorHandlingStrategy_unconfigured_returnsThrowExceptionStrategy() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        // Act
        ErrorHandlingStrategy strategy = configuration.getIncompleteFilteringErrorHandlingStrategy();

        // Assert
        assertThat(strategy, is(instanceOf(ThrowExceptionStrategy.class)));
    }

    @Test
    public void testGetUnstableResultErrorHandlingStrategy_unconfigured_returnsThrowExceptionStrategy() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        // Act
        ErrorHandlingStrategy strategy = configuration.getUnstableResultErrorHandlingStrategy();

        // Assert
        assertThat(strategy, is(instanceOf(ThrowExceptionStrategy.class)));
    }
}
