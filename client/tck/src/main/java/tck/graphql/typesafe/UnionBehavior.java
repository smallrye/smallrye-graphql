package tck.graphql.typesafe;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTypeInfo;

import org.eclipse.microprofile.graphql.Type;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.api.Union;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class UnionBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @Union
    @JsonbTypeInfo(key = "__typename", value = {
            @JsonbSubtype(alias = "SuperHero", type = SuperHero.class),
            @JsonbSubtype(alias = "NotFound", type = NotFound.class)
    })
    public interface SuperHeroResponse {
    }

    public static class SuperHero implements SuperHeroResponse {
        String name;

        String getName() {
            return name;
        }
    }

    @Type("SuperHeroNotFound")
    public static class NotFound implements SuperHeroResponse {
        String message;

        String getMessage() {
            return message;
        }
    }

    @GraphQLClientApi
    interface UnionApi {
        SuperHeroResponse find(String name);

        List<SuperHeroResponse> findAll(String name);
    }

    @Test
    void shouldUnionFindSpiderMan() {
        fixture.returnsData("'find':{'__typename':'SuperHero','name':'Spider-Man'}");
        var api = fixture.build(UnionApi.class);

        var response = api.find("Spider-Man");

        then(fixture.query()).isEqualTo("query find($name: String) { find(name: $name){" +
                "__typename " +
                "... on SuperHero {name} " +
                "... on SuperHeroNotFound {message}} }");
        then(fixture.variables()).isEqualTo("{'name':'Spider-Man'}");
        then(response).isInstanceOf(SuperHero.class);
        then(((SuperHero) response).getName()).isEqualTo("Spider-Man");
    }

    @Test
    void shouldNotUnionFindFoo() {
        fixture.returnsData("'find':{'__typename':'SuperHeroNotFound', 'message':'There is no hero named Foo'}");
        var api = fixture.build(UnionApi.class);

        var response = api.find("Foo");

        then(fixture.query()).isEqualTo("query find($name: String) { find(name: $name){" +
                "__typename " +
                "... on SuperHero {name} " +
                "... on SuperHeroNotFound {message}} }");
        then(fixture.variables()).isEqualTo("{'name':'Foo'}");
        then(response).isInstanceOf(NotFound.class);
        then(((NotFound) response).getMessage()).isEqualTo("There is no hero named Foo");
    }

    @Test
    void shouldNotUnionFindSomethingUnexpected() {
        fixture.returnsData("'find':{'__typename':'UnexpectedType', 'detail':'You dont know this type'}");
        var api = fixture.build(UnionApi.class);

        var throwable = catchThrowable(() -> api.find("Expect the unexpected"));

        then(fixture.query()).isEqualTo("query find($name: String) { find(name: $name){" +
                "__typename " +
                "... on SuperHero {name} " +
                "... on SuperHeroNotFound {message}} }");
        then(fixture.variables()).isEqualTo("{'name':'Expect the unexpected'}");
        then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("SRGQLDC035010: Cannot instantiate UnexpectedType");
    }

    @Test
    void shouldUnionFindAll() {
        fixture.returnsData("'findAll':[" +
                "{'__typename':'SuperHero','name':'Spider-Man'}," +
                "{'__typename':'SuperHeroNotFound','message':'The Nobody'}" +
                "]");
        var api = fixture.build(UnionApi.class);

        var response = api.findAll("Spider-Man");

        then(fixture.query()).isEqualTo("query findAll($name: String) { findAll(name: $name){" +
                "__typename " +
                "... on SuperHero {name} " +
                "... on SuperHeroNotFound {message}} }");
        then(fixture.variables()).isEqualTo("{'name':'Spider-Man'}");
        then(response).hasSize(2);
        then(response.get(0)).isInstanceOf(SuperHero.class);
        then(((SuperHero) response.get(0)).getName()).isEqualTo("Spider-Man");
        then(response.get(1)).isInstanceOf(NotFound.class);
        then(((NotFound) response.get(1)).getMessage()).isEqualTo("The Nobody");
    }
}
