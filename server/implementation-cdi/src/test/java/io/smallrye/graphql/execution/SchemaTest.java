package io.smallrye.graphql.execution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        this.schema = SchemaBuilder.build(index);
        Assert.assertNotNull(schema);
    }

    @Test
    public void testSchemaModelCreation() throws IOException {
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);
        Assert.assertNotNull(graphQLSchema);
        String schemaString = new SchemaPrinter(new Config() {
        }).print(graphQLSchema);
        Assert.assertNotNull(schemaString);

        LOG.info(schemaString);

        // Let's check some entries
        List<String> lines = getLines(schemaString);

        // Test interface creation
        Assert.assertTrue("Problem with Interface creation",
                lines.contains("interface BasicInterface {"));

        // TODO: Test interface with interface

        // Test interface description
        Assert.assertTrue("Problem with Interface description",
                lines.contains("\"Basically any sentient being with a name\""));

        // Test type with interface
        Assert.assertTrue("Problem with Type that implements an interface",
                lines.contains("type SuperHero implements Character {"));

        // Test type creation
        Assert.assertTrue("Problem with Type creation",
                lines.contains("type Item {"));

        // Test type description
        Assert.assertTrue("Problem with Type description",
                lines.contains("\"Something of use to a super hero\""));

        // Test enum creation
        Assert.assertTrue("Problem with Enum creation",
                lines.contains("enum CountDown {"));
        Assert.assertTrue("Problem with Enum values creation",
                lines.contains("THREE"));

        // Test enum usage in Field
        Assert.assertTrue("Problem with Enum as a field",
                lines.contains("countdownPlace: CountDown"));

        // TODO: Test enum with description
        // TODO: Test enum usage in argument

        // Test default mandatory primitive
        Assert.assertTrue("Problem with default mandatory primitive",
                lines.contains("artificialIntelligenceRating: Boolean!"));

        // Test non mandatory primitive
        Assert.assertTrue("Problem with non null primitive",
                lines.contains("supernatural: Boolean"));

        // Test date format description
        Assert.assertTrue("Problem with date format description",
                lines.contains("\"dd MMM yyyy 'at' HH:mm 'in zone' Z en-ZA\""));

        // Test ID Scalar
        Assert.assertTrue("Problem with Id Scalar field",
                lines.contains("id: ID!"));

        // Test Mutation
        Assert.assertTrue("Mutation type not created",
                lines.contains("type Mutation {"));

        // Test operation with arguments
        Assert.assertTrue("Problem with Mutation operation with argument",
                lines.contains("addHeroToTeam(hero: String, team: String): Team"));

        // Test operation description
        Assert.assertTrue("Problem with Mutation operation description",
                lines.contains("\"Adds a hero to the specified team and returns the updated team.\""));

        // TODO: Test default value json
        // provisionHero(hero: String, item: ItemInput = {}): SuperHero

        // Test default value
        Assert.assertTrue("Problem with Default value in mutation operation argument",
                lines.contains("updateItemPowerLevel(itemID: BigInteger!, powerLevel: Int = 5): Item"));

        Assert.assertTrue("Problem with Default value in query operation argument",
                lines.contains("allHeroesIn(city: String = \"New York, NY\"): [SuperHero]"));

        // Test source
        Assert.assertTrue("Problem with Source",
                lines.contains("currentLocation: String"));

        // Test source that is also a Query
        Assert.assertTrue("Problem with Source that is also a query",
                lines.contains("currentLocation(superHero: SuperHeroInput): String"));

        // Test source with parameters
        Assert.assertTrue("Problem with Source with parameters",
                lines.contains("secretToken(maskFirstPart: Boolean = true): TopSecretToken"));

        // Test multi-level array
        Assert.assertTrue("Problem with Multi-level arrays",
                lines.contains("trackHero(coordinates: [[BigDecimal]], name: String): SuperHero"));

        // Test Query
        Assert.assertTrue("Mutation type not created",
                lines.contains("type Query {"));

        // Test name starting with get
        Assert.assertTrue("Names shoud not start with getX",
                lines.contains("getaway: String"));

        // Test description with date format
        Assert.assertTrue("Problem with date scalar with description ",
                lines.contains("\"This is another datetime (ISO-8601)\""));

        // Test number format description
        Assert.assertTrue("Problem with number scalar default description ",
                lines.contains("\"#,###.## en-GB\""));

        // Test number format with description
        Assert.assertTrue("Problem with number scalar with description ",
                lines.contains("\"This is a formatted number (#0.0 en-GB)\""));
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
