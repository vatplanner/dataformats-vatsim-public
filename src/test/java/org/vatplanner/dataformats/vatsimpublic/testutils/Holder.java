package org.vatplanner.dataformats.vatsimpublic.testutils;

/**
 * Wraps a value for referencing. Simple replacement to mimic the old Holder
 * class that was available in JDKs before Java 11, needed for compatibility of
 * JDK 11+ with older code.
 * 
 * @param <T> type of wrapped value
 */
public class Holder<T> {
    public volatile T value;
}
