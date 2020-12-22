package org.vatplanner.dataformats.vatsimpublic.parser.json.v3;

import java.io.Reader;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFile;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileFormat;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class DataFileParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileParser.class);

    private static enum RootLevelKey implements JsonKey {
        GENERAL("general");

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

        DataFile out = new DataFile();
        out.setFormat(DataFileFormat.JSON3);

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
        } catch (JsonException | ClassCastException ex) {
            LOGGER.warn("Failed to parse JSON format on root level", ex);
        }

        return out;
    }

    // TODO: unit tests

}
