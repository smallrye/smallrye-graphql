package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate the a field should apply formatting
 * 
 * At the moment this is only on number and date scalars.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Format implements Serializable {

    private final String format;
    private final String locale;
    private final Type type;

    public Format(Type type, String format, String locale) {
        this.type = type;
        this.format = format;
        this.locale = locale;
    }

    public Type getType() {
        return this.type;
    }

    public String getFormat() {
        return this.format;
    }

    public String getLocale() {
        return this.locale;
    }

    public enum Type {
        NUMBER,
        DATE
    }
}
