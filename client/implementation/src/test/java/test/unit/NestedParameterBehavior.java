package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.NestedParameter;

class NestedParameterBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

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

    @GraphQlClientApi
    interface TeamsApi {
        Team team(String teamName, @NestedParameter("members") int limit);
    }

    @Test
    void shouldCallNestedParameterQuery() {
        fixture.returnsData("'team':{'members':[{'name':'Spiderman'},{'name':'Black Panther'},{'name':'Groot'}]}");
        TeamsApi api = fixture.build(TeamsApi.class);

        Team team = api.team("endgame", 3);

        then(fixture.query()).isEqualTo("query team($teamName: String, $limit: Int!) { team(teamName: $teamName) " +
                "{headQuarter members(limit: $limit) {name}} }");
        then(fixture.variables()).isEqualTo("{'teamName':'endgame','limit':3}");
        then(team.headQuarter).isNull();
        then(team.members.stream().map(SuperHero::getName)).containsExactly("Spiderman", "Black Panther", "Groot");
    }

    static class Universe {
        String name;
        List<Team> teams;
    }

    @GraphQlClientApi
    interface UniverseApi {
        Universe universeWithTeam(
                @NestedParameter("teams") String teamName,
                @NestedParameter("teams.members") Integer offset,
                @NestedParameter("teams.members") Integer limit);
    }

    @Test
    void shouldCallDeeplyNestedParameterQuery() {
        fixture.returnsData("'universeWithTeam':{'name':'Marvel','teams':[{'members':" +
                "[{'name':'Spiderman'},{'name':'Black Panther'},{'name':'Groot'}]}]}");
        UniverseApi api = fixture.build(UniverseApi.class);

        Universe universe = api.universeWithTeam("endgame", 32, 3);

        then(fixture.query())
                .isEqualTo("query universeWithTeam($teamName: String, $offset: Int, $limit: Int) { universeWithTeam() " +
                        "{name teams(teamName: $teamName) {headQuarter members(offset: $offset, limit: $limit) {name}}} }");
        then(fixture.variables()).isEqualTo("{'teamName':'endgame','offset':32,'limit':3}");
        then(universe.name).isEqualTo("Marvel");
        Team team = universe.teams.get(0);
        then(team.headQuarter).isNull();
        then(team.members.stream().map(SuperHero::getName)).containsExactly("Spiderman", "Black Panther", "Groot");
    }
}
