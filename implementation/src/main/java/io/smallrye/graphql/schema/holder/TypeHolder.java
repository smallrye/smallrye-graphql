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

import java.util.Objects;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

/**
 * Simple class to hold information about a type we care about
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * 
 */
public class TypeHolder {
    private final ClassInfo classInfo;
    private final AnnotationsHolder annotations;

    public TypeHolder(ClassInfo classInfo, AnnotationsHolder annotations) {
        this.classInfo = classInfo;
        this.annotations = annotations;
    }

    public DotName getDotName() {
        return classInfo.name();
    }

    public ClassInfo getClassInfo() {
        return this.classInfo;
    }

    public AnnotationsHolder getAnnotations() {
        return this.annotations;
    }

    @Override
    public String toString() {
        return classInfo.name().toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(classInfo.name());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeHolder other = (TypeHolder) obj;
        if (!Objects.equals(this.classInfo.name(), other.classInfo.name())) {
            return false;
        }
        return true;
    }

}
