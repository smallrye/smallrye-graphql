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

package io.smallrye.graphql.schema.holder;

import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

/**
 * Simple Annotations Holder that contains all annotation applicable for a certain context
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AnnotationsHolder {
    private final Map<DotName, AnnotationInstance> annotations;

    public AnnotationsHolder(Map<DotName, AnnotationInstance> annotations) {
        this.annotations = annotations;
    }

    public Map<DotName, AnnotationInstance> getAnnotations() {
        return this.annotations;
    }

    public AnnotationInstance getAnnotation(DotName key) {
        return this.annotations.get(key);
    }

    public AnnotationValue getAnnotationValue(DotName key) {
        return this.annotations.get(key).value();
    }

    public boolean containsOnOfTheseKeys(DotName... key) {
        for (DotName name : key) {
            if (this.annotations.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsKeyAndValidValue(DotName key) {
        return this.annotations.containsKey(key) && this.annotations.get(key).value() != null;
    }
}
