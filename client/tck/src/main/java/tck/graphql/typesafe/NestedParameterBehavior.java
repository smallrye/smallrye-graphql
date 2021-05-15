package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.eclipse.microprofile.graphql.Name;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.Multiple;
import io.smallrye.graphql.client.typesafe.api.NestedParameter;

class NestedParameterBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    static class SuperHero {
        String name;

        String getName() {
            return name;
        }
    }

    static class Team {
        String headQuarter;
        List<SuperHero> members;
    }

    @GraphQLClientApi
    interface TeamsApi {
        Team team(String teamName, @NestedParameter("members") int limit);
    }

    @Test
    void shouldCallNestedParameterQuery() {
        fixture.returnsData("'team':{'members':[{'name':'Spider-Man'},{'name':'Black Panther'},{'name':'Groot'}]}");
        TeamsApi api = fixture.build(TeamsApi.class);

        Team team = api.team("endgame", 3);

        then(fixture.query())
                .isEqualTo("query team($teamName: String, $limit: Int!) { team(teamName: $teamName) " +
                        "{headQuarter members(limit: $limit) {name}} }");
        then(fixture.variables()).isEqualTo("{'teamName':'endgame','limit':3}");
        then(team.headQuarter).isNull();
        then(team.members.stream().map(SuperHero::getName)).containsExactly("Spider-Man", "Black Panther", "Groot");
    }

    static class Universe {
        String name;
        List<Team> teams;
    }

    @GraphQLClientApi
    interface UniverseApi {
        Universe universeWithTeam(
                @NestedParameter("teams") String teamName,
                @NestedParameter("teams.members") Integer offset,
                @NestedParameter("teams.members") Integer limit);
    }

    @Test
    void shouldCallDeeplyNestedParameterQuery() {
        fixture.returnsData("'universeWithTeam':{'name':'Marvel','teams':[{'members':" +
                "[{'name':'Spider-Man'},{'name':'Black Panther'},{'name':'Groot'}]}]}");
        UniverseApi api = fixture.build(UniverseApi.class);

        Universe universe = api.universeWithTeam("endgame", 32, 3);

        then(fixture.query())
                .isEqualTo("query universeWithTeam($teamName: String, $offset: Int, $limit: Int) { universeWithTeam " +
                        "{name teams(teamName: $teamName) {headQuarter members(offset: $offset, limit: $limit) {name}}} }");
        then(fixture.variables()).isEqualTo("{'teamName':'endgame','offset':32,'limit':3}");
        then(universe.name).isEqualTo("Marvel");
        Team team = universe.teams.get(0);
        then(team.headQuarter).isNull();
        then(team.members.stream().map(SuperHero::getName)).containsExactly("Spider-Man", "Black Panther", "Groot");
    }

    @GraphQLClientApi
    interface HeroesAPI {
        HeroPair heroPairByNames(
                @NestedParameter("left") @Name("name") String leftName,
                @NestedParameter("right") @Name("name") String rightName);
    }

    static class Hero {
        String name;
        boolean good;
    }

    @Multiple
    static class HeroPair {
        @Name("hero")
        Hero left;
        @Name("hero")
        Hero right;
    }

    @Test
    void shouldCallMultipleWithParams() {
        fixture.returnsData("'left':{'name':'Spider-Man','good':true},'right':{'name':'Venom','good':false}");
        HeroesAPI api = fixture.build(HeroesAPI.class);

        HeroPair pair = api.heroPairByNames("Spider-Man", "Venom");

        then(fixture.query())
                .isEqualTo("query heroPairByNames($leftName: String, $rightName: String) {" +
                        "left:hero(name: $leftName) {name good} right:hero(name: $rightName) {name good}}");
        then(fixture.variables()).isEqualTo("{'leftName':'Spider-Man','rightName':'Venom'}");
        then(pair.left.name).isEqualTo("Spider-Man");
        then(pair.left.good).isTrue();
        then(pair.right.name).isEqualTo("Venom");
        then(pair.right.good).isFalse();
    }
}
