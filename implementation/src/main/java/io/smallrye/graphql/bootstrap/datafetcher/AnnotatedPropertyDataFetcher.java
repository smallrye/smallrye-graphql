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

package io.smallrye.graphql.bootstrap.datafetcher;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.jboss.jandex.Type;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.PropertyDataFetcher;
import io.smallrye.graphql.bootstrap.Annotations;
import io.smallrye.graphql.bootstrap.schema.helper.CollectionHelper;
import io.smallrye.graphql.bootstrap.schema.helper.FormatHelper;

/**
 * Extending the default property data fetcher and take the annotations into account
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotatedPropertyDataFetcher extends PropertyDataFetcher {
    private final FormatHelper formatHelper = new FormatHelper();

    private DateTimeFormatter dateTimeFormatter = null;
    private NumberFormat numberFormat = null;
    private final CollectionHelper collectionHelper = new CollectionHelper();

    public AnnotatedPropertyDataFetcher(String propertyName, Type type, Annotations annotations) {
        super(propertyName);
        if (formatHelper.isDateLikeTypeOrCollectionThereOf(type)) {
            this.dateTimeFormatter = formatHelper.getDateFormat(type, annotations);
        }
        if (formatHelper.isNumberLikeTypeOrCollectionThereOf(type)) {
            this.numberFormat = formatHelper.getNumberFormat(annotations);
        }
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Object o = super.get(environment);
        return get(o);
    }

    public Object get(Object o) {
        if (Optional.class.isInstance(o)) {
            Optional optional = Optional.class.cast(o);
            if (optional.isPresent()) {
                Object value = optional.get();
                return Collections.singletonList(handleFormatting(value));
            } else {
                return Collections.emptyList();
            }
        } else if (Collection.class.isInstance(o)) {
            Collection collection = Collection.class.cast(o);
            Collection transformedCollection = collectionHelper.newCollection(o.getClass());
            for (Object oo : collection) {
                transformedCollection.add(get(oo));
            }
            return transformedCollection;
        } else {
            return handleFormatting(o);
        }
    }

    private Object handleFormatting(Object o) {
        if (dateTimeFormatter != null) {
            return handleDateFormatting(o);
        } else if (numberFormat != null) {
            return handleNumberFormatting(o);
        } else {
            return o;
        }
    }

    private Object handleDateFormatting(Object o) {
        if (TemporalAccessor.class.isInstance(o)) {
            TemporalAccessor temporalAccessor = (TemporalAccessor) o;
            return dateTimeFormatter.format(temporalAccessor);
        } else {
            return o;
        }
    }

    private Object handleNumberFormatting(Object o) {
        if (Number.class.isInstance(o)) {
            Number number = (Number) o;
            return numberFormat.format(number);
        } else {
            return o;
        }
    }
}
