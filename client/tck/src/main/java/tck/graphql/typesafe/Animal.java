package tck.graphql.typesafe;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class Animal {
    private String name;
    private OptionalInt numberOfLegs;
    private OptionalLong numberOfTeeth;
    private OptionalDouble price;
    private Optional<String> alias;

    public Animal() {
    }

    public Animal(String name,
            OptionalInt numberOfLegs,
            OptionalLong numberOfTeeth,
            OptionalDouble price,
            Optional<String> alias) {
        this.name = name;
        this.numberOfLegs = numberOfLegs;
        this.numberOfTeeth = numberOfTeeth;
        this.price = price;
        this.alias = alias;
    }

    public Animal(String name, OptionalInt numberOfLegs) {
        this(name, numberOfLegs, null, null, null);
    }

    public Animal(String name, OptionalLong numberOfTeeth) {
        this(name, null, numberOfTeeth, null, null);

    }

    public Animal(String name, OptionalDouble price) {
        this(name, null, null, price, null);
    }

    public Animal(String name, Optional<String> alias) {
        this(name, null, null, null, alias);
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

    public Optional<String> getAlias() {
        return alias;
    }

    public void setAlias(Optional<String> alias) {
        this.alias = alias;
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
                Objects.equals(price, animal.price) &&
                Objects.equals(alias, animal.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, numberOfLegs, numberOfTeeth, price, alias);
    }

    @Override
    public String toString() {
        return "Animal [name=" + name + ", numberOfLegs=" + numberOfLegs + ", numberOfTeeth=" + numberOfTeeth
                + ", price=" + price + ", alias=" + alias + "]";
    }
}