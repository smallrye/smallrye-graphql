package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate that a field should apply transformation
 * 
 * At the moment this is only on number and date scalars.
 * 
 * We also mark if this is a JsonB annotated field o
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Transformation implements Serializable {

    private String format;
    private String locale;
    private Type type;
    private boolean jsonB; // If the transformation can happen with JsonB

    public Transformation() {
    }

    public Transformation(Type type,
            String format,
            String locale,
            boolean jsonB) {
        this.type = type;
        this.format = format;
        this.locale = locale;
        this.jsonB = jsonB;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLocale() {
        return this.locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isJsonB() {
        return jsonB;
    }

    public void setJsonB(boolean jsonB) {
        this.jsonB = jsonB;
    }

    public enum Type {
        NUMBER,
        DATE
    }

}
