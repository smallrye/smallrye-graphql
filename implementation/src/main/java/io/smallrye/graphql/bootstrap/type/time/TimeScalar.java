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
package io.smallrye.graphql.bootstrap.type.time;

import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.bootstrap.Annotations;
import io.smallrye.graphql.bootstrap.Argument;
import io.smallrye.graphql.bootstrap.Classes;
import io.smallrye.graphql.bootstrap.TransformException;
import io.smallrye.graphql.bootstrap.schema.helper.FormatHelper;

/**
 * Scalar for Time.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TimeScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(TimeScalar.class.getName());
    private final FormatHelper formatHelper = new FormatHelper();

    public TimeScalar() {
        super("Time", LocalTime.class, Time.class, OffsetTime.class);
    }

    @Override
    public Object transform(Object input, Argument argument) {

        if (argument.getType().name().equals(Classes.LOCALTIME)) {
            return transformToLocalTime(argument.getName(), input.toString(), argument.getType(),
                    argument.getAnnotations());
        } else if (argument.getType().name().equals(Classes.SQL_TIME)) {
            LocalTime localTime = transformToLocalTime(argument.getName(), input.toString(), argument.getType(),
                    argument.getAnnotations());
            return java.sql.Time.valueOf(localTime);
        } else if (argument.getType().name().equals(Classes.OFFSETTIME)) {
            return transformToOffsetTime(argument.getName(), input.toString(), argument.getType(),
                    argument.getAnnotations());
        } else {
            LOG.warn("Can not transform type [" + argument.getType().name() + "] with TimeScalar");
            return input;
        }
    }

    private LocalTime transformToLocalTime(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return LocalTime.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }
    }

    private OffsetTime transformToOffsetTime(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return OffsetTime.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }
    }
}
