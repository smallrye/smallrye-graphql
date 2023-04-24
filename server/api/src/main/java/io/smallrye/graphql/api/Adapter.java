/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.graphql.api;

/**
 * Adapter definition that allow adapting objects to and from each other
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Adapter<From extends Object, To extends Object> {

    /**
     * If the type is a generic type, unwrap and wrap again.
     * So a list of a object, the object will be adapted and added to a new list.
     *
     * @return if we need to unwrap
     */
    default boolean unwrap() {
        return false;
    }

    public To to(From o) throws Exception;

    public From from(To a) throws Exception;

}
