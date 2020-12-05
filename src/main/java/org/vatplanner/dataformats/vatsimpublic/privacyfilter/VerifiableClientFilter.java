package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import java.util.Set;
import java.util.function.UnaryOperator;

import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientFields;

/**
 * Filters raw lines from client and prefile sections of VATSIM data files and
 * provides verification of parsed {@link Client} fields for expected results.
 *
 * <p>
 * {@link #apply(java.lang.Object)} will be provided with a complete raw line of
 * client or prefile section as read from a data file and is expected to return
 * the resulting line with just the wanted information filtered out/sanitized as
 * specified. If the field to be processed cannot be located due to unhandled
 * syntax, input should be returned unmodified. If the result was leaking
 * information which should have been removed, this will be detectable by later
 * verification.
 * </p>
 *
 * <p>
 * While application runs on raw lines, verification receives individual fields
 * as available after parsing the result to a {@link Client} object.
 * </p>
 *
 * <p>
 * Application as well as verification must work independent from each other,
 * the same filter instance may be reused concurrently for every line in
 * multiple files which have nothing to do with each other. Only filter
 * configuration (final after construction) is allowed to be kept as a state,
 * everything else must be done stateless. Ideally, verification should use a
 * different approach to implementation than application, sharing or duplicating
 * code between verification and application should be avoided, if possible.
 * </p>
 *
 * @param <T> type as used for fields in {@link ClientFields}
 */
public interface VerifiableClientFilter<T> extends UnaryOperator<String> {

    /**
     * Returns all {@link Client} fields which are supposed to be modified by this
     * filter. All fields not listed by this method are expected to remain
     * unmodified on verification.
     *
     * @return all fields which are supposed to be modified
     */
    public Set<ClientFields.FieldAccess<T>> getAffectedFields();

    /**
     * Verifies the given field contents after filtering. Only the field content is
     * given as original and filtered arguments as retrieved from parsed
     * {@link Client} objects. The field can be identified by fieldAccess.
     *
     * <p>
     * Should only be called for fields indicated as potentially being modified by
     * {@link #getAffectedFields()}. All other fields shall be verified by caller
     * for exact match as they should remain unmodified. Although not required, it
     * is recommended to throw an Exception when called for an unhandled field to
     * improve safety as the filter will be unable to verify it.
     * </p>
     *
     * @param fieldAccess can be used to identify the field, content was retrieved
     *        for
     * @param original content before filter was applied
     * @param filtered content after filter was applied
     * @return true if field was only modified in the expected way, false if not
     *         filtered correctly or unintentional modification occurred
     */
    public boolean verifyAffectedField(ClientFields.FieldAccess<T> fieldAccess, T original, T filtered);
}
