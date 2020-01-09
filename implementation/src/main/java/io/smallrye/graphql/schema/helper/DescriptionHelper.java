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

import io.smallrye.graphql.schema.Annotations;

/**
 * Helper to get the correct Description.
 * Basically looking for the @Description annotation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class DescriptionHelper {

    @Inject
    private FormatHelper formatHelper;

    public Optional<String> getDescription(Annotations annotations) {
        if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(annotations.getAnnotationValue(Annotations.DESCRIPTION).asString());
        }
        return Optional.empty();
    }

    public Optional<String> getDescription(Annotations annotations, FieldInfo field) {
        return getDescription(annotations, field.type());
    }

    public Optional<String> getDescription(Annotations annotations, Type type) {
        if (formatHelper.isDateLikeTypeOrCollectionThereOf(type)) {
            String dateFormat = getDateFormat(annotations, type);
            if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
                return Optional.of(getGivenDescription(annotations) + " (" + dateFormat + ")");
            } else {
                return Optional.of(dateFormat);
            }
            // TODO: Add Number formatting    
        } else if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(getGivenDescription(annotations));
        } else {
            return Optional.empty();
        }
    }

    private String getDateFormat(Annotations annotations, Type type) {
        if (annotations.containsKeyAndValidValue(Annotations.JSONB_DATE_FORMAT)) {
            return annotations.getAnnotation(Annotations.JSONB_DATE_FORMAT).value().asString();
        } else {
            // return the default dates format
            return formatHelper.getDefaultFormat(type);
        }
    }

    private String getGivenDescription(Annotations annotations) {
        return annotations.getAnnotation(Annotations.DESCRIPTION).value().asString();
    }
}
