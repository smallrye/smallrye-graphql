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

package io.smallrye.graphql.bootstrap.schema.helper;

import java.util.Optional;

import io.smallrye.graphql.bootstrap.Annotations;

/**
 * Helping to figure out if there is a default value.
 * Looking for the @DefaultValue annotation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DefaultValueHelper {

    public Optional<Object> getDefaultValue(Annotations... annotations) {
        for (Annotations a : annotations) {
            if (a.containsKeyAndValidValue(Annotations.DEFAULT_VALUE)) {
                return Optional.of(a.getAnnotationValue(Annotations.DEFAULT_VALUE).value());
            }
        }
        return Optional.empty();
    }

}
