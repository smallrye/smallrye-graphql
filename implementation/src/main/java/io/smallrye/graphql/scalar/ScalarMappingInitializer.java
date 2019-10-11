package io.smallrye.graphql.scalar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * Create a Map of all custom Scalars
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class ScalarMappingInitializer {
    private static final Logger LOG = Logger.getLogger(ScalarMappingInitializer.class.getName());

    @Produces
    private final Map<DotName, GraphQLScalarType> scalarMapping = new HashMap<>(DEFAULT_MAPPING);

    @Inject
    private BeanManager beanManager;

    public void init(@Priority(Integer.MAX_VALUE - 1) @Observes @Initialized(ApplicationScoped.class) Object init) {
        LOG.debug("Finding all custom scalars...");

        Set<Bean<?>> beans = beanManager.getBeans(Object.class, new CustomScalarLiteral() {
        });

        for (Bean bean : beans) {
            CustomScalar customScalar = (CustomScalar) beanManager.getContext(bean.getScope()).get(bean,
                    beanManager.createCreationalContext(bean));

            LOG.debug("\t found " + bean.getBeanClass() + " for scalar type [" + customScalar.getName() + "] that maps to ["
                    + customScalar.forClass() + "]");

            scalarMapping.put(DotName.createSimple(customScalar.forClass().getName()), createGraphQLScalarType(customScalar));

        }
    }

    private GraphQLScalarType createGraphQLScalarType(CustomScalar customScalar) {
        return GraphQLScalarType.newScalar().name(customScalar.getName()).description(customScalar.getDescription())
                .coercing(new Coercing() {
                    @Override
                    public Object serialize(Object o) throws CoercingSerializeException {
                        return customScalar.serialize(o);
                    }

                    @Override
                    public Object parseValue(Object o) throws CoercingParseValueException {
                        return customScalar.deserialize(o);
                    }

                    @Override
                    public Object parseLiteral(Object o) throws CoercingParseLiteralException {
                        return customScalar.deserialize(o); // TODO: ??
                    }
                }).build();
    }

    private static final Map<DotName, GraphQLScalarType> DEFAULT_MAPPING = new HashMap<>();

    static {
        DEFAULT_MAPPING.put(DotName.createSimple(char[].class.getName()), Scalars.GraphQLString);
        DEFAULT_MAPPING.put(DotName.createSimple(char.class.getName()), Scalars.GraphQLString);
        DEFAULT_MAPPING.put(DotName.createSimple(String.class.getName()), Scalars.GraphQLString);
        DEFAULT_MAPPING.put(DotName.createSimple(Integer.class.getName()), Scalars.GraphQLInt);
        DEFAULT_MAPPING.put(DotName.createSimple(int.class.getName()), Scalars.GraphQLInt);
        DEFAULT_MAPPING.put(DotName.createSimple(Long.class.getName()), Scalars.GraphQLLong);
        DEFAULT_MAPPING.put(DotName.createSimple(long.class.getName()), Scalars.GraphQLLong);
        DEFAULT_MAPPING.put(DotName.createSimple(Float.class.getName()), Scalars.GraphQLFloat);
        DEFAULT_MAPPING.put(DotName.createSimple(float.class.getName()), Scalars.GraphQLFloat);
        DEFAULT_MAPPING.put(DotName.createSimple(Double.class.getName()), Scalars.GraphQLFloat);
        DEFAULT_MAPPING.put(DotName.createSimple(double.class.getName()), Scalars.GraphQLFloat);
        DEFAULT_MAPPING.put(DotName.createSimple(Boolean.class.getName()), Scalars.GraphQLBoolean);
        DEFAULT_MAPPING.put(DotName.createSimple(boolean.class.getName()), Scalars.GraphQLBoolean);
    }
}
