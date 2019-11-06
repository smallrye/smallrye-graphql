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

package io.smallrye.graphql.helper;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.holder.AnnotationsHolder;
import io.smallrye.graphql.index.Annotations;

/**
 * Helping to figure out of some should be marked as Non null
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class NonNullHelper {
    private static final Logger LOG = Logger.getLogger(NonNullHelper.class.getName());

    public boolean markAsNonNull(Type type, AnnotationsHolder annotations) {
        // check if the @NonNull annotation is present
        boolean hasNonNull = hasNonNull(annotations);
        // true if this is a primitive
        if (type.kind().equals(Type.Kind.PRIMITIVE)) {
            hasNonNull = true; // By implication
        }

        // check if the @DefaultValue annotation is present
        boolean hasDefaultValue = hasDefaultValue(annotations);
        if (hasDefaultValue) {
            if (hasNonNull) {
                LOG.warn("Ignoring non null on [" + type.name() + "] as there is a @DefaultValue");
            }
            return false;
        }

        return hasNonNull;
    }

    private boolean hasNonNull(AnnotationsHolder annotations) {

        return annotations.containsOnOfTheseKeys(Annotations.NON_NULL,
                Annotations.BEAN_VALIDATION_NOT_NULL,
                Annotations.BEAN_VALIDATION_NOT_EMPTY,
                Annotations.BEAN_VALIDATION_NOT_BLANK);
    }

    private boolean hasDefaultValue(AnnotationsHolder annotations) {
        return annotations.containsKeyAndValidValue(Annotations.DEFAULT_VALUE);
    }
}
