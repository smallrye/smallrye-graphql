package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.eclipse.microprofile.graphql.Mutation;
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
        List<HeadQuarter> headQuarters;
        List<SuperHero> members;
    }

    static class HeadQuarter {
        String name;

        String getName() {
            return name;
        }
    }

    @GraphQLClientApi
    interface TeamsApi {
        Team team(String teamName, @NestedParameter("members") int limit);
    }

    @Test
    void shouldCallNestedParameterQuery() {
        fixture.returnsData("'team':{" +
                "'members':[{'name':'Spider-Man'},{'name':'Black Panther'},{'name':'Groot'}]," +
                "'headQuarters':[{'name':'Earth'}]}");
        TeamsApi api = fixture.build(TeamsApi.class);

        Team team = api.team("endgame", 3);

        then(fixture.query())
                .isEqualTo("query team($teamName: String, $limit: Int!) { team(teamName: $teamName) " +
                        "{headQuarters {name} members(limit: $limit) {name}} }");
        then(fixture.variables()).isEqualTo("{'teamName':'endgame','limit':3}");
        then(team.headQuarters.stream().map(HeadQuarter::getName)).containsExactly("Earth");
        then(team.members.stream().map(SuperHero::getName)).containsExactly("Spider-Man", "Black Panther", "Groot");
    }

    @GraphQLClientApi
    interface MultipleTeamsApi {
        Team team(String teamName, @NestedParameter({ "members", "headQuarters" }) int limit);
    }

    @Test
    void shouldCallMultipleNestedParameterQuery() {
        fixture.returnsData(
                "'team':{'members':[{'name':'Spider-Man'},{'name':'Black Panther'},{'name':'Groot'}],'headQuarters':[]}");
        MultipleTeamsApi api = fixture.build(MultipleTeamsApi.class);

        Team team = api.team("endgame", 3);

        then(fixture.query())
                .isEqualTo("query team($teamName: String, $limit: Int!) { team(teamName: $teamName) " +
                        "{headQuarters(limit: $limit) {name} members(limit: $limit) {name}} }");
        then(fixture.variables()).isEqualTo("{'teamName':'endgame','limit':3}");
        then(team.headQuarters).isEmpty();
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
                        "{name teams(teamName: $teamName) {headQuarters {name} members(offset: $offset, limit: $limit) {name}}} }");
        then(fixture.variables()).isEqualTo("{'teamName':'endgame','offset':32,'limit':3}");
        then(universe.name).isEqualTo("Marvel");
        Team team = universe.teams.get(0);
        then(team.headQuarters).isNull();
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

    @GraphQLClientApi
    private interface MultiUpdateAPI {
        @Mutation
        MultiUpdate multiUpdate(
                @NestedParameter("updateLocation") String location,
                @NestedParameter("addTeam") String teamName,
                @NestedParameter("updateName") String name);
    }

    @Multiple
    static class MultiUpdate {
        String updateLocation;

        List<String> addTeam;

        Hero updateName;
    }

    @Test
    void shouldCallMultipleMutations() {
        fixture.returnsData("" +
                "'updateLocation':'loc-updated'," +
                "'addTeam':['team1', 'team2', 'new-team']," +
                "'updateName':{'name':'name-updated','good':false}");
        MultiUpdateAPI api = fixture.build(MultiUpdateAPI.class);

        MultiUpdate result = api.multiUpdate("new-loc", "new-team", "new-name");

        then(fixture.query()).isEqualTo("" +
                "mutation multiUpdate($location: String, $teamName: String, $name: String) {" +
                "updateLocation(location: $location) " +
                "addTeam(teamName: $teamName) " +
                "updateName(name: $name) {name good}" +
                "}");
        then(fixture.variables()).isEqualTo("{'location':'new-loc','teamName':'new-team','name':'new-name'}");
        then(result.updateLocation).isEqualTo("loc-updated");
        then(result.addTeam).containsExactly("team1", "team2", "new-team");
        then(result.updateName.name).isEqualTo("name-updated");
        then(result.updateName.good).isFalse();
    }
}
