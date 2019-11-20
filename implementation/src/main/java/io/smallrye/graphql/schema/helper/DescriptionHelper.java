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

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.schema.holder.AnnotationsHolder;
import io.smallrye.graphql.schema.holder.TypeHolder;

/**
 * Helper to get the correct Description.
 * Basically looking for the @Description annotation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class DescriptionHelper {

    @Inject
    private DateHelper dateHelper;

    public Optional<String> getDescription(TypeHolder typeHolder) {
        return getDescription(typeHolder.getAnnotations());
    }

    public Optional<String> getDescription(AnnotationsHolder annotationsHolder) {
        if (annotationsHolder.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(annotationsHolder.getAnnotationValue(Annotations.DESCRIPTION).asString());
        }

        return Optional.empty();
    }

    public Optional<String> getDescription(AnnotationsHolder annotationsHolder, FieldInfo field) {
        return getDescription(annotationsHolder, field.type());
    }

    public Optional<String> getDescription(AnnotationsHolder annotationsHolder, Type type) {
        if (annotationsHolder.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(annotationsHolder.getAnnotation(Annotations.DESCRIPTION).value().asString());
        } else if (dateHelper.isDateLikeTypeOrCollectionThereOf(type)) {
            if (annotationsHolder.containsKeyAndValidValue(Annotations.JSONB_DATE_FORMAT)) {
                return Optional.of(annotationsHolder.getAnnotation(Annotations.JSONB_DATE_FORMAT).value().asString());
            } else {
                // return the default dates format
                return Optional.of(dateHelper.getDefaultFormat(type));
            }
        }
        return Optional.empty();
    }
}
