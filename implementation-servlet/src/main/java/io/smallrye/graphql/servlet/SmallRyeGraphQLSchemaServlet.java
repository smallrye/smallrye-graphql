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
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    private GraphQLSchema graphQLSchema;

    private String schema;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (graphQLSchema != null) {
            SchemaPrinter schemaPrinter = new SchemaPrinter();
            this.schema = schemaPrinter.print(graphQLSchema);
        } else {
            LOG.warning("Can not create GraphQL Schema (null)");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("plain/text"); //TODO: Check the content type in the spec
        PrintWriter out = response.getWriter();
        out.print(this.schema);
        out.flush();
    }

}
