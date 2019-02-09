package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import java.util.function.Function;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileMetaData;
import org.vatplanner.dataformats.vatsimpublic.parser.FSDServer;
import org.vatplanner.dataformats.vatsimpublic.parser.VoiceServer;

/**
 * A simple first-tool-in-line filter attempting to provide a limited degree of
 * data removal/replacement as it may be required for privacy protection.
 * <p>
 * VATSIM data files contain personal information. Although VATSIM policies
 * inform users about the possibility that their data is being processed by
 * third parties, data protection laws may require a reduction of data before
 * processing or permanent storage.
 * </p>
 * <p>
 * The filter operates on and produces raw data files, so it is able to
 * introduce errors and produce corrupted or incomplete output. Therefore, it is
 * recommended to compare a {@link DataFile} parsed from original raw data to a
 * {@link DataFile} parsed from filter output. See
 * {@link #verifyOnlyWantedModifications(DataFile, DataFile)} and
 * {@link #verifyNoAdditionalLogMessages(DataFile, DataFile)}.
 * </p>
 * <p>
 * Filtering data files and verifying results may be a rather slow process. The
 * filter has been written with permanent archival and forwarding of unparsed
 * but pre-filtered data files in mind where filtering is a central one-time
 * operation which is thus not required to run efficiently. If you just want to
 * extract some limited amount of data then you will most-likely find that you
 * are better off just discarding individual fields from parsed data file
 * objects or copying wanted data selectively from {@link DataFile} objects
 * instead of bothering with this filter.
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
public class DataFileFilter {

    private final DataFileFilterConfiguration configuration;

    private static final int SUPPORTED_FORMAT_VERSION = 8;

    /**
     * Initializes a new data filter for given configuration. Note that
     * configuration must be final; any later modification to configuration may
     * not apply to this filter instance or yield unexpected results.
     *
     * @param configuration configuration for this filter instance, must be
     * final and not null
     */
    public DataFileFilter(DataFileFilterConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }

        // FIXME: finish implementation, then stop throwing this exception :)
        boolean isUnimplementedFeatureRequested = configuration.isFlightPlanRemarksRemoveAll()
                || !configuration.getFlightPlanRemarksRemoveAllIfContaining().isEmpty()
                || configuration.isRemoveRealNameAndHomebase()
                || configuration.isRemoveStreamingChannels()
                || configuration.isSubstituteObserverPrefix();
        if (isUnimplementedFeatureRequested) {
            throw new UnsupportedOperationException("a requested filter feature is still under development and cannot be used yet");
        }

        this.configuration = configuration;
    }

    /**
     * Checks if the given format version is officially supported. Format
     * version is available from {@link DataFileMetaData#getVersionFormat()}.
     * Data format may change in the future in which case the filter may have to
     * be updated. It is recommended to check compatibility before attempting to
     * apply the filter.
     *
     * @param formatVersion version to query support for
     * @return Is given format version supported? (true = supported, false =
     * unsupported)
     */
    public boolean isFormatVersionSupported(int formatVersion) {
        return (formatVersion == SUPPORTED_FORMAT_VERSION);
    }

    /**
     * Filters the given raw data file as configured and returns the result.
     * Filtering may result in some form of data corruption, so you may want to
     * parse & verify the result using
     * {@link #verifyNoAdditionalLogMessages(DataFile, DataFile)} and
     * {@link #verifyOnlyWantedModifications(DataFile, DataFile)} afterwards.
     * <p>
     * <b>Disclaimer (again):</b> The filter may fail to apply properly and not
     * only corrupt data but also fail to implement all configured filtering
     * steps as you would expect. Use at your own risk and read the full
     * disclaimer on class JavaDoc before use!
     * </p>
     *
     * @param formatVersion format version as available from
     * {@link DataFileMetaData#getVersionFormat()} after parsing
     * @param original raw data file to apply filter to
     * @return filtered data file (may be erroneous or incompletely filtered!)
     */
    public String filter(int formatVersion, String original) {
        // FIXME: implement
        return null;
    }

    /**
     * Checks if the filtered {@link DataFile} only lost information where it
     * has been requested to be removed, as compared to original file.
     * Verification is based on configuration provided to constructor.
     *
     * @param original file parsed from original raw data
     * @param filtered file parsed from filtered output
     * @return Is all non-filtered data still present 1:1 on filtered DataFile?
     * (true = all data OK; false = filtered file lost data which should not
     * have been removed)
     */
    public boolean verifyOnlyWantedModifications(DataFile original, DataFile filtered) {
        // FIXME: use checkEqualMetadata
        // FIXME: implement
        return false;
    }

    /**
     * Checks that no additional log messages are found in filtered
     * {@link DataFile} as compared to original file.
     *
     * @param original file parsed from original raw data
     * @param filtered file parsed from filtered output
     * @return Are no additional log messages found in filtered DataFile? (true
     * = no additional log messages; false = additional log messages found)
     */
    public boolean verifyNoAdditionalLogMessages(DataFile original, DataFile filtered) {
        // FIXME: implement
        return false;
    }

    /**
     * Checks if both meta data objects hold the same information.
     *
     * @param a first meta data object
     * @param b second meta data object
     * @return Are both meta data objects equal?
     */
    boolean checkEqualMetadata(DataFileMetaData a, DataFileMetaData b) {
        if (a == null) {
            return (b == null);
        } else if (b == null) {
            return false;
        }

        // QUESTION: move to .equals in DataFileMetaData?
        boolean isVersionFormatEqual = (a.getVersionFormat() == b.getVersionFormat());
        boolean isTimestampEqual = equalsNullSafe(a, b, DataFileMetaData::getTimestamp);
        boolean isNumberOfConnectedClientsEqual = (a.getNumberOfConnectedClients() == b.getNumberOfConnectedClients());
        boolean isMinimumDataFileRetrievalIntervalEqual = equalsNullSafe(a, b, DataFileMetaData::getMinimumDataFileRetrievalInterval);
        boolean isMinimumAtisFileRetrievalIntervalEqual = equalsNullSafe(a, b, DataFileMetaData::getMinimumAtisRetrievalInterval);

        boolean isEqual = isVersionFormatEqual && isTimestampEqual && isNumberOfConnectedClientsEqual //
                && isMinimumDataFileRetrievalIntervalEqual && isMinimumAtisFileRetrievalIntervalEqual;

        return isEqual;
    }

    /**
     * Checks if both voice server objects hold the same information.
     *
     * @param a first voice server object
     * @param b second voice server object
     * @return Are both voice server objects equal?
     */
    boolean checkEqualVoiceServer(VoiceServer a, VoiceServer b) {
        if (a == null) {
            return (b == null);
        } else if (b == null) {
            return false;
        }

        // QUESTION: move to .equals in VoiceServer?
        boolean isAddressEqual = equalsNullSafe(a, b, VoiceServer::getAddress);
        boolean isLocationEqual = equalsNullSafe(a, b, VoiceServer::getLocation);
        boolean isNameEqual = equalsNullSafe(a, b, VoiceServer::getName);
        boolean isClientConnectionAllowedEqual = equalsNullSafe(a, b, VoiceServer::isClientConnectionAllowed);
        boolean isRawServerTypeEqual = equalsNullSafe(a, b, VoiceServer::getRawServerType);

        boolean isEqual = isAddressEqual && isLocationEqual && isNameEqual && isClientConnectionAllowedEqual && isRawServerTypeEqual;

        return isEqual;
    }

    /**
     * Checks if both FSD server objects hold the same information.
     *
     * @param a first FSD server object
     * @param b second FSD server object
     * @return Are both FSD server objects equal?
     */
    boolean checkEqualFSDServer(FSDServer a, FSDServer b) {
        if (a == null) {
            return (b == null);
        } else if (b == null) {
            return false;
        }

        // QUESTION: move to .equals in FSDServer?
        boolean isIdEqual = equalsNullSafe(a, b, FSDServer::getId);
        boolean isAddressEqual = equalsNullSafe(a, b, FSDServer::getAddress);
        boolean isLocationEqual = equalsNullSafe(a, b, FSDServer::getLocation);
        boolean isNameEqual = equalsNullSafe(a, b, FSDServer::getName);
        boolean isClientConnectionAllowedEqual = equalsNullSafe(a, b, FSDServer::isClientConnectionAllowed);

        boolean isEqual = isIdEqual && isAddressEqual && isLocationEqual && isNameEqual && isClientConnectionAllowedEqual;

        return isEqual;
    }

    /**
     * Checks if the results returned by both objects' accessor is either equal
     * or null. If only one accessor result is null, the evaluation result will
     * always be false.
     *
     * @param <T> type of objects
     * @param a first object, must not be null
     * @param b second object, must not be null
     * @param accessor accessor to retrieve value to be compared; you will
     * likely want to provide a method reference to a getter
     * @return true if both accessor results are either null or equal
     * (determined by {@link Object#equals(Object)})
     */
    private <T> boolean equalsNullSafe(T a, T b, Function<T, Object> accessor) {
        Object objA = accessor.apply(a);
        Object objB = accessor.apply(b);

        if (objA == null) {
            return (objB == null);
        } else if (objB == null) {
            return false;
        }

        return objA.equals(objB);
    }
}
