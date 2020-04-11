package io.smallrye.graphql.schema.model;

/**
 * Represent an argument that is used on operations.
 * It's just a field with extra information to hold the
 * Java method argument Name
 * 
 * @see <a href="https://spec.graphql.org/draft/#sec-The-__Field-Type">Field</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Argument extends Field {

    private final String methodArgumentName; // This is the java method argument name

    public Argument(String methodArgumentName, String methodName, String name, String description, Reference reference) {
        super(methodName, name, description, reference);
        this.methodArgumentName = methodArgumentName;
    }

    public String getMethodArgumentName() {
        return methodArgumentName;
    }
}