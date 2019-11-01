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
package io.smallrye.graphql.scalar.buildin;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.scalar.CustomScalar;
import io.smallrye.graphql.scalar.CustomScalarMarker;

/**
 * Create a Scalar for LocalTime
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 *         TODO: Handle format ?? Default is ISO_TIME (HH:mm:ss)
 *         TODO: Exception and Literal
 * 
 */
@CustomScalarMarker
public class LocalTimeScalarProvider implements CustomScalar<LocalTime, String> {
    private static final Logger LOG = Logger.getLogger(LocalTimeScalarProvider.class.getName());

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public String getDescription() {
        return "Time Scalar";
    }

    @Override
    public String serialize(LocalTime localTime, Map<DotName, AnnotationInstance> annotations) {
        return localTime.format(getDateTimeFormatter(annotations));
    }

    @Override
    public LocalTime deserialize(String fromScalar, Map<DotName, AnnotationInstance> annotations) {
        try {
            LOG.warn("===== fromScalar: " + fromScalar);
            LOG.warn("===== annotations: " + annotations);
            LOG.warn("===== format: " + getDateTimeFormatter(annotations));
            return LocalTime.parse(fromScalar, getDateTimeFormatter(annotations));
        } catch (Throwable t) {
            return LocalTime.now();
        }

    }

    private DateTimeFormatter getDateTimeFormatter(Map<DotName, AnnotationInstance> annotations) {
        if (annotations.containsKey(Annotations.JSONB_DATE_FORMAT)) {
            AnnotationInstance ai = annotations.get(Annotations.JSONB_DATE_FORMAT);
            if (ai != null && ai.value() != null) {
                String format = ai.value().asString();
                return DateTimeFormatter.ofPattern(format);
            }
        }
        return DateTimeFormatter.ISO_TIME;
    }
}
