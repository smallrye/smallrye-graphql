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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.helper.FormatHelper;

/**
 * Scalar for Long.
 * Based on graphql-java's Scalars.GraphQLLong
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class LongScalar extends GraphQLScalarType implements Transformable {
    private static final Logger LOG = Logger.getLogger(LongScalar.class.getName());

    private FormatHelper formatHelper = new FormatHelper();

    public LongScalar() {
        super(Scalars.GraphQLLong.getName(), "Scalar for " + Long.class.getName(), new Coercing() {

            private Object convertImpl(Object input) throws NumberFormatException, ArithmeticException {
                if (input instanceof Long) {
                    return (Long) input;
                } else if (input instanceof Number) {
                    BigDecimal value = new BigDecimal(input.toString());
                    return value.longValueExact();
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
                            "Expected type 'Long' but was '" + input.getClass().getSimpleName() + "'.", e);
                }
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                try {
                    return convertImpl(input);
                } catch (NumberFormatException | ArithmeticException e) {
                    throw new CoercingParseValueException(
                            "Expected type 'Long' but was '" + input.getClass().getSimpleName() + "'.");
                }
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                if (input == null)
                    return null;
                if (input instanceof StringValue) {
                    // Here we allow strings through becauce of Numberformatting.
                    return ((StringValue) input).getValue();
                } else if (input instanceof IntValue) {
                    BigInteger value = ((IntValue) input).getValue();
                    if (value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0
                            || value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                        throw new CoercingParseLiteralException(
                                "Expected value to be in the Long range but it was '" + value.toString() + "'");
                    }
                    return value.longValue();
                }
                throw new CoercingParseLiteralException(
                        "Expected AST type 'IntValue' or 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");
            }

        });
    }

    @Override
    public Object transform(String name, String input, Type type, Annotations annotations) {

        NumberFormat numberFormat = formatHelper.getNumberFormat(getJsonBAnnotation(annotations));
        if (numberFormat != null) {
            try {
                Number number = numberFormat.parse(input);
                return number.longValue();
            } catch (ParseException ex) {
                throw new TransformException(ex, this, name, input);
            }
        }
        return input;
    }

    private AnnotationInstance getJsonBAnnotation(Annotations annotations) {
        if (annotations.containsOnOfTheseKeys(Annotations.JSONB_NUMBER_FORMAT)) {
            return annotations.getAnnotation(Annotations.JSONB_NUMBER_FORMAT);
        }
        return null;
    }

    public static final List<Class> SUPPORTED_TYPES = new ArrayList<>();

    static {
        SUPPORTED_TYPES.add(Long.class);
        SUPPORTED_TYPES.add(long.class);
    }
}
