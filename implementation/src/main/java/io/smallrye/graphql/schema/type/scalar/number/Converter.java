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

import io.smallrye.graphql.schema.Argument;

/**
 * Convert to the correct Type
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Converter {

    public Object fromBigDecimal(BigDecimal bigDecimal);

    public Object fromBigInteger(BigInteger value);

    public Object fromNumber(Number number, Argument argument);

    default boolean isInRange(BigInteger value) {
        return true;
    }

}
