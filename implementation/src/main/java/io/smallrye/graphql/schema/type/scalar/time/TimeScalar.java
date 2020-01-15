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

import java.sql.Time;
import java.time.LocalTime;

import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.Classes;

/**
 * Scalar for Time.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TimeScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(TimeScalar.class.getName());

    public TimeScalar() {
        super("Time", LocalTime.class, Time.class);
    }

    @Override
    public Object transform(Object input, Argument argument) {
        LocalTime localTime = transformToLocalTime(argument.getName(), input.toString(), argument.getType(),
                argument.getAnnotations());
        if (argument.getType().name().equals(Classes.LOCALTIME)) {
            return localTime;
        } else if (argument.getType().name().equals(Classes.SQL_TIME)) {
            return java.sql.Time.valueOf(localTime);
        } else {
            LOG.warn("Can not transform type [" + argument.getType().name() + "] with TimeScalar");
            return input;
        }
    }
}
