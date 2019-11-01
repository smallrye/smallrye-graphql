package io.smallrye.graphql.type;

import java.util.Objects;

import org.jboss.jandex.ClassInfo;

import graphql.schema.GraphQLTypeReference;

/**
 * Simple class to hold information about a type we care about
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * 
 */
public class TypeHolder {
    private final String nameInSchema;
    private final ClassInfo classInfo;

    public TypeHolder(String nameInSchema, ClassInfo classInfo) {
        this.nameInSchema = nameInSchema;
        this.classInfo = classInfo;
    }

    public String getNameInSchema() {
        return nameInSchema;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public GraphQLTypeReference getGraphQLTypeReference() {
        return GraphQLTypeReference.typeRef(this.nameInSchema);
    }

    @Override
    public String toString() {
        return "TypeHolder{" + "nameInSchema=" + nameInSchema + ", classInfo=" + classInfo + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.nameInSchema);
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
        final TypeHolder other = (TypeHolder) obj;
        if (!Objects.equals(this.nameInSchema, other.nameInSchema)) {
            return false;
        }
        return true;
    }

}
