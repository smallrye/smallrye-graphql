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

package io.smallrye.graphql.x.type;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;

import graphql.schema.GraphQLEnumType;
import io.smallrye.graphql.x.Annotations;
import io.smallrye.graphql.x.ObjectBag;
import io.smallrye.graphql.x.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.x.schema.helper.DescriptionHelper;
import io.smallrye.graphql.x.schema.helper.NameHelper;

/**
 * Create a map of all Enums.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EnumTypeCreator {

    private final NameHelper nameHelper = new NameHelper();
    private final DescriptionHelper descriptionHelper = new DescriptionHelper();
    private final AnnotationsHelper annotationsHelper = new AnnotationsHelper();

    private final ObjectBag objectBag;

    public EnumTypeCreator(ObjectBag objectBag) {
        this.objectBag = objectBag;
    }

    public GraphQLEnumType create(ClassInfo classInfo) {
        if (objectBag.getEnumMap().containsKey(classInfo.name())) {
            return objectBag.getEnumMap().get(classInfo.name());
        } else {
            Annotations annotations = annotationsHelper.getAnnotationsForClass(classInfo);
            String name = nameHelper.getEnumName(classInfo, annotations);

            GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
                    .name(name);

            // Description
            Optional<String> maybeDescription = descriptionHelper.getDescriptionForType(annotations);
            builder = builder.description(maybeDescription.orElse(null));

            // Values
            List<FieldInfo> fields = classInfo.fields();
            for (FieldInfo field : fields) {
                if (!field.type().kind().equals(Type.Kind.ARRAY)) {
                    builder = builder.value(field.name());
                }
            }
            GraphQLEnumType enumType = builder.build();
            objectBag.getEnumMap().put(classInfo.name(), enumType);
            return enumType;
        }
    }

}
