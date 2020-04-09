package io.smallrye.graphql.execution;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Test;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.testhelper.Indexer;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;

/**
 * Test a basic query
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class QueryTest {
    private static final Logger LOG = Logger.getLogger(QueryTest.class.getName());

    private ExecutionService executionService;

    @Rule
    public WeldInitiator weld = WeldInitiator.of(heroFinder,heroDatabase,sidekickDatabase,heroLocator);
    
    @Before
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        Schema schema = SchemaBuilder.build(index);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);

        this.executionService = new ExecutionService(getGraphQLConfig(), graphQLSchema);
        executionService.init();

    }

    @Test
    public void testQuery() throws IOException {
        JsonObject result = executionService.execute(toJsonObject(GET_HERO));
        JsonObject data = result.getJsonObject(DATA);
        
        LOG.error(">>>> data = " + data.toString());
    }

    private JsonObject toJsonObject(String graphQL) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("query", graphQL);
        return builder.build();
    }

    private GraphQLConfig getGraphQLConfig() {
        GraphQLConfig config = new GraphQLConfig();
        config.setAllowGet(false);
        config.setPrintDataFetcherException(true);
        return config;
    }

    private static final String DATA = "data";
    
    private static final String GET_HERO = "{\n" +
            "  superHero(name:\"Iron Man\") {\n" +
            "    \n" +
            "    name\n" +
            "    primaryLocation\n" +
            "    realName\n" +
            "    superPowers\n" +
            "    teamAffiliations {\n" +
            "      name\n" +
            "      members{\n" +
            "        name\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    
    
    // Create the CDI Beans in the TCK Tests app
    static Class heroFinder;
    static Class heroDatabase;
    static Class sidekickDatabase;
    static Class heroLocator;
    
    static {
        try {
            heroFinder = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.api.HeroFinder");
            heroDatabase = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.HeroDatabase");
            sidekickDatabase = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.SidekickDatabase");
            heroLocator = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.HeroLocator");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
