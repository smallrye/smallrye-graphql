/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.graphql.schema.type.scalar;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * Creating the scalars as needed
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class ScalarMappingInitializer {
    private static final Logger LOG = Logger.getLogger(ScalarMappingInitializer.class.getName());

    @Inject
    @ConfigProperty(name = "mp.graphql.passthroughScalars", defaultValue = "Date:java.time.LocalDate,Time:java.time.LocalTime,DateTime:java.time.LocalDateTime")
    private List<String> passthroughScalars;

    @Inject
    private BeanManager beanManager;

    @Produces
    private final Map<DotName, GraphQLScalarType> scalarMap = new HashMap<>();

    @PostConstruct
    void init() {
        scalarMap.putAll(MAPPING);
        addPassthroughScalars();
        addCustomScalars();
    }

    @Produces
    @Named("scalars")
    public Set<DotName> getKnownScalars() {
        return scalarMap.keySet();
    }

    private void addPassthroughScalars() {
        LOG.debug("Finding all passthrough scalars...");
        for (String kv : passthroughScalars) {
            String[] keyValue = kv.split(":");
            String name = keyValue[0];
            String className = keyValue[1];
            LOG.debug("\t found scalar type [" + name + "] that maps to [" + className + "]");

            PassthroughScalar passthroughScalar = new PassthroughScalar(name,
                    "Scalar for " + className + "that just let the value pass though");

            scalarMap.put(DotName.createSimple(className), passthroughScalar);
        }
    }

    private void addCustomScalars() {
        LOG.debug("Finding all custom scalars...");

        Set<Bean<?>> beans = beanManager.getBeans(Object.class, new CustomScalarLiteral() {
        });

        for (Bean bean : beans) {
            CustomScalar customScalar = (CustomScalar) beanManager.getContext(bean.getScope()).get(bean,
                    beanManager.createCreationalContext(bean));

            LOG.debug("\t found " + bean.getBeanClass() + " for scalar type [" + customScalar.getName() + "] that maps to ["
                    + customScalar.forClass() + "]");

            scalarMap.put(DotName.createSimple(customScalar.forClass().getName()), newGraphQLScalarType(customScalar));

        }
    }

    private GraphQLScalarType newGraphQLScalarType(CustomScalar customScalar) {
        return GraphQLScalarType.newScalar()
                .name(customScalar.getName())
                .description(customScalar.getDescription())
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
                        if (o.getClass().getName().equals(StringValue.class.getName())) {
                            StringValue stringValue = StringValue.class.cast(o);
                            String value = stringValue.getValue();
                            return parseValue(value);
                        } // TODO: Other types ? ArrayValue, BooleanValue, EnumValue, FloatValue, IntValue, NullValue, ObjectValue, ScalarValue;
                        return parseValue(o);
                    }
                }).build();
    }

    private static final Map<DotName, GraphQLScalarType> MAPPING = new HashMap<>();

    static {
        MAPPING.put(DotName.createSimple(char.class.getName()), Scalars.GraphQLChar);
        MAPPING.put(DotName.createSimple(Character.class.getName()), Scalars.GraphQLChar);

        MAPPING.put(DotName.createSimple(String.class.getName()), Scalars.GraphQLString);

        MAPPING.put(DotName.createSimple(Short.class.getName()), Scalars.GraphQLShort);
        MAPPING.put(DotName.createSimple(short.class.getName()), Scalars.GraphQLShort);

        MAPPING.put(DotName.createSimple(Integer.class.getName()), Scalars.GraphQLInt);
        MAPPING.put(DotName.createSimple(int.class.getName()), Scalars.GraphQLInt);

        MAPPING.put(DotName.createSimple(Long.class.getName()), Scalars.GraphQLLong);
        MAPPING.put(DotName.createSimple(long.class.getName()), Scalars.GraphQLLong);

        MAPPING.put(DotName.createSimple(BigInteger.class.getName()), Scalars.GraphQLBigInteger);
        MAPPING.put(DotName.createSimple(BigDecimal.class.getName()), Scalars.GraphQLBigDecimal);

        MAPPING.put(DotName.createSimple(Float.class.getName()), Scalars.GraphQLFloat);
        MAPPING.put(DotName.createSimple(float.class.getName()), Scalars.GraphQLFloat);
        MAPPING.put(DotName.createSimple(Double.class.getName()), Scalars.GraphQLFloat);
        MAPPING.put(DotName.createSimple(double.class.getName()), Scalars.GraphQLFloat);

        MAPPING.put(DotName.createSimple(Boolean.class.getName()), Scalars.GraphQLBoolean);
        MAPPING.put(DotName.createSimple(boolean.class.getName()), Scalars.GraphQLBoolean);

        MAPPING.put(DotName.createSimple(Byte.class.getName()), Scalars.GraphQLByte);
        MAPPING.put(DotName.createSimple(byte.class.getName()), Scalars.GraphQLByte);
    }
}
