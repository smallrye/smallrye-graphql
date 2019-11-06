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

package io.smallrye.graphql.type.scalar;

import java.lang.reflect.ParameterizedType;

/**
 * Implementations of this interface can provider scalars
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <T> This is the Type as represented in Java
 * @param <R> This is the Return Type in the Schema
 */
public interface CustomScalar<T, R> {
    public String getName();

    public String getDescription();

    public R serialize(T fromObject);

    public T deserialize(R fromScalar);

    public default Class<T> forClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }
}
