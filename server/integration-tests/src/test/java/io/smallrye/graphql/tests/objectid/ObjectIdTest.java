package io.smallrye.graphql.tests.objectid;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.bson.types.ObjectId;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
@RunAsClient
public class ObjectIdTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "objectId-test.war")
                .addClasses(SomeApi.class, ObjectIdAdapter.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void queryWithObjectIdArgumentTest() {
        final String id = ObjectId.get().toHexString();
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);

        String response = graphQLAssured
                .post("{ returnObjectId(id: \"" + id + "\") }");
        assertThat(response).contains("{\"data\":{\"returnObjectId\":\"" + id + "\"}}");
    }
}
