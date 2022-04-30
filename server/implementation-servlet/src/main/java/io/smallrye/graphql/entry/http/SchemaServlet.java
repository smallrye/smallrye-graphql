package io.smallrye.graphql.entry.http;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.execution.SchemaPrinter;

/**
 * Serving the GraphQL schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLSchemaServlet", urlPatterns = { "/graphql/schema.graphql" }, loadOnStartup = 2)
public class SchemaServlet extends HttpServlet {

    public static final String SCHEMA_PROP = "io.smallrye.graphql.servlet.bootstrap";
    private final SchemaPrinter schemaPrinter = new SchemaPrinter();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE);
        try (PrintWriter out = response.getWriter()) {
            GraphQLSchema schema = (GraphQLSchema) request.getServletContext().getAttribute(SCHEMA_PROP);
            out.print(schemaPrinter.print(schema));
            out.flush();
        } catch (IOException ex) {
            SmallRyeGraphQLServletLogging.log.ioException(ex);
        }
    }

    private static final String CONTENT_TYPE = "text/plain";

}
