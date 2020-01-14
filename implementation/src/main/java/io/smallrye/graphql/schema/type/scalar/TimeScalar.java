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

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.helper.FormatHelper;

/**
 * Scalar for Time.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TimeScalar extends GraphQLScalarType implements Transformable {
    private static final Logger LOG = Logger.getLogger(TimeScalar.class.getName());

    private FormatHelper formatHelper = new FormatHelper();

    public TimeScalar() {
        super("Time", "Scalar for " + LocalTime.class.getName(), new Coercing() {

            private Object convertImpl(Object input) throws DateTimeException {
                LOG.error(">>>>>>>>> convertImpl = " + input.getClass().getName());
                for (Class supportedType : SUPPORTED_TYPES) {
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
                LOG.error("===== serialize [" + input + "]");
                if (input == null)
                    return null;
                try {
                    return convertImpl(input);
                } catch (DateTimeException e) {
                    throw new CoercingSerializeException(
                            "Expected type 'Date' but was '" + input.getClass().getSimpleName() + "'.", e);
                }
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                LOG.error("===== parseValue [" + input + "]");
                try {
                    return convertImpl(input);
                } catch (DateTimeException e) {
                    throw new CoercingParseValueException(
                            "Expected type 'Date' but was '" + input.getClass().getSimpleName() + "'.");
                }
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                LOG.error("===== parseLiteral [" + input + "]");
                if (input == null)
                    return null;

                LOG.error(">>>>>>>>> parseLiteral = " + input.getClass().getName());

                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");
                }
                return ((StringValue) input).getValue();
            }

        });
    }

    @Override
    public Object transform(String name, String input, Type type) {

        // TODO: Check if there is a formatting annotation and apply
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, null);
            LocalTime localTime = LocalTime.parse(input, dateFormat);
            if (type.name().equals(Classes.LOCALTIME)) {
                return localTime;
            } else if (type.name().equals(Classes.SQL_TIME)) {
                return toSqlDate(localTime);
            } else {
                LOG.warn("Can not transform type [" + type.name() + "] with TimeScalar");
                return input;
            }
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }

    }

    private java.sql.Time toSqlDate(LocalTime dateToConvert) {
        return java.sql.Time.valueOf(dateToConvert);
    }

    public static final List<Class> SUPPORTED_TYPES = new ArrayList<>();

    static {
        SUPPORTED_TYPES.add(LocalTime.class);
        SUPPORTED_TYPES.add(java.sql.Time.class);
    }

}
