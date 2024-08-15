package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

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
    public interface SuperHero {
        String getName();
    }

    public static class MainCharacter implements SuperHero {
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

    public static class SideKick implements SuperHero {
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
    interface UnionApi {
        SuperHero find(String name);
    }

    @Test
    void shouldUnionFindBatman() {
        fixture.returnsData("'find':{'__typename':'MainCharacter','name':'Batman','superPower':'Money'}");
        UnionApi api = fixture.build(UnionApi.class);

        SuperHero response = api.find("Batman");

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
    void shouldUnionFindRobin() {
        fixture.returnsData("'find':{'__typename':'SideKick', 'name':'Robin', 'mainCharacter':'Batman'}");
        UnionApi api = fixture.build(UnionApi.class);

        SuperHero response = api.find("Robin");

        then(fixture.query()).isEqualTo("query find($name: String) { find(name: $name){" +
                "__typename " +
                "... on MainCharacter {name superPower} " +
                "... on SideKick {name mainCharacter}} }");
        then(fixture.variables()).isEqualTo("{'name':'Robin'}");
        then(response.getName()).isEqualTo("Robin");
        then(response).isInstanceOf(SideKick.class);
        then(((SideKick) response).getMainCharacter()).isEqualTo("Batman");
    }
}
