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

import javax.enterprise.context.ApplicationScoped;

import org.jboss.jandex.IndexView;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.schema.GraphQLSchemaInitializer;

/**
 * Bootstrap MicroProfile GraphQL
 * This scan all classes for annotations and create the GraphQL Schema.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class SmallRyeGraphQLBootstrap {
    private GraphQLSchema graphQLSchema;

    public GraphQLSchema bootstrap(IndexView index) {
        GraphQLSchemaInitializer graphQLSchemaInitializer = new GraphQLSchemaInitializer(index);
        graphQLSchema = graphQLSchemaInitializer.generateGraphQLSchema();
        return graphQLSchema;
    }

    public GraphQLSchema getSchema() {
        return graphQLSchema;
    }
}
