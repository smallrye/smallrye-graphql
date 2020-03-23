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
package io.smallrye.graphql.bootstrap;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.bootstrap.schema.ArgumentTypeNotFoundException;

/**
 * Simple Argument Holder that contains meta data about an argument
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Argument {
    private static final Logger LOG = Logger.getLogger(Argument.class);

    private String name;
    private String description;
    private Type type;
    private Annotations annotations;

    public Argument() {
        super();
    }

    public Argument(String name, String description, Type type, Annotations annotations) {
        super();
        this.name = name;
        this.description = description;
        this.type = type;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Annotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
    }

    public Class getArgumentClass() {
        Type.Kind kind = type.kind();
        String typename = type.name().toString();
        if (kind.equals(Type.Kind.PRIMITIVE)) {
            return Classes.getPrimativeClassType(typename);
        } else {
            try {
                return forName(typename);
            } catch (ClassNotFoundException ex) {
                throw new ArgumentTypeNotFoundException(ex);
            }
        }
    }

    private static Class<?> forName(String className) throws ClassNotFoundException {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>) () -> {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                if (loader != null) {
                    try {
                        return Class.forName(className, false, loader);
                    } catch (ClassNotFoundException cnfe) {
                        // try using this class's classloader
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Failed to load class, " + className + " using TCCL, " + loader, cnfe);
                        }
                    }
                }
                return Class.forName(className, false, Argument.class.getClassLoader());
            });
        } catch (PrivilegedActionException pae) {
            throw new ClassNotFoundException(className, pae.getCause());
        }
    }
}
