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

package io.smallrye.graphql.schema;

import java.util.Map;
import java.util.Optional;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbNumberFormat;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Ignore;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Interface;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.NumberFormat;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.eclipse.microprofile.graphql.Type;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

/**
 * All the annotations we care about for a certain context
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Annotations {

    private final Map<DotName, AnnotationInstance> annotations;

    public Annotations(Map<DotName, AnnotationInstance> annotations) {
        this.annotations = annotations;
    }

    public Map<DotName, AnnotationInstance> getAnnotations() {
        return this.annotations;
    }

    public AnnotationInstance getAnnotation(DotName key) {
        return this.annotations.get(key);
    }

    public AnnotationValue getAnnotationValue(DotName key) {
        return this.annotations.get(key).value();
    }

    public boolean containsOneOfTheseKeys(DotName... key) {
        for (DotName name : key) {
            if (this.annotations.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasGraphQLAnnotations() {
        for (DotName dotName : annotations.keySet()) {
            if (dotName.toString().startsWith(GRAPHQL_PACKAGE)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasGraphQLFormatingAnnotations() {
        for (DotName dotName : annotations.keySet()) {
            if (dotName.equals(DATE_FORMAT) ||
                    dotName.equals(NUMBER_FORMAT)) {
                return true;
            }
        }
        return false;
    }

    public Optional<AnnotationInstance> getOneOfTheseAnnotation(DotName... key) {
        for (DotName name : key) {
            if (this.annotations.containsKey(name)) {
                return Optional.of(this.annotations.get(name));
            }
        }
        return Optional.empty();
    }

    public boolean containsKeyAndValidValue(DotName key) {
        return this.annotations.containsKey(key) && this.annotations.get(key).value() != null;
    }

    public static final DotName GRAPHQL_API = DotName.createSimple(GraphQLApi.class.getName());
    public static final DotName QUERY = DotName.createSimple(Query.class.getName());
    public static final DotName MUTATION = DotName.createSimple(Mutation.class.getName());
    public static final DotName INPUT = DotName.createSimple(Input.class.getName());
    public static final DotName TYPE = DotName.createSimple(Type.class.getName());
    public static final DotName INTERFACE = DotName.createSimple(Interface.class.getName());
    public static final DotName ENUM = DotName.createSimple(org.eclipse.microprofile.graphql.Enum.class.getName());

    public static final DotName ID = DotName.createSimple(Id.class.getName());
    public static final DotName DESCRIPTION = DotName.createSimple(Description.class.getName());
    public static final DotName JSONB_DATE_FORMAT = DotName.createSimple(JsonbDateFormat.class.getName());
    public static final DotName JSONB_NUMBER_FORMAT = DotName.createSimple(JsonbNumberFormat.class.getName());
    public static final DotName JSONB_PROPERTY = DotName.createSimple(JsonbProperty.class.getName());

    public static final DotName DATE_FORMAT = DotName.createSimple(DateFormat.class.getName());
    public static final DotName NUMBER_FORMAT = DotName.createSimple(NumberFormat.class.getName());

    public static final DotName IGNORE = DotName.createSimple(Ignore.class.getName());
    public static final DotName JSONB_TRANSIENT = DotName.createSimple(JsonbTransient.class.getName());

    public static final DotName NON_NULL = DotName.createSimple(NonNull.class.getName());
    public static final DotName DEFAULT_VALUE = DotName.createSimple(DefaultValue.class.getName());
    public static final DotName BEAN_VALIDATION_NOT_NULL = DotName.createSimple("javax.validation.constraints.NotNull");
    public static final DotName BEAN_VALIDATION_NOT_EMPTY = DotName.createSimple("javax.validation.constraints.NotEmpty");
    public static final DotName BEAN_VALIDATION_NOT_BLANK = DotName.createSimple("javax.validation.constraints.NotBlank");

    public static final DotName NAME = DotName.createSimple(Name.class.getName());

    public static final DotName SOURCE = DotName.createSimple(Source.class.getName());

    private static final String GRAPHQL_PACKAGE = "org.eclipse.microprofile.graphql.";
}
