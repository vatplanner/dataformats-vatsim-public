package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.vatplanner.dataformats.vatsimpublic.UnconfiguredException;

/**
 * Factory to instantiate data file filters.
 */
public class VerifiableClientFilterFactory {

    /**
     * Returns a list of all filters which need to be applied to match the given
     * configuration.
     *
     * @param configuration configuration describing filters to be instantiated
     * @return filters matching configuration
     * @throws UnconfiguredException when configuration does not request any
     * feature at all
     */
    public List<VerifiableClientFilter<?>> buildFromConfiguration(DataFileFilterConfiguration configuration) throws UnconfiguredException {
        if (configuration == null) {
            throw new IllegalArgumentException("unable to build filter chain from null configuration");
        }

        boolean isAnyFeatureConfigured = configuration.isFlightPlanRemarksRemoveAll()
                || configuration.isRemoveRealNameAndHomebase()
                || configuration.isRemoveStreamingChannels()
                || configuration.isSubstituteObserverPrefix()
                || !configuration.getFlightPlanRemarksRemoveAllIfContaining().isEmpty();

        if (!isAnyFeatureConfigured) {
            throw new UnconfiguredException("unable to build filter chain when no features have been requested");
        }

        if (configuration.isRemoveStreamingChannels()) {
            // TODO: remove when implemented
            throw new UnsupportedOperationException("a requested filter feature is still under development and cannot be used yet");
        }

        List<VerifiableClientFilter<?>> filters = new ArrayList<>();

        if (configuration.isRemoveRealNameAndHomebase()) {
            filters.add(new RemoveRealNameAndHomebaseFilter());
        }

        if (configuration.isSubstituteObserverPrefix()) {
            filters.add(new SubstituteObserverPrefixFilter());
        }

        Collection<String> flightPlanRemarkTriggers = configuration.getFlightPlanRemarksRemoveAllIfContaining();
        if (configuration.isFlightPlanRemarksRemoveAll()) {
            filters.add(createFlightPlanRemarksRemoveAllFilter(null));
        } else if (!flightPlanRemarkTriggers.isEmpty()) {
            filters.add(createFlightPlanRemarksRemoveAllFilter(flightPlanRemarkTriggers));
        }

        return filters;
    }

    FlightPlanRemarksRemoveAllFilter createFlightPlanRemarksRemoveAllFilter(Collection<String> triggers) {
        return new FlightPlanRemarksRemoveAllFilter(triggers);
    }
}
