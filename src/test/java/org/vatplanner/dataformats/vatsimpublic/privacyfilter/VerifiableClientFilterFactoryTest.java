package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vatplanner.dataformats.vatsimpublic.UnconfiguredException;

class VerifiableClientFilterFactoryTest {

    private VerifiableClientFilterFactory spyFactory;

    @BeforeEach
    void setUp() {
        VerifiableClientFilterFactory templateFactory = new VerifiableClientFilterFactory();
        spyFactory = spy(templateFactory);
    }

    @Test
    void testBuildFromConfiguration_null_throwsIllegalArgumentException() {
        // Arrange (nothing to do)

        // Act
        ThrowingCallable action = () -> spyFactory.buildFromConfiguration(null);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBuildFromConfiguration_noFiltersConfigured_throwsUnconfiguredException() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(false)
            .setFlightPlanRemarksRemoveAllIfContaining(new ArrayList<>())
            .setRemoveRealNameAndHomebase(false)
            .setRemoveStreamingChannels(false)
            .setSubstituteObserverPrefix(false);

        // Act
        ThrowingCallable action = () -> spyFactory.buildFromConfiguration(configuration);

        // Assert
        assertThatThrownBy(action).isInstanceOf(UnconfiguredException.class);
    }

    @Test
    void testBuildFromConfiguration_removeStreamingChannels_throwsUnsupportedOperationException() {
        // TODO: remove when implemented

        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setRemoveStreamingChannels(true);

        // Act
        ThrowingCallable action = () -> spyFactory.buildFromConfiguration(configuration);

        // Assert
        assertThatThrownBy(action).isInstanceOf(UnsupportedOperationException.class);
    }

    static Stream<Arguments> dataProviderConfigurationWithExpectedClasses() {
        Collection<String> EMPTY = Collections.emptyList();

        return Stream.of(
            // TODO: add removal of external account data when implemented

            // single
            Arguments.of(true, EMPTY, false, false, false, asList(FlightPlanRemarksRemoveAllFilter.class)),
            Arguments.of(false, asList("abc"), false, false, false, asList(FlightPlanRemarksRemoveAllFilter.class)),
            Arguments.of(true, asList("abc"), false, false, false, asList(FlightPlanRemarksRemoveAllFilter.class)),
            Arguments.of(false, EMPTY, true, false, false, asList(RemoveRealNameAndHomebaseFilter.class)),
            Arguments.of(false, EMPTY, false, false, true, asList(SubstituteObserverPrefixFilter.class)),

            // combined 2
            Arguments.of(true, EMPTY, true, false, false,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             RemoveRealNameAndHomebaseFilter.class
                         )
            ),

            Arguments.of(true, EMPTY, false, false, true,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             SubstituteObserverPrefixFilter.class
                         )
            ),

            Arguments.of(false, asList("abc"), true, false, false,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             RemoveRealNameAndHomebaseFilter.class
                         )
            ),

            Arguments.of(false, asList("abc"), false, false, true,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             SubstituteObserverPrefixFilter.class //
                         )
            ),

            Arguments.of(true, asList("abc"), true, false, false,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             RemoveRealNameAndHomebaseFilter.class //
                         )
            ),

            Arguments.of(true, asList("abc"), false, false, true,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             SubstituteObserverPrefixFilter.class //
                         )
            ),

            Arguments.of(false, EMPTY, true, false, true,
                         asList(
                             RemoveRealNameAndHomebaseFilter.class,
                             SubstituteObserverPrefixFilter.class //
                         )
            ),

            // combined 3
            Arguments.of(true, EMPTY, true, false, true,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             RemoveRealNameAndHomebaseFilter.class,
                             SubstituteObserverPrefixFilter.class //
                         )
            ),

            Arguments.of(false, asList("abc"), true, false, true,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             RemoveRealNameAndHomebaseFilter.class,
                             SubstituteObserverPrefixFilter.class //
                         )
            ),

            Arguments.of(true, asList("abc"), true, false, true,
                         asList(
                             FlightPlanRemarksRemoveAllFilter.class,
                             RemoveRealNameAndHomebaseFilter.class,
                             SubstituteObserverPrefixFilter.class //
                         )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderConfigurationWithExpectedClasses")
    void testBuildFromConfiguration_filtersConfigured_returnsExpectedInstances(boolean flightPlanRemarksRemoveAll, Collection<String> flightPlanRemarksRemoveAllIfContaining, boolean removeRealNameAndHomebase, boolean removeStreamingChannels, boolean substituteObserverPrefix, Collection<Class<VerifiableClientFilter>> expectedFilters) {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(flightPlanRemarksRemoveAll)
            .setFlightPlanRemarksRemoveAllIfContaining(flightPlanRemarksRemoveAllIfContaining)
            .setRemoveRealNameAndHomebase(removeRealNameAndHomebase)
            .setRemoveStreamingChannels(removeStreamingChannels)
            .setSubstituteObserverPrefix(substituteObserverPrefix);

        // Act
        List<VerifiableClientFilter<?>> result = spyFactory.buildFromConfiguration(configuration);

        // Assert
        assertThat(expectedFilters).isNotEmpty()
                                   .allSatisfy(
                                       expectedFilter -> assertThat(result).anySatisfy(expectedFilter::isInstance)
                                   );
    }

    @ParameterizedTest
    @MethodSource("dataProviderConfigurationWithExpectedClasses")
    void testBuildFromConfiguration_filtersConfigured_returnsNoAdditionalInstances(boolean flightPlanRemarksRemoveAll, Collection<String> flightPlanRemarksRemoveAllIfContaining, boolean removeRealNameAndHomebase, boolean removeStreamingChannels, boolean substituteObserverPrefix, Collection<Class<VerifiableClientFilter>> expectedFilters) {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(flightPlanRemarksRemoveAll)
            .setFlightPlanRemarksRemoveAllIfContaining(flightPlanRemarksRemoveAllIfContaining)
            .setRemoveRealNameAndHomebase(removeRealNameAndHomebase)
            .setRemoveStreamingChannels(removeStreamingChannels)
            .setSubstituteObserverPrefix(substituteObserverPrefix);

        // Act
        List<VerifiableClientFilter<?>> result = spyFactory.buildFromConfiguration(configuration);

        // Assert
        assertThat(result).hasSameSizeAs(expectedFilters);
    }

    static Stream<Arguments> dataProviderFlightPlanRemarksRemoveAll() {
        List<Object> EMPTY = Collections.emptyList();

        return Stream.of(
            Arguments.of(true, EMPTY),
            Arguments.of(false, asList("abc"))
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderFlightPlanRemarksRemoveAll")
    void testBuildFromConfiguration_removeFlightPlanRemarks_returnsInstanceCreatedThroughProxyMethod(boolean flightPlanRemarksRemoveAll, Collection<String> flightPlanRemarksRemoveAllIfContaining) {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(flightPlanRemarksRemoveAll)
            .setFlightPlanRemarksRemoveAllIfContaining(flightPlanRemarksRemoveAllIfContaining);

        FlightPlanRemarksRemoveAllFilter expectedMockFilter = mock(FlightPlanRemarksRemoveAllFilter.class);
        doReturn(expectedMockFilter).when(spyFactory).createFlightPlanRemarksRemoveAllFilter(any());

        // Act
        List<VerifiableClientFilter<?>> result = spyFactory.buildFromConfiguration(configuration);

        // Assert
        assertThat(result).containsExactly(expectedMockFilter);
    }

    @Test
    void testBuildFromConfiguration_removeFlightPlanRemarksUnconditional_createsInstanceWithNullTriggers() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(true);

        // Act
        spyFactory.buildFromConfiguration(configuration);

        // Assert
        verify(spyFactory).createFlightPlanRemarksRemoveAllFilter(isNull());
    }

    @Test
    void testBuildFromConfiguration_removeFlightPlanRemarksUsingTriggers_createsInstanceWithSameTriggers() {
        // Arrange
        List<String> expectedTriggers = asList("a");

        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(false)
            .setFlightPlanRemarksRemoveAllIfContaining(expectedTriggers);

        // Act
        spyFactory.buildFromConfiguration(configuration);

        // Assert
        verify(spyFactory).createFlightPlanRemarksRemoveAllFilter(same(expectedTriggers));
    }
}
