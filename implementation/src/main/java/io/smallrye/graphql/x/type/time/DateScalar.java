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
package io.smallrye.graphql.x.type.time;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.x.Annotations;
import io.smallrye.graphql.x.Argument;
import io.smallrye.graphql.x.Classes;
import io.smallrye.graphql.x.TransformException;
import io.smallrye.graphql.x.schema.helper.FormatHelper;

/**
 * Scalar for Date.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(DateScalar.class.getName());
    private final FormatHelper formatHelper = new FormatHelper();

    public DateScalar() {
        super("Date", LocalDate.class, Date.class);
    }

    @Override
    public Object transform(Object input, Argument argument) {
        LocalDate localDate = transformToLocalDate(argument.getName(), input.toString(), argument.getType(),
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

    private LocalDate transformToLocalDate(String name, String input, Type type, Annotations annotations) {
        try {
            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
            return LocalDate.parse(input, dateFormat);
        } catch (DateTimeParseException dtpe) {
            throw new TransformException(dtpe, this, name, input);
        }

    }

}
