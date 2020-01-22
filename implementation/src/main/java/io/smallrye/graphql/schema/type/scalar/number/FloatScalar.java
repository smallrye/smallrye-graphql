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

import org.jboss.jandex.DotName;

import graphql.Scalars;
import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.Classes;

/**
 * Scalar for Float.
 * Based on graphql-java's Scalars.GraphQLFloat
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FloatScalar extends AbstractNumberScalar {

    public FloatScalar() {

        super(Scalars.GraphQLFloat.getName(),
                new Converter() {
                    @Override
                    public Object fromBigDecimal(BigDecimal bigDecimal) {
                        return bigDecimal.floatValue();
                    }

                    @Override
                    public Object fromBigInteger(BigInteger bigInteger) {
                        return bigInteger.floatValue();
                    }

                    @Override
                    public Object fromNumber(Number number, Argument argument) {
                        DotName argumentName = argument.getType().name();

                        if (argumentName.equals(Classes.FLOAT) || argumentName.equals(Classes.FLOAT_PRIMATIVE)) {
                            return number.floatValue();
                        } else {
                            return number.doubleValue();
                        }
                    }
                },
                Float.class, float.class, Double.class, double.class);
    }

}
