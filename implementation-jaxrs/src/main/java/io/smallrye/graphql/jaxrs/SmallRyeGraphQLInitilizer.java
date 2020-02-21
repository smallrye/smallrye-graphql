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
package io.smallrye.graphql.jaxrs;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.smallrye.graphql.SmallRyeGraphQLBootstrap;

/**
 * Handler for the schema and the execution
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class SmallRyeGraphQLInitilizer {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLInitilizer.class.getName());

    @Inject
    private SmallRyeGraphQLBootstrap bootstrap;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        bootstrap.generateSchema();
        LOG.info("SmallRye GraphQL Server started");
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        LOG.info("SmallRye GraphQL Server stoped");
    }
}
