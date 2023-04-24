package io.smallrye.graphql.scalar.number;

import io.smallrye.graphql.scalar.AbstractScalar;

/**
 * Base Scalar for Numbers.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractNumberScalar extends AbstractScalar {

    public AbstractNumberScalar(String name,
            Converter converter,
            Class... supportedTypes) {

        super(name, new NumberCoercing(name, converter, supportedTypes), supportedTypes);

    }
}
