package org.vatplanner.dataformats.vatsimpublic.export;

import java.io.OutputStream;

public interface Writer<T> {
    void serialize(T content, OutputStream os);
}
