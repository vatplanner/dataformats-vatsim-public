package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.vatplanner.dataformats.vatsimpublic.UnconfiguredException;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class VerifiableClientFilterFactoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private VerifiableClientFilterFactory spyFactory;

    @Before
    public void setUp() {
        VerifiableClientFilterFactory templateFactory = new VerifiableClientFilterFactory();
        spyFactory = spy(templateFactory);
    }

    @Test
    public void testBuildFromConfiguration_null_throwsIllegalArgumentException() {
        // Arrange
        thrown.expect(IllegalArgumentException.class);

        // Act
        spyFactory.buildFromConfiguration(null);

        // Assert (nothing to do)
    }

    @Test
    public void testBuildFromConfiguration_noFiltersConfigured_throwsUnconfiguredException() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(false)
            .setFlightPlanRemarksRemoveAllIfContaining(new ArrayList<>())
            .setRemoveRealNameAndHomebase(false)
            .setRemoveStreamingChannels(false)
            .setSubstituteObserverPrefix(false);

        thrown.expect(UnconfiguredException.class);

        // Act
        spyFactory.buildFromConfiguration(configuration);

        // Assert (nothing to do)
    }

    @Test
    public void testBuildFromConfiguration_removeStreamingChannels_throwsUnsupportedOperationException() {
        // TODO: remove when implemented

        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setRemoveStreamingChannels(true);

        thrown.expect(UnsupportedOperationException.class);

        // Act
        spyFactory.buildFromConfiguration(configuration);

        // Assert (nothing to do)
    }

    @DataProvider
    public static Object[][] dataProviderConfigurationWithExpectedClasses() {
        Collection<String> EMPTY = asList();

        return new Object[][] {
            // TODO: add removal of external account data when implemented

            // single
            { true, EMPTY, false, false, false, asList(FlightPlanRemarksRemoveAllFilter.class) }, //
            { false, asList("abc"), false, false, false, asList(FlightPlanRemarksRemoveAllFilter.class) }, //
            { true, asList("abc"), false, false, false, asList(FlightPlanRemarksRemoveAllFilter.class) }, //
            { false, EMPTY, true, false, false, asList(RemoveRealNameAndHomebaseFilter.class) }, //
            { false, EMPTY, false, false, true, asList(SubstituteObserverPrefixFilter.class) }, //

            // combined 2
            { true, EMPTY, true, false, false,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    RemoveRealNameAndHomebaseFilter.class //
                ) //
            }, //

            { true, EMPTY, false, false, true,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    SubstituteObserverPrefixFilter.class //
                ) //
            }, //

            { false, asList("abc"), true, false, false,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    RemoveRealNameAndHomebaseFilter.class //
                ) //
            }, //

            { false, asList("abc"), false, false, true,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    SubstituteObserverPrefixFilter.class //
                ) //
            }, //

            { true, asList("abc"), true, false, false,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    RemoveRealNameAndHomebaseFilter.class //
                ) //
            }, //

            { true, asList("abc"), false, false, true,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    SubstituteObserverPrefixFilter.class //
                ) //
            }, //

            { false, EMPTY, true, false, true,
                asList(
                    RemoveRealNameAndHomebaseFilter.class,
                    SubstituteObserverPrefixFilter.class //
                ) //
            }, //

            // combined 3
            { true, EMPTY, true, false, true,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    RemoveRealNameAndHomebaseFilter.class,
                    SubstituteObserverPrefixFilter.class //
                ) //
            }, //

            { false, asList("abc"), true, false, true,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    RemoveRealNameAndHomebaseFilter.class,
                    SubstituteObserverPrefixFilter.class //
                ) //
            }, //

            { true, asList("abc"), true, false, true,
                asList(
                    FlightPlanRemarksRemoveAllFilter.class,
                    RemoveRealNameAndHomebaseFilter.class,
                    SubstituteObserverPrefixFilter.class //
                ) //
            }, //
        };
    }

    @Test
    @UseDataProvider("dataProviderConfigurationWithExpectedClasses")
    public void testBuildFromConfiguration_filtersConfigured_returnsExpectedInstances(boolean flightPlanRemarksRemoveAll, Collection<String> flightPlanRemarksRemoveAllIfContaining, boolean removeRealNameAndHomebase, boolean removeStreamingChannels, boolean substituteObserverPrefix, Collection<Class<VerifiableClientFilter>> expectedFilters) {
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
        assertThat("at least one filter has to be expected", expectedFilters, is(not(empty())));

        for (Class<VerifiableClientFilter> expectedFilter : expectedFilters) {
            assertTrue(
                "instance of " + expectedFilter.getSimpleName() + " is expected to be returned",
                result.stream().anyMatch(expectedFilter::isInstance));
        }
    }

    @Test
    @UseDataProvider("dataProviderConfigurationWithExpectedClasses")
    public void testBuildFromConfiguration_filtersConfigured_returnsNoAdditionalInstances(boolean flightPlanRemarksRemoveAll, Collection<String> flightPlanRemarksRemoveAllIfContaining, boolean removeRealNameAndHomebase, boolean removeStreamingChannels, boolean substituteObserverPrefix, Collection<Class<VerifiableClientFilter>> expectedFilters) {
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
        assertThat("at least one filter has to be expected", expectedFilters, is(not(empty())));

        assertThat(result, hasSize(expectedFilters.size()));
    }

    @DataProvider
    public static Object[][] dataProviderFlightPlanRemarksRemoveAll() {
        List<Object> EMPTY = asList();

        return new Object[][] {
            { true, EMPTY }, //
            { false, asList("abc") }, //
        };
    }

    @Test
    @UseDataProvider("dataProviderFlightPlanRemarksRemoveAll")
    public void testBuildFromConfiguration_removeFlightPlanRemarks_returnsInstanceCreatedThroughProxyMethod(boolean flightPlanRemarksRemoveAll, Collection<String> flightPlanRemarksRemoveAllIfContaining) {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(flightPlanRemarksRemoveAll)
            .setFlightPlanRemarksRemoveAllIfContaining(flightPlanRemarksRemoveAllIfContaining);

        FlightPlanRemarksRemoveAllFilter expectedMockFilter = mock(FlightPlanRemarksRemoveAllFilter.class);
        doReturn(expectedMockFilter).when(spyFactory).createFlightPlanRemarksRemoveAllFilter(any());

        // Act
        List<VerifiableClientFilter<?>> result = spyFactory.buildFromConfiguration(configuration);

        // Assert
        assertThat(result, contains(expectedMockFilter));
    }

    @Test
    public void testBuildFromConfiguration_removeFlightPlanRemarksUnconditional_createsInstanceWithNullTriggers() {
        // Arrange
        DataFileFilterConfiguration configuration = new DataFileFilterConfiguration()
            .setFlightPlanRemarksRemoveAll(true);

        // Act
        spyFactory.buildFromConfiguration(configuration);

        // Assert
        verify(spyFactory).createFlightPlanRemarksRemoveAllFilter(isNull());
    }

    @Test
    public void testBuildFromConfiguration_removeFlightPlanRemarksUsingTriggers_createsInstanceWithSameTriggers() {
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
