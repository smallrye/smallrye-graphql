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
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * Scalar for Float.
 * Based on graphql-java's Scalars.GraphQLFloat
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FloatScalar extends GraphQLScalarType {
    private static final Logger LOG = Logger.getLogger(FloatScalar.class.getName());

    public FloatScalar() {
        super(Scalars.GraphQLFloat.getName(), "Scalar for " + Float.class.getName(), new Coercing() {

            private Object convertImpl(Object input) throws NumberFormatException {
                if (input instanceof Number) {
                    BigDecimal value = new BigDecimal(input.toString());
                    return value.doubleValue();
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
                            "Expected type 'Float' but was '" + input.getClass().getSimpleName() + "'.", e);
                }
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                try {
                    return convertImpl(input);
                } catch (NumberFormatException | ArithmeticException e) {
                    throw new CoercingParseValueException(
                            "Expected type 'Float' but was '" + input.getClass().getSimpleName() + "'.");
                }
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                if (input == null)
                    return null;
                if (input instanceof IntValue) {
                    return ((IntValue) input).getValue().doubleValue();
                } else if (input instanceof FloatValue) {
                    return ((FloatValue) input).getValue().doubleValue();
                } else {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'IntValue' or 'FloatValue' but was '" + input.getClass().getSimpleName() + "'.");
                }
            }

        });
    }
}
