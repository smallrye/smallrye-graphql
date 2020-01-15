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
package io.smallrye.graphql.schema.type.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

import org.jboss.jandex.AnnotationInstance;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.type.scalar.AbstractScalar;
import io.smallrye.graphql.schema.type.scalar.TransformException;

/**
 * Base Scalar for Numbers.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractNumberScalar extends AbstractScalar {
    private final FormatHelper formatHelper = new FormatHelper();
    private final Converter converter;

    public <T> AbstractNumberScalar(String name,
            Converter converter,
            Class... supportedTypes) {

        super(name, new Coercing() {

            private Object convertImpl(Object input) throws NumberFormatException {

                for (Class supportedClass : supportedTypes) {
                    if (supportedClass.isInstance(input)) {
                        return supportedClass.cast(input);
                    }
                }

                if (input instanceof Number) {
                    BigDecimal value = new BigDecimal(input.toString());
                    return converter.fromBigDecimal(value);
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
                            "Expected type '" + name + "' but was '" + input.getClass().getSimpleName() + "'.", e);
                }
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                try {
                    return convertImpl(input);
                } catch (NumberFormatException e) {
                    throw new CoercingParseValueException(
                            "Expected type '" + name + "' but was '" + input.getClass().getSimpleName() + "'.");
                }
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                if (input == null)
                    return null;
                if (input instanceof StringValue) {
                    try {
                        BigDecimal value = new BigDecimal(((StringValue) input).getValue());
                        return converter.fromBigDecimal(value);
                    } catch (NumberFormatException e) {
                        // Here we allow strings through becauce of Numberformatting.
                        return ((StringValue) input).getValue();
                    }

                } else if (input instanceof IntValue) {
                    BigInteger value = ((IntValue) input).getValue();
                    if (!converter.isInRange(value)) {
                        throw new CoercingParseLiteralException(
                                "Expected value to be in the " + name + " range but it was '" + value.toString() + "'");
                    }
                    return converter.fromBigInteger(value);

                } else if (input instanceof FloatValue) {
                    BigDecimal value = ((FloatValue) input).getValue();
                    return converter.fromBigDecimal(value);
                }
                throw new CoercingParseLiteralException(
                        "Expected AST type 'IntValue' or 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");
            }

        }, supportedTypes);

        this.converter = converter;
    }

    @Override
    public Object transform(Object input, Argument argument) {
        NumberFormat numberFormat = formatHelper.getNumberFormat(getJsonBAnnotation(argument.getAnnotations()));
        if (numberFormat != null) {
            try {
                Number number = numberFormat.parse(input.toString());
                return converter.fromNumber(number);
            } catch (ParseException ex) {
                throw new TransformException(ex, this, argument.getName(), input.toString());
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
}