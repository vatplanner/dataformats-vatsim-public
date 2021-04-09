package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.COMMENT_PREFIX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.Parser;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;

public class VatSpyFileParser implements Parser<VatSpyFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VatSpyFileParser.class);

    private static final Pattern SECTION_PATTERN = Pattern.compile("\\[(\\S+)]");
    private static final int SECTION_NAME = 1;

    private static enum Section {
        COUNTRIES("Countries", new CountryParser(), VatSpyFile::addCountry),
        AIRPORTS("Airports", new AirportParser(), VatSpyFile::addAirport),
        FLIGHT_INFORMATION_REGIONS("FIRs", new FlightInformationRegionParser(), VatSpyFile::addFlightInformationRegion),
        UPPER_INFORMATION_REGIONS("UIRs", new UpperInformationRegionParser(), VatSpyFile::addUpperInformationRegion),
        INTERNATIONAL_DATE_LINE("IDL", new GeoPoint2DParser(), VatSpyFile::addInternationalDateLinePoint);

        static final Map<String, Section> BY_NAME = new HashMap<>();

        final String name;
        final Function<String, Object> parser;
        final BiConsumer<VatSpyFile, Object> consumer;

        static {
            for (Section section : values()) {
                BY_NAME.put(section.name, section);
            }
        }

        @SuppressWarnings("unchecked")
        <T> Section(String name, Function<String, T> parser, BiConsumer<VatSpyFile, T> consumer) {
            this.name = name;
            this.parser = (Function<String, Object>) parser;
            this.consumer = (BiConsumer<VatSpyFile, Object>) consumer;
        }

        static Section byName(String name) {
            Section section = BY_NAME.get(name);
            if (section == null) {
                throw new IllegalArgumentException("unknown section name: \"" + name + "\"");
            }
            return section;
        }

        void process(String line, VatSpyFile target) {
            consumer.accept(target, parser.apply(line));
        }
    }

    @Override
    public VatSpyFile deserialize(CharSequence s) {
        try (
            StringReader sr = new StringReader(s.toString());
            BufferedReader br = new BufferedReader(sr) //
        ) {
            return deserialize(br);
        } catch (IOException ex) {
            throw new RuntimeException("deserialization failed", ex);
        }
    }

    @Override
    public VatSpyFile deserialize(Reader reader) {
        BufferedReader br;
        if (reader instanceof BufferedReader) {
            br = (BufferedReader) reader;
        } else {
            br = new BufferedReader(reader);
        }

        VatSpyFile out = new VatSpyFile();
        Section section = null;

        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }

                Matcher sectionMatcher = SECTION_PATTERN.matcher(line);
                if (sectionMatcher.matches()) {
                    try {
                        section = Section.byName(sectionMatcher.group(SECTION_NAME));
                    } catch (IllegalArgumentException ex) {
                        section = null;
                        out.addParserLogEntry(new ParserLogEntry(
                            "section header",
                            line,
                            true,
                            "unknown section",
                            ex //
                        ));
                    }
                } else if (section == null) {
                    out.addParserLogEntry(new ParserLogEntry(
                        null,
                        line,
                        true,
                        "data outside a supported section",
                        null //
                    ));
                } else {
                    try {
                        section.process(line, out);
                    } catch (Exception ex) {
                        out.addParserLogEntry(new ParserLogEntry(
                            section.name,
                            line,
                            true,
                            ex.toString(),
                            ex //
                        ));
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("IOException while parsing", ex);
            out.addParserLogEntry(new ParserLogEntry(
                null,
                null,
                false,
                "IOException while parsing",
                ex //
            ));
        }

        return out;
    }

}
