package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserHelpers;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.json.JsonHelpers;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

public class PrefileJsonProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrefileJsonProcessor.class);

    static final String SECTION_NAME = "prefiles";

    private final FlightPlanJsonProcessor flightPlanProcessor;

    private static enum Key implements JsonKey {
        VATSIM_ID("cid"),
        REAL_NAME("name"),
        CALLSIGN("callsign"),
        FLIGHT_PLAN("flight_plan"),
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

    public PrefileJsonProcessor(FlightPlanJsonProcessor flightPlanProcessor) {
        this.flightPlanProcessor = flightPlanProcessor;
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

        out.setRawClientType(ClientType.PILOT_PREFILED);
        out.setEffectiveClientType(ClientType.PILOT_PREFILED);

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
            object::getString, //
            Key.LAST_UPDATED, //
            location, //
            logCollector, //
            ParserHelpers::parseToInstantUtc //
        ).ifPresent(out::setLastUpdated);

        JsonHelpers.processMandatory( //
            object::getMap, //
            Key.FLIGHT_PLAN, //
            JsonObject.class, //
            location, //
            logCollector, //
            (Consumer<JsonObject>) x -> flightPlanProcessor.deserializeSingle(x, out, location, logCollector) //
        );

        return out;
    }

    // TODO: unit tests
}
