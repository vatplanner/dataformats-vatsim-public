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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vatplanner.dataformats.vatsimpublic.parser.Parser;
import org.vatplanner.dataformats.vatsimpublic.parser.ParserLogEntry;
import org.vatplanner.dataformats.vatsimpublic.utils.GeoPoint2D;
import org.vatplanner.dataformats.vatsimpublic.utils.OutOfRange;

/**
 * Parser for the <code>FIRBoundaries.dat</code> file of VAT-Spy.
 */
public class FIRBoundaryFileParser implements Parser<FIRBoundaryFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FIRBoundaryFileParser.class);

    private static final Pattern PATTERN = Pattern.compile(
        "([^|]+)\\|([01])\\|([01])\\|(\\d+)"
            + "\\|(" + SUBPATTERN_FLOATING_POINT + ")\\|(" + SUBPATTERN_FLOATING_POINT + ")" //
            + "\\|(" + SUBPATTERN_FLOATING_POINT + ")\\|(" + SUBPATTERN_FLOATING_POINT + ")" //
            + "\\|(" + SUBPATTERN_FLOATING_POINT + ")\\|(" + SUBPATTERN_FLOATING_POINT + ")" //
            + SUBPATTERN_OPTIONAL_INLINE_COMMENT //
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
            BufferedReader br = new BufferedReader(sr) //
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
        GeoPoint2DParser pointParser = new GeoPoint2DParser();

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
                            ex //
                        ));

                        // parsing could have failed due to premature end of point list
                        if (PATTERN.matcher(line).matches()) {
                            out.addParserLogEntry(new ParserLogEntry(
                                boundary.getId() + " points",
                                null,
                                false,
                                "missing " + remainingPoints + " points",
                                null //
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
                            null //
                        ));
                        continue;
                    }

                    try {
                        boundary = new FIRBoundary(
                            matcher.group(ID),
                            matcher.group(OCEANIC).equals("1"),
                            matcher.group(EXTENSION).equals("1"),
                            new GeoPoint2D(
                                Double.parseDouble(matcher.group(BOUNDS_LATITUDE_MIN)),
                                Double.parseDouble(matcher.group(BOUNDS_LONGITUDE_MIN)) //
                            ),
                            new GeoPoint2D(
                                Double.parseDouble(matcher.group(BOUNDS_LATITUDE_MAX)),
                                Double.parseDouble(matcher.group(BOUNDS_LONGITUDE_MAX)) //
                            ),
                            new GeoPoint2D(
                                Double.parseDouble(matcher.group(CENTER_LATITUDE)),
                                Double.parseDouble(matcher.group(CENTER_LONGITUDE)) //
                            ),
                            points //
                        );
                    } catch (OutOfRange ex) {
                        out.addParserLogEntry(new ParserLogEntry(
                            null,
                            line,
                            true,
                            "Out of range data in FIR " + matcher.group(ID),
                            ex
                        ));
                        continue;
                    }

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
                    null //
                ));
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
