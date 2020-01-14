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

import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * Scalar for BigDecimal.
 * Based on graphql-java's Scalars.GraphQLBigDecimal
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BigDecimalScalar extends GraphQLScalarType implements Transformable {
    private static final Logger LOG = Logger.getLogger(BigDecimalScalar.class.getName());

    public BigDecimalScalar() {
        super(Scalars.GraphQLBigDecimal.getName(), "Scalar for " + BigDecimal.class.getName(), new Coercing() {

            private Object convertImpl(Object input) throws NumberFormatException {
                if (input instanceof Number) {
                    return new BigDecimal(input.toString());
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
                } catch (NumberFormatException e) {
                    throw new CoercingSerializeException(
                            "Expected type 'BigDecimal' but was '" + input.getClass().getSimpleName() + "'.", e);
                }
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                try {
                    return convertImpl(input);
                } catch (NumberFormatException e) {
                    throw new CoercingParseValueException(
                            "Expected type 'BigDecimal' but was '" + input.getClass().getSimpleName() + "'.");
                }
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                if (input == null)
                    return null;
                if (input instanceof StringValue) {
                    try {
                        return new BigDecimal(((StringValue) input).getValue());
                    } catch (NumberFormatException e) {
                        throw new CoercingParseLiteralException(
                                "Unable to turn AST input into a 'BigDecimal' : '" + String.valueOf(input) + "'");
                    }
                } else if (input instanceof IntValue) {
                    return new BigDecimal(((IntValue) input).getValue());
                } else if (input instanceof FloatValue) {
                    return ((FloatValue) input).getValue();
                }
                throw new CoercingParseLiteralException(
                        "Expected AST type 'IntValue', 'StringValue' or 'FloatValue' but was '"
                                + input.getClass().getSimpleName() + "'.");
            }
        });
    }
}
