package test;

import static org.assertj.core.api.Assertions.contentOf;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.File;

import org.junit.Test;

public class GeneratorIT {
    @Test
    public void shouldHaveGeneratedApi() {
        then(contentOf(sourceFile("SuperHeroesApi"))).isEqualTo("" +
                "package io.smallrye.graphql.client.generator.test;\n" +
                "\n" +
                "import java.util.List;\n" +
                "import org.eclipse.microprofile.graphql.Query;\n" +
                "\n" +
                "public interface SuperHeroesApi {\n" +
                "    List<Team> teams();\n" +
                "    @Query(\"heroesIn\") List<SuperHero> heroesLocatedIn(String location);\n" +
                "}\n");
    }

    @Test
    public void shouldHaveGeneratedTeam() {
        then(contentOf(sourceFile("Team"))).isEqualTo("" +
                "package io.smallrye.graphql.client.generator.test;\n" +
                "\n" +
                "public class Team {\n" +
                "    String name;\n" +
                "}\n");
    }

    @Test
    public void shouldHaveGeneratedSuperHero() {
        then(contentOf(sourceFile("SuperHero"))).isEqualTo("" +
                "package io.smallrye.graphql.client.generator.test;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class SuperHero {\n" +
                "    String name;\n" +
                "    List<String> superPowers;\n" +
                "    String realName;\n" +
                "}\n");
    }

    private File sourceFile(String file) {
        return new File("target/generated-sources/annotations/io/smallrye/graphql/client/generator/test/" + file + ".java");
    }
}
