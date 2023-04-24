package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "Transformation{" + "format=" + format + ", locale=" + locale + ", type=" + type + ", jsonB=" + jsonB + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.format);
        hash = 89 * hash + Objects.hashCode(this.locale);
        hash = 89 * hash + Objects.hashCode(this.type);
        hash = 89 * hash + (this.jsonB ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Transformation other = (Transformation) obj;
        if (this.jsonB != other.jsonB) {
            return false;
        }
        if (!Objects.equals(this.format, other.format)) {
            return false;
        }
        if (!Objects.equals(this.locale, other.locale)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

}
