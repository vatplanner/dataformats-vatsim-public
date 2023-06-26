package org.vatplanner.dataformats.vatsimpublic.parser;

import java.io.Reader;

/**
 * Generic interface for a parser deserializing content to objects of type
 * <code>T</code>.
 *
 * @param <T> type of deserialization result
 */
public interface Parser<T> {
    /**
     * Parses the content of given {@link CharSequence} to an object.
     *
     * @param s content to be parsed
     * @return parsed content
     */
    T deserialize(CharSequence s);

    /**
     * Parses the content available from given {@link Reader} to an object.
     *
     * @param reader provides access to content to be parsed
     * @return parsed content
     */
    T deserialize(Reader reader);

    /**
     * Proxy to {@link #deserialize(Reader)} for backwards-compatibility.
     *
     * @param reader provides access to content to be parsed
     * @return parsed content
     * @deprecated call {@link #deserialize(Reader)} instead
     */
    @Deprecated
    default T parse(Reader reader) {
        return deserialize(reader);
    }

    /**
     * Proxy to {@link #deserialize(CharSequence)} for backwards-compatibility.
     *
     * @param s content to be parsed
     * @return parsed content
     * @deprecated call {@link #deserialize(CharSequence)} instead
     */
    default T parse(CharSequence s) {
        return deserialize(s);
    }
}
