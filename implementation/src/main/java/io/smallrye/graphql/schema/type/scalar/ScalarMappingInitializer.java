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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;
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
public class ScalarMappingInitializer {
    private static final Logger LOG = Logger.getLogger(ScalarMappingInitializer.class.getName());

    // Provide your own list like this > SomeName:some.class.Name,...
    @Inject
    @ConfigProperty(name = "mp.graphql.passthroughScalars", defaultValue = "")
    private List<String> passthroughScalars;

    @Produces
    private final Map<DotName, GraphQLScalarType> scalarMap = new HashMap<>();

    @PostConstruct
    void init() {
        scalarMap.putAll(MAPPING);
        addPassthroughScalars();
    }

    @Produces
    @Named("scalars")
    public Set<DotName> getKnownScalars() {
        return scalarMap.keySet();
    }

    private void addPassthroughScalars() {
        LOG.info("Adding all default passthrough scalars...");
        for (Map.Entry<String, List<String>> entry : PASS_THROUGH_SCALARS.entrySet()) {
            String name = entry.getKey();
            List<String> values = entry.getValue();
            for (String className : values) {
                addToScalarMap(name, className);
            }
        }

        if (passthroughScalars != null && !passthroughScalars.isEmpty()) {
            LOG.info("Adding all user configured passthrough scalars...");
            for (String kv : passthroughScalars) {
                String[] keyValue = kv.split(":");
                String name = keyValue[0];
                String className = keyValue[1];
                addToScalarMap(name, className);
            }
        }
    }

    private void addToScalarMap(String name, String className) {
        LOG.info("\t found scalar type [" + name + "] that maps to [" + className + "]");

        PassthroughScalar passthroughScalar = new PassthroughScalar(name,
                "Scalar for " + className + "that just let the value pass though");

        scalarMap.put(DotName.createSimple(className), passthroughScalar);
    }

    private static final Map<DotName, GraphQLScalarType> MAPPING = new HashMap<>();
    private static final Map<String, List<String>> PASS_THROUGH_SCALARS = new HashMap<>();

    static {
        MAPPING.put(DotName.createSimple(char.class.getName()), Scalars.GraphQLChar);
        MAPPING.put(DotName.createSimple(Character.class.getName()), Scalars.GraphQLChar);

        MAPPING.put(DotName.createSimple(String.class.getName()), Scalars.GraphQLString);
        MAPPING.put(DotName.createSimple(UUID.class.getName()), Scalars.GraphQLString);

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

        PASS_THROUGH_SCALARS.computeIfAbsent("Date", k -> new ArrayList<>()).add(LocalDate.class.getName());
        PASS_THROUGH_SCALARS.computeIfAbsent("Date", k -> new ArrayList<>()).add(Date.class.getName());
        PASS_THROUGH_SCALARS.computeIfAbsent("Date", k -> new ArrayList<>()).add(java.sql.Date.class.getName());
        PASS_THROUGH_SCALARS.computeIfAbsent("Time", k -> new ArrayList<>()).add(LocalTime.class.getName());
        PASS_THROUGH_SCALARS.computeIfAbsent("DateTime", k -> new ArrayList<>()).add(LocalDateTime.class.getName());
    }

}
