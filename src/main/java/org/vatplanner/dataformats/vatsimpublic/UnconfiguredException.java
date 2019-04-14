package org.vatplanner.dataformats.vatsimpublic;

/**
 * {@link RuntimeException} used to indicate that a configuration did not
 * specify any actions to be taken.
 */
public class UnconfiguredException extends RuntimeException {

    public UnconfiguredException(String msg) {
        super(msg);
    }

}
