package io.smallrye.graphql.tests.records;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
public class RecordImplementingInterfaceTest {
    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "default.war")
                .addClasses(CustomerProductResource.class, InternetLine.class, MobileLine.class, CustomerProduct.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void testRecordImplementingInterface() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        final String query = """
                query {
                  products {
                    name
                  }
                }
                """;
        final String expected = """
                {
                  "data": {
                    "products": [
                      {
                        "name": "Fiber 100"
                      },
                      {
                        "name": "Mobile 1"
                      },
                      {
                        "name": "Fiber 200"
                      },
                      {
                        "name": "Mobile 2"
                      }
                    ]
                  }
                }
                """;
        assertThat(graphQLAssured.post(query)).isEqualToIgnoringWhitespace(expected);

    }

    @GraphQLApi
    public static class CustomerProductResource {

        @Query
        public List<CustomerProduct> getProducts() {
            return List.of(
                    new InternetLine("Fiber 100", 100),
                    new MobileLine("Mobile 1", "123456789"),
                    new InternetLine("Fiber 200", 200),
                    new MobileLine("Mobile 2", "987654321"));
        }
    }

    public sealed interface CustomerProduct permits InternetLine, MobileLine {
        @Name("name") // @Name("") is also valid, since it automatically uses the field name
        String name();
    }

    public record InternetLine(String name, int speed) implements CustomerProduct {
    }

    public record MobileLine(String name, String phoneNumber) implements CustomerProduct {
    }
}
