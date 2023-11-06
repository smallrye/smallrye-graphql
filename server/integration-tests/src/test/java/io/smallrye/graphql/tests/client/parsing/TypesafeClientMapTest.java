package io.smallrye.graphql.tests.client.parsing;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeClientMapTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(MapApi.class, ComplexToComplexMapWrapper.class, Foo.class);
    }

    @ArquillianResource
    URL testingURL;

    protected MapClientApi client;

    @Before
    public void prepare() {
        client = new VertxTypesafeGraphQLClientBuilder()
                .endpoint(testingURL.toString() + "graphql")
                .build(MapClientApi.class);
    }

    @GraphQLApi
    public static class MapApi {

        @Query
        public Map<Integer, String> scalarToScalar(Map<Integer, String> input) {
            return input;
        }

        @Query
        public Map<Foo, Integer> complexToScalar(Map<Foo, Integer> input) {
            return input;
        }

        @Query
        public Map<Integer, Foo> scalarToComplex(Map<Integer, Foo> input) {
            return input;
        }

        @Query
        public ComplexToComplexMapWrapper complexToComplexWrapped(ComplexToComplexMapWrapper input) {
            return input;
        }

    }

    @GraphQLClientApi
    public interface MapClientApi {

        Map<Integer, String> scalarToScalar(Map<Integer, String> input);

        Map<Foo, Integer> complexToScalar(Map<Foo, Integer> input);

        Map<Integer, Foo> scalarToComplex(Map<Integer, Foo> input);

        ComplexToComplexMapWrapper complexToComplexWrapped(ComplexToComplexMapWrapper input);

    }

    public static class ComplexToComplexMapWrapper {

        private Map<Foo, Foo> map;

        public Map<Foo, Foo> getMap() {
            return map;
        }

        public void setMap(Map<Foo, Foo> map) {
            this.map = map;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComplexToComplexMapWrapper that = (ComplexToComplexMapWrapper) o;
            return Objects.equals(map, that.map);
        }

        @Override
        public int hashCode() {
            return Objects.hash(map);
        }
    }

    public static class Foo {
        private String bar;

        public Foo() {
        }

        public Foo(String bar) {
            this.bar = bar;
        }

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Foo foo = (Foo) o;
            return Objects.equals(bar, foo.bar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bar);
        }
    }

    @Test
    public void scalarToScalar() {
        Map<Integer, String> input = new HashMap<>();
        input.put(1, "a");
        input.put(2, "b");
        Map<Integer, String> result = client.scalarToScalar(input);
        assertEquals("a", result.get(1));
        assertEquals("b", result.get(2));
        assertEquals(2, result.size());
    }

    @Test
    public void complexToScalar() {
        try {
            Map<Foo, Integer> input = new HashMap<>();
            input.put(new Foo("a"), 68);
            input.put(new Foo("x"), 55);
            Map<Foo, Integer> result = client.complexToScalar(input);
            assertEquals(68L, result.get(new Foo("a")).longValue());
            assertEquals(55L, result.get(new Foo("x")).longValue());
            assertEquals(2, result.size());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @Test
    public void scalarToComplex() {
        try {
            Map<Integer, Foo> input = new HashMap<>();
            input.put(68, new Foo("a"));
            input.put(55, new Foo("x"));
            Map<Integer, Foo> result = client.scalarToComplex(input);
            assertEquals(result.get(68), new Foo("a"));
            assertEquals(result.get(55), new Foo("x"));
            assertEquals(2, result.size());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @Test
    public void complexToComplexWrapped() {
        try {
            ComplexToComplexMapWrapper input = new ComplexToComplexMapWrapper();
            Map<Foo, Foo> wrappedMap = new HashMap<>();
            wrappedMap.put(new Foo("a"), new Foo("aa"));
            wrappedMap.put(new Foo("b"), new Foo("bb"));
            input.setMap(wrappedMap);

            ComplexToComplexMapWrapper result = client.complexToComplexWrapped(input);
            assertEquals(new Foo("aa"), result.getMap().get(new Foo("a")));
            assertEquals(new Foo("bb"), result.getMap().get(new Foo("b")));
            assertEquals(2, result.getMap().size());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

}
