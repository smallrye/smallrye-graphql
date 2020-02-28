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
package io.smallrye.graphql.bootstrap.schema.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.logging.Logger;

import io.smallrye.graphql.bootstrap.Annotations;

/**
 * Finds all @Source fields.
 * Creates a map of fields that needs to be added to certain Objects due to @Source annotation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceFieldHelper {
    private static final Logger LOG = Logger.getLogger(SourceFieldHelper.class.getName());

    public Map<DotName, List<MethodParameterInfo>> getAllSourceAnnotations(IndexView index) {
        Map<DotName, List<MethodParameterInfo>> sourceFields = new HashMap<>();
        Collection<AnnotationInstance> sourceAnnotations = index.getAnnotations(Annotations.SOURCE);
        for (AnnotationInstance ai : sourceAnnotations) {
            AnnotationTarget target = ai.target();
            if (target.kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                MethodParameterInfo methodParameter = target.asMethodParameter();
                short position = methodParameter.position();
                DotName name = methodParameter.method().parameters().get(position).name();
                sourceFields.computeIfAbsent(name, k -> new ArrayList<>()).add(methodParameter);
            } else {
                LOG.warn("Ignoring " + ai.target() + " on kind " + ai.target().kind() + ". Only expecting @"
                        + Annotations.SOURCE.local() + " on Method parameters");
            }
        }
        return sourceFields;
    }

}
