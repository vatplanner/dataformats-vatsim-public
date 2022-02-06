package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import static java.lang.Integer.parseUnsignedInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserHelpers;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntryCollector;
import org.vatplanner.dataformats.vatsimpublic.parser.json.JsonHelpers;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * Processes controller and ATIS information in JSON format.
 * 
 * <p>
 * Except for {@link Key#ATIS_DESIGNATOR} <code>controllers</code> and
 * <code>atis</code> sections share the same fields so one processor
 * implementation can fit both types of objects. The exact {@link ClientType} to
 * be processed (either {@link ClientType#ATC_CONNECTED} or
 * {@link ClientType#ATIS}) is decided at construction time.
 * </p>
 */
public class ControllerAtisJsonProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAtisJsonProcessor.class);

    static final String SECTION_NAME_CONTROLLERS = "controllers";
    static final String SECTION_NAME_ATIS = "atis";
    private static final String LINE_SEPARATOR = "\n";

    private final String sectionName;
    private final ClientType clientType;
    private final Map<Integer, FacilityType> facilityTypeByJsonId;
    private final Map<Integer, ControllerRating> controllerRatingByJsonId;

    private static enum Key implements JsonKey {
        VATSIM_ID("cid"),
        REAL_NAME("name"),
        CALLSIGN("callsign"),
        FREQUENCY("frequency"),
        FACILITY_TYPE("facility"),
        CONTROLLER_RATING("rating"),
        SERVER_ID("server"),
        VISUAL_RANGE("visual_range"),
        ATIS_DESIGNATOR("atis_code"),
        CONTROLLER_MESSAGE("text_atis"),
        LAST_UPDATED("last_updated"),
        LOGON_TIME("logon_time");

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

    public ControllerAtisJsonProcessor(ClientType clientType, Map<Integer, FacilityType> facilityTypeByJsonId, Map<Integer, ControllerRating> controllerRatingByJsonId) {
        this.clientType = clientType;
        this.facilityTypeByJsonId = facilityTypeByJsonId;
        this.controllerRatingByJsonId = controllerRatingByJsonId;

        switch (clientType) {
            case ATC_CONNECTED:
                sectionName = SECTION_NAME_CONTROLLERS;
                break;

            case ATIS:
                sectionName = SECTION_NAME_ATIS;
                break;

            default:
                throw new IllegalArgumentException("Unsupported client type: " + clientType);
        }
    }

    public List<Client> deserializeMultiple(JsonArray array, ParserLogEntryCollector logCollector) {
        return JsonHelpers.processArraySkipOnError(//
            array, //
            JsonObject.class, //
            sectionName, //
            logCollector, //
            x -> deserializeSingle(x, logCollector) //
        );
    }

    public Client deserializeSingle(JsonObject object, ParserLogEntryCollector logCollector) {
        Client out = new Client();

        out.setRawClientType(clientType);
        out.setEffectiveClientType(clientType);

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.VATSIM_ID, //
            sectionName, //
            logCollector, //
            out::setVatsimID //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.REAL_NAME, //
            sectionName, //
            logCollector, //
            out::setRealName //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.CALLSIGN, //
            sectionName, //
            logCollector, //
            out::setCallsign //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.FREQUENCY, //
            sectionName, //
            logCollector, //
            this::parseFrequency //
        ).ifPresent(out::setServedFrequencyKilohertz);

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.FACILITY_TYPE, //
            sectionName, //
            logCollector, //
            facilityTypeByJsonId::get //
        ).ifPresent(out::setFacilityType);

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.CONTROLLER_RATING, //
            sectionName, //
            logCollector, //
            controllerRatingByJsonId::get //
        ).ifPresent(out::setControllerRating);

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.SERVER_ID, //
            sectionName, //
            logCollector, //
            out::setServerId //
        );

        JsonHelpers.processMandatory( //
            object::getInteger, //
            Key.VISUAL_RANGE, //
            sectionName, //
            logCollector, //
            out::setVisualRange //
        );

        // NOTE: According to spec text_atis (CONTROLLER_MESSAGE) should be mandatory
        // but it actually is null if no such information is present.
        out.setControllerMessage(
            JsonHelpers.processOptional( //
                object::getCollection, //
                Key.CONTROLLER_MESSAGE, //
                JsonArray.class, //
                sectionName, //
                logCollector, //
                (Function<JsonArray, String>) x -> concatArrayOfStrings(x, logCollector) //
            ).orElse("") //
        );

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.LAST_UPDATED, //
            sectionName, //
            logCollector, //
            ParserHelpers::parseToInstantUtc //
        ).ifPresent(out::setLastUpdated);

        JsonHelpers.processMandatory( //
            object::getString, //
            Key.LOGON_TIME, //
            sectionName, //
            logCollector, //
            ParserHelpers::parseToInstantUtc //
        ).ifPresent(out::setLogonTime);

        if (clientType == ClientType.ATIS) {
            JsonHelpers.processOptional( //
                object::getString, //
                Key.ATIS_DESIGNATOR, //
                sectionName, //
                logCollector, //
                out::setAtisDesignator //
            );
        }

        return out;
    }

    private int parseFrequency(String s) {
        int decimalPointIndex = s.indexOf('.');

        if (decimalPointIndex < 0) {
            throw new IllegalArgumentException("Frequency cannot be converted: \"" + s + "\"");
        }

        int mhzPart = parseUnsignedInt(s.substring(0, decimalPointIndex));
        int khzPart = parseUnsignedInt(s.substring(decimalPointIndex + 1));

        return mhzPart * 1000 + khzPart;
    }

    private String concatArrayOfStrings(JsonArray arr, ParserLogEntryCollector logCollector) {
        List<String> lines = new ArrayList<String>();
        JsonHelpers.processArrayFailEmptyOnError( //
            arr, //
            String.class, //
            sectionName, //
            logCollector, //
            lines::add //
        );

        return String.join(LINE_SEPARATOR, lines);
    }

    // TODO: unit tests
}
