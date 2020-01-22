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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.type.scalar.AbstractScalar;
import io.smallrye.graphql.schema.type.scalar.TransformException;

/**
 * Base Scalar for Dates.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractDateScalar extends AbstractScalar {
    private static final Logger LOG = Logger.getLogger(AbstractDateScalar.class.getName());

    private FormatHelper formatHelper = new FormatHelper();

    public AbstractDateScalar(String name, Class... supportedTypes) {
        super(name, new Coercing() {

            private Object convertImpl(Object input) throws DateTimeException {
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
            public Object serialize(Object input) throws CoercingSerializeException {
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
            public Object parseValue(Object input) throws CoercingParseValueException {
                try {
                    return convertImpl(input);
                } catch (DateTimeException e) {
                    throw new CoercingParseValueException(
                            "Expected type '" + name + "' but was '" + input.getClass().getSimpleName() + "'.");
                }
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                if (input == null)
                    return null;

                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");
                }
                return ((StringValue) input).getValue();
            }
        }, supportedTypes);
    }

    protected LocalDate transformToLocalDate(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return LocalDate.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }

    }

    protected LocalDateTime transformToLocalDateTime(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return LocalDateTime.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }
    }

    public LocalTime transformToLocalTime(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return LocalTime.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }

    }
}
