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

import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.language.IntValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * Scalar for Short.
 * Based on graphql-java's Scalars.GraphQLShort
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ShortScalar extends GraphQLScalarType {
    private static final Logger LOG = Logger.getLogger(ShortScalar.class.getName());

    public ShortScalar() {
        super(Scalars.GraphQLShort.getName(), "Scalar for " + Short.class.getName(), new Coercing() {

            private Object convertImpl(Object input) throws NumberFormatException, ArithmeticException {
                if (input instanceof Short) {
                    return (Short) input;
                } else if (input instanceof Number) {
                    BigDecimal value = new BigDecimal(input.toString());
                    return value.shortValueExact();
                } else if (input instanceof String) {
                    return input;
                } else {
                    throw new NumberFormatException("" + input);
                }
            }

            @Override
            public Object serialize(Object input) throws CoercingSerializeException {
                if (input == null)
                    return null;
                try {
                    return convertImpl(input);
                } catch (NumberFormatException | ArithmeticException e) {
                    throw new CoercingSerializeException(
                            "Expected type 'Short' but was '" + input.getClass().getSimpleName() + "'.", e);
                }
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                try {
                    return convertImpl(input);
                } catch (NumberFormatException | ArithmeticException e) {
                    throw new CoercingParseValueException(
                            "Expected type 'Short' but was '" + input.getClass().getSimpleName() + "'.");
                }
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                if (input == null)
                    return null;
                if (!(input instanceof IntValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'IntValue' but was '" + input.getClass().getSimpleName() + "'.");
                }
                BigInteger value = ((IntValue) input).getValue();
                if (value.compareTo(BigInteger.valueOf(Short.MIN_VALUE)) < 0
                        || value.compareTo(BigInteger.valueOf(Short.MAX_VALUE)) > 0) {
                    throw new CoercingParseLiteralException(
                            "Expected value to be in the Short range but it was '" + value.toString() + "'");
                }
                return value.shortValue();
            }

        });
    }
}
