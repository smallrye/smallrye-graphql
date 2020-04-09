package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate the a field should apply formatting
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Formatter implements Serializable {

    public static enum Type {
        DATE,
        NUMBER
    }

    private final Type type;
    private final String format;
    private final String locale;

    public Formatter(Type type, String format, String locale) {
        this.type = type;
        this.format = format;
        this.locale = locale;
    }

    public Type getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    public String getLocale() {
        return locale;
    }
}
