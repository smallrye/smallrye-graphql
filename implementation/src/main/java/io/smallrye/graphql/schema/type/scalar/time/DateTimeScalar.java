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
package io.smallrye.graphql.schema.type.scalar.time;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.Classes;

/**
 * Scalar for DateTime.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateTimeScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(DateTimeScalar.class.getName());

    public DateTimeScalar() {
        super("DateTime", LocalDateTime.class, Date.class, Timestamp.class);
    }

    @Override
    public Object transform(Object input, Argument argument) {
        LocalDateTime localDateTime = transformToLocalDateTime(argument.getName(), input.toString(), argument.getType(),
                argument.getAnnotations());
        if (argument.getType().name().equals(Classes.LOCALDATETIME)) {
            return localDateTime;
        } else if (argument.getType().name().equals(Classes.UTIL_DATE)) {
            return toUtilDate(localDateTime);
        } else if (argument.getType().name().equals(Classes.SQL_TIMESTAMP)) {
            return java.sql.Timestamp.valueOf(localDateTime);
        } else {
            LOG.warn("Can not transform type [" + argument.getType().name() + "] with DateTimeScalar");
            return input;
        }
    }

    private Date toUtilDate(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZoneId.systemDefault())
                        .toInstant());
    }
}
