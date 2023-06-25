package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collection;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling.ErrorHandlingStrategy;
import org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling.ThrowExceptionStrategy;

class DataFileFilterConfigurationTest {

    @Test
    void testSetFlightPlanRemarksRemoveAllIfContaining_null_throwsIllegalArgumentException() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        // Act
        ThrowingCallable action = () -> configuration.setFlightPlanRemarksRemoveAllIfContaining(null);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGetFlightPlanRemarksRemoveAllIfContaining_setNull_returnsNotNull() {
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
        assertThat(result).isNotNull();
    }

    @Test
    void testSetFlightPlanRemarksRemoveAllIfContaining_emptyList_doesNotThrowAnyException() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();
        ArrayList<String> emptyList = new ArrayList<>();

        // Act
        ThrowingCallable action = () -> configuration.setFlightPlanRemarksRemoveAllIfContaining(emptyList);

        // Assert
        assertThatCode(action).doesNotThrowAnyException();
    }

    @Test
    void testGetUnwantedModificationErrorHandlingStrategy_unconfigured_returnsThrowExceptionStrategy() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        // Act
        ErrorHandlingStrategy strategy = configuration.getUnwantedModificationErrorHandlingStrategy();

        // Assert
        assertThat(strategy).isInstanceOf(ThrowExceptionStrategy.class);
    }

    @Test
    void testGetIncompleteFilteringErrorHandlingStrategy_unconfigured_returnsThrowExceptionStrategy() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        // Act
        ErrorHandlingStrategy strategy = configuration.getIncompleteFilteringErrorHandlingStrategy();

        // Assert
        assertThat(strategy).isInstanceOf(ThrowExceptionStrategy.class);
    }

    @Test
    void testGetUnstableResultErrorHandlingStrategy_unconfigured_returnsThrowExceptionStrategy() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration();

        // Act
        ErrorHandlingStrategy strategy = configuration.getUnstableResultErrorHandlingStrategy();

        // Assert
        assertThat(strategy).isInstanceOf(ThrowExceptionStrategy.class);
    }
}
