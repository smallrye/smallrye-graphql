package io.smallrye.graphql.tests.adapting;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.Collection;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.AdaptWith;
import io.smallrye.graphql.api.Adapter;
import io.smallrye.graphql.api.Scalar;
import io.smallrye.graphql.tests.GraphQLAssured;

@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class AdaptTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "adapt-test.war")
                .addClasses(ModelA.class, ModelB.class, AdaptApi.class, CustomJsonbTypeAdapter.class, CustomAdapter.class);
    }

    @ArquillianResource
    URL testingURL;

    public static class ModelA {
        @AdaptToScalar(Scalar.String.class)
        private ModelB modelB;

        @AdaptWith(CustomAdapter.class)
        public Collection<Long> someField;

        @JsonbTypeAdapter(CustomJsonbTypeAdapter.class)
        public String myOtherField;

        public ModelA() {
        }

        public ModelB getModelB() {
            return modelB;
        }

        public void setModelB(ModelB modelB) {
            this.modelB = modelB;
        }
    }

    public static class ModelB {
        private String id;

        public ModelB() {
        }

        // the `ModelB` object needs to have a constructor that takes a String (or Int / Date / etc.),
        // or have a setter method for the String (or Int / Date / etc.),
        // or have a fromString (or fromInt / fromDate - depending on the Scalar type) static method.
        public static ModelB fromString(String id) {
            ModelB modelB = new ModelB();
            modelB.id = id;
            return modelB;
        }

        @Override
        public String toString() {
            return id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @GraphQLApi
    public static class AdaptApi {
        @Query
        public ModelA getSomeString(ModelA modelA) {
            return modelA;
        }
    }

    public static class CustomAdapter implements Adapter<Long, String> {

        @Override
        public String to(Long o) throws Exception {
            return String.valueOf(o);
        }

        @Override
        public Long from(String a) throws Exception {
            return Long.parseLong(a);
        }
    }

    public static class CustomJsonbTypeAdapter implements JsonbAdapter<String, Long> {

        @Override
        public Long adaptToJson(String s) throws Exception {
            return Long.parseLong(s);
        }

        @Override
        public String adaptFromJson(Long aLong) throws Exception {
            return String.valueOf(aLong);
        }
    }

    @Test
    public void adaptTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        assertThat(graphQLAssured
                .post("query { someString(modelA: { modelB: \"3\", someField: [\"5\",\"8\"], myOtherField: 4}) { modelB someField myOtherField }}"))
                .containsIgnoringWhitespaces(
                        "{\"data\":{\"someString\":{\"modelB\":\"3\",\"someField\":[\"5\",\"8\"],\"myOtherField\": 4}}}")
                .doesNotContain("error");
    }
}
