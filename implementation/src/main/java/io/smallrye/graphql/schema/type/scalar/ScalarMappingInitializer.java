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
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

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
    public Map<DotName, GraphQLScalarType> getScalarMap() {
        return MAPPING;
    }

    private static final Map<DotName, GraphQLScalarType> MAPPING = new HashMap<>();

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
        for (Class c : LongScalar.SUPPORTED_TYPES) {
            MAPPING.put(DotName.createSimple(c.getName()), longScalar);
        }

        DateScalar dateScalar = new DateScalar();
        for (Class c : DateScalar.SUPPORTED_TYPES) {
            MAPPING.put(DotName.createSimple(c.getName()), dateScalar);
        }

        TimeScalar timeScalar = new TimeScalar();
        for (Class c : TimeScalar.SUPPORTED_TYPES) {
            MAPPING.put(DotName.createSimple(c.getName()), timeScalar);
        }

        DateTimeScalar dateTimeScalar = new DateTimeScalar();
        for (Class c : DateTimeScalar.SUPPORTED_TYPES) {
            MAPPING.put(DotName.createSimple(c.getName()), dateTimeScalar);
        }

    }

}
