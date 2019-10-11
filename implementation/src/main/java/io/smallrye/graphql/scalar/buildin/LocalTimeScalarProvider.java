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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import io.smallrye.graphql.scalar.CustomScalar;
import io.smallrye.graphql.scalar.CustomScalarMarker;

/**
 * Create a Scalar for LocalTime
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 *         TODO: Handle format ?? Default is ISO_TIME (yyyy-MM-dd)
 *         TODO: Exception and Literal
 * 
 */
@CustomScalarMarker
public class LocalTimeScalarProvider implements CustomScalar<LocalTime, String> {

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public String getDescription() {
        return "Time Scalar";
    }

    @Override
    public String serialize(LocalTime localTime) {
        return localTime.format(DateTimeFormatter.ISO_TIME);
    }

    @Override
    public LocalTime deserialize(String fromScalar) {
        return LocalTime.parse(fromScalar);
    }

}
