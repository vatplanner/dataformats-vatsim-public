package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class PilotJsonProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PilotJsonProcessor.class);

    static final String SECTION_NAME = "pilots";

    private final FlightPlanJsonProcessor flightPlanProcessor;
    private final Map<Integer, PilotRating> pilotRatingByJsonId;

    private static enum Key implements JsonKey {
        VATSIM_ID("cid"),
        REAL_NAME("name"),
        PILOT_RATING("pilot_rating"),
        CALLSIGN("callsign"),
        SERVER_ID("server"),
        LATITUDE("latitude"),
        LONGITUDE("longitude"),
        ALTITUDE("altitude"),
        GROUND_SPEED("groundspeed"),
        TRANSPONDER("transponder"),
        HEADING("heading"),
        QNH_INCH_MERCURY("qnh_i_hg"),
        QNH_HECTOPASCAL("qnh_mb"),
        FLIGHT_PLAN("flight_plan"),
        LOGON_TIME("logon_time"),
        LAST_UPDATED("last_updated");

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

    public PilotJsonProcessor(FlightPlanJsonProcessor flightPlanProcessor, Map<Integer, PilotRating> pilotRatingByJsonId) {
        this.flightPlanProcessor = flightPlanProcessor;
        this.pilotRatingByJsonId = pilotRatingByJsonId;
    }

    public List<Client> deserializeMultiple(JsonArray array, ParserLogEntryCollector logCollector) {
        return JsonHelpers.processArraySkipOnError(//
            array, //
            JsonObject.class, //
            SECTION_NAME, //
            logCollector, //
            x -> deserializeSingle(x, logCollector) //
        );
    }

    public Client deserializeSingle(JsonObject object, ParserLogEntryCollector logCollector) {
        Client out = new Client();

        out.setRawClientType(ClientType.PILOT_CONNECTED);
        out.setEffectiveClientType(ClientType.PILOT_CONNECTED);

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.VATSIM_ID, //
            SECTION_NAME, //
            logCollector, //
            out::setVatsimID //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.CALLSIGN, //
            SECTION_NAME, //
            logCollector, //
            out::setCallsign //
        );

        String location = SECTION_NAME + " " + out.getVatsimID() + " " + out.getCallsign();

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.REAL_NAME, //
            location, //
            logCollector, //
            out::setRealName //
        );

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.PILOT_RATING, //
            location, //
            logCollector, //
            pilotRatingByJsonId::get //
        ).ifPresent(out::setPilotRating);

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.SERVER_ID, //
            location, //
            logCollector, //
            out::setServerId //
        );

        JsonHelpers.processMandatory( //
            object::getDouble, //
            Key.LATITUDE, //
            location, //
            logCollector, //
            out::setLatitude //
        );

        JsonHelpers.processMandatory( //
            object::getDouble, //
            Key.LONGITUDE, //
            location, //
            logCollector, //
            out::setLongitude //
        );

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.ALTITUDE, //
            location, //
            logCollector, //
            out::setAltitudeFeet //
        );

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.GROUND_SPEED, //
            location, //
            logCollector, //
            out::setGroundSpeed //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.TRANSPONDER, //
            location, //
            logCollector, //
            (Function<String, Integer>) Integer::parseUnsignedInt //
        ).ifPresent(out::setTransponderCodeDecimal);

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.HEADING, //
            location, //
            logCollector, //
            this::limitHeading //
        ).ifPresent(out::setHeading);

        JsonHelpers.processMandatory( //
            object::getDouble, //
            Key.QNH_INCH_MERCURY, //
            location, //
            logCollector, //
            out::setQnhInchMercury //
        );

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.QNH_HECTOPASCAL, //
            location, //
            logCollector, //
            out::setQnhHectopascal //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.LOGON_TIME, //
            location, //
            logCollector, //
            Instant::parse //
        ).ifPresent(out::setLogonTime);

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.LAST_UPDATED, //
            location, //
            logCollector, //
            Instant::parse //
        ).ifPresent(out::setLastUpdated);

        JsonHelpers.processOptional( //
            object::getMap, //
            Key.FLIGHT_PLAN, //
            JsonObject.class, //
            location, //
            logCollector, //
            (Consumer<JsonObject>) x -> flightPlanProcessor.deserializeSingle(x, out, location, logCollector) //
        );

        return out;
    }

    private int limitHeading(int original) {
        if (original == 360) {
            return 0;
        } else if (original >= 0 && original <= 359) {
            return original;
        }

        throw new IllegalArgumentException("heading is out of range: " + original);
    }

    // TODO: unit tests
}
