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

package io.smallrye.graphql.bootstrap.type;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;

import graphql.schema.GraphQLEnumType;
import io.smallrye.graphql.bootstrap.Annotations;
import io.smallrye.graphql.bootstrap.ObjectBag;
import io.smallrye.graphql.bootstrap.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.bootstrap.schema.helper.DescriptionHelper;
import io.smallrye.graphql.bootstrap.schema.helper.NameHelper;

/**
 * Create a map of all Enums.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EnumTypeCreator {

    private final NameHelper nameHelper = new NameHelper();
    private final DescriptionHelper descriptionHelper = new DescriptionHelper();
    private final AnnotationsHelper annotationsHelper = new AnnotationsHelper();

    public GraphQLEnumType create(ClassInfo classInfo) {
        if (ObjectBag.ENUM_MAP.containsKey(classInfo.name())) {
            return ObjectBag.ENUM_MAP.get(classInfo.name());
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
            ObjectBag.ENUM_MAP.put(classInfo.name(), enumType);
            return enumType;
        }
    }

}
