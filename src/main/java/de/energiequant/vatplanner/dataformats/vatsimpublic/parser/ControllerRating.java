package de.energiequant.vatplanner.dataformats.vatsimpublic.parser;

import java.util.Map;
import java.util.TreeMap;

/**
 * Controller ratings are publicly visible permission levels.
 * <p>These levels can be roughly explained as follows (unofficial, see below):</p>
 * <ul>
 * <li>
 * Ratings ({@link #S1} to {@link #C3}) are earned through common ATC training.
 * </li>
 * <li>
 * Instructor ratings ({@link #I} and {@link #I3}) require extra application at
 * local VACCs and are given based on controller rating as well as other
 * criteria. These ratings are only required as permission to train other
 * controllers.
 * </li>
 * <li>
 * Supervisors ({@link #SUP}) are providing help to all members and thus have
 * additional permissions on the network. They can also be called to intervene
 * on violation of rules or conflicts.
 * </li>
 * <li>
 * Administrators ({@link #ADM}) extend Supervisor permissions. In particular,
 * this level is assigned to all members of the Board of Governors and Founders
 * as per Code of Regulations.
 * </li>
 * </ul>
 * 
 * <p>Official documents describing some of these permissions on a global level
 * (VATSIM divisions, regions and VACCs may build on these policies):</p>
 * <ul>
 * <li><a href="https://www.vatsim.net/documents/code-of-regulations">Code of Regulations</a>, §1.01G</li>
 * <li><a href="https://www.vatsim.net/documents/code-of-conduct">Code of Conduct</a></li>
 * <li><a href="https://www.vatsim.net/documents/global-ratings-policy">Global Ratings Policy</a></li>
 * </ul>
 */
public enum ControllerRating {
    /**
     * Observer is the default rating, all accounts may connect as observers.
     * This is the lowest and only rating available without having started
     * ATC training.
     */
    OBS(1),
    
    /**
     * S1 rating is described as "Ground Controller Student" by Code of
     * Regulations and "Tower Trainee" by Global Ratings Policy.
     * This is one level above {@link #OBS}.
     */
    S1(2),
    
    /**
     * S2 rating is described as "Tower" by Code of Regulations and
     * "Tower Controller" by Global Ratings Policy.
     * This is one level above {@link #S1}.
     */
    S2(3),
    
    /**
     * S3 rating is described as "TMA Controller" by both Code of Regulations
     * and Global Ratings Policy.
     * This is one level above {@link #S2}.
     */
    S3(4),
    
    /**
     * C1 rating is described as "Center" by Code of Regulations and
     * "Enroute Controller" by Global Ratings Policy.
     * This is one level above {@link #S3}.
     */
    C1(5),
    
    /**
     * C3 rating is described as "Senior Controller" by both Code of Regulations
     * and Global Ratings Policy.
     * This is one level above {@link #C1}.
     */
    C3(7),
    
    /**
    * I rating is described as "Instructor" by Code of Regulations.
    */
    I(8),
    
    /**
    * I3 rating is described as "Senior Instructor" by Code of Regulations.
    */
    I3(10),
    
    /**
    * SUP rating is described as "Supervisor" by Code of Regulations.
    */
    SUP(11),
    
    /**
    * ADM rating is described as "Administrator" by Code of Regulations.
    */
    ADM(12)
    ;
    
    private final int statusFileId;
    
    private static final Map<Integer, ControllerRating> ratingById = new TreeMap<>();
    
    static {
        ratingById.put(OBS.statusFileId, OBS);
        ratingById.put(S1.statusFileId, S1);
        ratingById.put(S2.statusFileId, S2);
        ratingById.put(S3.statusFileId, S3);
        ratingById.put(C1.statusFileId, C1);
        ratingById.put(C3.statusFileId, C3);
        ratingById.put(I.statusFileId, I);
        ratingById.put(I3.statusFileId, I3);
        ratingById.put(SUP.statusFileId, SUP);
        ratingById.put(ADM.statusFileId, ADM);
    }
    
    private ControllerRating(int statusFileId) {
        this.statusFileId = statusFileId;
    }
    
    /**
     * Resolves the given ID as used on status files (data.txt) to the
     * corresponding {@link ControllerRating} enum.
     * @param statusFileId ID as used on data.txt status file
     * @return resolved enumeration object or null if unknown
     */
    public static ControllerRating resolveStatusFileId(int statusFileId) {
        ControllerRating resolved = ratingById.get(statusFileId);
        
        if (resolved != null) {
            return resolved;
        }
        
        throw new IllegalArgumentException(String.format("unknown controller rating ID %d", statusFileId));
    }
}
