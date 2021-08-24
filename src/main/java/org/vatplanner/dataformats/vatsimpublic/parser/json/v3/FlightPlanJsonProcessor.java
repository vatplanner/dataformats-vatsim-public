package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.time.Duration;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserHelpers;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.json.JsonHelpers;

import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class FlightPlanJsonProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightPlanJsonProcessor.class);

    private static enum Key implements JsonKey {
        FLIGHT_PLAN_TYPE("flight_rules"),
        AIRCRAFT_TYPE("aircraft"),
        AIRCRAFT_TYPE_FAA("aircraft_faa"),
        AIRCRAFT_TYPE_SHORT("aircraft_short"),
        DEPARTURE_AIRPORT_CODE("departure"),
        DESTINATION_AIRPORT_CODE("arrival"),
        ALTERNATE_AIRPORT_CODE("alternate"),
        TRUE_AIR_SPEED("cruise_tas"),
        ALTITUDE("altitude"),
        DEPARTURE_TIME("deptime"),
        TIME_ENROUTE("enroute_time"),
        TIME_FUEL("fuel_time"),
        REMARKS("remarks"),
        ROUTE("route");

        private final String key;

        private Key(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return null;
        }
    }

    public void deserializeSingle(JsonObject object, Client target, String sectionName, ParserLogEntryCollector logCollector) {
        JsonHelpers.processMandatory( //
            object::getString, //
            Key.FLIGHT_PLAN_TYPE, //
            sectionName, //
            logCollector, //
            target::setRawFlightPlanType //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.AIRCRAFT_TYPE, //
            sectionName, //
            logCollector, //
            target::setAircraftType //
        );

        JsonHelpers.processOptional( //
            object::getString, //
            Key.AIRCRAFT_TYPE_FAA, //
            sectionName, //
            logCollector, //
            target::setAircraftTypeFaa //
        );

        JsonHelpers.processOptional( //
            object::getString, //
            Key.AIRCRAFT_TYPE_SHORT, //
            sectionName, //
            logCollector, //
            target::setAircraftTypeShort //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.DEPARTURE_AIRPORT_CODE, //
            sectionName, //
            logCollector, //
            target::setFiledDepartureAirportCode //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.DESTINATION_AIRPORT_CODE, //
            sectionName, //
            logCollector, //
            target::setFiledDestinationAirportCode //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.ALTERNATE_AIRPORT_CODE, //
            sectionName, //
            logCollector, //
            target::setFiledAlternateAirportCode //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.TRUE_AIR_SPEED, //
            sectionName, //
            logCollector, //
            (Function<String, Integer>) Integer::parseUnsignedInt //
        ).ifPresent(target::setFiledTrueAirSpeed);

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.ALTITUDE, //
            sectionName, //
            logCollector, //
            target::setRawFiledAltitude //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.DEPARTURE_TIME, //
            sectionName, //
            logCollector, //
            (Function<String, Integer>) Integer::parseInt //
        ).ifPresent(target::setRawDepartureTimePlanned);

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.TIME_ENROUTE, //
            sectionName, //
            logCollector, //
            (Function<String, Duration>) x -> ParserHelpers.parseDirectConcatenatedDuration(x, true) //
        ).ifPresent(target::setFiledTimeEnroute);

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.TIME_FUEL, //
            sectionName, //
            logCollector, //
            (Function<String, Duration>) x -> ParserHelpers.parseDirectConcatenatedDuration(x, true) //
        ).ifPresent(target::setFiledTimeFuel);

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.REMARKS, //
            sectionName, //
            logCollector, //
            target::setFlightPlanRemarks //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.ROUTE, //
            sectionName, //
            logCollector, //
            target::setFiledRoute //
        );
    }

    // TODO: unit tests
}
