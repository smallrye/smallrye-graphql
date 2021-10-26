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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

/**
 * Allow adapting objects using an Adapter class<br>
 * This is an experimental feature that might move to the spec.
 * <br>
 * Example:
 * 
 * <pre>
 * public class Profile {
 *     //  Map a EmailAdress to an Address
 *     {@literal @}AdaptWith(EmailAdapter.class)
 *     private Address email;
 *
 *     // other getters/setters...
 * }
 * </pre>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Documented
@Experimental("Allow you to map to and from another object. Not covered by the specification. " +
        "Subject to change.")
public @interface AdaptWith {

    public Class<? extends Adapter> value();
}
