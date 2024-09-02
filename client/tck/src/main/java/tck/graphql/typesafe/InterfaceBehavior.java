package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTypeInfo;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class InterfaceBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @JsonbTypeInfo(key = "__typename", value = {
            @JsonbSubtype(alias = "MainCharacter", type = MainCharacter.class),
            @JsonbSubtype(alias = "SideKick", type = SideKick.class)
    })
    public interface SearchResult {
        String getName();
    }

    public static class MainCharacter implements SearchResult {
        String name;
        String superPower;

        @Override
        public String getName() {
            return name;
        }

        public String getSuperPower() {
            return superPower;
        }
    }

    public static class SideKick implements SearchResult {
        String name;
        String mainCharacter;

        @Override
        public String getName() {
            return name;
        }

        public String getMainCharacter() {
            return mainCharacter;
        }
    }

    @GraphQLClientApi
    interface InterfaceApi {
        SearchResult find(String name);

        List<SearchResult> all();
    }

    @Test
    void shouldInterfaceFindBatman() {
        fixture.returnsData("'find':{'__typename':'MainCharacter','name':'Batman','superPower':'Money'}");
        var api = fixture.build(InterfaceApi.class);

        var response = api.find("Batman");

        then(fixture.query()).isEqualTo("query find($name: String) { find(name: $name){" +
                "__typename " +
                "... on MainCharacter {name superPower} " +
                "... on SideKick {name mainCharacter}} }");
        then(fixture.variables()).isEqualTo("{'name':'Batman'}");
        then(response.getName()).isEqualTo("Batman");
        then(response).isInstanceOf(MainCharacter.class);
        then(((MainCharacter) response).getSuperPower()).isEqualTo("Money");
    }

    @Test
    void shouldInterfaceFindRobin() {
        fixture.returnsData("'find':{'__typename':'SideKick', 'name':'Robin', 'mainCharacter':'Batman'}");
        var api = fixture.build(InterfaceApi.class);

        var response = api.find("Robin");

        then(fixture.query()).isEqualTo("query find($name: String) { find(name: $name){" +
                "__typename " +
                "... on MainCharacter {name superPower} " +
                "... on SideKick {name mainCharacter}} }");
        then(fixture.variables()).isEqualTo("{'name':'Robin'}");
        then(response.getName()).isEqualTo("Robin");
        then(response).isInstanceOf(SideKick.class);
        then(((SideKick) response).getMainCharacter()).isEqualTo("Batman");
    }

    @Test
    void shouldInterfaceFindAll() {
        fixture.returnsData("'all':[" +
                "{'__typename':'MainCharacter','name':'Batman','superPower':'Money'}," +
                "{'__typename':'SideKick', 'name':'Robin', 'mainCharacter':'Batman'}" +
                "]");
        var api = fixture.build(InterfaceApi.class);

        var response = api.all();

        then(fixture.query()).isEqualTo("query all { all{" +
                "__typename " +
                "... on MainCharacter {name superPower} " +
                "... on SideKick {name mainCharacter}} }");
        then(fixture.variables()).isEqualTo("{}");
        then(response).hasSize(2);
        then(response.get(0).getName()).isEqualTo("Batman");
        then(response.get(0)).isInstanceOf(MainCharacter.class);
        then(((MainCharacter) response.get(0)).getSuperPower()).isEqualTo("Money");
        then(response.get(1).getName()).isEqualTo("Robin");
        then(response.get(1)).isInstanceOf(SideKick.class);
        then(((SideKick) response.get(1)).getMainCharacter()).isEqualTo("Batman");
    }
}
