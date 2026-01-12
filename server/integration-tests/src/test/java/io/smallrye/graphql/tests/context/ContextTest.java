package io.smallrye.graphql.tests.context;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.spi.EventingService;
import io.smallrye.graphql.tests.GraphQLAssured;

/**
 * Verifies that the correct SmallRyeContext is properly propagated to field executions
 * during more complex execution scenarios, including an EventingService.
 */
@ExtendWith(ArquillianExtension.class)
public class ContextTest {

    @GraphQLApi
    public static class ComplexApi {

        @Inject
        SmallRyeContext ctx;

        @Query
        public Dummy query1() {
            if (!ctx.getField().getName().equals("query1")) {
                throw new RuntimeException("ERROR: Wrong selected fields: " + ctx.getSelectedFields());
            }
            return new Dummy("ok");
        }

        @Query
        public Dummy query2() {
            if (!ctx.getField().getName().equals("query2")) {
                throw new RuntimeException("ERROR: Wrong selected fields: " + ctx.getSelectedFields());
            }
            return new Dummy("ok");
        }

        @Query
        public Dummy query3() {
            if (!ctx.getField().getName().equals("query3")) {
                throw new RuntimeException("ERROR: Wrong selected fields: " + ctx.getSelectedFields());
            }
            return new Dummy("ok");
        }

        public String source(@Source Dummy dummy) {
            return ctx.getPath();
        }

    }

    public static class Events implements EventingService {

        private static final Map<String, String> requestContexts = new ConcurrentHashMap<>();
        private static final Map<String, String> fetchContexts = new ConcurrentHashMap<>();
        private static Logger LOG = Logger.getLogger(Events.class.getName());

        private static String getFieldNameExecutionId(Context context) {
            return context.getFieldName() + " " + context.getExecutionId();
        }

        @Override
        public void afterDataFetch(Context context) {
            String fieldNameExecutionId = getFieldNameExecutionId(context);
            String remove = fetchContexts.remove(fieldNameExecutionId);
            LOG.info("afterDataFetch " + fieldNameExecutionId + " " + remove);
            if (remove == null) {
                LOG.warning(fieldNameExecutionId + " twice");
                throw new NullPointerException(fieldNameExecutionId);
            }
        }

        @Override
        public void beforeDataFetch(Context context) {
            String fieldNameExecutionId = getFieldNameExecutionId(context);
            LOG.info("beforeDataFetch " + fieldNameExecutionId);
            fetchContexts.put(fieldNameExecutionId, fieldNameExecutionId);
        }

        @Override
        public void afterExecute(Context context) {
            String remove = requestContexts.remove(context.getExecutionId());
            LOG.info("afterExecute " + context.getExecutionId() + " " + remove);
            if (remove == null) {
                LOG.warning(context.getExecutionId() + " twice");
                throw new NullPointerException(context.getExecutionId());
            }
        }

        @Override
        public void beforeExecute(Context context) {
            LOG.info("beforeExecute " + context.getExecutionId());
            requestContexts.put(context.getExecutionId(), context.getExecutionId());
        }

        @Override
        public String getConfigKey() {
            return null;
        }
    }

    public static class Dummy {

        private String ok;

        public Dummy(String ok) {
            this.ok = ok;
        }

        public String getOk() {
            return ok;
        }

        public void setOk(String ok) {
            this.ok = ok;
        }
    }

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "context-test.war")
                .addAsResource(new StringAsset(Events.class.getName()),
                        "META-INF/services/io.smallrye.graphql.spi.EventingService")
                .addClasses(ComplexApi.class, Dummy.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void test() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response1 = graphQLAssured.post("{query1 {ok source} query2 {ok source} query3 {ok source}}");
        assertEquals("{\"data\":{\"query1\":{\"ok\":\"ok\",\"source\":\"/query1/source\"}," +
                "\"query2\":{\"ok\":\"ok\",\"source\":\"/query2/source\"}," +
                "\"query3\":{\"ok\":\"ok\",\"source\":\"/query3/source\"}}}", response1);
    }
}
