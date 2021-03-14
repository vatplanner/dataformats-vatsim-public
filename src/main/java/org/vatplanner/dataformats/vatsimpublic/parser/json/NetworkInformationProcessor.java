package org.vatplanner.dataformats.vatsimpublic.parser.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.NetworkInformation;
import org.vatplanner.dataformats.vatsimpublic.parser.Parser;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

/**
 * Parses status.json which defines meta information such as reference URLs to
 * fetch other information from.
 */
public class NetworkInformationProcessor implements Parser<NetworkInformation> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkInformationProcessor.class);

    private static enum RootLevelKey implements JsonKey {
        DATA_FILES("data"),
        METAR("metar"),
        USER_STATISTICS("user");

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

    @Override
    public NetworkInformation deserialize(CharSequence s) {
        try (Reader reader = new StringReader(s.toString())) {
            return deserialize(reader);
        } catch (IOException ex) {
            throw new RuntimeException("deserialization failed", ex);
        }
    }

    @Override
    public NetworkInformation deserialize(Reader reader) {
        NetworkInformation out = new NetworkInformation();

        try {
            JsonObject root = (JsonObject) Jsoner.deserialize(reader);

            JsonHelpers.processMandatoryUnsafe( //
                root::getMap, //
                RootLevelKey.DATA_FILES, //
                (Consumer<Map<String, JsonArray>>) x -> addAllAsStringsToMapping(
                    x,
                    out::addAsDataFileUrl //
                ) //
            );

            JsonHelpers.processMandatoryUnsafe( //
                root::getCollection, //
                RootLevelKey.METAR, //
                (Consumer<JsonArray>) x -> addAllAsStringsToMapping(
                    x,
                    out::addAsUrl,
                    NetworkInformation.PARAMETER_KEY_URL_METAR //
                ) //
            );

            JsonHelpers.processMandatoryUnsafe( //
                root::getCollection, //
                RootLevelKey.USER_STATISTICS, //
                (Consumer<JsonArray>) x -> addAllAsStringsToMapping(
                    x,
                    out::addAsUrl,
                    NetworkInformation.PARAMETER_KEY_URL_USER_STATISTICS //
                ) //
            );
        } catch (JsonException | ClassCastException ex) {
            LOGGER.warn("Failed to parse JSON format on root level", ex);
        }

        return out;
    }

    private void addAllAsStringsToMapping(JsonArray arr, BiConsumer<String, String> mappingConsumer, String key) {
        for (Object value : arr) {
            mappingConsumer.accept(key, (String) value);
        }
    }

    private void addAllAsStringsToMapping(Map<String, JsonArray> map, BiConsumer<String, String> mappingConsumer) {
        for (Map.Entry<String, JsonArray> entry : map.entrySet()) {
            addAllAsStringsToMapping(
                entry.getValue(),
                mappingConsumer,
                entry.getKey() //
            );
        }
    }
}
