package org.vatplanner.dataformats.vatsimpublic.graph;

import java.time.Instant;
import java.util.Collection;
import static java.util.Collections.unmodifiableCollection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Member;
import org.vatplanner.dataformats.vatsimpublic.entities.status.Report;

/**
 * Keeps track of unique entities and time-related indexes needed during graph
 * import.
 */
public class GraphIndex {

    private final Map<Integer, Member> membersByVatsimId = new HashMap<>();
    private final NavigableMap<Instant, Report> reportsByRecordTime = new TreeMap<>();

    /**
     * Adds a new member to the index. Members are uniquely identified by VATSIM
     * ID, so two different members must not have the same ID.
     *
     * @param member member to add; unique by VATSIM ID
     */
    public void add(Member member) {
        // TODO: fail if VATSIM ID already registered with a different instance
        membersByVatsimId.put(member.getVatsimId(), member);
    }

    /**
     * Adds a new report to the index. Reports are indexed uniquely by recording
     * time, so two different reports must not have the same recording time.
     *
     * @param report report to add; unique by recording time
     */
    public void add(Report report) {
        // TODO: fail if already registered
        reportsByRecordTime.put(report.getRecordTime(), report);
    }

    /**
     * Returns the latest report indexed before the given report's recording
     * time.
     *
     * @param report report to look up next oldest (previous) report for
     * @return next oldest report by recording time; null if unavailable
     */
    public Report getLatestReportBefore(Report report) {
        return getValue(reportsByRecordTime.lowerEntry(report.getRecordTime()));
    }

    /**
     * Returns the member identified by given VATSIM ID.
     *
     * @param vatsimId VATSIM ID to get member for
     * @return member by given VATSIM ID; null if unavailable
     */
    public Member getMemberByVatsimId(int vatsimId) {
        return membersByVatsimId.get(vatsimId);
    }

    private <T> T getValue(Map.Entry<?, T> entry) {
        return (entry == null) ? null : entry.getValue();
    }

    /**
     * Returns all indexed reports.
     *
     * @return all indexed reports
     */
    public Collection<Report> getAllReports() {
        return unmodifiableCollection(reportsByRecordTime.values());
    }

    /**
     * Returns all indexed members.
     *
     * @return all indexed members
     */
    public Collection<Member> getAllMembers() {
        return unmodifiableCollection(membersByVatsimId.values());
    }

    // TODO: unit tests
}
