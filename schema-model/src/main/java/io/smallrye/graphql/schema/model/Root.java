package io.smallrye.graphql.schema.model;

/**
 * Represents any root entry (type/input/interface/enum) in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class Root extends Reference {
    private final String description;

    public Root(String className, String name, ReferenceType type, String description) {
        super(className, name, type);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    //    public void setDescription(String description) {
    //        this.description = description;
    //    }
}
