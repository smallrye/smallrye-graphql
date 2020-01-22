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
package io.smallrye.graphql.schema.type.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import graphql.Scalars;
import io.smallrye.graphql.schema.Argument;

/**
 * Scalar for Short.
 * Based on graphql-java's Scalars.GraphQLShort
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ShortScalar extends AbstractNumberScalar {

    public ShortScalar() {

        super(Scalars.GraphQLShort.getName(),
                new Converter() {
                    @Override
                    public Object fromBigDecimal(BigDecimal bigDecimal) {
                        return bigDecimal.shortValueExact();
                    }

                    @Override
                    public Object fromBigInteger(BigInteger bigInteger) {
                        return bigInteger.shortValue();
                    }

                    @Override
                    public Object fromNumber(Number number, Argument argument) {
                        return number.shortValue();
                    }

                    @Override
                    public boolean isInRange(BigInteger value) {
                        return (value.compareTo(BigInteger.valueOf(Short.MIN_VALUE)) < 0
                                || value.compareTo(BigInteger.valueOf(Short.MIN_VALUE)) > 0);
                    }

                },
                Short.class, short.class);
    }

}
