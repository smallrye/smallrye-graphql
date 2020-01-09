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

import java.math.BigInteger;

import org.jboss.logging.Logger;

import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * Just passing the values along.
 * This is used when we want to interpret, on run-time, the annotations and transform using that.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PassthroughScalar extends GraphQLScalarType {
    private static final Logger LOG = Logger.getLogger(PassthroughScalar.class.getName());

    public PassthroughScalar(String name, String description) {
        super(name, description, new Coercing() {

            @Override
            public Object serialize(Object input) throws CoercingSerializeException {
                return input;
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                return input;
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                if (input == null || input.getClass().getName().equals(NullValue.class.getName())) {
                    return null;
                } else if (input.getClass().getName().equals(StringValue.class.getName())) {
                    StringValue stringValue = StringValue.class.cast(input);
                    String value = stringValue.getValue();
                    return parseValue(value);
                } else if (input.getClass().getName().equals(IntValue.class.getName())) {
                    IntValue intValue = IntValue.class.cast(input);
                    BigInteger value = intValue.getValue();
                    return parseValue(value);
                } else {
                    // TODO: Other types ? ArrayValue, BooleanValue, EnumValue, FloatValue, ObjectValue, ScalarValue;

                    LOG.warn("Not handling literal [" + input + "] of type [" + input.getClass().getName()
                            + "] - returning default");
                    return parseValue(input);
                }
            }

        });
    }
}
