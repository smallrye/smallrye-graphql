package io.smallrye.graphql.type;

import org.jboss.jandex.ClassInfo;

import graphql.schema.GraphQLTypeReference;

/**
 * Simple class to hold information about a type we care about
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * 
 *         TODO: Equal on nameInSchema ? So that we can not have more than one ?
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

}
