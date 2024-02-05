package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private String graphQLClassName;
    private AdaptTo adaptTo = null; // If the type is mapped to a scalar
    private AdaptWith adaptWith = null; // If the type is mapped to another type with an adapter
    private Map<String, Reference> classParametrizedTypes;
    private Map<String, Reference> parentClassParametrizedTypes;
    private boolean addParametrizedTypeNameExtension;
    private List<DirectiveInstance> directiveInstances;

    /**
     * If this is wrapped in generics or an array, this contain the info, examples are arrays, collections, async, optional or
     * just plain generic.
     */
    private Wrapper wrapper = null;

    public Reference() {
    }

    protected Reference(String className, String name, ReferenceType type) {
        this.className = className;
        this.name = name;
        this.type = type;
    }

    private Reference(Builder builder) {
        this.className = builder.className;
        this.name = builder.name;
        this.type = builder.type;
        this.graphQLClassName = builder.graphQLClassName;
        this.adaptTo = builder.adaptTo;
        this.adaptWith = builder.adaptWith;
        this.classParametrizedTypes = builder.classParametrizedTypes;
        this.parentClassParametrizedTypes = builder.parentClassParametrizedTypes;
        this.addParametrizedTypeNameExtension = builder.addParametrizedTypeNameExtension;
        this.directiveInstances = builder.directiveInstances;
        this.wrapper = builder.wrapper;
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
     *
     * @return the type
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
    public String getGraphQLClassName() {
        if (this.graphQLClassName != null) {
            return graphQLClassName;
        }
        return className;
    }

    public void setGraphQLClassName(String graphQLClassName) {
        this.graphQLClassName = graphQLClassName;
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

    public Map<String, Reference> getAllParametrizedTypes() {
        if (classParametrizedTypes == null && parentClassParametrizedTypes == null) {
            return new HashMap<>();
        }
        if (classParametrizedTypes == null) {
            return parentClassParametrizedTypes;
        }
        if (parentClassParametrizedTypes == null) {
            return classParametrizedTypes;
        }

        // fixme: the way how the ReferenceCreator is implemented this was used if
        // two types of type variables were used smt like:
        //  Class<A> extends Class<B>
        // but since if the TYPE is Parametrized it does not look at the (in the ReferenceCreator)
        // extended part type vars...
        Stream<Map.Entry<String, Reference>> combinedStream = Stream.concat(
                classParametrizedTypes.entrySet().stream(),
                parentClassParametrizedTypes.entrySet().stream());

        return combinedStream.collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    public Map<String, Reference> getClassParametrizedTypes() {
        return classParametrizedTypes;
    }

    public void setClassParametrizedTypes(Map<String, Reference> classParametrizedTypes) {
        this.classParametrizedTypes = classParametrizedTypes;
    }

    public boolean hasClassParameterizedTypes() {
        return getAllParametrizedTypes() != null
                && !getAllParametrizedTypes().isEmpty();
    }

    public Reference getClassParametrizedType(String name) {
        if (hasClassParameterizedTypes()) {
            return getAllParametrizedTypes().get(name);
        }
        return null;
    }

    public Map<String, Reference> getParentClassParametrizedTypes() {
        return parentClassParametrizedTypes;
    }

    public void setParentClassParametrizedTypes(Map<String, Reference> parentClassParametrizedTypes) {
        this.parentClassParametrizedTypes = parentClassParametrizedTypes;
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
        return "Reference{" +
                "className=" + className +
                ", name=" + name +
                ", type=" + type +
                ", graphQLClassName=" + graphQLClassName +
                ", adaptTo=" + adaptTo +
                ", adaptWith=" + adaptWith +
                ", directiveInstances=" + directiveInstances +
                ", classParametrizedTypes" + classParametrizedTypes +
                ", extendedClassParametrizedTypes" + parentClassParametrizedTypes +
                ", wrapper=" + wrapper
                + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.className);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.graphQLClassName);
        hash = 97 * hash + Objects.hashCode(this.adaptTo);
        hash = 97 * hash + Objects.hashCode(this.adaptWith);
        hash = 97 * hash + Objects.hashCode(this.directiveInstances);
        hash = 97 * hash + Objects.hashCode(this.classParametrizedTypes);
        hash = 97 * hash + Objects.hashCode(this.parentClassParametrizedTypes);
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
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.graphQLClassName, other.graphQLClassName)) {
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
        if (!Objects.equals(this.directiveInstances, other.directiveInstances)) {
            return false;
        }
        if (!Objects.equals(this.classParametrizedTypes, other.classParametrizedTypes)) {
            return false;
        }
        if (!Objects.equals(this.parentClassParametrizedTypes, other.parentClassParametrizedTypes)) {
            return false;
        }
        return Objects.equals(this.wrapper, other.wrapper);
    }

    public static class Builder {

        private String className;
        private String name;
        private ReferenceType type;
        private String graphQLClassName;
        private AdaptTo adaptTo = null;
        private AdaptWith adaptWith = null;
        private Map<String, Reference> classParametrizedTypes;
        private Map<String, Reference> parentClassParametrizedTypes;
        private boolean addParametrizedTypeNameExtension;
        private List<DirectiveInstance> directiveInstances;
        private Wrapper wrapper;

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(ReferenceType type) {
            this.type = type;
            return this;
        }

        public Builder graphQLClassName(String graphQLClassName) {
            this.graphQLClassName = graphQLClassName;
            return this;
        }

        public Builder adaptTo(AdaptTo adaptTo) {
            this.adaptTo = adaptTo;
            return this;
        }

        public Builder adaptWith(AdaptWith adaptWith) {
            this.adaptWith = adaptWith;
            return this;
        }

        public Builder classParametrizedTypes(Map<String, Reference> classParametrizedTypes) {
            this.classParametrizedTypes = classParametrizedTypes;
            return this;
        }

        public Builder extendedClassParametrizedTypes(Map<String, Reference> extendedClassParametrizedTypes) {
            this.parentClassParametrizedTypes = extendedClassParametrizedTypes;
            return this;
        }

        public Builder addParametrizedTypeNameExtension(boolean addParametrizedTypeNameExtension) {
            this.addParametrizedTypeNameExtension = addParametrizedTypeNameExtension;
            return this;
        }

        public Builder directiveInstances(List<DirectiveInstance> directiveInstances) {
            this.directiveInstances = directiveInstances;
            return this;
        }

        public Builder wrapper(Wrapper wrapper) {
            this.wrapper = wrapper;
            return this;
        }

        public Builder reference(Reference reference) {
            this.className(reference.getClassName());
            this.name(reference.getName());
            this.type(reference.getType());
            this.graphQLClassName(reference.getGraphQLClassName());
            this.adaptTo(reference.getAdaptTo());
            this.adaptWith(reference.getAdaptWith());
            this.classParametrizedTypes(reference.getClassParametrizedTypes());
            this.addParametrizedTypeNameExtension(reference.isAddParametrizedTypeNameExtension());
            this.directiveInstances(reference.getDirectiveInstances());
            this.wrapper(reference.getWrapper());

            return this;
        }

        public Reference build() {
            return new Reference(this);
        }
    }
}
