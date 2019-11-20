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

package io.smallrye.graphql.schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLEnumType;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.holder.TypeHolder;

/**
 * Create a map of all Enums.
 * 
 * It produces a maps, that can be injected anywhere in the code:
 * - enumMap - contains all enum types.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class EnumTypeInitializer {
    private static final Logger LOG = Logger.getLogger(EnumTypeInitializer.class.getName());

    @Inject
    @Named("enum")
    private Map<DotName, TypeHolder> enums;

    @Produces
    private final Map<DotName, GraphQLEnumType> enumMap = new HashMap<>();

    @Inject
    private NameHelper nameHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @PostConstruct
    void init() {
        for (Map.Entry<DotName, TypeHolder> e : enums.entrySet()) {
            this.enumMap.put(e.getKey(), createEnumType(e.getValue()));
            LOG.debug("adding [" + e.getKey() + "] to the enums list");
        }

    }

    private GraphQLEnumType createEnumType(TypeHolder typeHolder) {
        String name = nameHelper.getEnumName(typeHolder);
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
                .name(name);

        // Description
        Optional<String> maybeDescription = descriptionHelper.getDescription(typeHolder);
        builder = builder.description(maybeDescription.orElse(null));

        // Values
        List<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {
            if (!field.type().kind().equals(Type.Kind.ARRAY)) {
                builder = builder.value(field.name());
            }
        }
        return builder.build();
    }

}
