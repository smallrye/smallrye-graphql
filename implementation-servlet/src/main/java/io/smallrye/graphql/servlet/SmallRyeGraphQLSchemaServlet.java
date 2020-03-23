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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;

/**
 * Serving the GraphQL schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLSchemaServlet", urlPatterns = { "/graphql/schema.graphql" }, loadOnStartup = 2)
public class SmallRyeGraphQLSchemaServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLSchemaServlet.class.getName());

    public static final String SCHEMA_PROP = "io.smallrye.graphql.servlet.bootstrap";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE);
        try (PrintWriter out = response.getWriter()) {
            GraphQLSchema schema = (GraphQLSchema) request.getServletContext().getAttribute(SCHEMA_PROP);
            out.print(graphQLSchemaToString(schema));
            out.flush();
        } catch (IOException ex) {
            LOG.log(Logger.Level.ERROR, null, ex);
        }
    }

    private String graphQLSchemaToString(GraphQLSchema schema) {
        return SCHEMAPRINTER.print(schema);
    }

    private static final String CONTENT_TYPE = "text/plain";
    private static final SchemaPrinter SCHEMAPRINTER;
    static {
        SchemaPrinter.Options options = SchemaPrinter.Options.defaultOptions();
        options = options.descriptionsAsHashComments(false);
        options = options.includeDirectives(false);
        options = options.includeExtendedScalarTypes(false);
        options = options.includeIntrospectionTypes(false);
        options = options.includeScalarTypes(false);
        options = options.includeSchemaDefinition(false);
        options = options.useAstDefinitions(false);
        SCHEMAPRINTER = new SchemaPrinter(options);
    }
}
