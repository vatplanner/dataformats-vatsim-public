package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.COMMENT_PREFIX;
import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_FLOATING_POINT;
import static org.vatplanner.dataformats.vatsimpublic.parser.vatspy.Helper.SUBPATTERN_OPTIONAL_INLINE_COMMENT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.commons.exceptions.OutOfRange;
import org.vatplanner.commons.geo.GeoPoint2D;
import org.vatplanner.commons.geo.GeoPoint2DMode;
import org.vatplanner.dataformats.vatsimpublic.parser.Parser;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.utils.Optionals;

/**
 * Parser for the <code>FIRBoundaries.dat</code> file of VAT-Spy.
 */
public class FIRBoundaryFileParser implements Parser<FIRBoundaryFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FIRBoundaryFileParser.class);

    private static final Pattern PATTERN = Pattern.compile(
        "([^|]+)\\|([01])\\|([01])\\|(\\d+)"
            + "\\|(" + SUBPATTERN_FLOATING_POINT + ")\\|(" + SUBPATTERN_FLOATING_POINT + ")"
            + "\\|(" + SUBPATTERN_FLOATING_POINT + ")\\|(" + SUBPATTERN_FLOATING_POINT + ")"
            + "\\|(" + SUBPATTERN_FLOATING_POINT + ")\\|(" + SUBPATTERN_FLOATING_POINT + ")"
            + SUBPATTERN_OPTIONAL_INLINE_COMMENT
    );
    private static final int ID = 1;
    private static final int OCEANIC = 2;
    private static final int EXTENSION = 3;
    private static final int POINTS = 4;
    private static final int BOUNDS_LATITUDE_MIN = 5;
    private static final int BOUNDS_LONGITUDE_MIN = 6;
    private static final int BOUNDS_LATITUDE_MAX = 7;
    private static final int BOUNDS_LONGITUDE_MAX = 8;
    private static final int CENTER_LATITUDE = 9;
    private static final int CENTER_LONGITUDE = 10;

    @Override
    public FIRBoundaryFile deserialize(CharSequence s) {
        try (
            StringReader sr = new StringReader(s.toString());
            BufferedReader br = new BufferedReader(sr)
        ) {
            return deserialize(br);
        } catch (IOException ex) {
            throw new RuntimeException("deserialization failed", ex);
        }
    }

    @Override
    public FIRBoundaryFile deserialize(Reader reader) {
        BufferedReader br;
        if (reader instanceof BufferedReader) {
            br = (BufferedReader) reader;
        } else {
            br = new BufferedReader(reader);
        }

        int remainingPoints = 0;

        FIRBoundaryFile out = new FIRBoundaryFile();
        GeoPoint2DParser pointParser = new GeoPoint2DParser(GeoPoint2DMode.NORMALIZE);

        try {
            String line;
            FIRBoundary boundary = null;
            List<GeoPoint2D> points = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }

                if (remainingPoints > 0) {
                    try {
                        points.add(pointParser.apply(line));
                    } catch (IllegalArgumentException ex) {
                        out.addParserLogEntry(new ParserLogEntry(
                            boundary.getId() + " points",
                            line,
                            true,
                            "point could not be parsed",
                            ex
                        ));

                        // parsing could have failed due to premature end of point list
                        if (PATTERN.matcher(line).matches()) {
                            out.addParserLogEntry(new ParserLogEntry(
                                boundary.getId() + " points",
                                null,
                                false,
                                "missing " + remainingPoints + " points",
                                null
                            ));
                            remainingPoints = 0;
                        }
                    }
                }
                remainingPoints--;

                if (remainingPoints < 0) {
                    points = new ArrayList<>();
                    boundary = null;

                    Matcher matcher = PATTERN.matcher(line);
                    if (!matcher.matches()) {
                        out.addParserLogEntry(new ParserLogEntry(
                            null,
                            line,
                            true,
                            "unhandled data",
                            null
                        ));
                        continue;
                    }

                    Optional<GeoPoint2D> boundsMinimum = createPoint(
                        matcher.group(BOUNDS_LATITUDE_MIN), matcher.group(BOUNDS_LONGITUDE_MIN),
                        out::addParserLogEntry,
                        () -> "FIRBoundary header " + matcher.group(ID) + " bounds min",
                        GeoPoint2DMode.STRICT,
                        GeoPoint2DMode.NORMALIZE
                    );

                    Optional<GeoPoint2D> boundsMaximum = createPoint(
                        matcher.group(BOUNDS_LATITUDE_MAX), matcher.group(BOUNDS_LONGITUDE_MAX),
                        out::addParserLogEntry,
                        () -> "FIRBoundary header " + matcher.group(ID) + " bounds max",
                        GeoPoint2DMode.STRICT,
                        GeoPoint2DMode.NORMALIZE
                    );

                    Optional<GeoPoint2D> center = createPoint(
                        matcher.group(CENTER_LATITUDE), matcher.group(CENTER_LONGITUDE),
                        out::addParserLogEntry,
                        () -> "FIRBoundaries header " + matcher.group(ID) + " center",
                        GeoPoint2DMode.STRICT,
                        GeoPoint2DMode.NORMALIZE,
                        GeoPoint2DMode.WRAP
                    );

                    if (!Optionals.allPresent(boundsMinimum, boundsMaximum, center)) {
                        out.addParserLogEntry(new ParserLogEntry(
                            "FIRBoundaries header " + matcher.group(ID),
                            line,
                            true,
                            "at least one header coordinate for FIR " + matcher.group(ID) + " is missing, boundary will not be loaded",
                            null
                        ));
                        continue;
                    }

                    boundary = new FIRBoundary(
                        matcher.group(ID),
                        matcher.group(OCEANIC).equals("1"),
                        matcher.group(EXTENSION).equals("1"),
                        boundsMinimum.get(),
                        boundsMaximum.get(),
                        center.get(),
                        points
                    );

                    remainingPoints = Integer.parseUnsignedInt(matcher.group(POINTS));
                    out.add(boundary);
                }
            }

            if (remainingPoints > 0) {
                out.addParserLogEntry(new ParserLogEntry(
                    boundary.getId() + " points",
                    null,
                    false,
                    "missing " + remainingPoints + " points at end of file",
                    null
                ));
            }
        } catch (IOException ex) {
            LOGGER.warn("IOException while parsing", ex);
            out.addParserLogEntry(new ParserLogEntry(
                null,
                null,
                false,
                "IOException while parsing",
                ex
            ));
        }

        return out;
    }

    /**
     * Tries to create a {@link GeoPoint2D} from given coordinates using all {@link GeoPoint2DMode}s in order.
     * The first mode is expected to be the normal case. All further modes are fallbacks and will cause log messages
     * even if the line can finally be accepted. If all attempts at creating a {@link GeoPoint2D} fail, the line will be
     * logged as rejected and an empty {@link Optional} will be returned.
     *
     * @param latitude     latitude to be processed
     * @param longitude    longitude to be processed
     * @param logCollector receives a {@link ParserLogEntry} in case of a processing warning or error
     * @param logSection   must provide the section name to be used to identify the {@link ParserLogEntry}
     * @param modes        allowed modes, in descending order of preference; first mode is expected normal case
     * @return point if any of the modes succeeded; empty if not
     */
    private Optional<GeoPoint2D> createPoint(String latitude, String longitude, Consumer<ParserLogEntry> logCollector, Supplier<String> logSection, GeoPoint2DMode... modes) {
        ParserLogEntry logEntry = null;
        GeoPoint2D point = null;

        for (int i = 0; i < modes.length; i++) {
            GeoPoint2DMode mode = modes[i];
            boolean isLastAttempt = (i >= modes.length - 1);

            try {
                point = mode.createPoint(latitude, longitude);
                break;
            } catch (OutOfRange ex) {
                logEntry = new ParserLogEntry(
                    logSection.get(),
                    null,
                    isLastAttempt,
                    ex.getMessage(),
                    ex
                );
            }
        }

        if (logEntry != null) {
            logCollector.accept(logEntry);
        }

        return Optional.ofNullable(point);
    }
}
