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

/**
 * Specifies that the annotated method provides the implementation (ie. the
 * resolver) for a GraphQL ubscription. <br>
 * <br>
 * For example, a user might annotate a method as such:
 * 
 * <pre>
 * public class CharacterService {
 *     {@literal @}Subscription
 *     {@literal @}Description("Get stock quote changes as they happen")
 *     public Publisher{@literal <}Stock{@literal >} getStockQuote(String stockCode) {
 *         //...
 *     }
 * }
 * </pre>
 *
 * Schema generation of this would result in a stanza such as:
 * 
 * <pre>
 * type Subscription {
 *    "Get stock quote changes as they happen"
 *    stockQuote(stockCode: string): [Stock]
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Subscription {
    /**
     * @return the name to use for the subscription. If empty, annotated method's name is used.
     */
    String value() default "";
}