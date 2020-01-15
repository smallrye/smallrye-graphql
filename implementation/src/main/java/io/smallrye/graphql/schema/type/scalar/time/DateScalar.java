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
package io.smallrye.graphql.schema.type.scalar.time;

import java.sql.Date;
import java.time.LocalDate;

import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.Classes;

/**
 * Scalar for Date.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(DateScalar.class.getName());

    public DateScalar() {
        super("Date", LocalDate.class, Date.class);
    }

    @Override
    public Object transform(Object input, Argument argument) {
        LocalDate localDate = super.transformToLocalDate(argument.getName(), input.toString(), argument.getType(),
                argument.getAnnotations());

        if (argument.getType().name().equals(Classes.LOCALDATE)) {
            return localDate;
        } else if (argument.getType().name().equals(Classes.SQL_DATE)) {
            return Date.valueOf(localDate);
        } else {
            LOG.warn("Can not transform type [" + argument.getType().name() + "] with DateScalar");
            return input;
        }
    }

}
