/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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

import static io.smallrye.graphql.api.GraphQLResultMode.ERROR_FIELDS;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

/**
 * Instead of directly returning the data of the return type, this annotation wraps it in a <code>Result</code> type,
 * that also contains fields for all declared exceptions,
 * e.g. annotating a query like
 * <br/>
 * <code>SuperHero findByName(String name) throws SuperHeroNotFoundException {...}</code>
 * <br/>
 * results in a GraphQL Schema like this:
 * <br/>
 * 
 * <pre>
 * <code>
 * type SuperHeroResult {
 *   superHero: SuperHero
 *   error_superHeroNotFound: SuperHeroNotFound
 * }
 *
 * type Query {
 *   findByName(name: String): SuperHeroResult
 * }
 * </code>
 * </pre>
 * <p>
 * When placed at a method, only this method is wrapped.
 * When placed at a class, all methods in the class are wrapped.
 * When placed at a package, all methods in all classes in the package are wrapped.
 */
@Retention(RUNTIME)
@Target({ METHOD, TYPE, ANNOTATION_TYPE })
@Experimental("Not covered by the specification. Subject to change.")
public @interface GraphQLResult {
    GraphQLResultMode mode() default ERROR_FIELDS;
}
