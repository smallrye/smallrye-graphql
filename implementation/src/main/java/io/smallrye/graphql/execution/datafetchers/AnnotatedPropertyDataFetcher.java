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

package io.smallrye.graphql.execution.datafetchers;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.PropertyDataFetcher;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.schema.helper.DateHelper;
import io.smallrye.graphql.schema.holder.AnnotationsHolder;

/**
 * Extending the default property data fetcher and take the annotations into account
 * TODO: Make a general annotation for marshaling/unmarshaling
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotatedPropertyDataFetcher extends PropertyDataFetcher {
    private static final Logger LOG = Logger.getLogger(AnnotatedPropertyDataFetcher.class.getName());

    private final DateHelper dateHelper = new DateHelper();

    private DateTimeFormatter dateTimeFormatter = null;

    public AnnotatedPropertyDataFetcher(String propertyName, Type type, AnnotationsHolder annotations) {
        super(propertyName);

        if (dateHelper.isDateLikeTypeOrCollectionThereOf(type)) {
            if (annotations.containsKeyAndValidValue(Annotations.JSONB_DATE_FORMAT)) {
                String format = annotations.getAnnotationValue(Annotations.JSONB_DATE_FORMAT).asString();
                this.dateTimeFormatter = DateTimeFormatter.ofPattern(format);
            }
        }
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Object o = super.get(environment);

        // Date
        if (dateTimeFormatter != null) {
            return dateTimeFormatter.format((TemporalAccessor) o);
        } else {
            return o;
        }

        // TODO: Add tests for other types
    }
}
