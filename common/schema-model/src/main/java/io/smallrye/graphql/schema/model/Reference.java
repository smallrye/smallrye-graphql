package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a reference to some other type (type/input/enum/interface) This so that, as we are scanning, we can refer
 * to a type that might not exist yet. All types extends this.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Reference implements Serializable {

    private String className;
    private String name;
    private ReferenceType type;
    private String graphQlClassName;
    private AdaptTo adaptTo = null; // If the type is mapped to a scalar
    private AdaptWith adaptWith = null; // If the type is mapped to another type with an adapter
    private Map<String, Reference> parametrizedTypeArguments;
    private boolean addParametrizedTypeNameExtension;
    private List<DirectiveInstance> directiveInstances;

    /**
     * If this is wrapped in generics or an array, this contain the info, examples are arrays, collections, async, optional or
     * just plain generic.
     */
    private Wrapper wrapper = null;

    public Reference() {
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName,
            Map<String, Reference> parametrizedTypeArguments, boolean addParametrizedTypeNameExtension) {
        this(className, name, type, graphQlClassName, parametrizedTypeArguments, addParametrizedTypeNameExtension, null, null,
                null);
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName) {
        this(className, name, type, graphQlClassName, null, false, null, null, null);
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName,
            Map<String, Reference> parametrizedTypeArguments, boolean addParametrizedTypeNameExtension,
            AdaptTo adaptTo, AdaptWith adaptWith, List<DirectiveInstance> directiveInstances) {
        this.className = className;
        this.name = name;
        this.type = type;
        this.graphQlClassName = graphQlClassName;
        this.parametrizedTypeArguments = parametrizedTypeArguments;
        this.adaptTo = adaptTo;
        this.adaptWith = adaptWith;
        this.addParametrizedTypeNameExtension = addParametrizedTypeNameExtension;
        this.directiveInstances = directiveInstances;
    }

    public Reference(String className, String name, ReferenceType type) {
        this(className, name, type, className, null, false, null, null, null);
    }

    public Reference(String className, String name, ReferenceType type, Map<String, Reference> parametrizedTypeArguments,
            boolean addParametrizedTypeNameExtension) {
        this(className, name, type, className, parametrizedTypeArguments, addParametrizedTypeNameExtension, null, null, null);
    }

    public Reference(final Reference reference) {
        this(reference.className, reference.name, reference.type, reference.graphQlClassName,
                reference.parametrizedTypeArguments, reference.addParametrizedTypeNameExtension, reference.adaptTo,
                reference.adaptWith,
                reference.directiveInstances);
    }

    /**
     * This represent the Java Class Name
     *
     * @return String full class name
     */
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * This represents the GraphQL Name
     * 
     * @return String name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * This represent the GraphQL Type
     */
    public ReferenceType getType() {
        return type;
    }

    public void setType(ReferenceType type) {
        this.type = type;
    }

    /**
     * The class into which this reference can be converted, which can be handled by graphql
     *
     * For example, String for Dates or formatted Numbers, BigInteger for long and BigInteger.
     *
     * Used for transforming.
     *
     * @return full class name
     */
    public String getGraphQlClassName() {
        return graphQlClassName;
    }

    public void setGraphQlClassName(String graphQlClassName) {
        this.graphQlClassName = graphQlClassName;
    }

    public AdaptTo getAdaptTo() {
        return adaptTo;
    }

    public void setAdaptTo(AdaptTo adaptTo) {
        this.adaptTo = adaptTo;
    }

    public boolean isAdaptingTo() {
        return this.adaptTo != null;
    }

    public AdaptWith getAdaptWith() {
        return adaptWith;
    }

    public void setAdaptWith(AdaptWith adaptWith) {
        this.adaptWith = adaptWith;
    }

    public boolean isAdaptingWith() {
        return this.adaptWith != null;
    }

    public Map<String, Reference> getParametrizedTypeArguments() {
        return parametrizedTypeArguments;
    }

    public void setParametrizedTypeArguments(Map<String, Reference> parametrizedTypeArguments) {
        this.parametrizedTypeArguments = parametrizedTypeArguments;
    }

    public boolean isAddParametrizedTypeNameExtension() {
        return addParametrizedTypeNameExtension;
    }

    public void setAddParametrizedTypeNameExtension(boolean addParametrizedTypeNameExtension) {
        this.addParametrizedTypeNameExtension = addParametrizedTypeNameExtension;
    }

    public List<DirectiveInstance> getDirectiveInstances() {
        return directiveInstances;
    }

    public boolean hasDirectiveInstances() {
        return directiveInstances != null && !directiveInstances.isEmpty();
    }

    public void setDirectiveInstances(List<DirectiveInstance> directiveInstances) {
        this.directiveInstances = directiveInstances;
    }

    public void addDirectiveInstance(DirectiveInstance directiveInstance) {
        this.directiveInstances.add(directiveInstance);
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public boolean hasWrapper() {
        return this.wrapper != null;
    }

    @Override
    public String toString() {
        return "Reference{" + "className=" + className + ", name=" + name + ", type=" + type + ", graphQlClassName="
                + graphQlClassName + ", adaptTo=" + adaptTo + ", adaptWith=" + adaptWith + ", parametrizedTypeArguments="
                + parametrizedTypeArguments + ", addParametrizedTypeNameExtension=" + addParametrizedTypeNameExtension
                + ", directiveInstances=" + directiveInstances + ", wrapper=" + wrapper + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.className);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.graphQlClassName);
        hash = 97 * hash + Objects.hashCode(this.adaptTo);
        hash = 97 * hash + Objects.hashCode(this.adaptWith);
        hash = 97 * hash + Objects.hashCode(this.parametrizedTypeArguments);
        hash = 97 * hash + (this.addParametrizedTypeNameExtension ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.directiveInstances);
        hash = 97 * hash + Objects.hashCode(this.wrapper);
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
        final Reference other = (Reference) obj;
        if (this.addParametrizedTypeNameExtension != other.addParametrizedTypeNameExtension) {
            return false;
        }
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.graphQlClassName, other.graphQlClassName)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.adaptTo, other.adaptTo)) {
            return false;
        }
        if (!Objects.equals(this.adaptWith, other.adaptWith)) {
            return false;
        }
        if (!Objects.equals(this.parametrizedTypeArguments, other.parametrizedTypeArguments)) {
            return false;
        }
        if (!Objects.equals(this.directiveInstances, other.directiveInstances)) {
            return false;
        }
        if (!Objects.equals(this.wrapper, other.wrapper)) {
            return false;
        }
        return true;
    }

}
