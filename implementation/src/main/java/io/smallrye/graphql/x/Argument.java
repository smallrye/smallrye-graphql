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
package io.smallrye.graphql.x;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

/**
 * Simple Argument Holder that contains meta data about an argument
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Argument {
    private static final Logger LOG = Logger.getLogger(Argument.class);

    private final String name;
    private final String description;
    private final Type type;
    private final Annotations annotations;
    private final Class argumentClass;

    public Argument(String name, String description, Type type, Annotations annotations) {
        super();
        this.name = name;
        this.description = description;
        this.type = type;
        this.annotations = annotations;
        this.argumentClass = Classes.loadClass(type.name().toString());
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public Annotations getAnnotations() {
        return annotations;
    }

    public Class getArgumentClass() {
        return argumentClass;
    }
}
