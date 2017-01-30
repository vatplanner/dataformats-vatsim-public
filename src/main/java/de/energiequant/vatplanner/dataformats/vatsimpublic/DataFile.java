package de.energiequant.vatplanner.dataformats.vatsimpublic;

import java.util.HashMap;
import java.util.Map;

public class DataFile {
    private final Map<String, DataFileSection> sections = new HashMap<>();
    
    public static boolean parseFromString() {
        return false;
    }
}
