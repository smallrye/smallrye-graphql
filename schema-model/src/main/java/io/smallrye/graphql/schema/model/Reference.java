package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Represents a reference to some other root entry (type/input/enum/interface)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Reference implements Serializable {
    private String className;
    private String name;
    private ReferenceType type;

    //    public Reference() {
    //    }

    public Reference(String className, String name, ReferenceType type) {
        this.className = className;
        this.name = name;
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReferenceType getType() {
        return type;
    }

    public void setType(ReferenceType type) {
        this.type = type;
    }
}
