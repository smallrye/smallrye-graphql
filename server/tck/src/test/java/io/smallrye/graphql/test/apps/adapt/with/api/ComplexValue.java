package io.smallrye.graphql.test.apps.adapt.with.api;

import java.util.Arrays;
import java.util.List;

public class ComplexValue {

    private int id;
    private List<String> values;

    public ComplexValue() {
    }

    public ComplexValue(int id, String... values) {
        this.id = id;
        this.values = Arrays.asList(values);
    }

    public ComplexValue(int id, List<String> values) {
        this.id = id;
        this.values = values;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + this.id;
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
        final ComplexValue other = (ComplexValue) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ComplexValue{" + "id=" + id + ", values=" + values + '}';
    }
}
