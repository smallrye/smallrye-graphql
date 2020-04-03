package io.smallrye.graphql.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

import io.smallrye.graphql.schema.GraphQLCreator;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Test the model creation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLCreatorTest {
    private static final Logger LOG = Logger.getLogger(GraphQLCreatorTest.class.getName());

    @Test
    public void testSchemaModelCreation() throws IOException {

        IndexView index = getTCKIndex();
        LOG.error(">>>>>>>> index = " + index);
        Schema schema = GraphQLCreator.bootstrap(index);

        Assert.assertNotNull(schema);
        LOG.error(">>>>>>>> " + toString(schema));
    }

    private String toString(Schema schema) {
        JsonbConfig config = new JsonbConfig()
                .withFormatting(true);

        Jsonb jsonb = JsonbBuilder.create(config);
        return jsonb.toJson(schema);
    }

    private IndexView getTCKIndex() {
        Indexer indexer = new Indexer();
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/basic/api");
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/api");
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/db");
        indexDirectory(indexer, "org/eclipse/microprofile/graphql/tck/apps/superhero/model");
        return indexer.complete();
    }

    private void indexDirectory(Indexer indexer, String baseDir) {
        InputStream directoryStream = getResourceAsStream(baseDir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(directoryStream));
        reader.lines()
                .filter(resName -> resName.endsWith(".class"))
                .map(resName -> Paths.get(baseDir, resName))
                .forEach(path -> index(indexer, path.toString()));
    }

    private InputStream getResourceAsStream(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }

    private void index(Indexer indexer, String resName) {
        try {
            InputStream stream = getResourceAsStream(resName);
            indexer.index(stream);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
