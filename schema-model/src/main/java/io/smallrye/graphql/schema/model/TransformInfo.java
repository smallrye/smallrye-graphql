package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate the a field should apply transformation
 * 
 * At the moment this is only on number and date scalars.
 * 
 * We also mark if this is a JsonB annotated field o
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TransformInfo implements Serializable {

    private final String format;
    private final String locale;
    private final Type type;
    private final boolean valid; // If the format is valid for this type
    private final boolean jsonB; // If the transformation can happen with JsonB

    public TransformInfo(Type type,
            String format,
            String locale,
            boolean valid,
            boolean jsonB) {
        this.type = type;
        this.format = format;
        this.locale = locale;
        this.valid = valid;
        this.jsonB = jsonB;
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

    public boolean isJsonB() {
        return jsonB;
    }

    public boolean isValid() {
        return valid;
    }
}
