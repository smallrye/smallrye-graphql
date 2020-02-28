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

import javax.inject.Inject;
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

    @Inject
    GraphQLSchema graphQLSchema;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/plain");
        try (PrintWriter out = response.getWriter()) {
            out.print(graphQLSchemaToString());
            out.flush();
        } catch (IOException ex) {
            LOG.log(Logger.Level.ERROR, null, ex);
        }
    }

    private String graphQLSchemaToString() {
        return schemaPrinter.print(graphQLSchema);
    }

    private final SchemaPrinter schemaPrinter = new SchemaPrinter();
}
