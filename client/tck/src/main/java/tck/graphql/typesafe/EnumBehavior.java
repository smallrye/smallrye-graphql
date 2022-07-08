package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;
import static tck.graphql.typesafe.EnumBehavior.Episode.EMPIRE;
import static tck.graphql.typesafe.EnumBehavior.Episode.JEDI;
import static tck.graphql.typesafe.EnumBehavior.Episode.NEWHOPE;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class EnumBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    public static class DescribedValue<T> {

        public DescribedValue() {
        }

        T value;
        String description;

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    enum Episode {
        NEWHOPE,
        EMPIRE,
        JEDI
    }

    @GraphQLClientApi
    interface EpisodeApi {
        Episode episode();
    }

    @Test
    void shouldCallEnumQuery() {
        fixture.returnsData("'episode':'JEDI'");
        EpisodeApi api = fixture.build(EpisodeApi.class);

        Episode episode = api.episode();

        then(fixture.query()).isEqualTo("query episode { episode }");
        then(episode).isEqualTo(JEDI);
    }

    @GraphQLClientApi
    interface EpisodesApi {
        List<Episode> episodes();
    }

    @Test
    void shouldCallEnumListQuery() {
        fixture.returnsData("'episodes':['NEWHOPE','EMPIRE','JEDI']");
        EpisodesApi api = fixture.build(EpisodesApi.class);

        List<Episode> episode = api.episodes();

        then(fixture.query()).isEqualTo("query episodes { episodes }");
        then(episode).containsExactly(NEWHOPE, EMPIRE, JEDI);
    }

    @GraphQLClientApi
    interface EpisodeFilterApi {
        List<String> characters(Episode episode);
    }

    @Test
    void shouldCallEnumFilterQuery() {
        fixture.returnsData("'characters':['Luke', 'Darth']");
        EpisodeFilterApi api = fixture.build(EpisodeFilterApi.class);

        List<String> characters = api.characters(JEDI);

        then(fixture.query()).isEqualTo("query characters($episode: Episode) { characters(episode: $episode) }");
        then(fixture.variables()).isEqualTo("{'episode':'JEDI'}");
        then(characters).containsExactly("Luke", "Darth");
    }

    @GraphQLClientApi
    interface EpisodeGenericApi {
        DescribedValue<Episode> describedEpisode();
    }

    @Test
    void shouldCallGenericQuery() {
        fixture.returnsData("'describedEpisode':{'value':'NEWHOPE','description':'Episode 4'}");
        EpisodeGenericApi api = fixture.build(EpisodeGenericApi.class);

        DescribedValue<Episode> episode = api.describedEpisode();

        then(fixture.query()).isEqualTo("query describedEpisode { describedEpisode {value description} }");
        then(episode.description).isEqualTo("Episode 4");
        then(episode.value).isEqualTo(NEWHOPE);
    }
}
