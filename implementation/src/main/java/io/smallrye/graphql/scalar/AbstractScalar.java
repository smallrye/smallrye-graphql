package io.smallrye.graphql.scalar;

import java.util.Arrays;
import java.util.List;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

/**
 * Base Scalar for all of our own scalars
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractScalar extends GraphQLScalarType {
    private final List<Class> supportedTypes;

    public AbstractScalar(String name,
            Coercing coercing,
            Class... supportedTypes) {

        super(name, "Scalar for " + name, coercing);
        this.supportedTypes = Arrays.asList(supportedTypes);

    }

    public List<Class> getSupportedClasses() {
        return this.supportedTypes;
    }

}
