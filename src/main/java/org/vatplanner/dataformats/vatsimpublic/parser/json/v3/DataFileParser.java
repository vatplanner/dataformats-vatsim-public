package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.entities.status.ControllerRating;
import org.vatplanner.dataformats.vatsimpublic.entities.status.FacilityType;
import org.vatplanner.dataformats.vatsimpublic.entities.status.PilotRating;
import org.vatplanner.dataformats.vatsimpublic.parser.Client;
import org.vatplanner.dataformats.vatsimpublic.parser.ClientType;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileFormat;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class DataFileParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileParser.class);

    private static enum RootLevelKey implements JsonKey {
        GENERAL("general"),
        SERVERS("servers"),
        RATINGS("ratings"),
        PILOT_RATINGS("pilot_ratings"),
        FACILITIES("facilities"),
        ATIS("atis"),
        CONTROLLERS("controllers"),
        PILOTS("pilots"),
        PREFILES("prefiles");

        private final String key;

        private RootLevelKey(String key) {
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

    public DataFile parse(Reader br) {
        GeneralSectionJsonProcessor generalSectionProcessor = new GeneralSectionJsonProcessor();
        FSDServerJsonProcessor fsdServerProcessor = new FSDServerJsonProcessor();
        IdNameMappingProcessor shortKeyIdNameMappingProcessor = new IdNameMappingProcessor(
            IdNameMappingProcessor.JsonKeys.shortKeys() //
        );
        IdNameMappingProcessor longKeyIdNameMappingProcessor = new IdNameMappingProcessor(
            IdNameMappingProcessor.JsonKeys.longKeys() //
        );
        FlightPlanJsonProcessor flightPlanProcessor = new FlightPlanJsonProcessor();
        PrefileJsonProcessor prefileProcessor = new PrefileJsonProcessor(flightPlanProcessor);

        DataFile out = new DataFile();
        out.setFormat(DataFileFormat.JSON3);
        out.setVoiceServers(new ArrayList<>());

        try {
            JsonObject root = (JsonObject) Jsoner.deserialize(br);

            JsonHelpers.processMandatory( //
                root::getMap, //
                RootLevelKey.GENERAL, //
                JsonObject.class, //
                GeneralSectionJsonProcessor.SECTION_NAME, //
                out, //
                (Consumer<JsonObject>) x -> out.setMetaData(generalSectionProcessor.deserialize(x, out)) //
            );

            JsonHelpers.processMandatory( //
                root::getCollection, //
                RootLevelKey.SERVERS, //
                JsonArray.class, //
                FSDServerJsonProcessor.SECTION_NAME, //
                out, //
                (Consumer<JsonArray>) x -> out.setFsdServers(fsdServerProcessor.deserializeMultiple(x, out)) //
            );

            Map<Integer, FacilityType> facilityTypeByJsonId = JsonHelpers.processMandatory( //
                root::getCollection, //
                RootLevelKey.FACILITIES, //
                JsonArray.class, //
                RootLevelKey.FACILITIES.getKey(), //
                out, //
                (Function<JsonArray, Map<Integer, FacilityType>>) x -> shortKeyIdNameMappingProcessor
                    .deserializeMappingFromJsonId( //
                        x, //
                        FacilityType::resolveShortName, //
                        FacilityType.values(), //
                        RootLevelKey.FACILITIES.getKey(), //
                        out //
                    ) //
            ).orElse(new HashMap<Integer, FacilityType>());

            Map<Integer, ControllerRating> controllerRatingByJsonId = JsonHelpers.processMandatory( //
                root::getCollection, //
                RootLevelKey.RATINGS, //
                JsonArray.class, //
                RootLevelKey.RATINGS.getKey(), //
                out, //
                (Function<JsonArray, Map<Integer, ControllerRating>>) x -> shortKeyIdNameMappingProcessor
                    .deserializeMappingFromJsonId( //
                        x, //
                        ControllerRating::resolveShortName, //
                        ControllerRating.values(), //
                        RootLevelKey.RATINGS.getKey(), //
                        out //
                    ) //
            ).orElse(new HashMap<Integer, ControllerRating>());

            Map<Integer, PilotRating> pilotRatingByJsonId = JsonHelpers.processMandatory( //
                root::getCollection, //
                RootLevelKey.PILOT_RATINGS, //
                JsonArray.class, //
                RootLevelKey.PILOT_RATINGS.getKey(), //
                out, //
                (Function<JsonArray, Map<Integer, PilotRating>>) x -> longKeyIdNameMappingProcessor
                    .deserializeMappingFromJsonId( //
                        x, //
                        PilotRating::resolveShortName, //
                        PilotRating.values(), //
                        RootLevelKey.PILOT_RATINGS.getKey(), //
                        out //
                    ) //
            ).orElse(new HashMap<Integer, PilotRating>());

            ArrayList<Client> clients = new ArrayList<Client>();
            ControllerAtisJsonProcessor atisProcessor = new ControllerAtisJsonProcessor(ClientType.ATIS,
                facilityTypeByJsonId, controllerRatingByJsonId);
            ControllerAtisJsonProcessor controllerProcessor = new ControllerAtisJsonProcessor(ClientType.ATC_CONNECTED,
                facilityTypeByJsonId, controllerRatingByJsonId);
            PilotJsonProcessor pilotProcessor = new PilotJsonProcessor(flightPlanProcessor, pilotRatingByJsonId);

            JsonHelpers.processMandatory( //
                root::getCollection, //
                RootLevelKey.ATIS, //
                JsonArray.class, //
                ControllerAtisJsonProcessor.SECTION_NAME_ATIS, //
                out, //
                (Consumer<JsonArray>) x -> clients.addAll(atisProcessor.deserializeMultiple(x, out)) //
            );

            JsonHelpers.processMandatory( //
                root::getCollection, //
                RootLevelKey.CONTROLLERS, //
                JsonArray.class, //
                ControllerAtisJsonProcessor.SECTION_NAME_CONTROLLERS, //
                out, //
                (Consumer<JsonArray>) x -> clients.addAll(controllerProcessor.deserializeMultiple(x, out)) //
            );

            JsonHelpers.processMandatory( //
                root::getCollection, //
                RootLevelKey.PILOTS, //
                JsonArray.class, //
                PilotJsonProcessor.SECTION_NAME, //
                out, //
                (Consumer<JsonArray>) x -> clients.addAll(pilotProcessor.deserializeMultiple(x, out)) //
            );

            JsonHelpers.processMandatory( //
                root::getCollection, //
                RootLevelKey.PREFILES, //
                JsonArray.class, //
                PrefileJsonProcessor.SECTION_NAME, //
                out, //
                (Consumer<JsonArray>) x -> clients.addAll(prefileProcessor.deserializeMultiple(x, out)) //
            );

            out.setClients(clients);
        } catch (JsonException | ClassCastException ex) {
            LOGGER.warn("Failed to parse JSON format on root level", ex);
        }

        return out;
    }

    // TODO: unit tests

}
