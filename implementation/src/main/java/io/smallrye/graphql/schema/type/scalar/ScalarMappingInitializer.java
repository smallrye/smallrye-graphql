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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

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

    @Produces
    private final Map<DotName, GraphQLScalarType> scalarMap = new HashMap<>();

    @PostConstruct
    void init() {
        scalarMap.putAll(MAPPING);
    }

    @Produces
    @Named("scalars")
    public Set<DotName> getKnownScalars() {
        return scalarMap.keySet();
    }

    private static final Map<DotName, GraphQLScalarType> MAPPING = new HashMap<>();

    private static PassthroughScalar passthroughScalar(String name) {
        return new PassthroughScalar(name, "Scalar for " + name + "that just let the value pass though");
    }

    static {
        MAPPING.put(DotName.createSimple(char.class.getName()), Scalars.GraphQLChar);
        MAPPING.put(DotName.createSimple(Character.class.getName()), Scalars.GraphQLChar);

        MAPPING.put(DotName.createSimple(String.class.getName()), Scalars.GraphQLString);
        MAPPING.put(DotName.createSimple(UUID.class.getName()), Scalars.GraphQLString);

        ShortScalar shortScalar = new ShortScalar();
        MAPPING.put(DotName.createSimple(Short.class.getName()), shortScalar);
        MAPPING.put(DotName.createSimple(short.class.getName()), shortScalar);

        IntegerScalar integerScalar = new IntegerScalar();
        MAPPING.put(DotName.createSimple(Integer.class.getName()), integerScalar);
        MAPPING.put(DotName.createSimple(int.class.getName()), integerScalar);

        FloatScalar floatScalar = new FloatScalar();
        MAPPING.put(DotName.createSimple(Float.class.getName()), floatScalar);
        MAPPING.put(DotName.createSimple(float.class.getName()), floatScalar);
        MAPPING.put(DotName.createSimple(Double.class.getName()), floatScalar);
        MAPPING.put(DotName.createSimple(double.class.getName()), floatScalar);

        MAPPING.put(DotName.createSimple(Boolean.class.getName()), Scalars.GraphQLBoolean);
        MAPPING.put(DotName.createSimple(boolean.class.getName()), Scalars.GraphQLBoolean);

        ByteScalar byteScalar = new ByteScalar();
        MAPPING.put(DotName.createSimple(Byte.class.getName()), byteScalar);
        MAPPING.put(DotName.createSimple(byte.class.getName()), byteScalar);

        BigIntegerScalar bigIntegerScalar = new BigIntegerScalar();
        MAPPING.put(DotName.createSimple(BigInteger.class.getName()), bigIntegerScalar);

        BigDecimalScalar bigDecimalScalar = new BigDecimalScalar();
        MAPPING.put(DotName.createSimple(BigDecimal.class.getName()), bigDecimalScalar);

        LongScalar longScalar = new LongScalar();
        MAPPING.put(DotName.createSimple(Long.class.getName()), longScalar);
        MAPPING.put(DotName.createSimple(long.class.getName()), longScalar);

        PassthroughScalar dateScalar = passthroughScalar("Date");
        MAPPING.put(DotName.createSimple(LocalDate.class.getName()), dateScalar);
        MAPPING.put(DotName.createSimple(Date.class.getName()), dateScalar);
        MAPPING.put(DotName.createSimple(java.sql.Date.class.getName()), dateScalar);

        PassthroughScalar timeScalar = passthroughScalar("Time");
        MAPPING.put(DotName.createSimple(LocalTime.class.getName()), timeScalar);

        PassthroughScalar dateTimeScalar = passthroughScalar("DateTime");
        MAPPING.put(DotName.createSimple(LocalDateTime.class.getName()), dateTimeScalar);

    }

}
