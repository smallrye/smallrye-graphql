package tck.graphql.typesafe;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class Animal {
    private String name;
    private OptionalInt numberOfLegs;
    private OptionalLong numberOfTeeth;
    private OptionalDouble price;

    public Animal() {
    }

    public Animal(String name,
            OptionalInt numberOfLegs,
            OptionalLong numberOfTeeth,
            OptionalDouble price) {
        this.name = name;
        this.numberOfLegs = numberOfLegs;
        this.numberOfTeeth = numberOfTeeth;
        this.price = price;
    }

    public Animal(String name, OptionalInt numberOfLegs) {
        this(name, numberOfLegs, null, null);
    }

    public Animal(String name, OptionalLong numberOfTeeth) {
        this(name, null, numberOfTeeth, null);

    }

    public Animal(String name, OptionalDouble price) {
        this(name, null, null, price);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OptionalInt getNumberOfLegs() {
        return numberOfLegs;
    }

    public void setNumberOfLegs(OptionalInt numberOfLegs) {
        this.numberOfLegs = numberOfLegs;
    }

    public OptionalLong getNumberOfTeeth() {
        return numberOfTeeth;
    }

    public void setNumberOfTeeth(OptionalLong numberOfTeeth) {
        this.numberOfTeeth = numberOfTeeth;
    }

    public OptionalDouble getPrice() {
        return price;
    }

    public void setPrice(OptionalDouble price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Animal animal = (Animal) o;
        return Objects.equals(name, animal.name) &&
                Objects.equals(numberOfLegs, animal.numberOfLegs) &&
                Objects.equals(numberOfTeeth, animal.numberOfTeeth) &&
                Objects.equals(price, animal.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, numberOfLegs, numberOfTeeth, price);
    }
}