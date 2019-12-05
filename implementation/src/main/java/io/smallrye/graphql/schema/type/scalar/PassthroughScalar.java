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

    public PassthroughScalar(String name, String description) {
        super(name, description, new Coercing() {
            @Override
            public Object serialize(Object o) throws CoercingSerializeException {
                return o;
            }

            @Override
            public Object parseValue(Object o) throws CoercingParseValueException {
                return o;
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

        });
    }
}
