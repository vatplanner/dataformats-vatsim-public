package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import static org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling.DefaultErrorHandlingStrategies.THROW_EXCEPTION;

import java.util.ArrayList;
import java.util.Collection;

import org.vatplanner.dataformats.vatsimpublic.privacyfilter.errorhandling.ErrorHandlingStrategy;

/**
 * Configures the actions to be performed by {@link DataFileFilter}. See
 * {@link DataFileFilter} for more information.
 * <p>
 * Note that <strong>default configuration is empty</strong>, i.e. it defaults
 * to let the filter perform <strong>no changes at all</strong>.
 * </p>
 * <p>
 * <strong>DISCLAIMER:</strong><br>
 * THERE IS NO GUARANTEE THAT THE PROVIDED PRIVACY FILTERING MECHANISM WORKS
 * RELIABLY, NEITHER IN TERMS OF PROTECTING PRIVACY NOR MAINTAINING INTEGRITY OR
 * AVAILABILITY OF DATA.<br>
 * IT IS ONLY TO BE UNDERSTOOD AS A SINGLE FIRST TOOL YOU CAN UTILIZE IN YOUR
 * OWN EFFORTS TO BUILD, OPERATE AND MAINTAIN A CHAIN OF COMPONENTS PROCESSING
 * AND STORING DATA IN A LEGALLY COMPLIANT, TRUSTWORTHY AND RESPONSIBLE WAY
 * THROUGHOUT YOUR ENTIRE INDIVIDUALLY CRAFTED SYSTEM. APPLYING THIS FILTER,
 * EVEN WHEN WORKING AS DESIRED, CAN NOT FREE YOU FROM YOUR OWN
 * RESPONSIBILITIES.<br>
 * CONFIGURATION OF THE FILTER AS WELL AS ALL FURTHER HANDLING OF DATA REQUIRES
 * YOUR OWN JUDGMENT AND PRE-CAUTIONS. BY COMMON SENSE, ALL DATA SHOULD BE
 * REDUCED FURTHER WHENEVER POSSIBLE, WHICH IS BEYOND THE CAPABILITIES PROVIDED
 * BY THIS INITIAL FILTER.<br>
 * PROCESSING ANY INFORMATION REMAINING AFTER APPLYING THE FILTER STILL REQUIRES
 * INDIVIDUAL ASSESSMENT AND IS YOUR OWN RESPONSIBILITY!<br>
 * FILTERED DATA OBVIOUSLY REMAINS SUBJECT TO VATSIM POLICIES AND RESTRICTIONS.
 * EXPLANATIONS OF CONFIGURATION OPTIONS AS WELL AS THIS DISCLAIMER ITSELF ONLY
 * DESCRIBE THOUGHTS AND OBSERVATIONS BY PROGRAM AUTHORS AND ARE NOT TO BE
 * CONFUSED WITH LEGAL ADVICE WHICH CANNOT BE PROVIDED.
 * </p>
 */
public class DataFileFilterConfiguration {

    private boolean removeRealNameAndHomebase = false;
    private boolean substituteObserverPrefix = false;
    private boolean removeStreamingChannels = false;
    private boolean flightPlanRemarksRemoveAll = false;
    private Collection<String> flightPlanRemarksRemoveAllIfContaining = new ArrayList<>();

    private ErrorHandlingStrategy unwantedModificationErrorHandlingStrategy = THROW_EXCEPTION;
    private ErrorHandlingStrategy incompleteFilteringErrorHandlingStrategy = THROW_EXCEPTION;
    private ErrorHandlingStrategy unstableResultErrorHandlingStrategy = THROW_EXCEPTION;

    public boolean isRemoveRealNameAndHomebase() {
        return removeRealNameAndHomebase;
    }

    /**
     * Clients and prefilings include a (not necessarily true) user real name and
     * optional homebase ICAO code which often resembles the user's actual
     * real-world location. This is obviously the most personal information we could
     * receive but at the same time it is completely irrelevant for most use-cases.
     * If you need to identify users, the unique VATSIM account ID is a much better
     * suited alias, so this field can usually be cleared without any ill
     * side-effects.
     * <p>
     * When set to true, the realname field will be cleared without replacement.
     * </p>
     *
     * @param removeRealNameAndHomebase Clear realname field without replacement?
     * @return this instance for method-chaining
     */
    public DataFileFilterConfiguration setRemoveRealNameAndHomebase(boolean removeRealNameAndHomebase) {
        this.removeRealNameAndHomebase = removeRealNameAndHomebase;
        return this;
    }

    public boolean isSubstituteObserverPrefix() {
        return substituteObserverPrefix;
    }

    /**
     * By VATSIM Code of Conduct, observers are required to log in with a call sign
     * ending in "_OBS". It is common practice to choose the initial characters of a
     * user's real name as a prefix (e.g. John Doe => "JD_OBS").
     * <p>
     * When set to true, all callsigns ending in "_OBS" will be substituted by a
     * generic alias "XX_OBS".
     * </p>
     *
     * @param substituteObserverPrefix Substitute all callsigns ending in "_OBS" by
     *        a generic alias "XX_OBS"?
     * @return this instance for method-chaining
     */
    public DataFileFilterConfiguration setSubstituteObserverPrefix(boolean substituteObserverPrefix) {
        this.substituteObserverPrefix = substituteObserverPrefix;
        return this;
    }

    public boolean isRemoveStreamingChannels() {
        return removeStreamingChannels;
    }

    /**
     * Users who stream their experience to public video services often announce
     * their channel URLs in flight plan remarks or controller info lines. It is
     * debatable if removal of such information would really be necessary since
     * those users intended to publicly promote their channels and wanted to get
     * their streaming channel/user associated with their VATSIM account in the
     * first place. Furthermore, attempts to remove that information can have a
     * <strong>high rate of false positives</strong> and remove other information as
     * well.
     * <p>
     * When set to true, a few different common patterns of announcing account data
     * in remarks will be tried to identify and remove a word that <em>could</em> be
     * a channel/user/page name.
     * </p>
     *
     * @param removeStreamingChannels Attempt to remove streaming service
     *        channel/user names?
     * @return this instance for method-chaining
     */
    public DataFileFilterConfiguration setRemoveStreamingChannels(boolean removeStreamingChannels) {
        // TODO: rename option to better fit current implementation
        this.removeStreamingChannels = removeStreamingChannels;
        return this;
    }

    public boolean isFlightPlanRemarksRemoveAll() {
        return flightPlanRemarksRemoveAll;
    }

    /**
     * Users can file all kind of free-text information into their flight plan
     * remarks which sometimes includes voluntarily provided personal information.
     * Non-personal information provided in remarks may make sense to be processed
     * and analyzed (for example formatted ICAO field 18 information), thus you may
     * want to have a look at alternatives to simply blanking the field
     * unconditionally.
     * <p>
     * If you do not intend to process flight plan remarks anyway, simply clearing
     * the free-text information of that field may be the easiest trouble-free
     * choice.
     * </p>
     * <p>
     * When set to true, flight plan remarks are cleared without replacement except
     * for the mandatory communication type flag (text only/receive only/full
     * voice).
     * </p>
     * <p>
     * Also see the conditional
     * {@link #setFlightPlanRemarksRemoveAllIfContaining(java.util.Collection)}
     * option as an alternative.
     * </p>
     *
     * @param flightPlanRemarksRemoveAll Clear free-text flight plan remarks without
     *        replacement? (communication type flag is kept)
     * @return this instance for method-chaining
     */
    public DataFileFilterConfiguration setFlightPlanRemarksRemoveAll(boolean flightPlanRemarksRemoveAll) {
        this.flightPlanRemarksRemoveAll = flightPlanRemarksRemoveAll;
        return this;
    }

    public Collection<String> getFlightPlanRemarksRemoveAllIfContaining() {
        // TODO: return a copy
        return flightPlanRemarksRemoveAllIfContaining;
    }

    /**
     * You can define strings which trigger removal of all free-text information
     * from flight plan remarks (excluding communication type).
     * <p>
     * Users may intend to advise each other about personal situations which could
     * be troublesome to store permanently and possibly associate them with during
     * data collection & analysis. One example would be a pilot with physical
     * disability wanting to advise controllers of their situation's unintentional
     * impact on their performance. While that information is very useful while
     * actively participating in live online service, it is generally unwanted
     * information in all other cases which could violate privacy rights,
     * anti-discrimination law and common sense...
     * </p>
     * <p>
     * If any of the given strings match case-insensitively, free-text flight plan
     * remarks are cleared without replacement. Strings are matched across complete
     * remarks, so they don't need to be full words but can also be word parts. The
     * mandatory communication type flag (text only/receive only/full voice) is kept
     * as well as pre-filing system indication (e.g. <code>+VFPS+</code>).
     * </p>
     * <p>
     * The given {@link Collection} of trigger strings must not contain any items
     * which are null or empty (including strings consisting of just white-spaces).
     * </p>
     * <p>
     * Conditional filtering allows you to only clear remarks field when content was
     * identified as potentially troublesome. To apply such conditional filtering,
     * {@link #setFlightPlanRemarksRemoveAll(boolean)} option must be set to false,
     * otherwise unconditional filtering will still be applied regardless of this
     * option.
     * </p>
     *
     * @param flightPlanRemarksRemoveAllIfContaining case-insensitive strings
     *        triggering removal of free-text flight plan remarks if found
     * @return this instance for method-chaining
     */
    public DataFileFilterConfiguration setFlightPlanRemarksRemoveAllIfContaining(Collection<String> flightPlanRemarksRemoveAllIfContaining) {
        if (flightPlanRemarksRemoveAllIfContaining == null) {
            throw new IllegalArgumentException(
                "list of search strings must not be null; set to empty list instead if you want to disable the feature" //
            );
        }

        // TODO: save a copy
        this.flightPlanRemarksRemoveAllIfContaining = flightPlanRemarksRemoveAllIfContaining;

        return this;
    }

    public ErrorHandlingStrategy getUnwantedModificationErrorHandlingStrategy() {
        return unwantedModificationErrorHandlingStrategy;
    }

    /**
     * Provides a strategy to be called when filtering modified unexpected fields.
     *
     * @param unwantedModificationErrorHandlingStrategy called on modification to
     *        unexpected fields
     */
    public void setUnwantedModificationErrorHandlingStrategy(ErrorHandlingStrategy unwantedModificationErrorHandlingStrategy) {
        this.unwantedModificationErrorHandlingStrategy = unwantedModificationErrorHandlingStrategy;
    }

    public ErrorHandlingStrategy getIncompleteFilteringErrorHandlingStrategy() {
        return incompleteFilteringErrorHandlingStrategy;
    }

    /**
     * Provides a strategy to be called when filter was unsuccessful and
     * verification of affected fields still indicates that unwanted data remained.
     *
     * @param incompleteFilteringErrorHandlingStrategy called when unwanted data
     *        remains after filtering
     */
    public void setIncompleteFilteringErrorHandlingStrategy(ErrorHandlingStrategy incompleteFilteringErrorHandlingStrategy) {
        this.incompleteFilteringErrorHandlingStrategy = incompleteFilteringErrorHandlingStrategy;
    }

    public ErrorHandlingStrategy getUnstableResultErrorHandlingStrategy() {
        return unstableResultErrorHandlingStrategy;
    }

    /**
     * Provides a strategy to be called when applying the same filter again on its
     * result yields a different, thus unstable output.
     *
     * @param unstableResultErrorHandlingStrategy called when filter produces
     *        unstable results
     */
    public void setUnstableResultErrorHandlingStrategy(ErrorHandlingStrategy unstableResultErrorHandlingStrategy) {
        this.unstableResultErrorHandlingStrategy = unstableResultErrorHandlingStrategy;
    }

}
