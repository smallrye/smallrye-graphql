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
import io.smallrye.graphql.execution.SchemaPrinter;

/**
 * Serving the GraphQL schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebServlet(name = "SmallRyeGraphQLSchemaServlet", urlPatterns = { "/graphql/schema.graphql" }, loadOnStartup = 2)
public class SchemaServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SchemaServlet.class.getName());

    public static final String SCHEMA_PROP = "io.smallrye.graphql.servlet.bootstrap";

    @Inject
    private SchemaPrinter schemaPrinter;

    public SchemaServlet() {
    }

    public SchemaServlet(SchemaPrinter schemaPrinter) {
        this.schemaPrinter = schemaPrinter;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE);
        try (PrintWriter out = response.getWriter()) {
            GraphQLSchema schema = (GraphQLSchema) request.getServletContext().getAttribute(SCHEMA_PROP);
            out.print(schemaPrinter.print(schema));
            out.flush();
        } catch (IOException ex) {
            LOG.log(Logger.Level.ERROR, null, ex);
        }
    }

    private static final String CONTENT_TYPE = "text/plain";

}
