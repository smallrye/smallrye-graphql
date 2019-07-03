/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.graphql;

import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;

import org.eclipse.microprofile.graphql.GraphQLApi;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;

/**
 * This is a CDI extension that detects GraphQL components
 * and generate the GraphQL Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLExtension implements Extension {
    private static final Logger LOG = Logger.getLogger(GraphQLExtension.class.getName());

    private final Set<Bean<?>> graphQLComponents = new LinkedHashSet<>();

    private GraphQLSchema schema;

    private void createGraphQLSchema(@Observes AfterDeploymentValidation abd, BeanManager beanManager) {
        GraphQLSchemaGenerator schemaGen = new GraphQLSchemaGenerator()
                .withResolverBuilders(new AnnotatedResolverBuilder());

        if (!graphQLComponents.isEmpty()) {
            for (Bean component : graphQLComponents) {
                Type beanClass = component.getBeanClass();
                CreationalContext<?> creationalContext = beanManager.createCreationalContext(component);
                schemaGen.withOperationsFromSingleton(
                        beanManager.getReference(component, beanClass, creationalContext),
                        component.getBeanClass());
                LOG.log(Level.INFO, "GraphQL Component [{0}] registered", component.getBeanClass());
            }
            schema = schemaGen.generate();

            SchemaProducer schemaProducer = CDI.current().select(SchemaProducer.class).get();
            schemaProducer.setGraphQLSchema(schema);

            LOG.info("All GraphQL Components added to the endpoint.");
        } else {
            LOG.warning("No GraphQL Components found. Make sure to annotate your endpoint(s) with @GraphQLApi.");
        }
    }

    // Detect and store GraphQLComponents
    private <X> void detectGraphQLApiBeans(@Observes ProcessBean<X> event) {
        if (event.getAnnotated().isAnnotationPresent(GraphQLApi.class)) {
            graphQLComponents.add(event.getBean());
        }
    }
}
