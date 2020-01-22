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

package io.smallrye.graphql.schema.helper;

import javax.enterprise.context.Dependent;

import io.smallrye.graphql.schema.Annotations;

/**
 * Helping to figure out if we should ignore a field.
 * Looking for the @Ignore and other relevant annotations.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class IgnoreHelper {

    public boolean shouldIgnore(Annotations annotations) {
        return annotations.containsOneOfTheseKeys(Annotations.IGNORE,
                Annotations.JSONB_TRANSIENT);
    }
}
