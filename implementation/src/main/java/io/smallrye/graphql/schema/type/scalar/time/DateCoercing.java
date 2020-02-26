/*
 * Copyright 2020 Red Hat, Inc.
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
package io.smallrye.graphql.schema.type.scalar.time;

import java.time.DateTimeException;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

/**
 * The Coercing used by numbers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateCoercing implements Coercing {

    private final Class[] supportedTypes;
    private final String name;

    public DateCoercing(String name, Class... supportedTypes) {
        this.name = name;
        this.supportedTypes = supportedTypes;
    }

    private Object convertImpl(Object input) {
        for (Class supportedType : supportedTypes) {
            if (supportedType.isInstance(input)) {
                return supportedType.cast(input);
            }
        }

        if (input instanceof String) {
            return input;
        } else {
            throw new DateTimeException("" + input);
        }
    }

    // Get's called on startup for @DefaultValue
    @Override
    public Object serialize(Object input) {
        if (input == null)
            return null;
        try {
            return convertImpl(input);
        } catch (DateTimeException e) {
            throw new CoercingSerializeException(
                    "Expected type '" + name + "' but was '" + input.getClass().getSimpleName() + "'.", e);
        }
    }

    @Override
    public Object parseValue(Object input) {
        try {
            return convertImpl(input);
        } catch (DateTimeException e) {
            throw new CoercingParseValueException(
                    "Expected type '" + name + "' but was '" + input.getClass().getSimpleName() + "'.");
        }
    }

    @Override
    public Object parseLiteral(Object input) {
        if (input == null)
            return null;

        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException(
                    "Expected AST type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");
        }
        return ((StringValue) input).getValue();
    }
}
