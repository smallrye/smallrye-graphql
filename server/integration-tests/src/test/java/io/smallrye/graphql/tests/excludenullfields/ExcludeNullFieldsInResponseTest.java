package io.smallrye.graphql.tests.excludenullfields;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.config.ConfigKey;
import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
@RunAsClient
public class ExcludeNullFieldsInResponseTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "excludeNullFields-test.war")
                .addClasses(Customer.class, CustomerApi.class);
    }

    @ArquillianResource
    URL testingURL;

    public static class Customer {
        private String name;
        private String optionalPassword;

        public Customer() {
        }

        public Customer(String name, String optionalPassword) {
            this.name = name;
            this.optionalPassword = optionalPassword;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOptionalPassword() {
            return optionalPassword;
        }

        public void setOptionalPassword(String optionalPassword) {
            this.optionalPassword = optionalPassword;
        }
    }

    @GraphQLApi
    public static class CustomerApi {
        @Query
        public Collection<Customer> getCustomers() {
            return List.of(new Customer("Son Goku", null),
                    new Customer("Gohan", "mysupersecretpassword"));

        }

        public Integer getAge(@Source Customer customer) {
            return (customer.getName().equals("Gohan")) ? null : 42;
        }

        public String getCity(@Source Customer customer) {
            throw new RuntimeException("Error was thrown !!!");
            //  return "Prague";
        }

        @Query
        public String getNullData() {
            return null;
        }
    }

    @Test
    public void queryShouldNotReturnNonNullError() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("{ customers { name optionalPassword age city } }");
        assertThat(response).contains("\"data\":{\"customers\":[{\"name\":\"Son Goku\"," +
                "\"age\":42,\"city\":null},{\"name\":\"Gohan\",\"optionalPassword\":\"mysupersecretpassword\"," +
                "\"city\":null}]}");
        assertThat(response).contains("error");
        assertThat(response)
                .contains("\"path\":[\"customers\",0,\"city\"]")
                .contains("\"path\":[\"customers\",1,\"city\"]");
    }

    @Test
    public void queryNonNullDataField() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("{ nullData }");
        assertThat(response).contains("{\"data\":{}}");
    }

    @Before
    public void setExcludeNullFieldProperty() {
        System.setProperty(ConfigKey.EXCLUDE_NULL_FIELDS_IN_RESPONSES, "true");
    }

    @After
    public void clearExcludeNullFieldProperty() {
        System.clearProperty(ConfigKey.EXCLUDE_NULL_FIELDS_IN_RESPONSES);
    }
}
