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

import javax.enterprise.context.Dependent;

import org.jboss.jandex.Type;

import io.smallrye.graphql.index.Classes;

/**
 * Helping with dates
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class DateHelper {

    public boolean isDateLikeTypeOrCollectionThereOf(Type type) {
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

    public String getDefaultFormat(Type type) {
        // return the default dates format
        if (type.name().equals(Classes.LOCALDATE)) {
            return ISO_DATE;
        } else if (type.name().equals(Classes.LOCALTIME)) {
            return ISO_TIME;
        } else if (type.name().equals(Classes.LOCALDATETIME)) {
            return ISO_DATE_TIME;
        }
        throw new RuntimeException("Not a valid Date Type [" + type.name().toString() + "]");
    }

    private static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO_DATE = "yyyy-MM-dd";
    private static final String ISO_TIME = "HH:mm:ss";
}
