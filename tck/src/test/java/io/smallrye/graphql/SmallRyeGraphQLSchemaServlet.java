package io.smallrye.graphql;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Making the GraphQL Schema available via HTTP
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeGraphQLSchemaServlet extends HttpServlet {

    private String schemaString;

    public SmallRyeGraphQLSchemaServlet(String schemaString) {
        this.schemaString = schemaString;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("plain/text"); //TODO: Check the content type in the spec
        PrintWriter out = response.getWriter();
        out.print(schemaString);
        out.flush();
    }

    @Override
    public String getServletInfo() {
        return "The GraphQL Schema";
    }
}
