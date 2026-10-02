package tck.graphql.typesafe;

import jakarta.json.JsonValue;

import java.util.Objects;

public class Zebra {
    String name;
    Integer horns;
    JsonValue meta;

    public Zebra() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHorns() {
        return horns;
    }

    public void setHorns(Integer horns) {
        this.horns = horns;
    }

    public JsonValue getMeta() {
        return meta;
    }

    public void setMeta(JsonValue meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Zebra zebra = (Zebra) o;
        return Objects.equals(getName(), zebra.getName()) && Objects.equals(getHorns(), zebra.getHorns())
                && Objects.equals(getMeta(), zebra.getMeta());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getHorns(), getMeta());
    }
}
