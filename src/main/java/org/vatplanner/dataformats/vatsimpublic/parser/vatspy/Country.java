package org.vatplanner.dataformats.vatsimpublic.parser.vatspy;

import java.util.Optional;

public class Country {
    private final String name;
    private final String icaoPrefix;
    private final Optional<String> centerName;

    public Country(String name, String icaoPrefix, String centerName) {
        this.name = name;
        this.icaoPrefix = icaoPrefix;
        this.centerName = Helper.emptyIfBlank(centerName);
    }

    public String getName() {
        return name;
    }

    public String getIcaoPrefix() {
        return icaoPrefix;
    }

    public Optional<String> getCenterName() {
        return centerName;
    }
}
