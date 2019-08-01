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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;

/**
 * To make the schema available over HTTP
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLSchemaServlet extends HttpServlet {

    private final GraphQLSchema schema;

    public GraphQLSchemaServlet(GraphQLSchema schema) {
        this.schema = schema;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SchemaPrinter schemaPrinter = new SchemaPrinter(SchemaPrinter.Options.defaultOptions());
        String json = schemaPrinter.print(schema);
        response.setContentType("plain/text"); //TODO: Check the content type in the spec
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    @Override
    public String getServletInfo() {
        return "The GraphQL schema";
    }
}
