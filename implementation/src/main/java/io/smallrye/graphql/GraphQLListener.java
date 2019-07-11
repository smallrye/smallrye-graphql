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

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLSchema;
import graphql.servlet.SimpleGraphQLHttpServlet;

/**
 * Dynamically adding the Endpoint and Schema Servlets
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebListener
public class GraphQLListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(GraphQLListener.class.getName());

    @Inject
    private GraphQLSchema schema;

    @Inject
    @ConfigProperty(name = "mp.graphql.contextpath", defaultValue = "/graphql")
    private String path;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        if (schema != null) {
            // The Endpoint
            SimpleGraphQLHttpServlet graphQLServlet = SimpleGraphQLHttpServlet.newBuilder(schema).build();
            ServletRegistration.Dynamic endpointservlet = context.addServlet(GRAPHQL_SERVLET_NAME, graphQLServlet);
            endpointservlet.addMapping(path + SLASH_STAR);

            // The Schema
            GraphQLSchemaServlet graphQLSchemaServlet = new GraphQLSchemaServlet(schema);
            ServletRegistration.Dynamic schemaservlet = context.addServlet(GRAPHQL_SCHEMA_SERVLET_NAME, graphQLSchemaServlet);
            schemaservlet.addMapping(path + SLASH_SCHEMA_GRAPHQL);
        }
        LOG.info("GraphQL Endpoint available on " + path);
    }

    private static final String GRAPHQL_SERVLET_NAME = "GraphQLServlet";
    private static final String GRAPHQL_SCHEMA_SERVLET_NAME = "GraphQLSchemaServlet";
    private static final String SLASH_STAR = "/*";
    private static final String SLASH_SCHEMA_GRAPHQL = "/schema.graphql";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
