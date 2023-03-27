package test;

import static graphql.ErrorType.InvalidSyntax;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.contentOf;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.data.MapEntry.entry;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.Test;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.schema.idl.errors.SchemaProblem;
import io.smallrye.graphql.client.generator.Generator;
import io.smallrye.graphql.client.generator.GraphQLGeneratorException;

public class GeneratorBehavior {
    private static final String HEROES = "{heroes { name realName }}";
    private static final String HEROES_IN_LOCATION = "query heroesIn($location: String) {heroesIn(location: $location) { name realName }}";

    private static final String HEROES_METHOD = "List<SuperHero> heroes();";
    private static final String HEROES_IN_LOCATION_METHOD = "List<SuperHero> heroesIn(String location);";

    private static final MapEntry<String, String> CLASS_SUPER_HERO_WITH_REAL_NAME = entry("the_package.SuperHero", "" +
            "package the_package;\n" +
            "\n" +
            "public class SuperHero {\n" +
            "    String name;\n" +
            "    String realName;\n" +
            "}\n");

    private static final MapEntry<String, String> CLASS_TEAM = entry("the_package.Team", "" +
            "package the_package;\n" +
            "\n" +
            "public class Team {\n" +
            "    String name;\n" +
            "}\n");

    private static final String SCHEMA = contentOf(new File("src/test/resources/schema.graphql"));

    private String schema = SCHEMA;

    @Test
    public void shouldGenerateApiForOneSimpleQuery() {
        Generator generator = givenGeneratorFor(HEROES);

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import java.util.List;\n\n", HEROES_METHOD),
                CLASS_SUPER_HERO_WITH_REAL_NAME);
    }

    @Test
    public void shouldGenerateApiForOneParameterizedQuery() {
        Generator generator = givenGeneratorFor(HEROES_IN_LOCATION);

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import java.util.List;\n\n", HEROES_IN_LOCATION_METHOD),
                CLASS_SUPER_HERO_WITH_REAL_NAME);
    }

    @Test
    public void shouldFailToGenerateApiWithInvalidSchema() {
        schema = "foo";
        Generator generator = givenGeneratorFor(HEROES);

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessage("can't parse schema: foo")
                .hasCauseInstanceOf(SchemaProblem.class);
        thenErrorCauseOf(thrown)
                .hasErrorType(InvalidSyntax)
                .hasMessage("Invalid syntax with offending token 'foo' at line 1 column 1");
    }

    @Test
    public void shouldFailToGenerateApiWithSchemaWithoutQuery() {
        schema = "type SuperHero { name: String }";
        Generator generator = givenGeneratorFor(HEROES);

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessage("'Query' type not found in schema");
    }

    @Test
    public void shouldFailToGenerateApiWithUnknownQuery() {
        Generator generator = givenGeneratorFor("{unknown { name }}");

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessageStartingWith("field (method) 'unknown' not found in [");
    }

    @Test
    public void shouldFailToGenerateApiWithInvalidQuery() {
        Generator generator = givenGeneratorFor("invalid {heroes}");

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessage("can't parse query: invalid {heroes}");
    }

    @Test
    public void shouldGenerateApiWithoutQueries() {
        Generator generator = givenGeneratorFor();

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles, generatedApiWith(""));
    }

    @Test
    public void shouldFailToGenerateApiWithMultipleDefinitionInQuery() {
        Generator generator = givenGeneratorFor("query foo {heroes {name}} mutation bar {name}");

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessage("expected exactly one definition but found [query foo, mutation bar]");
    }

    @Test
    public void shouldFailToGenerateApiWithMultipleQuery() {
        Generator generator = givenGeneratorFor("{heroes { name } heroesIn(location: String) { realName }}");

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessage("expected exactly one field but got [heroes, heroesIn]");
    }

    @Test
    public void shouldGenerateApiWithMultipleQueries() {
        Generator generator = givenGeneratorFor(HEROES, HEROES_IN_LOCATION);

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import java.util.List;\n\n", HEROES_METHOD, HEROES_IN_LOCATION_METHOD),
                CLASS_SUPER_HERO_WITH_REAL_NAME);
    }

    @Test
    public void shouldFailToGenerateApiWithMultipleQueriesSelectingDifferentFields() {
        Generator generator = givenGeneratorFor("{heroes { name }}", HEROES_IN_LOCATION);

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessage("already generated SuperHero");
    }

    @Test
    public void shouldGenerateApiWithExplicitQuery() {
        Generator generator = givenGeneratorFor("query {heroes { name realName }}");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import java.util.List;\n\n", HEROES_METHOD),
                CLASS_SUPER_HERO_WITH_REAL_NAME);
    }

    @Test
    public void shouldGenerateApiWithNamedQuery() {
        Generator generator = givenGeneratorFor("query foo {heroes { name realName }}");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import java.util.List;\nimport org.eclipse.microprofile.graphql.Query;\n\n",
                        "@Query(\"heroes\") List<SuperHero> foo();"),
                CLASS_SUPER_HERO_WITH_REAL_NAME);
    }

    @Test
    public void shouldGenerateApiWithAliasedQuery() {
        Generator generator = givenGeneratorFor("{foo: heroes { name realName }}");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import java.util.List;\nimport org.eclipse.microprofile.graphql.Query;\n\n",
                        "@Query(\"heroes\") List<SuperHero> foo();"),
                CLASS_SUPER_HERO_WITH_REAL_NAME);
    }

    @Test
    public void shouldGenerateApiWithNonNullReturnType() {
        Generator generator = givenGeneratorFor("{countHeroes}");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import org.eclipse.microprofile.graphql.NonNull;\n\n", "@NonNull Integer countHeroes();"));
    }

    @Test
    public void shouldGenerateApiWithIntegerQuery() {
        Generator generator = givenGeneratorFor("{countTeams}");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("", "Integer countTeams();"));
    }

    @Test
    public void shouldGenerateApiWithBooleanQuery() {
        Generator generator = givenGeneratorFor("query heroExists($name: String) {heroExists(name: $name)}");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("", "Boolean heroExists(String name);"));
    }

    @Test
    public void shouldGenerateApiWithFloatQuery() {
        Generator generator = givenGeneratorFor("{averageTeamSize}");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("", "Float averageTeamSize();"));
    }

    @Test
    public void shouldGenerateApiWithIdQuery() {
        Generator generator = givenGeneratorFor("query heroId($name: String) {heroId(name: $name)}");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("", "String heroId(String name);"));
    }

    @Test
    public void shouldGenerateApiWithIntParameter() {
        Generator generator = givenGeneratorFor("query teamsLargerThan($size: Int) {teamsLargerThan(size: $size) {name} }");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import java.util.List;\n\n", "List<Team> teamsLargerThan(Integer size);"),
                CLASS_TEAM);
    }

    @Test
    public void shouldGenerateApiWithNonNullParameter() {
        Generator generator = givenGeneratorFor("query teamsLargerThan($size: Int!) {teamsLargerThan(size: $size) {name} }");

        Map<String, String> generatedFiles = generator.generateSourceFiles();

        thenContainsExactly(generatedFiles,
                generatedApiWith("import java.util.List;\nimport org.eclipse.microprofile.graphql.NonNull;\n\n",
                        "List<Team> teamsLargerThan(@NonNull Integer size);"),
                CLASS_TEAM);
    }

    @Test
    public void shouldFailToGenerateApiWithUndefinedParameter() {
        Generator generator = givenGeneratorFor("query teamsLargerThan($foo: Int) {teamsLargerThan(size: $bar)}");

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessage("no definition found for parameter 'bar' in [foo]");
    }

    @Test
    public void shouldFailToGenerateApiWithConstantParameter() {
        Generator generator = givenGeneratorFor("query teamsLargerThanThree{teamsLargerThan(size: 3) {name}}");

        GraphQLGeneratorException thrown = catchThrowableOfType(generator::generateSourceFiles,
                GraphQLGeneratorException.class);

        then(thrown).hasMessage("unsupported type IntValue{value=3} for argument 'size'");
        // will be:
        // Map<String, String> generatedFiles = generator.generateSourceFiles();
        //
        // thenContainsExactly(generatedFiles,
        //     generatedApiWith("import java.util.List;\n\n",
        //         "@Query(\"teamsLargerThan\")\n" +
        //             "        @Constant(\"size\", \"3\")\n" +
        //             "        List<Team> teamsLargerThree();\n"),
        //             TEAM);
    }

    private Generator givenGeneratorFor(String... queries) {
        return new Generator("the_package", "GeneratedApi", schema, asList(queries));
    }

    @SafeVarargs
    private final void thenContainsExactly(Map<String, String> actual, Map.Entry<String, String>... expected) {
        Stream.of(expected).forEach(entry -> thenContains(actual, entry.getKey(), entry.getValue()));
        if (expected.length == 0)
            then(actual).isEmpty();
        else
            then(actual).containsOnlyKeys(Stream.of(expected).map(Entry::getKey).collect(toSet()));
    }

    private void thenContains(Map<String, String> actual, String key, String value) {
        then(actual.keySet()).describedAs("keys of \n----------\n%s\n----------\n", actual).contains(key);
        then(actual.get(key)).describedAs("value of %s", key).isEqualTo(value);
    }

    private MapEntry<String, String> generatedApiWith(String imports, String... methods) {
        return entry("the_package.GeneratedApi", "" +
                "package the_package;\n" +
                "\n" +
                imports +
                "public interface GeneratedApi {\n" +
                ((methods.length == 0) ? "" : "    " + join("\n    ", methods) + "\n") +
                "}\n");
    }

    private GraphQlErrorAssert thenErrorCauseOf(GraphQLGeneratorException thrown) {
        List<GraphQLError> errors = ((SchemaProblem) thrown.getCause()).getErrors();
        then(errors).hasSize(1);
        GraphQLError error = errors.get(0);
        return new GraphQlErrorAssert(error);
    }

    private static class GraphQlErrorAssert {
        private final GraphQLError error;

        public GraphQlErrorAssert(GraphQLError error) {
            this.error = error;
        }

        public GraphQlErrorAssert hasErrorType(ErrorType errorType) {
            then(error.getErrorType()).isEqualTo(errorType);
            return this;
        }

        public void hasMessage(String message) {
            then(error.getMessage()).isEqualTo(message);
        }
    }
}
