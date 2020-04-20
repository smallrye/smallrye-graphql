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

    private String methodArgumentName; // This is the java method argument name
    private boolean sourceArgument = false; // Flag if this is a source argument

    public Argument() {
    }

    public Argument(String methodArgumentName, String methodName, String propertyName, String name, String description,
            Reference reference) {
        super(methodName, propertyName, name, description, reference);
        this.methodArgumentName = methodArgumentName;
    }

    public String getMethodArgumentName() {
        return methodArgumentName;
    }

    public void setMethodArgumentName(String methodArgumentName) {
        this.methodArgumentName = methodArgumentName;
    }

    public void setSourceArgument(boolean sourceArgument) {
        this.sourceArgument = sourceArgument;
    }

    public boolean isSourceArgument() {
        return sourceArgument;
    }
}