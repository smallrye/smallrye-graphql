package io.smallrye.graphql.schema.model;

import java.util.Collection;
import java.util.List;

/**
 * Represents one of an enum's values. Is part of {@link EnumType}.
 *
 * @author Felix König (de.felix.koenig@gmail.com)
 */
public final class EnumValue {

    private String description;
    private String value;
    private List<DirectiveInstance> directiveInstances;

    public EnumValue() {
    }

    public EnumValue(String description, String value, List<DirectiveInstance> directiveInstances) {
        this.description = description;
        this.value = value;
        this.directiveInstances = directiveInstances;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EnumValue{" +
                "description=" + description +
                ", value=" + value +
                ", directiveInstances=" + directiveInstances +
                '}';
    }

    public boolean hasDirectiveInstances() {
        return directiveInstances != null && !directiveInstances.isEmpty();
    }

    public Collection<DirectiveInstance> getDirectiveInstances() {
        return directiveInstances;
    }

}
