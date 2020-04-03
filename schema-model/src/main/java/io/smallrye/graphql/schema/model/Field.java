package io.smallrye.graphql.schema.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a field on a type/input/interface
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Field extends Entry {
    private Reference returnType;
    private List<Parameter> parameters;

    private boolean mandatory = false;
    private boolean collection = false;

    public Reference getReturnType() {
        return returnType;
    }

    public void setReturnType(Reference returnType) {
        this.returnType = returnType;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(Parameter parameter) {
        if (this.parameters == null) {
            this.parameters = new LinkedList<>();
        }
        this.parameters.add(parameter);
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }
}
