package tck.graphql.typesafe;

import java.util.Objects;

import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.graphql.Input;

@Input("SomeInput")
public class SomeInput {
    private String name;

    @JsonbProperty("first")
    private String firstAtrribute;

    @JsonbProperty(value = "second", nillable = true)
    private String secondAtrribute;

    @JsonbNillable
    private String third;

    public SomeInput() {
    }

    public SomeInput(String name) {
        this.name = name;
    }

    public SomeInput(String name, String first, String second, String third) {
        this.name = name;
        this.firstAtrribute = first;
        this.secondAtrribute = second;
        this.third = third;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, firstAtrribute, secondAtrribute, third);
    }

    @Override
    public String toString() {
        return "SomeInput [name=" + name + ", firstAtrribute=" + firstAtrribute + ", secondAtrribute=" + secondAtrribute
                + ", third=" + third + "]";
    }
}
