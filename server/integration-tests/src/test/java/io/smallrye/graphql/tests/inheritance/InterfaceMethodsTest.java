package io.smallrye.graphql.tests.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
public class InterfaceMethodsTest {
    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "default.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Api.class, Film.class, Hero.class, Person.class, MathConstants.class);
    }

    @ArquillianResource
    URL testingURL;

    public interface Person {
        default int getAge() {
            return 42;
        }
    }

    public static class Hero implements Person {
        // NOTHING !!!
    }

    public static class Film {
        private int filmId;
        private List<Hero> heroes;

        public Film() {
        }

        public Film(int id, List<Hero> heroes) {
            setId(id);
            setHeroes(heroes);
        }

        public List<Hero> getHeroes() {
            return heroes;
        }

        public void setHeroes(List<Hero> heroes) {
            this.heroes = heroes;
        }

        public int getId() {
            return filmId;
        }

        public void setId(int filmId) {
            this.filmId = filmId;
        }
    }

    @GraphQLApi
    public static class Api {
        @Query
        public Film getFilm(int filmId) {
            return new Film(1, List.of(new Hero()));
        }

        @Query
        public MathConstants getConstants() {
            return new MathConstants();
        }
    }

    @Test
    public void testWithDefaultMethod() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("query getFilmHeroes {\n" +
                        "  film(filmId: 1) {\n" +
                        "    heroes {\n" +
                        "      age\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n");
        assertThat(response).isEqualTo("{\"data\":{\"film\":{\"heroes\":[{\"age\":42}]}}}");
    }

    public interface InterfaceA {
        default double getPi() {
            return 3.14159;
        }

        default double getTau() {
            return 6.28;
        }
    }

    public interface InterfaceB extends InterfaceA {
        @Override
        default double getPi() {
            return 3.1415926;
        }

        default double getE() {
            return 2.716;
        }
    }

    public interface InterfaceC {
        default double getGoldenRatio() {
            return 1.618;
        }
    }

    public static class MathConstants implements InterfaceB, InterfaceC {

    }

    @Test
    public void testWithMultipleDefaultMethods() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured
                .post("query constants {\n" +
                        "  constants {\n" +
                        "    pi \n" +
                        "    goldenRatio \n" +
                        "    tau \n" +
                        "    e \n" +
                        "  }\n" +
                        "}\n");
        assertThat(response)
                .isEqualTo("{\"data\":{\"constants\":{\"pi\":3.1415926,\"goldenRatio\":1.618,\"tau\":6.28,\"e\":2.716}}}");
    }

}
