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

package io.smallrye.graphql.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.SmallRyeGraphQLBootstrap;
import io.smallrye.graphql.bootstrap.index.IndexInitializer;
import io.smallrye.graphql.execution.GraphQLProducer;

/**
 * Bootstrap the application on startup
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebListener
public class SmallRyeGraphQLStartupListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLStartupListener.class.getName());

    @Inject
    private GraphQLProducer graphQLProducer;

    private final IndexInitializer indexInitializer = new IndexInitializer();

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            String realPath = sce.getServletContext().getRealPath("WEB-INF/classes");
            URL url = Paths.get(realPath).toUri().toURL();
            IndexView index = indexInitializer.createIndex(url);
            GraphQLSchema graphQLSchema = SmallRyeGraphQLBootstrap.bootstrap(index);
            graphQLProducer.setGraphQLSchema(graphQLSchema);
            sce.getServletContext().setAttribute(SmallRyeGraphQLSchemaServlet.SCHEMA_PROP, graphQLSchema);
            LOG.info("SmallRye GraphQL initialized");
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("SmallRye GraphQL destroyed");
    }
}
