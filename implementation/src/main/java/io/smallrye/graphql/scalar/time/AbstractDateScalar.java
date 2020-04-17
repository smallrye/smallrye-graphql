package io.smallrye.graphql.scalar.time;

import io.smallrye.graphql.scalar.AbstractScalar;

/**
 * Base Scalar for Dates.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractDateScalar extends AbstractScalar {

    public AbstractDateScalar(String name,
            Converter converter,
            Class... supportedTypes) {

        super(name, new DateCoercing(name, converter, supportedTypes), supportedTypes);
    }
}
