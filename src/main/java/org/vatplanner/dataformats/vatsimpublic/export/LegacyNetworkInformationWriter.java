package org.vatplanner.dataformats.vatsimpublic.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.DataFileFormat;
import org.vatplanner.dataformats.vatsimpublic.parser.NetworkInformation;

/**
 * Provides serialization for a {@link NetworkInformation} object back to the
 * legacy key-value plain-text format.
 */
public class LegacyNetworkInformationWriter implements Writer<NetworkInformation> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyNetworkInformationWriter.class);

    private static final String LINE_END = "\n";
    private static final char SEPARATOR = '=';

    private final String header;

    public LegacyNetworkInformationWriter(String header) {
        this.header = header;
    }

    @Override
    public void serialize(NetworkInformation content, OutputStream os) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.ISO_8859_1);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.append(header);
            bw.append(LINE_END);

            bw.append(";");
            bw.append(LINE_END);
            bw.append(";");
            bw.append(LINE_END);

            bw.append(content.getWhazzUpString());
            bw.append(LINE_END);

            encodeAll("msg0", content.getStartupMessages(), bw);
            encodeAllUrls("url0", content.getDataFileUrls(DataFileFormat.LEGACY), bw);
            encodeAllUrls("json3", content.getDataFileUrls(DataFileFormat.JSON3), bw);
            encodeAllUrls("url1", content.getServersFileUrls(), bw);
            encodeAllUrls("moveto0", content.getMovedToUrls(), bw);
            encodeAllUrls("metar0", content.getMetarUrls(), bw);
            encodeAllUrls("user0", content.getUserStatisticsUrls(), bw);

            bw.append(";");
            bw.append(LINE_END);

            bw.append("; END");
            bw.append(LINE_END);

            bw.flush();
            osw.flush();
        } catch (Exception ex) {
            LOGGER.warn("Serialization of NetworkInformation failed", ex);
        }
    }

    private void encodeAll(String key, List<String> values, BufferedWriter bw) throws IOException {
        if (values.isEmpty()) {
            return;
        }

        bw.append(";");
        bw.append(LINE_END);

        for (String value : values) {
            bw.append(key);
            bw.append(SEPARATOR);
            bw.append(value);
            bw.append(LINE_END);
        }
    }

    private void encodeAllUrls(String key, List<URL> urls, BufferedWriter bw) throws IOException {
        encodeAll(key, urls.stream().map(URL::toString).collect(Collectors.toList()), bw);
    }

}
