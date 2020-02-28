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
package io.smallrye.graphql.bootstrap.type.number;

import java.text.NumberFormat;
import java.text.ParseException;

import io.smallrye.graphql.bootstrap.Argument;
import io.smallrye.graphql.bootstrap.TransformException;
import io.smallrye.graphql.bootstrap.schema.helper.FormatHelper;
import io.smallrye.graphql.bootstrap.type.AbstractScalar;

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

        super(name, new NumberCoercing(name, converter, supportedTypes), supportedTypes);

        this.converter = converter;
    }

    @Override
    public Object transform(Object input, Argument argument) {
        NumberFormat numberFormat = formatHelper.getNumberFormat(argument.getAnnotations());
        if (numberFormat != null) {
            try {
                Number number = numberFormat.parse(input.toString());
                return converter.fromNumber(number, argument);
            } catch (ParseException ex) {
                throw new TransformException(ex, this, argument.getName(), input.toString());
            }
        }
        return input;

    }
}
