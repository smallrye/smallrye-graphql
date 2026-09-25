package tck.graphql.typesafe;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.Objects;

public class Giraffe {
    String name;
    Double height;
    JsonNode meta;

    public Giraffe() {
        this.meta = JsonNodeFactory.instance.objectNode();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public JsonNode getMeta() {
        return meta;
    }

    public void setMeta(JsonNode meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Giraffe giraffe = (Giraffe) o;
        return Objects.equals(getName(), giraffe.getName()) && Objects.equals(getHeight(), giraffe.getHeight())
                && getMeta().equals(giraffe.getMeta());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getHeight(), getMeta().hashCode());
    }
}
