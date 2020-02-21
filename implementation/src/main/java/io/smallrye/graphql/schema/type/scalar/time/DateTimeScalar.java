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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.helper.FormatHelper;
import io.smallrye.graphql.schema.type.scalar.TransformException;

/**
 * Scalar for DateTime.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateTimeScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(DateTimeScalar.class.getName());
    private final FormatHelper formatHelper = new FormatHelper();

    public DateTimeScalar() {
        super("DateTime", LocalDateTime.class, Date.class, Timestamp.class, ZonedDateTime.class, OffsetDateTime.class);
    }

    @Override
    public Object transform(Object input, Argument argument) {

        if (argument.getType().name().equals(Classes.LOCALDATETIME)) {
            return transformToLocalDateTime(argument.getName(), input.toString(), argument.getType(),
                    argument.getAnnotations());
        } else if (argument.getType().name().equals(Classes.UTIL_DATE)) {
            LocalDateTime localDateTime = transformToLocalDateTime(argument.getName(), input.toString(), argument.getType(),
                    argument.getAnnotations());
            return toUtilDate(localDateTime);
        } else if (argument.getType().name().equals(Classes.SQL_TIMESTAMP)) {
            LocalDateTime localDateTime = transformToLocalDateTime(argument.getName(), input.toString(), argument.getType(),
                    argument.getAnnotations());
            return java.sql.Timestamp.valueOf(localDateTime);
        } else if (argument.getType().name().equals(Classes.OFFSETDATETIME)) {
            return transformToOffsetDateTime(argument.getName(), input.toString(), argument.getType(),
                    argument.getAnnotations());
        } else if (argument.getType().name().equals(Classes.ZONEDDATETIME)) {
            return transformToZonedDateTime(argument.getName(), input.toString(), argument.getType(),
                    argument.getAnnotations());
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

    private LocalDateTime transformToLocalDateTime(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return LocalDateTime.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }
    }

    private ZonedDateTime transformToZonedDateTime(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return ZonedDateTime.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }
    }

    private OffsetDateTime transformToOffsetDateTime(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return OffsetDateTime.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }
    }
}