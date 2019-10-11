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
package io.smallrye.graphql.scalar.buildin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.smallrye.graphql.scalar.CustomScalar;
import io.smallrye.graphql.scalar.CustomScalarMarker;

/**
 * Create a Scalar for LocalDateTime
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 *         TODO: Handle format ?? Default is ISO_DATE_TIME (yyyy-MM-dd)
 *         TODO: Exception and Literal
 * 
 */
@CustomScalarMarker
public class LocalDateTimeScalarProvider implements CustomScalar<LocalDateTime, String> {

    @Override
    public String getName() {
        return "DateTime";
    }

    @Override
    public String getDescription() {
        return "DateTime Scalar";
    }

    @Override
    public String serialize(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public LocalDateTime deserialize(String fromScalar) {
        return LocalDateTime.parse(fromScalar);
    }

}
