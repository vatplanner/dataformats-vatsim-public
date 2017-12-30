package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.time.Duration;
import java.time.Instant;

/**
 * Combines information about VATSIM online pilots, prefiled flight plans and
 * online ATC stations, distinguished by {@link #clientType} as read from
 * data.txt status file.
 * <p>Combination into a single class follows the original record format on
 * data.txt status files which is identical for all types although some
 * fields will (and can) never be set:</p>
 * <ul>
 * <li>ATC only:
 * <ul>
 * <li>{@link #facilityType}</li>
 * <li>{@link #frequencyKilohertz}</li>
 * <li>{@link #lastMessageUpdate}</li>
 * <li>{@link #message}</li>
 * <li>{@link #rating}</li>
 * <li>{@link #visualRange}</li>
 * </ul>
 * </li>
 * <li>Pilot and prefiled flight plans only:
 * <ul>
 * <li>{@link #aircraftType}</li>
 * <li>{@link #alternateAirportCode}</li>
 * <li>{@link #altitude} (always 0 for ATCs)</li>
 * <li>{@link #departureAirportCode}</li>
 * <li>{@link #departureTimePlanned}</li>
 * <li>{@link #departureTimeActual}</li>
 * <li>{@link #destinationAirportCode}</li>
 * <li>{@link #filedAltitude}</li>
 * <li>{@link #filedTimeEnroute}</li>
 * <li>{@link #filedTimeFuel}</li>
 * <li>{@link #filedTrueAirSpeed}</li>
 * <li>{@link #fileRevision}</li>
 * <li>{@link #flightPlanType}</li>
 * <li>{@link #groundSpeed}</li>
 * <li>{@link #heading}</li>
 * <li>{@link #qnhHectopascal}</li>
 * <li>{@link #qnhInchMercury}</li>
 * <li>{@link #remarks}</li>
 * <li>{@link #route}</li>
 * </ul>
 * </li>
 * <li>Not seen being used in the wild but still defined by format:
 * <ul>
 * <li>{@link #departureAirportLatitude}</li>
 * <li>{@link #departureAirportLongitude}</li>
 * <li>{@link #destinationAirportLatitude}</li>
 * <li>{@link #destinationAirportLongitude}</li>
 * </ul>
 * </li>
 * </ul>
 * <p>Some fields allow for further interpretation or have a special format:</p>
 * <ul>
 * <li>{@link #aircraftType}: ICAO aircraft type code. Should include equipment code as suffix, may include wake category as prefix (examples: B738/M, H/A332/X, B737). Not reliable as this is an informal free-text field and sometimes contains alternate/IATA codes or common mistakes (such as B77W for a Boeing 777 which is neither a valid ICAO nor IATA code).</li>
 * <li>{@link #callsign}: Callsigns can be chosen freely by pilots although some codes may be reserved for virtual airlines by convention. Callsigns on VATSIM omit hyphens which would be used in the real-world to separate country prefixes for plane registrations (e.g. all non-airline flights).</li>
 * <li>{@link #fileRevision}: Flights passing through online controlled airspace will usually see many revisions of their original flight plan (mostly {@link #route}) as edited by ATC when the plane changes airspace or an initial clearance is given by departure airport. Flight plan revisions are tracked by this counter.
 * <li>{@link #message}: Multi-line string containing ATIS message for ATIS stations, otherwise general remarks about ATC stations such as contact information, controller's estimated online times or station's spatial coverage. May contain a URL to the voice room on first line if prefixed with "$ ". Update timestamps are provided by {@link #lastMessageUpdate}.</li>
 * <li>{@link #realName}: By convention, pilots should add a 4-letter ICAO code for their "home base". Pilots often choose the closest airport to their actual home.</li>
 * <li>{@link #remarks}: Pilot clients add voice capability flags (T = Text only; R = Receive voice, send text; V = full voice). Other than that, pilots are free to enter any remarks they may find useful. Pilots sometimes attach full ICAO field 18 information which provides highly detailed information generally not needed for simulation (for example PBN/..., DOF/... etc.).</li>
 * </ul>
 */
public class Client {
    // callsign:cid:realname:clienttype:
    // frequency:latitude:longitude:altitude:groundspeed:planned_aircraft:planned_tascruise:
    // planned_depairport:planned_altitude:planned_destairport:server:protrevision:rating:transponder:facilitytype:visualrange:planned_revision:planned_flighttype
    // planned_deptime:planned_actdeptime:planned_hrsenroute:planned_minenroute:planned_hrsfuel:planned_minfuel:planned_altairport:planned_remarks
    // planned_route:planned_depairport_lat:planned_depairport_lon:planned_destairport_lat:planned_destairport_lon:
    // atis_message:time_last_atis_received:time_logon:heading:QNH_iHg:QNH_Mb:
    private String callsign; // also on prefiling
    private int vatsimID; // also on prefiling
    private String realName; // may include home base for pilots; also on prefiling
    private ClientType clientType;
    private int frequencyKilohertz; // ATC only
    private double latitude;
    private double longitude;
    private int altitude;
    private int groundSpeed;
    
    // filing
    private String aircraftType; // B738/M, H/A332/X, B737
    private int filedTrueAirSpeed;
    private String departureAirportCode;
    private int filedAltitude;
    private String destinationAirportCode;
    
    // actual data
    private String serverId;
    private int protocolVersion;
    private int rating; // decode
    private int transponderCode;
    private int facilityType; // decode
    private int visualRange; // nm?
    
    // filing
    private int fileRevision;
    private String flightPlanType; // I = IFR, V = VFR
    private Instant departureTimePlanned; // may be 0; values can omit leading zeros!
    private Instant departureTimeActual; // may be 0, may be equal, may be actual value - who or what sets this? Values can omit leading zeros!
    private Duration filedTimeEnroute; // data: two fields, hours + minutes
    private Duration filedTimeFuel; // data: two fields, hours + minutes
    private String alternateAirportCode;
    private String remarks;
    private String route;
    private double departureAirportLatitude; // seems unused, always 0
    private double departureAirportLongitude; // seems unused, always 0
    private double destinationAirportLatitude; // seems unused, always 0
    private double destinationAirportLongitude; // seems unused, always 0
    
    // ATC only
    private String message; // decode "atis_message": first line prefixed "$ " => voice URL; multi-line formatting with "^" and special character as CR LF?
    private Instant lastMessageUpdate; // time_last_atis_received
    
    // all connected
    private Instant logonTime;
    
    // Pilots only
    private int heading;
    private double qnhInchMercury;
    private double qnhHectopascal;
    
    // TODO: generate getters & setters
}
