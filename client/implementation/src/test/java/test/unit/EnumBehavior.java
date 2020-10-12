package test.unit;

import static org.assertj.core.api.BDDAssertions.then;
import static test.unit.EnumBehavior.Episode.EMPIRE;
import static test.unit.EnumBehavior.Episode.JEDI;
import static test.unit.EnumBehavior.Episode.NEWHOPE;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

public class EnumBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    enum Episode {
        NEWHOPE,
        EMPIRE,
        JEDI
    }

    @GraphQlClientApi
    interface EpisodeApi {
        Episode episode();
    }

    @Test
    public void shouldCallEnumQuery() {
        fixture.returnsData("'episode':'JEDI'");
        EpisodeApi api = fixture.builder().build(EpisodeApi.class);

        Episode episode = api.episode();

        then(fixture.query()).isEqualTo("query episode { episode }");
        then(episode).isEqualTo(JEDI);
    }

    @GraphQlClientApi
    interface EpisodesApi {
        List<Episode> episodes();
    }

    @Test
    public void shouldCallEnumListQuery() {
        fixture.returnsData("'episodes':['NEWHOPE','EMPIRE','JEDI']");
        EpisodesApi api = fixture.builder().build(EpisodesApi.class);

        List<Episode> episode = api.episodes();

        then(fixture.query()).isEqualTo("query episodes { episodes }");
        then(episode).containsExactly(NEWHOPE, EMPIRE, JEDI);
    }

    @GraphQlClientApi
    interface EpisodeFilterApi {
        List<String> characters(Episode episode);
    }

    @Test
    public void shouldCallEnumFilterQuery() {
        fixture.returnsData("'characters':['Luke', 'Darth']");
        EpisodeFilterApi api = fixture.builder().build(EpisodeFilterApi.class);

        List<String> characters = api.characters(JEDI);

        then(fixture.query()).isEqualTo("query characters($episode: Episode) { characters(episode: $episode) }");
        then(fixture.variables()).isEqualTo("{'episode':'JEDI'}");
        then(characters).containsExactly("Luke", "Darth");
    }
}
