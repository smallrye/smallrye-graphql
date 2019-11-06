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

import java.util.Optional;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.holder.AnnotationsHolder;
import io.smallrye.graphql.holder.TypeHolder;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;

/**
 * Helper to get the correct Description.
 * Basically looking for the @Description annotation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class DescriptionHelper {

    //    private Optional<String> getDescription(MethodInfo methodInfo) {
    //        // See if there is a @Description Annotation on the method
    //        AnnotationInstance descriptionAnnotation = methodInfo.annotation(Annotations.DESCRIPTION);
    //        return getDescription(descriptionAnnotation);
    //    }
    //
    //    private Optional<String> getDescription(AnnotationInstance descriptionAnnotation) {
    //        if (descriptionAnnotation != null) {
    //            AnnotationValue value = descriptionAnnotation.value();
    //            if (value != null) {
    //                return Optional.of(value.asString());
    //            }
    //        }
    //        return Optional.empty();
    //    }

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
        Type type = field.type();
        if (annotationsHolder.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(annotationsHolder.getAnnotation(Annotations.DESCRIPTION).value().asString());
        } else if (isDateLikeTypeOrCollectionThereOf(type)) {
            if (annotationsHolder.containsKeyAndValidValue(Annotations.JSONB_DATE_FORMAT)) {
                return Optional.of(annotationsHolder.getAnnotation(Annotations.JSONB_DATE_FORMAT).value().asString());
            } else {
                // return the default dates format
                if (type.name().equals(Classes.LOCALDATE)) {
                    return Optional.of(ISO_DATE);
                } else if (type.name().equals(Classes.LOCALTIME)) {
                    return Optional.of(ISO_TIME);
                } else if (type.name().equals(Classes.LOCALDATETIME)) {
                    return Optional.of(ISO_DATE_TIME);
                }
            }
        }
        return Optional.empty();
    }

    private boolean isDateLikeTypeOrCollectionThereOf(Type type) {
        switch (type.kind()) {
            case PARAMETERIZED_TYPE:
                // Collections
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return isDateLikeTypeOrCollectionThereOf(typeInCollection);
            case ARRAY:
                // Array
                Type typeInArray = type.asArrayType().component();
                return isDateLikeTypeOrCollectionThereOf(typeInArray);
            default:
                return type.name().equals(Classes.LOCALDATE)
                        || type.name().equals(Classes.LOCALTIME)
                        || type.name().equals(Classes.LOCALDATETIME);
        }
    }

    private static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO_DATE = "yyyy-MM-dd";
    private static final String ISO_TIME = "HH:mm:ss";
}
