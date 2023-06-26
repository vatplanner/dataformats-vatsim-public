package org.vatplanner.dataformats.vatsimpublic.parser.json.onlinetransceivers;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.OnlineTransceiversFile;
import org.vatplanner.dataformats.vatsimpublic.parser.Parser;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.Jsoner;

public class OnlineTransceiversFileProcessor implements Parser<OnlineTransceiversFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineTransceiversFileProcessor.class);

    @Override
    public OnlineTransceiversFile deserialize(Reader reader) {
        OnlineTransceiverStationProcessor stationProcessor = new OnlineTransceiverStationProcessor();

        OnlineTransceiversFile out = new OnlineTransceiversFile();

        out.setFormat(OnlineTransceiversFile.Format.INITIAL);

        try {
            JsonArray root = (JsonArray) Jsoner.deserialize(reader);

            out.setStations(stationProcessor.deserializeMultiple(root, out));
        } catch (JsonException | ClassCastException ex) {
            LOGGER.warn("Failed to parse JSON format on root level", ex);

            out.addParserLogEntry(
                new ParserLogEntry("root", null, true, "Failed to parse JSON format on root level", ex)
            );
        }

        return out;
    }

    @Override
    public OnlineTransceiversFile deserialize(CharSequence s) {
        try (Reader reader = new StringReader(s.toString())) {
            return deserialize(reader);
        } catch (IOException ex) {
            throw new RuntimeException("deserialization failed", ex);
        }
    }

    // TODO: unit tests
}
