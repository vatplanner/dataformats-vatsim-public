package org.vatplanner.dataformats.vatsimpublic.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Provides enumerable access to all fields of parsed {@link Client} objects.
 * This is mainly (only?) useful to verify filter results.
 */
public class ClientFields {
    // FIXME: write script to auto-generate https://area-51.blog/2009/11/07/using-groovy-to-generate-java-sources-in-maven/

    private ClientFields() {
        // utility class; hide constructor
    }

    /**
     * All {@link Client} object fields resulting in {@link String} return
     * values.
     */
    public enum StringFields implements FieldAccess<String> {
        CALLSIGN(Client::getCallsign), //
        REAL_NAME(Client::getRealName), //
        AIRCRAFT_TYPE(Client::getAircraftType), //
        FILED_DEPARTURE_AIRPORT_CODE(Client::getFiledDepartureAirportCode), //
        RAW_FILED_ALTITUDE(Client::getRawFiledAltitude), //
        FILED_DESTINATION_AIRPORT_CODE(Client::getFiledDestinationAirportCode), //
        SERVER_ID(Client::getServerId), //
        RAW_FLIGHT_PLAN_TYPE(Client::getRawFlightPlanType), //
        FILED_ALTERNATE_AIRPORT_CODE(Client::getFiledAlternateAirportCode), //
        FLIGHT_PLAN_REMARKS(Client::getFlightPlanRemarks), //
        FILED_ROUTE(Client::getFiledRoute), //
        ;

        private final Function<Client, String> getterMethod;

        private StringFields(Function<Client, String> getterMethod) {
            this.getterMethod = getterMethod;
        }

        @Override
        public Function<Client, String> getter() {
            return getterMethod;
        }
    }

    /**
     * Abstraction of field access shared over all field enums.
     *
     * @param <T> type of field
     */
    public static interface FieldAccess<T> {

        /**
         * Returns a method reference to the getter for the described field.
         *
         * @return getter method reference
         */
        Function<Client, T> getter();

        /**
         * Uses the getter method to retrieve field content from given
         * {@link Client}.
         *
         * @param client client instance to retrieve field content from
         * @return field content
         */
        default T getFrom(Client client) {
            return getter().apply(client);
        }
    }

    /**
     * Returns a {@link Set} containing all fields of {@link Client} objects,
     * regardless of their type.
     *
     * @param <T> abstract type forcing elements to be both {@link Enum} and
     * {@link FieldAccess}
     * @return all {@link Client} fields
     */
    public static <T extends Enum & FieldAccess<?>> Set<T> getAllFields() {
        Set<T> all = new HashSet<>();

        all.addAll((Collection<T>) Arrays.asList(StringFields.values()));

        return all;
    }
}
