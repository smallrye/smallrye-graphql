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

package io.smallrye.graphql;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.smallrye.graphql.index.IndexInitializer;
import io.smallrye.graphql.schema.GraphQLSchemaInitializer;

/**
 * Bootstrap MicroProfile GraphQL
 * This scan all classes for annotations and create the GraphQL Schema.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class SmallRyeGraphQLBootstrap {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLBootstrap.class.getName());

    @Inject
    private GraphQLSchemaInitializer graphQLSchemaInitializer;

    @Produces
    private IndexView index;

    @Produces
    private GraphQLSchema graphQLSchema;

    @Produces
    @Named("graphQLSchema")
    private String graphQLSchemaString;

    public SmallRyeGraphQLBootstrap() {
    }

    public SmallRyeGraphQLBootstrap(IndexView index) {
        this.index = index;
    }

    public GraphQLSchema generateSchema() {
        if (this.index == null) {
            try (InputStream stream = getClass().getResourceAsStream("META-INF/jandex.idx")) {
                IndexReader reader = new IndexReader(stream);
                this.index = reader.read();
                LOG.info("Loaded index from [META-INF/jandex.idx]");
            } catch (IOException ex) {
                IndexInitializer indexInitializer = new IndexInitializer();
                this.index = indexInitializer.createIndex();
                LOG.info("Loaded index from generation via classpath");
            }
        }
        return indexToGraphQLSchema();
    }

    public String getGraphQLSchemaString() {
        if (this.graphQLSchema == null) {
            generateSchema();
        }
        return this.graphQLSchemaString;
    }

    private GraphQLSchema indexToGraphQLSchema() {
        this.graphQLSchema = graphQLSchemaInitializer.generateGraphQLSchema();

        if (graphQLSchema != null) {
            SchemaPrinter schemaPrinter = new SchemaPrinter();
            this.graphQLSchemaString = schemaPrinter.print(graphQLSchema);
        }

        return this.graphQLSchema;
    }

}
