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

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.index.IndexInitializer;
import io.smallrye.graphql.bootstrap.schema.GraphQLSchemaInitializer;

/**
 * Bootstrap MicroProfile GraphQL
 * This scan all classes for annotations and create the GraphQL Schema.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeGraphQLBootstrap {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLBootstrap.class.getName());

    public GraphQLSchema bootstrap() {
        return bootstrap(createIndex());
    }

    public GraphQLSchema bootstrap(IndexView index) {
        GraphQLSchemaInitializer graphQLSchemaInitializer = new GraphQLSchemaInitializer(index);
        return graphQLSchemaInitializer.generateGraphQLSchema();
    }

    private IndexView createIndex() {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("META-INF/jandex.idx")) {
            IndexReader reader = new IndexReader(stream);
            IndexView i = reader.read();
            LOG.info("Loaded index from [META-INF/jandex.idx]");
            return i;
        } catch (IOException ex) {
            IndexInitializer indexInitializer = new IndexInitializer();
            IndexView i = indexInitializer.createIndex();
            LOG.info("Loaded index from generation via classpath");
            return i;
        }
    }

}
