package io.smallrye.graphql.test.apps.adapt.with.api;

public class ComplexKey {

    private int id;
    private String key;
    private String description;

    public ComplexKey() {
    }

    public ComplexKey(int id, String key, String description) {
        this.id = id;
        this.key = key;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.id;
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
        final ComplexKey other = (ComplexKey) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ComplexKey{" + "id=" + id + ", key=" + key + ", description=" + description + '}';
    }
}
