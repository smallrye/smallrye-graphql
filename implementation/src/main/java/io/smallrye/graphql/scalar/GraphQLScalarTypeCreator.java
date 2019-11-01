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

package io.smallrye.graphql.scalar;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;

/**
 * Creating the scalars as needed
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLScalarTypeCreator {
    private static final Logger LOG = Logger.getLogger(GraphQLScalarTypeCreator.class.getName());

    @Inject
    private BeanManager beanManager;

    private final Map<DotName, CustomScalar> customScalarMap = new HashMap<>();

    @PostConstruct
    void init() {
        LOG.debug("Finding all custom scalars...");

        Set<Bean<?>> beans = beanManager.getBeans(Object.class, new CustomScalarLiteral() {
        });

        for (Bean bean : beans) {
            CustomScalar customScalar = (CustomScalar) beanManager.getContext(bean.getScope()).get(bean,
                    beanManager.createCreationalContext(bean));

            LOG.debug("\t found " + bean.getBeanClass() + " for scalar type [" + customScalar.getName() + "] that maps to ["
                    + customScalar.forClass() + "]");

            customScalarMap.put(DotName.createSimple(customScalar.forClass().getName()), customScalar);
        }
    }

    public boolean isScalarType(DotName dotName) {
        return MAPPING.containsKey(dotName) || customScalarMap.containsKey(dotName);
    }

    public int size() {
        return MAPPING.size();
    }

    public GraphQLScalarType getGraphQLScalarType(DotName name, Map<DotName, AnnotationInstance> annotations) {

        if (MAPPING.containsKey(name)) {
            return MAPPING.get(name);
        } else if (customScalarMap.containsKey(name)) {
            GraphQLScalarType newGraphQLScalarType = newGraphQLScalarType(customScalarMap.get(name), annotations);
            MAPPING.put(name, newGraphQLScalarType);
            return newGraphQLScalarType;
        }

        throw new RuntimeException("This is not a scalar type [" + name + "]");
    }

    private GraphQLScalarType newGraphQLScalarType(CustomScalar customScalar, Map<DotName, AnnotationInstance> annotations) {
        return GraphQLScalarType.newScalar()
                .name(customScalar.getName())
                .description(customScalar.getDescription())
                .coercing(new AnnotatedCoercing(customScalar, annotations)).build();
    }

    private static final Map<DotName, GraphQLScalarType> MAPPING = new HashMap<>();

    static {
        MAPPING.put(DotName.createSimple(char.class.getName()), Scalars.GraphQLChar);
        MAPPING.put(DotName.createSimple(Character.class.getName()), Scalars.GraphQLChar);

        MAPPING.put(DotName.createSimple(char[].class.getName()), Scalars.GraphQLString);
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
