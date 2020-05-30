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

        then(fixture.query()).isEqualTo("episode");
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

        then(fixture.query()).isEqualTo("episodes");
        then(episode).containsExactly(NEWHOPE, EMPIRE, JEDI);
    }
}
