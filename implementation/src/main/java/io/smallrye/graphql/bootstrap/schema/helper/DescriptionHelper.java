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

import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.bootstrap.Annotations;

/**
 * Helper to get the correct Description.
 * Basically looking for the @Description annotation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DescriptionHelper {

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
            String dateFormat = formatHelper.getDateFormat(annotations, type);
            if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
                return Optional.of(getGivenDescription(annotations) + " (" + dateFormat + ")");
            } else {
                return Optional.of(dateFormat);
            }
        } else if (formatHelper.isNumberLikeTypeOrCollectionThereOf(type)) {
            Optional<String> numberFormat = formatHelper.getNumberFormatValue(annotations);
            if (numberFormat.isPresent()) {
                if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
                    return Optional.of(getGivenDescription(annotations) + " (" + numberFormat.get() + ")");
                } else {
                    return numberFormat;
                }
            }
        }

        if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(getGivenDescription(annotations));
        } else {
            return Optional.empty();
        }
    }

    private String getGivenDescription(Annotations annotations) {
        return annotations.getAnnotation(Annotations.DESCRIPTION).value().asString();
    }

    private final FormatHelper formatHelper = new FormatHelper();
}
