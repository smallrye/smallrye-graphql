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
package io.smallrye.graphql.x.type.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.jandex.DotName;

import graphql.Scalars;
import io.smallrye.graphql.x.Argument;
import io.smallrye.graphql.x.Classes;

/**
 * Scalar for BigInteger.
 * Based on graphql-java's Scalars.GraphQLBigInteger
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BigIntegerScalar extends AbstractNumberScalar {

    public BigIntegerScalar() {

        super(Scalars.GraphQLBigInteger.getName(),
                new Converter() {
                    @Override
                    public Object fromBigDecimal(BigDecimal bigDecimal) {
                        return bigDecimal.toBigIntegerExact();
                    }

                    @Override
                    public Object fromBigInteger(BigInteger bigInteger) {
                        return bigInteger;
                    }

                    @Override
                    public Object fromNumber(Number number, Argument argument) {
                        DotName argumentName = argument.getType().name();

                        if (argumentName.equals(Classes.LONG) || argumentName.equals(Classes.LONG_PRIMATIVE)) {
                            return number.longValue();
                        } else {
                            return new BigInteger(number.toString());
                        }
                    }
                },
                BigInteger.class, Long.class, long.class);
    }

}
