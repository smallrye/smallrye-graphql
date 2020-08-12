package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Test the graphql-java Schema creation from the schema model
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SchemaTest {
    private static final Logger LOG = Logger.getLogger(SchemaTest.class.getName());

    private Schema schema;

    @BeforeEach
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        this.schema = SchemaBuilder.build(index);
        assertNotNull(schema);
    }

    @Test
    public void testSchemaModelCreation() {
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema).getGraphQLSchema();
        assertNotNull(graphQLSchema);
        String schemaString = new SchemaPrinter(new Config() {
        }).print(graphQLSchema);
        assertNotNull(schemaString);

        LOG.info(schemaString);

        // Let's check some entries
        List<String> lines = getLines(schemaString);

        // Test interface creation
        assertTrue(lines.contains("interface BasicInterface {"),
                "Problem with Interface creation");

        // TODO: Test interface with interface

        // Test interface description
        assertTrue(lines.contains("\"Basically any sentient being with a name\""),
                "Problem with Interface description");

        // Test type with interface
        assertTrue(lines.contains("type SuperHero implements Character {"),
                "Problem with Type that implements an interface");

        // Test type creation
        assertTrue(lines.contains("type Item {"),
                "Problem with Type creation");

        // Test type description
        assertTrue(lines.contains("\"Something of use to a super hero\""),
                "Problem with Type description");

        // Test enum creation
        assertTrue(lines.contains("enum CountDown {"),
                "Problem with Enum creation");
        assertTrue(lines.contains("THREE"),
                "Problem with Enum values creation");

        // Test enum usage in Field
        assertTrue(lines.contains("countdownPlace: CountDown"),
                "Problem with Enum as a field");

        // TODO: Test enum with description
        // TODO: Test enum usage in argument

        // Test default mandatory primitive
        assertTrue(lines.contains("artificialIntelligenceRating: Boolean!"),
                "Problem with default mandatory primitive");

        // Test non mandatory primitive
        assertTrue(lines.contains("supernatural: Boolean"),
                "Problem with non null primitive");

        // Test date format description
        assertTrue(lines.contains("\"dd MMM yyyy 'at' HH:mm 'in zone' Z en-ZA\""),
                "Problem with date format description");

        // Test ID Scalar
        assertTrue(lines.contains("id: ID!"),
                "Problem with Id Scalar field");

        // Test Mutation
        assertTrue(lines.contains("type Mutation {"),
                "Mutation type not created");

        // Test operation with arguments
        assertTrue(lines.contains("addHeroToTeam(hero: String, team: String): Team"),
                "Problem with Mutation operation with argument");

        // Test operation description
        assertTrue(lines.contains("\"Adds a hero to the specified team and returns the updated team.\""),
                "Problem with Mutation operation description");

        // TODO: Test default value json
        // provisionHero(hero: String, item: ItemInput = {}): SuperHero

        // Test default value
        assertTrue(lines.contains("updateItemPowerLevel(itemID: BigInteger!, powerLevel: Int = 5): Item"),
                "Problem with Default value in mutation operation argument");

        assertTrue(lines.contains("allHeroesIn(city: String = \"New York, NY\"): [SuperHero]"),
                "Problem with Default value in query operation argument");

        // Test source
        assertTrue(lines.contains("currentLocation: String"),
                "Problem with Source");

        // Test source that is also a Query
        assertTrue(lines.contains("currentLocation(superHero: SuperHeroInput): String"),
                "Problem with Source that is also a query");

        // Test source with parameters
        assertTrue(lines.contains("secretToken(maskFirstPart: Boolean = true): TopSecretToken"),
                "Problem with Source with parameters");

        // Test multi-level array
        assertTrue(lines.contains("trackHero(coordinates: [[BigDecimal]], name: String): SuperHero"),
                "Problem with Multi-level arrays");

        // Test Query
        assertTrue(lines.contains("type Query {"),
                "Mutation type not created");

        // Test name starting with get
        assertTrue(lines.contains("getaway: String"),
                "Names should not start with getX");

        // Test description with date format
        assertTrue(lines.contains("\"This is another datetime (ISO-8601)\""),
                "Problem with date scalar with description ");

        // Test number format description
        assertTrue(lines.contains("\"#,###.## en-GB\""),
                "Problem with number scalar default description ");

        // Test number format with description
        assertTrue(lines.contains("\"This is a formatted number (#0.0 en-GB)\""),
                "Problem with number scalar with description ");
    }

    private List<String> getLines(String input) {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(input)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line != null && !line.isEmpty() && !line.equals("}")) {
                    line = line.trim();
                    lines.add(line);
                }
            }
        }

        return lines;
    }
}
